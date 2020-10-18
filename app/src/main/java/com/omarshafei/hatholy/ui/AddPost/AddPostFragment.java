package com.omarshafei.hatholy.ui.AddPost;

import android.Manifest;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;

import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.omarshafei.hatholy.MainActivity;
import com.omarshafei.hatholy.R;
import com.omarshafei.hatholy.ui.Search.Post;
import com.squareup.picasso.Picasso;
import java.io.ByteArrayOutputStream;
import java.util.UUID;
import static android.app.Activity.RESULT_OK;

public class AddPostFragment extends Fragment implements AdapterView.OnItemSelectedListener{

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_OPEN_GALLERY = 2;

    private Spinner spinner;
    ArrayAdapter<CharSequence> spinnerAdapter;
    private ImageView missingImage;
    private EditText phoneNumber;
    private ProgressBar mProgressBar;
    private Button addPostButton;
    private Uri mImageUri;
    private Bitmap imageBitmap;
    public static CollectionReference postsRef;
    private StorageReference storageRef;
    private FirebaseFirestore firebaseFirestore;
    private BottomNavigationView mBottomNavigationView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_add_post, container, false);

        spinner         = root.findViewById(R.id.missing_spinner);
        ImageButton addImageGallery = root.findViewById(R.id.add_image_gallery);
        ImageButton addImageCamera = root.findViewById(R.id.add_image_camera);
        missingImage    = root.findViewById(R.id.missing_image_view);
        phoneNumber     = root.findViewById(R.id.number_edit_text);
        addPostButton = root.findViewById(R.id.add_post_button);
        mProgressBar = root.findViewById(R.id.progress_bar);

        mBottomNavigationView = requireActivity().findViewById(R.id.nav_view);

        firebaseFirestore = FirebaseFirestore.getInstance();

        //maximum cash size is 30MB
        firebaseFirestore.setFirestoreSettings(new FirebaseFirestoreSettings.Builder()
                .setCacheSizeBytes(30)
                .build());

        postsRef = firebaseFirestore.collection("Posts");
        storageRef = FirebaseStorage.getInstance().getReference().child("images/" + UUID.randomUUID().toString());
        //fill the spinner with the data
        spinnerAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.missing, R.layout.spinner_text);

        spinnerAdapter.setDropDownViewResource(R.layout.spinner_layout);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(this);

        //add image from gallery
        addImageGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addImageFromGallery();
            }
        });
        //add image from Camera
        addImageCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addImageFromCamera();
            }
        });

        addPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addPost();
            }
        });
        return root;
    }

    private void addPost() {
        final String number = phoneNumber.getText().toString();
        final String missingType = spinner.getSelectedItem().toString();
        if(isNetworkAvailable(getContext()) && isValidPhoneNumber(number) && isMissingTypeExist(missingType) && isImageExist(mImageUri)) {
            //disable the button until the upload is finished
            addPostButton.setEnabled(false);

            storageRef.putFile(mImageUri).addOnSuccessListener(
                        new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                            {
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        mProgressBar.setProgress(0);
                                    }
                                }, 500);
                                // Image uploaded successfully
                                storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String imageUrl = uri.toString();
                                        postsRef.add(new Post(number, missingType, imageUrl));
                                    }
                                });
                                Toast.makeText(getContext(), "تم رفع المنشور", Toast.LENGTH_SHORT).show();
                                mBottomNavigationView.setSelectedItemId(R.id.navigation_search);
                            }
                        })
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                                double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                                mProgressBar.setProgress((int) progress);
                            }
                        });
        }
    }


    // check whether mobile is connected to internet and returns true if connected
    public static boolean isNetworkAvailable(Context context) {
        if(context == null) {
            Toast.makeText(context, "لا يوجد اتصال بالانترنت", Toast.LENGTH_SHORT).show();
            return false;
        }

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {

            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
                if (capabilities != null) {
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        return true;
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        return true;
                    }  else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)){
                        return true;
                    }
                }
            }

            else {
                try {
                    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                    if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
                        Log.i("update_status", "Network is available : true");
                        return true;
                    }
                } catch (Exception e) {
                    Log.i("update_status", "" + e.getMessage());
                }
            }
        }
        Log.i("update_status","Network is available : FALSE ");
        Toast.makeText(context, "لا يوحد اتصال بالانترنت", Toast.LENGTH_SHORT).show();
        return false;
    }

    private boolean isValidPhoneNumber(String number) {
        boolean isValid = true;

        if( number.trim().isEmpty() ) {
            Toast.makeText(getContext(), "من فضلك اكتب رقم الموبايل", Toast.LENGTH_LONG).show();
            isValid = false;
        }
        else if(number.trim().length() != 11) {
            Toast.makeText(getContext(), "رقم الموبايل غلط", Toast.LENGTH_LONG).show();
            isValid = false;
        }
        else if(number.trim().charAt(0) != '0') {
            Toast.makeText(getContext(), "رقم الموبايل غلط", Toast.LENGTH_LONG).show();
            isValid = false;
        }
        else if(number.trim().charAt(1) != '1') {
            Toast.makeText(getContext(), "رقم الموبايل غلط", Toast.LENGTH_LONG).show();
            isValid = false;
        }
        else if(number.trim().charAt(2) != '0' && number.trim().charAt(2) != '1' && number.trim().charAt(2) != '2' && number.trim().charAt(2) != '5') {
            Toast.makeText(getContext(), "رقم الموبايل غلط", Toast.LENGTH_LONG).show();
            isValid = false;
        }
        return isValid;
    }
    private boolean isMissingTypeExist(String missingType) {
        if(missingType.equals("اختار نوع الحاجة") ) {
            Toast.makeText(getContext(), "من فضلك اختار نوع الحاجة", Toast.LENGTH_SHORT).show();
            return false;
        }
        else return true;
    }

    private boolean isImageExist(Uri mImageUri) {
        if(mImageUri != null)         return true;
        else {
            Toast.makeText(getContext(), "اختار صورة من فضلك", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private void addImageFromGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, REQUEST_OPEN_GALLERY);
    }

    private void addImageFromCamera() {
        checkPermissions();
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void checkPermissions() {
        if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 4);
        }

        if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 3);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_OPEN_GALLERY && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            mImageUri = data.getData();
        }

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            Bundle extras = (data).getExtras();
            if (extras != null) {
                imageBitmap = (Bitmap) extras.get("data");
            }
            mImageUri = getImageUri(requireContext(), imageBitmap);
        }

        Picasso.get().load(mImageUri).fit().into(missingImage);
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 50, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
    }

    public void onNothingSelected(AdapterView<?> adapterView) {
    }
}
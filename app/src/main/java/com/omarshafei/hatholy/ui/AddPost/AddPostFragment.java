package com.omarshafei.hatholy.ui.AddPost;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.omarshafei.hatholy.R;
import com.omarshafei.hatholy.ui.Search.Post;
import com.squareup.picasso.Picasso;

import static android.app.Activity.RESULT_OK;

public class AddPostFragment extends Fragment implements AdapterView.OnItemSelectedListener{

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_OPEN_GALLERY = 2;
    private Spinner spinner;
    private ImageButton addImageGallery;
    private ImageButton addImageCamera;
    private ImageView missingImage;
    private EditText phoneNumber;
    private Button addPostButton;
    private Uri mImageUri;
    private CollectionReference postsRef;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_add_post, container, false);

        spinner         = root.findViewById(R.id.missing_spinner);
        addImageGallery = root.findViewById(R.id.add_image_gallery);
        addImageCamera  = root.findViewById(R.id.add_image_camera);
        missingImage    = root.findViewById(R.id.missing_image_view);
        phoneNumber     = root.findViewById(R.id.number_edit_text);
        addPostButton   = root.findViewById(R.id.add_post_button);
        postsRef = FirebaseFirestore.getInstance().collection("Posts");
        //fill the spinner with the data
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.missing, R.layout.spinner_text);
        adapter.setDropDownViewResource(R.layout.spinner_layout);
        spinner.setAdapter(adapter);
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
        String number = phoneNumber.getText().toString();
        String missingType = spinner.getSelectedItem().toString();
        if(isValidPhoneNumber(number)) {
            postsRef.add(new Post(number, missingType));
            Toast.makeText(getContext(), "Post added", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isValidPhoneNumber(String number) {
        boolean isValid = true;
        if(number.trim().length() == 11) {
            isValid = true;
        }

        else if( number.trim().isEmpty() ) {
            Toast.makeText(getContext(), "اكتب رقم موبايلك من فضلك متخنقناش", Toast.LENGTH_LONG).show();
            isValid = false;
        }
        else if(number.trim().length() != 11) {
            Toast.makeText(getContext(), "اكتب الرقم صح الله يخليك", Toast.LENGTH_LONG).show();
            isValid = false;
        }
        return isValid;
    }

    private void addImageFromGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, REQUEST_OPEN_GALLERY);
    }

    private void addImageFromCamera() {
        if(Build.VERSION.SDK_INT >= 23){
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 2);
        }
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_OPEN_GALLERY && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            mImageUri = data.getData();
            Picasso.get().load(mImageUri).into(missingImage);
        }

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            if(data != null) {
                Bundle extras = (data).getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                missingImage.setImageBitmap(imageBitmap);
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        //text = adapterView.getItemAtPosition(i).toString();
    }
    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }
}
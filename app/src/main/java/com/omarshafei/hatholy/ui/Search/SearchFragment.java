package com.omarshafei.hatholy.ui.Search;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.omarshafei.hatholy.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.PriorityQueue;

public class SearchFragment extends Fragment implements AdapterView.OnItemSelectedListener{

    private Spinner spinner;
    private ArrayList<Post> postsList = new ArrayList<>();
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private CollectionReference postsRef = FirebaseFirestore.getInstance().collection("Posts");
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_search, container, false);

        spinner = root.findViewById(R.id.missing_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.missing, R.layout.spinner_text);
        adapter.setDropDownViewResource(R.layout.spinner_layout);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        setupRecyclerView(root);
        return root;
    }

    private void setupRecyclerView(View root) {
        postsList = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(), postsList, new ClickListener() {
            @Override
            public void onPositionClicked(int position) {
                String  phoneNumber = postsList.get(position).getPhoneNumber();
                dialPhoneNumber(phoneNumber);

            }
        });
        recyclerView = root.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(postAdapter);
    }

    private void dialPhoneNumber(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if(adapterView.getItemAtPosition(i).toString().equals("اختار نوع الحاجة")) {
            postsRef.get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            postsList.clear();
                            for(QueryDocumentSnapshot documentSnapshot: queryDocumentSnapshots) {
                                Post post = documentSnapshot.toObject(Post.class);

                                String number = post.getPhoneNumber();
                                String missingType = post.getMissingType();
                                String imageUrl = post.getImageUrl();
                                postsList.add(new Post(number, missingType, imageUrl));
                            }
                            postAdapter.notifyDataSetChanged();
                        }
                    });
        } else {
            postsRef.whereEqualTo("missingType", adapterView.getItemAtPosition(i).toString()).get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            postsList.clear();
                            for(QueryDocumentSnapshot documentSnapshot: queryDocumentSnapshots) {
                                Post post = documentSnapshot.toObject(Post.class);

                                String number = post.getPhoneNumber();
                                String missingType = post.getMissingType();
                                String imageUrl = post.getImageUrl();
                                postsList.add(new Post(number, missingType, imageUrl));
                            }
                            postAdapter.notifyDataSetChanged();
                        }
                    });
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }
}
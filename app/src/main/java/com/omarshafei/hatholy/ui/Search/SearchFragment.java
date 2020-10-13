package com.omarshafei.hatholy.ui.Search;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.omarshafei.hatholy.R;
import java.util.ArrayList;
import java.util.Objects;

public class SearchFragment extends Fragment implements AdapterView.OnItemSelectedListener{

    private ArrayList<Post> postsList = new ArrayList<>();
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private CollectionReference postsRef = FirebaseFirestore.getInstance().collection("Posts");
    private ShimmerFrameLayout shimmerFrameLayout;
    Spinner spinner;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_search, container, false);

        spinner = root.findViewById(R.id.missing_spinner);
        shimmerFrameLayout = root.findViewById(R.id.shimmer_view_container);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.missing, R.layout.spinner_text);
        adapter.setDropDownViewResource(R.layout.spinner_layout);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        setupRecyclerView(root);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        shimmerFrameLayout.startShimmerAnimation();
    }

    @Override
    public void onStop() {
        super.onStop();
        shimmerFrameLayout.stopShimmerAnimation();
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
        postsList.clear();

        if(adapterView.getItemAtPosition(i).toString().equals("اختار نوع الحاجة")) {
            postsRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    for(QueryDocumentSnapshot documentSnapshot: queryDocumentSnapshots) {
                        Post post = documentSnapshot.toObject(Post.class);

                        String number = post.getPhoneNumber();
                        String missingType = post.getMissingType();
                        String imageUrl = post.getImageUrl();
                        postsList.add(new Post(number, missingType, imageUrl));
                    }
                    postAdapter.notifyDataSetChanged();
                    shimmerFrameLayout.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            });
        }
        else {
            postsRef.whereEqualTo("missingType", adapterView.getItemAtPosition(i).toString()).get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            for(QueryDocumentSnapshot documentSnapshot: queryDocumentSnapshots) {
                                Post post = documentSnapshot.toObject(Post.class);

                                String number = post.getPhoneNumber();
                                String missingType = post.getMissingType();
                                String imageUrl = post.getImageUrl();
                                postsList.add(new Post(number, missingType, imageUrl));
                            }
                            postAdapter.notifyDataSetChanged();
                            shimmerFrameLayout.setVisibility(View.GONE);
                        }
                    });
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }
}
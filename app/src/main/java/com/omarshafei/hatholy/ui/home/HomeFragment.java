package com.omarshafei.hatholy.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.omarshafei.hatholy.R;

public class HomeFragment extends Fragment {

    private TextView textView;
    private TextView textView2;
    private BottomNavigationView mBottomNavigationView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);
        textView = root.findViewById(R.id.textView);
        textView2 = root.findViewById(R.id.textView2);
        mBottomNavigationView = requireActivity().findViewById(R.id.nav_view);
        //open the fragment programmatically
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBottomNavigationView.setSelectedItemId(R.id.navigation_add_post);
            }
        });
        textView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBottomNavigationView.setSelectedItemId(R.id.navigation_search);
            }
        });
        return root;
    }
}
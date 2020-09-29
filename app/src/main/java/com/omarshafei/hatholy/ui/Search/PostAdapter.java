package com.omarshafei.hatholy.ui.Search;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.omarshafei.hatholy.R;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;
import java.util.ArrayList;


public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private ArrayList<Post> data;
    private final ClickListener listener;


    public PostAdapter(Context context, ArrayList<Post> data, ClickListener listener) {
        this.data = data;
        LayoutInflater.from(context);
        this.listener = listener;
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post currentPost = data.get(position);
        Picasso.get()
                .load(currentPost.getImageUrl())
                .fit()
                .into(holder.missingImage);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_item, parent, false);
        return new PostViewHolder(view, listener);
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private ImageView missingImage;
        private ImageButton callButton;
        //to handle button click
        private WeakReference<ClickListener> listenerRef;

        public PostViewHolder(@NonNull View itemView, ClickListener listener) {
            super(itemView);
            missingImage = itemView.findViewById(R.id.image_View);
            callButton = itemView.findViewById(R.id.image_Button);

            listenerRef = new WeakReference<>(listener);
            callButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            listenerRef.get().onPositionClicked(getAdapterPosition());
        }
    }

}

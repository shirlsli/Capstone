package com.example.capstone;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.capstone.fragments.PoemDetailsFragment;
import com.example.capstone.models.Poem;
import com.example.capstone.models.Post;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.SaveCallback;

import org.parceler.Parcels;

import java.util.List;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {

    private Context context;
    private List<Post> posts;

    public PostsAdapter(Context context, List<Post> posts) {
        this.context = context;
        this.posts = posts;
    }

    // Clean all elements of the recycler
    public void clear() {
        posts.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<Post> list) {
        posts.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView ivProfile;
        private TextView tvAuthor;
        private TextView tvPoem;
        private TextView tvTimeStamp;

        public ViewHolder(@NonNull View itemView) throws NullPointerException {
            super(itemView);
            itemView.setOnClickListener(this);
            ivProfile = itemView.findViewById(R.id.ivProfile);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            tvPoem = itemView.findViewById(R.id.tvPoem);
            tvTimeStamp = itemView.findViewById(R.id.tvTimeStamp);
        }

        public void bind(Post post) {
            ParseFile profile = (ParseFile) post.getAuthor().get("profilePic");
            Uri uri = Uri.parse(profile.getUrl());
            Glide.with(context).load(uri).centerCrop().transform(new RoundedCorners(360)).into(ivProfile);
            tvAuthor.setText(post.getAuthor().getUsername());
            Poem poem = (Poem) post.getPoem();
            tvPoem.setText(poem.getPoemString());
            tvTimeStamp.setText(post.getRelativeTimeAgo(post.getCreatedAt().toString()));
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            FragmentManager fragmentManager = ((AppCompatActivity)context).getSupportFragmentManager();

            if (position != RecyclerView.NO_POSITION) {
                Post post = posts.get(position);
                Fragment poemDetailsFragment = new PoemDetailsFragment();
                Bundle bundle = new Bundle();
                bundle.putParcelable("Poem", post.getPoem());
                poemDetailsFragment.setArguments(bundle);
                fragmentManager.beginTransaction().replace(R.id.flContainer, poemDetailsFragment).commit();
            }
        }
    }
}

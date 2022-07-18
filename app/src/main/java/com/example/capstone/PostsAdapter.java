package com.example.capstone;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.example.capstone.models.User;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {

    private static final String TAG = "PostsAdapter";
    private Context context;
    private List<Post> posts;

    public PostsAdapter(Context context, List<Post> posts) {
        this.context = context;
        this.posts = posts;
    }

    public void clear() {
        posts.clear();
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

        private CircleImageView ivProfile;
        private TextView tvAuthor;
        private TextView tvPoem;
        private Button bFriend;
        private TextView tvTimeStamp;
        private List<User> curUserFriends;

        public ViewHolder(@NonNull View itemView) throws NullPointerException {
            super(itemView);
            itemView.setOnClickListener(this);
            ivProfile = itemView.findViewById(R.id.ivProfile);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            tvPoem = itemView.findViewById(R.id.tvPoem);
            bFriend = itemView.findViewById(R.id.bFriend);
            tvTimeStamp = itemView.findViewById(R.id.tvTimeStamp);
        }

        public void bind(Post post) {
            ParseFile profile = (ParseFile) post.getAuthor().get("profilePic");
            Uri uri = Uri.parse(profile.getUrl());
            Glide.with(context).load(uri).centerCrop().transform(new RoundedCorners(360)).into(ivProfile);
            tvAuthor.setText(post.getAuthor().getUsername());
            if (!post.getAuthor().getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
                Log.i(TAG, ParseUser.getCurrentUser().getObjectId());
                bFriend.setVisibility(View.VISIBLE);
                bFriend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onFriendClick();
                    }
                });
            }
            Poem poem = (Poem) post.getPoem();
            String poemString = "";
            for (int i = 0; i < poem.getPoemLines().size(); i++) {
                poemString += poem.getPoemLines().get(i).getPoemLine();
                if (i == 3 || i == 7 || i == 11) {
                    poemString += "\n";
                }
                poemString += "\n";
            }
            tvPoem.setText(poemString);
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
                bundle.putString("fromFeed", "Feed");
                bundle.putParcelable("Poem", post.getPoem());
                Log.i(TAG, "Parcelled item: " + post.getPoem());
                poemDetailsFragment.setArguments(bundle);
                fragmentManager.beginTransaction().replace(R.id.flContainer, poemDetailsFragment).addToBackStack( "feed_poem" ).commit();
            }
        }

        private void onFriendClick() {
            int position = getAdapterPosition();
            FragmentManager fragmentManager = ((AppCompatActivity)context).getSupportFragmentManager();
            if (position != RecyclerView.NO_POSITION) {
                Post post = posts.get(position);
                ParseQuery<User> currentUserQuery = ParseQuery.getQuery(User.class);
                currentUserQuery.include(User.KEY_FRIENDS);
                currentUserQuery.whereEqualTo("objectId", ParseUser.getCurrentUser().getObjectId());
                currentUserQuery.findInBackground(new FindCallback<User>() {
                    @Override
                    public void done(List<User> objects, ParseException e) {
                        if (e != null) {
                            Log.e(TAG, "Friend not added", e);
                        } else {
                            objects.get(0).addFriends(post.getAuthor());
                            Log.i(TAG, "Friend added!" + objects.get(0).getFriends());
                        }
                    }
                });
            }
        }
    }
}

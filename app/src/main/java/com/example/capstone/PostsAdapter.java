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
import android.widget.Toast;

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
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {

    private static final String TAG = "PostsAdapter";
    private Context context;
    private List<Post> posts;
    private User user;

    public PostsAdapter(Context context, List<Post> posts, User user) {
        this.context = context;
        this.posts = posts;
        this.user = user;
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
            bFriend.setVisibility(View.GONE);
            if (!post.getAuthor().getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
                bFriend.setVisibility(View.VISIBLE);
                for (int i = 0; i < user.getFriends().size(); i++) {
                    if (user.getFriends().get(i).getObjectId().equals(post.getAuthor().getObjectId())) {
                        bFriend.setText("Unfriend");
                    } else {
                        bFriend.setText(R.string.friend);
                    }
                    bFriend.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (bFriend.getText().toString().equals("Unfriend")) {
                                onUnFriendClick();
                            } else {
                                onFriendClick();
                            }
                        }
                    });
                }
            }
            Poem poem = post.getPoem();
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
            if (position != RecyclerView.NO_POSITION) {
                Post post = posts.get(position);
                ParseQuery<User> friendQuery = ParseQuery.getQuery(User.class);
                friendQuery.whereEqualTo("objectId", post.getAuthor().getObjectId());
                friendQuery.getFirstInBackground(new GetCallback<User>() {
                    @Override
                    public void done(User friendUser, ParseException e) {
                        String tempObjId = friendUser.getObjectId();
                        for (int i = 0; i < user.getFriends().size(); i++) {
                            if (user.getFriends().get(i).getObjectId().equals(tempObjId)) {
                                return;
                            }
                        }
                        user.addFriends(friendUser);
                        user.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e != null) {
                                    Log.e(TAG, "New friend not saved :(", e);
                                } else {
                                    Log.i(TAG, "New friend saved!");
                                    bFriend.setText("Unfriend");
                                }
                            }
                        });
                    }
                });
            }
        }

        private void onUnFriendClick() {
            int position = getAbsoluteAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Post post = posts.get(position);
                ParseQuery<User> friendQuery = ParseQuery.getQuery(User.class);
                friendQuery.whereEqualTo("objectId", post.getAuthor().getObjectId());
                friendQuery.getFirstInBackground(new GetCallback<User>() {
                    @Override
                    public void done(User friendUser, ParseException e) {
                        List<User> temp = user.getFriends();
                        String tempObtId = friendUser.getObjectId();
                        User userTemp = null;
                        for (int i = 0; i < temp.size(); i++) {
                            if (temp.get(i).getObjectId().equals(tempObtId)) {
                                userTemp = temp.get(i);
                            }
                        }
                        temp.remove(userTemp);
                        user.put("friends", temp);
                        user.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e != null) {
                                    Log.e(TAG, "Friend not removed D:", e);
                                } else {
                                    Log.i(TAG, "Friend was removed!");
                                    bFriend.setText(R.string.friend);
                                }
                            }
                        });
                    }
                });
            }
        }
    }
}

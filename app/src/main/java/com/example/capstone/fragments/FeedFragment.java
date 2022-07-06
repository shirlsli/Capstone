package com.example.capstone.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.capstone.PostsAdapter;
import com.example.capstone.R;
import com.example.capstone.models.Line;
import com.example.capstone.models.Poem;
import com.example.capstone.models.Post;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

public class FeedFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    protected PostsAdapter adapter;
    protected List<Post> allPosts;
    private RecyclerView rvPosts;

    public FeedFragment() {
        // Required empty public constructor
    }

    public static FeedFragment newInstance(String param1, String param2) {
        FeedFragment fragment = new FeedFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_feed, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvPosts = view.findViewById(R.id.rvPoems);
        allPosts = new ArrayList<>();
        adapter = new PostsAdapter(view.getContext(), allPosts);

        rvPosts.setAdapter(adapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(view.getContext());
        rvPosts.setLayoutManager(linearLayoutManager);
        queryPosts();
    }

    private void queryPosts() {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
//        query.include("Poem.poemLines");
        query.include(Post.KEY_AUTHOR);
        query.include(Post.KEY_POEM);
        query.include(Post.KEY_POEM + "." + Poem.KEY_POEM_LINES);
        // include (Post.KEY_POEM) --> (Poem.KEY_POEM_LINES)
        query.setLimit(20);
        query.addDescendingOrder("createdAt");
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
                if (e != null) {
                    Log.e("issue_getting_posts", "Issue with getting posts", e);
                    return;
                }
                for (Post post : posts) {
//                    ((Poem) post.getPoem()).getPoemLines();
//                    post.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
//                        @Override
//                        public void done(ParseObject object, ParseException e) {
//                            Log.i("debug_query", "Post: ");
//                        }
//                    });
                    Log.i("no_issue_getting_posts", "Post: " + post.getPoem() + ", username: " + post.getAuthor().getUsername());
                }
                allPosts.addAll(posts);
//                rvPosts.smoothScrollToPosition(0);
                adapter.notifyDataSetChanged();
            }
        });
    }

}
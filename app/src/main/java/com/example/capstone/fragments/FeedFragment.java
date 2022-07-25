package com.example.capstone.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.capstone.FeedActivity;
import com.example.capstone.PostsAdapter;
import com.example.capstone.R;
import com.example.capstone.models.Poem;
import com.example.capstone.models.Post;
import com.example.capstone.models.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import smartdevelop.ir.eram.showcaseviewlib.GuideView;
import smartdevelop.ir.eram.showcaseviewlib.config.DismissType;

public class FeedFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    protected PostsAdapter adapter;
    protected List<Post> allPosts;
    private RecyclerView rvPosts;
    private FloatingActionButton fabGenerate;
    private SwipeRefreshLayout swipeContainer;
    private boolean activateTutorial;
    private static final String TAG = "FeedFragment";

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
        fabGenerate = view.findViewById(R.id.fabGenerate);

        swipeContainer = view.findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchTimelineAsync(0);
            }
        });
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        FeedActivity feedActivity = (FeedActivity) getActivity();
        activateTutorial = feedActivity.getActivateTutorial();
        fabGenerate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment generateFragment = new GenerateFragment();
                Bundle bundle = new Bundle();
                bundle.putBoolean("activateTutorial", activateTutorial);
                generateFragment.setArguments(bundle);
                getParentFragmentManager().beginTransaction().replace(R.id.flContainer, generateFragment).addToBackStack( "generate_poem" ).commit();
            }
        });
        if (activateTutorial) {
            new GuideView.Builder(getContext())
                    .setTitle("Creating Poem Lines and Poems")
                    .setContentText("Tap here to generate a poem line\n and create a poem using your\n friends' poem lines!")
                    .setTargetView(fabGenerate)
                    .setDismissType(DismissType.targetView)
                    .build()
                    .show();
        }

        rvPosts.setAdapter(adapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(view.getContext());
        rvPosts.setLayoutManager(linearLayoutManager);
        queryPosts();
    }

    public void fetchTimelineAsync(int page) {
        adapter.clear();
        queryPosts();
        swipeContainer.setRefreshing(false);
    }

    private void queryPosts() {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_AUTHOR);
        query.include(Post.KEY_POEM);
        query.include(Post.KEY_POEM + "." + Poem.KEY_POEM_LINES);
        query.setLimit(20);
        query.addDescendingOrder("createdAt");
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting posts", e);
                    return;
                }
                for (Post post : posts) {
                    Log.i(TAG, "Post: " + post.getPoem() + ", username: " + post.getAuthor().getUsername());
                }
                allPosts.addAll(posts);
//                rvPosts.smoothScrollToPosition(0);
                adapter.notifyDataSetChanged();
            }
        });
    }

}
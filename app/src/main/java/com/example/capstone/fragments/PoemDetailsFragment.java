package com.example.capstone.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.capstone.R;
import com.example.capstone.models.Poem;
import com.example.capstone.models.Post;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONException;

import java.text.DateFormat;
import java.util.Date;

public class PoemDetailsFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private LinearLayout poemLayout;
    private Poem poem;
    private String fromFeed;
    private TextView tvDateDetails;
    private Button bPost;
    private static final String TAG = "PoemDetailsFragment";

    public PoemDetailsFragment() {
        // Required empty public constructor
    }

    public static PoemDetailsFragment newInstance(String param1, String param2) {
        PoemDetailsFragment fragment = new PoemDetailsFragment();
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
        return inflater.inflate(R.layout.fragment_poem_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        poemLayout = view.findViewById(R.id.poemLayout);
        Bundle bundle = getArguments();
        if (bundle != null) {
            fromFeed = getArguments().getString("fromFeed");
            poem = bundle.getParcelable("Poem");
            tvDateDetails = view.findViewById(R.id.tvDateDetails);
            bPost = view.findViewById(R.id.bPost);
            createDateTextView(view, poem);
            for (int i = 0; i < poem.getPoemLines().size(); i++) {
                TextView tvNewLine = new TextView(view.getContext());
                tvNewLine.setText(poem.getPoemLines().get(i).getPoemLine());
                if (i == 4 || i == 8 || i == 12) {
                    TextView tvBlank = new TextView(getContext());
                    poemLayout.addView(tvBlank);
                }
                setLayoutFormat(tvNewLine, 12, 0, 20, 0, 0);
                poemLayout.addView(tvNewLine);
            }
            if (fromFeed == null) {
                bPost.setVisibility(View.VISIBLE);
                bPost.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onPostClicked();
                    }
                });
            } else {
                bPost.setVisibility(View.GONE);
            }
        }
    }

    private void onPostClicked() {
        Post post = new Post();
        post.setPoem(poem);
        post.setAuthor(ParseUser.getCurrentUser());
        post.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Post has not been saved", e);
                    Toast.makeText(getActivity(), "Post could not be saved.", Toast.LENGTH_SHORT).show();
                } else {
                    Log.i(TAG, "Post has been saved!");
                }
            }
        });

        Fragment feedFragment = new FeedFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean("activateTutorial", false);
        feedFragment.setArguments(bundle);
        getParentFragmentManager().beginTransaction().replace(R.id.flContainer, feedFragment).commit();
    }

    private void createDateTextView(View view, Poem poem) {
        Date date = poem.getCreatedAt();
        DateFormat df = DateFormat.getDateInstance();
        String reportDate = df.format(date);
        tvDateDetails.setText(reportDate);
    }

    private void setLayoutFormat(TextView textview, int textSize, int left, int top, int right, int bottom) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(left, top, right, bottom);
        textview.setLayoutParams(params);
        textview.setTextSize(textSize);
    }
}

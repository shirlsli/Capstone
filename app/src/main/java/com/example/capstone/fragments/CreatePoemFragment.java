package com.example.capstone.fragments;

import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.capstone.R;
import com.example.capstone.models.Line;
import com.example.capstone.models.OpenAIThread;
import com.example.capstone.models.Poem;
import com.example.capstone.models.User;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;


public class CreatePoemFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private Poem poem;
    private Line poemLine;
    private LinearLayout friendsLinesLayout;
    private LinearLayout poemLayout;
    private ImageView ivForwardArrow;
    private TextView tvPrompt;

    private String mParam1;
    private String mParam2;

    public CreatePoemFragment() {
        // Required empty public constructor
    }

    public static CreatePoemFragment newInstance(String param1, String param2) {
        CreatePoemFragment fragment = new CreatePoemFragment();
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
        return inflater.inflate(R.layout.fragment_create_poem, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            poem = bundle.getParcelable("Poem");
            poemLine = bundle.getParcelable("Line");
            poemLayout = view.findViewById(R.id.poemLayout);
            friendsLinesLayout = view.findViewById(R.id.friendsLinesLayout);
            ivForwardArrow = view.findViewById(R.id.ivForwardArrow2);
            tvPrompt = view.findViewById(R.id.tvPrompt);
            try {
                createPoem(view);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    public void createPoem(View view) throws ParseException {
        // brand new query for the current user
        friendsLinesLayout.removeAllViews();
        poemLayout.setVisibility(View.VISIBLE);
        TextView tvTemp = new TextView(getContext());
        tvTemp.setText(poemLine.getPoemLine());
        poemLayout.addView(tvTemp);
        ParseQuery<User> currentUserQuery = ParseQuery.getQuery(User.class);
        currentUserQuery.include(User.KEY_FRIENDS);
        currentUserQuery.whereEqualTo("objectId", ParseUser.getCurrentUser().getObjectId());
        currentUserQuery.findInBackground(new FindCallback<User>() {
            @Override
            public void done(List<User> objects, ParseException e) {
                poemLine.setAuthor(ParseUser.getCurrentUser());
                poem.addAuthor(ParseUser.getCurrentUser());
                ivForwardArrow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Fragment confirmPoemFragment = new ConfirmPoemFragment();
                        Bundle bundle = new Bundle();
                        bundle.putParcelable("Poem", poem);
                        confirmPoemFragment.setArguments(bundle);
                        getParentFragmentManager().beginTransaction().replace(R.id.flContainer, confirmPoemFragment).addToBackStack( "generate_poem" ).commit();
                    }
                });
                query(objects);
            }
        });
    }

    private void query(List<User> objects) {
        if (objects != null && objects.get(0).getFriends() != null) {
            ParseQuery<Line> poemLineQuery = ParseQuery.getQuery(Line.class);
            poemLineQuery.whereContainedIn(Line.KEY_AUTHOR, objects.get(0).getFriends());
            poemLineQuery.setLimit(20);
            poemLineQuery.addDescendingOrder("createdAt");
            poemLineQuery.include(Line.KEY_AUTHOR);
            poemLineQuery.include(Line.KEY_POEM_LINE);
            poemLineQuery.findInBackground(new FindCallback<Line>() {
                @Override
                public void done(List<Line> objects, ParseException e) {
                    if (e != null) {
                        Log.e("tag", objects.toString(), e);
                    } else {
                        poem.updatePoem(poemLine);
                        tvPrompt.setVisibility(View.VISIBLE);
                        ArrayList<Line> friendLines = new ArrayList<>(objects);
                        addFriendLines(friendLines, poem);
                    }
                }
            });
        } else {
            TextView tvNoFriends = new TextView(getContext());
            tvNoFriends.setText(R.string.noFriendsPrompt);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0,0,0,20);
            tvNoFriends.setLayoutParams(params);
            tvNoFriends.setTextSize(20);
            tvNoFriends.setTypeface(Typeface.DEFAULT_BOLD);
            friendsLinesLayout.addView(tvNoFriends);
            try {
                OpenAIThread openAIThread = new OpenAIThread("day");
                openAIThread.start();
                openAIThread.join();
                String[] generatedLines = openAIThread.getGeneratedLines();
                ArrayList<Line> convertedLines = new ArrayList<>();
                for (int i = 2; i < generatedLines.length; i++) {
                    Line line = new Line();
                    line.setPoemLine(generatedLines[i]);
                    convertedLines.add(line);
                }
                poem.updatePoem(poemLine);
                addFriendLines(convertedLines, poem);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void addFriendLines(ArrayList<Line> friendLines, Poem poem) {
        for (int i = 0; i < friendLines.size(); i++) {
            Line friendLine = new Line();
            friendLine.setPoemLine(friendLines.get(i).getPoemLine());
            TextView tvTestString = new TextView(getContext());
            tvTestString.setText(friendLine.getPoemLine());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(16,0,0,20);
            tvTestString.setLayoutParams(params);
            tvTestString.setTextSize(20);
            tvTestString.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tvTestString.setTextColor(getResources().getColor(R.color.gray));
                    selectFriendLine(friendLine);
                }
            });
            friendsLinesLayout.addView(tvTestString);
        }
        friendsLinesLayout.setVisibility(View.VISIBLE);
    }

    private void selectFriendLine(Line friendLine) {
        friendLine.setAuthor(ParseUser.getCurrentUser());
        poem.updatePoem(friendLine);
        TextView tvTemp = new TextView(getContext());
        tvTemp.setText(friendLine.getPoemLine());
        poemLayout.addView(tvTemp);
    }
}
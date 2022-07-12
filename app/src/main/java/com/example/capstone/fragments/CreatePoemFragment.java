package com.example.capstone.fragments;

import android.app.SearchManager;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import androidx.appcompat.widget.SearchView;

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
    private String prompt;
    private ImageView ivAdd;
    private String[] generatedLines;
    private ImageView ivMinus;
//    private MenuItem searchItem;
//    private SearchView searchView;

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
        setHasOptionsMenu(true);
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
            prompt = bundle.getString("Prompt");
            generatedLines = bundle.getStringArray("GeneratedLines");
            poemLayout = view.findViewById(R.id.poemLayout);
            friendsLinesLayout = view.findViewById(R.id.friendsLinesLayout);
            ivForwardArrow = view.findViewById(R.id.ivForwardArrow2);
            tvPrompt = view.findViewById(R.id.tvPrompt);
            ivAdd = view.findViewById(R.id.ivAdd);
            ivMinus = view.findViewById(R.id.ivMinus);
            try {
                createPoem(view);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
//        Toolbar toolbar = view.findViewById(R.id.toolbar);
//        AppCompatActivity activity = (AppCompatActivity) getActivity();
//        activity.setSupportActionBar(toolbar);
//        activity.getSupportActionBar().setTitle("Search for poem lines");
    }

//    @Override
//    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
//        super.onCreateOptionsMenu(menu, inflater);
//        inflater.inflate(R.menu.menu, menu);
//        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
//        searchView.setIconified(true);
//        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
//        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
//        searchItem = menu.findItem(R.id.action_search);
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                // perform query here
//
//                // workaround to avoid issues with some emulators and keyboard devices firing twice if a keyboard enter is used
//                // see https://code.google.com/p/android/issues/detail?id=24599
//                searchView.clearFocus();
//
//                return true;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                return false;
//            }
//        });
//    }

    public void createPoem(View view) throws ParseException {
        // brand new query for the current user
        friendsLinesLayout.removeAllViews();
        friendsLinesLayout.setVisibility(View.GONE);
        poemLayout.setVisibility(View.VISIBLE);
        tvPrompt.setVisibility(View.VISIBLE);
        TextView tvTemp = new TextView(getContext());
        tvTemp.setText(poemLine.getPoemLine());
        setLayout(tvTemp);
        poemLayout.addView(tvTemp);
        ivAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                poemLayout.setVisibility(View.GONE);
                ivAdd.setVisibility(View.GONE);
                ivMinus.setVisibility(View.VISIBLE);
                ivMinus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        switchToAdd();
                    }
                });
                // if friendsLinesLayout does not have any views
                if (friendsLinesLayout.getChildCount() > 0) {
                    friendsLinesLayout.setVisibility(View.VISIBLE);
                } else {
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
                        ArrayList<Line> friendLines = new ArrayList<>(objects);
                        addFriendLines(friendLines, poem);
                    }
                }
            });
        } else {
            TextView tvNoFriends = new TextView(getContext());
            tvNoFriends.setText(R.string.noFriendsPrompt);
            setLayout(tvNoFriends);
            tvNoFriends.setTypeface(Typeface.DEFAULT_BOLD);
            friendsLinesLayout.addView(tvNoFriends);
            ArrayList<Line> convertedLines = new ArrayList<>();
            for (int i = 2; i < generatedLines.length; i++) {
                Line line = new Line();
                line.setPoemLine(generatedLines[i]);
                convertedLines.add(line);
            }
            poem.updatePoem(poemLine);
            addFriendLines(convertedLines, poem);
        }
    }

    private void addFriendLines(ArrayList<Line> friendLines, Poem poem) {
        for (int i = 0; i < friendLines.size(); i++) {
            Line friendLine = new Line();
            friendLine.setPoemLine(friendLines.get(i).getPoemLine());
            TextView tvTestString = new TextView(getContext());
            tvTestString.setText(friendLine.getPoemLine());
            setLayout(tvTestString);
            tvTestString.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tvTestString.setTextColor(getResources().getColor(R.color.gray));
                    selectFriendLine(friendLine, tvTestString);
                }
            });
            friendsLinesLayout.addView(tvTestString);
        }
        friendsLinesLayout.setVisibility(View.VISIBLE);
    }

    private void selectFriendLine(Line friendLine, TextView tvTestString) {
        friendLine.setAuthor(ParseUser.getCurrentUser());
        poem.updatePoem(friendLine);
        TextView tvTemp = new TextView(getContext());
        tvTemp.setText(friendLine.getPoemLine());
        tvTemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deletePoemLine(tvTemp, tvTestString, friendLine);
            }
        });
        setLayout(tvTemp);
        poemLayout.addView(tvTemp);
        switchToAdd();
    }

    private void deletePoemLine(TextView tvTemp, TextView tvTestString, Line poemLine) {
        // removes textview from poemLayout and un-grays the selected poem line
        poemLayout.removeView(tvTemp);
        poem.getPoemLines().remove(poemLine);
        tvTestString.setTextColor(getResources().getColor(R.color.black));
    }

    private void setLayout(TextView tvTemp) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0,0,0,20);
        tvTemp.setLayoutParams(params);
        tvTemp.setTextSize(20);
    }

    private void switchToAdd() {
        poemLayout.setVisibility(View.VISIBLE);
        friendsLinesLayout.setVisibility(View.GONE);
        ivAdd.setVisibility(View.VISIBLE);
        ivMinus.setVisibility(View.GONE);
    }
}
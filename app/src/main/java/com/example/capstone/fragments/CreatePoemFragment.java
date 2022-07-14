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

import com.example.capstone.PostsAdapter;
import com.example.capstone.R;
import com.example.capstone.SearchAdapter;
import com.example.capstone.models.Line;
import com.example.capstone.models.OpenAIThread;
import com.example.capstone.models.Poem;
import com.example.capstone.models.Post;
import com.example.capstone.models.User;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.woxthebox.draglistview.DragListView;

import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;


public class CreatePoemFragment extends Fragment implements SearchAdapter.EventListener {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private ArrayList<String> poemLines;
    private String poemLine;
    private LinearLayout friendsLinesLayout;
    private LinearLayout poemLayout;
    private ImageView ivForwardArrow;
    private TextView tvPrompt;
    private String prompt;
    private ImageView ivAdd;
    private String[] generatedLines;
    private ImageView ivMinus;
    private DragListView mDragListView;
    protected SearchAdapter adapter;
    protected List<String> allFriendsLines;
    private RecyclerView rvFriendsLines;
    private TextView tvLinesCount;
    private String linesCount;

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
        rvFriendsLines = view.findViewById(R.id.rvFriendsLines);
        Bundle bundle = getArguments();
        if (bundle != null) {
            poemLines = new ArrayList<>();
            poemLine = bundle.getString("Line");
            prompt = bundle.getString("Prompt");
            generatedLines = bundle.getStringArray("GeneratedLines");
//            mDragListView = (DragListView) view.findViewById(R.id.drag_list_view);
            poemLayout = view.findViewById(R.id.poemLayout);
            friendsLinesLayout = view.findViewById(R.id.friendsLinesLayout);
            ivForwardArrow = view.findViewById(R.id.ivForwardArrow2);
            tvPrompt = view.findViewById(R.id.tvPrompt);
            ivAdd = view.findViewById(R.id.ivAdd);
            ivMinus = view.findViewById(R.id.ivMinus);
            tvLinesCount = view.findViewById(R.id.tvLinesCount);
            linesCount = "/12 lines";
            try {
                createPoem();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    public void createPoem() throws ParseException {
        // brand new query for the current user
        friendsLinesLayout.removeAllViews();
        friendsLinesLayout.setVisibility(View.GONE);
        poemLayout.setVisibility(View.VISIBLE);
        tvPrompt.setVisibility(View.VISIBLE);
        TextView tvTemp = new TextView(getContext());
        tvTemp.setText(poemLine);
        setLayout(tvTemp);
        poemLayout.addView(tvTemp);
        poemLines.add(poemLine);
        String temp = poemLines.size() + linesCount;
        tvLinesCount.setText(temp);
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
                if (friendsLinesLayout.getChildCount() > 0) {
                    friendsLinesLayout.setVisibility(View.VISIBLE);
                } else {
                    ParseQuery<User> currentUserQuery = ParseQuery.getQuery(User.class);
                    currentUserQuery.include(User.KEY_FRIENDS);
                    currentUserQuery.whereEqualTo("objectId", ParseUser.getCurrentUser().getObjectId());
                    currentUserQuery.findInBackground(new FindCallback<User>() {
                        @Override
                        public void done(List<User> objects, ParseException e) {
                            ivForwardArrow.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Fragment confirmPoemFragment = new ConfirmPoemFragment();
                                    Bundle bundle = new Bundle();
                                    bundle.putStringArrayList("Poem", poemLines);
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
                        ArrayList<String> friendLines = new ArrayList<>();
                        for (int i = 0; i < objects.size(); i++) {
                            friendLines.add(objects.get(i).getPoemLine());
                        }
                        for (int i = 3; i < generatedLines.length - 1; i++) {
                            friendLines.add(generatedLines[i]);
                        }
                        addFriendLines(friendLines);
                    }
                }
            });
        } else {
            tvPrompt.setText(R.string.noFriendsPrompt);
            ArrayList<String> convertedLines = new ArrayList<>();
            for (int i = 2; i < generatedLines.length; i++) {
                convertedLines.add(generatedLines[i]);
            }
            addFriendLines(convertedLines);
        }
    }

    public void onEvent(String data) {
        selectFriendLine(data);
    }

    private void addFriendLines(ArrayList<String> friendLines) {
            allFriendsLines = friendLines;
            adapter = new SearchAdapter(getView().getContext(), allFriendsLines, this);
            rvFriendsLines.setAdapter(adapter);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getView().getContext());
            rvFriendsLines.setLayoutManager(linearLayoutManager);
            friendsLinesLayout.setVisibility(View.VISIBLE);
    }

    private void selectFriendLine(String line) {
        if (poemLines.size() < 12) {
            tvLinesCount.setTextColor(getResources().getColor(R.color.gray));
            poemLines.add(line);
            TextView tvTemp = new TextView(getContext());
            tvTemp.setText(poemLines.get(poemLines.size() - 1));
            tvTemp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        deletePoemLine(tvTemp, poemLines.get(poemLines.size() - 1));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            });
            setLayout(tvTemp);
            poemLayout.addView(tvTemp);
            String temp = poemLines.size() + linesCount;
            tvLinesCount.setText(temp);
        } else {
            tvLinesCount.setTextColor(getResources().getColor(R.color.purple_500));
        }
        switchToAdd();
    }

    private void deletePoemLine(TextView tvTemp, String poemLine) throws ParseException {
        poemLayout.removeView(tvTemp);
        poemLines.remove(poemLine);
        String temp = poemLines.size() + linesCount;
        tvLinesCount.setText(temp);
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
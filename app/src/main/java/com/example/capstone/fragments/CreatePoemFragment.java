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
import android.widget.EditText;
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
import com.example.capstone.models.Query;
import com.example.capstone.models.User;
import com.jmedeisis.draglinearlayout.DragLinearLayout;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;


public class CreatePoemFragment extends Fragment implements SearchAdapter.EventListener {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private ArrayList<String> poemLines;
    private String poemLine;
    private LinearLayout friendsLinesLayout;
    private LinearLayout poemLayout;
    private ImageView ivForwardArrow;
    private String prompt;
    private ImageView ivAdd;
    private ArrayList<String> generatedLines;
    private ImageView ivMinus;
    protected SearchAdapter adapter;
    protected List<String> allFriendsLines;
    private RecyclerView rvFriendsLines;
    private TextView tvLinesCount;
    private String linesCount;
    private DragLinearLayout dragLinearLayout;
    private EditText etSearch;

    private String mParam1;
    private String mParam2;
    private static final String TAG = "CreatePoemFragment";

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
            poemLines = bundle.getStringArrayList("Poem");
            poemLine = bundle.getString("Line");
            prompt = bundle.getString("Prompt");
            generatedLines = bundle.getStringArrayList("GeneratedLines");
            poemLayout = view.findViewById(R.id.poemLayout);
            friendsLinesLayout = view.findViewById(R.id.friendsLinesLayout);
            ivForwardArrow = view.findViewById(R.id.ivForwardArrow2);
            ivForwardArrow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Fragment confirmPoemFragment = new ConfirmPoemFragment();
                    Bundle bundle = new Bundle();
                    bundle.putStringArrayList("Poem", poemLines);
                    bundle.putString("PoemLine", poemLine);
                    confirmPoemFragment.setArguments(bundle);
                    getParentFragmentManager().beginTransaction().replace(R.id.flContainer, confirmPoemFragment).addToBackStack( "generate_poem" ).commit();
                }
            });
            ivAdd = view.findViewById(R.id.ivAdd);
            ivMinus = view.findViewById(R.id.ivMinus);
            tvLinesCount = view.findViewById(R.id.tvLinesCount);
            etSearch = view.findViewById(R.id.etSearch);
            dragLinearLayout = (DragLinearLayout) view.findViewById(R.id.dragDropContainer);
            dragLinearLayout.setOnViewSwapListener(new DragLinearLayout.OnViewSwapListener() {
                @Override
                public void onSwap(View firstView, int firstPosition,
                                   View secondView, int secondPosition) {
                    int firstIndex = poemLines.indexOf(((TextView) firstView).getText().toString());
                    int secondIndex = poemLines.indexOf(((TextView) secondView).getText().toString());
                    if (firstIndex >= 0 && secondIndex >= 0) {
                        String temp = poemLines.get(secondIndex);
                        poemLines.set(secondIndex, poemLines.get(firstIndex));
                        poemLines.set(firstIndex, temp);
                    }
                }
            });
            linesCount = "/16 lines";
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
        for (int i = 0; i < poemLines.size(); i++) {
            TextView tvTemp = new TextView(getContext());
            tvTemp.setText(poemLines.get(i));
            setLayout(tvTemp);
            if (!poemLines.get(i).equals(poemLine)) {
                tvTemp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            deletePoemLine(tvTemp, tvTemp.getText().toString());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
            dragLinearLayout.addView(tvTemp);
            dragLinearLayout.setViewDraggable(tvTemp, tvTemp);
        }
        if (poemLines.size() == 0) {
            TextView tvTemp = new TextView(getContext());
            tvTemp.setText(poemLine);
            setLayout(tvTemp);
            dragLinearLayout.addView(tvTemp);
            dragLinearLayout.setViewDraggable(tvTemp, tvTemp);
            poemLines.add(poemLine);
        }
        String temp = poemLines.size() + linesCount;
        tvLinesCount.setText(temp);
        ivAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddClicked();
            }
        });
    }

    private void onAddClicked() {
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
            ExecutorService service = Executors.newSingleThreadExecutor();
            service.execute(new Runnable() {
                @Override
                public void run() {
                    Query query = new Query(etSearch.getText().toString());
                    try {
                        query.call(new Runnable() {
                            @Override
                            public void run() {
                                generatedLines = query.getFriendsLines();
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        includeGeneratedLines(generatedLines);
                                    }
                                });
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    service.shutdown();
                }
            });
        }
    }

    private void includeGeneratedLines(ArrayList<String> friendLines) {
        allFriendsLines = friendLines;
        adapter = new SearchAdapter(getView().getContext(), allFriendsLines, this);
        rvFriendsLines.setAdapter(adapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getView().getContext());
        rvFriendsLines.setLayoutManager(linearLayoutManager);
        friendsLinesLayout.setVisibility(View.VISIBLE);
        for (int i = 0; i < generatedLines.size(); i++) {
            if (!generatedLines.get(i).equals("")) {
                friendLines.add(generatedLines.get(i));
            }
        }
    }

    public void onEvent(String data) {
        selectFriendLine(data);
    }

    private void selectFriendLine(String line) {
        if (poemLines.size() < 16) {
            tvLinesCount.setTextColor(getResources().getColor(R.color.gray));
            poemLines.add(line);
            TextView tvTemp = new TextView(getContext());
            tvTemp.setText(poemLines.get(poemLines.size() - 1));
            tvTemp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        deletePoemLine(tvTemp, tvTemp.getText().toString());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            });
            setLayout(tvTemp);
            dragLinearLayout.addView(tvTemp);
            dragLinearLayout.setViewDraggable(tvTemp, tvTemp);
            String temp = poemLines.size() + linesCount;
            tvLinesCount.setText(temp);
        } else {
            tvLinesCount.setTextColor(getResources().getColor(R.color.purple_500));
        }
        switchToAdd();
    }

    private void deletePoemLine(TextView tvTemp, String poemLine) throws ParseException {
        dragLinearLayout.removeDragView(tvTemp);
        poemLines.remove(poemLine);
        String temp = poemLines.size() + linesCount;
        tvLinesCount.setText(temp);
    }

    private void setLayout(TextView tvTemp) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0,0,0,20);
        tvTemp.setLayoutParams(params);
        tvTemp.setTextSize(16);
    }

    private void switchToAdd() {
        poemLayout.setVisibility(View.VISIBLE);
        friendsLinesLayout.setVisibility(View.GONE);
        ivAdd.setVisibility(View.VISIBLE);
        ivMinus.setVisibility(View.GONE);
    }
}
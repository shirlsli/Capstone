package com.example.capstone.fragments;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.capstone.R;
import com.example.capstone.SearchAdapter;
import com.example.capstone.models.Query;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.jmedeisis.draglinearlayout.DragLinearLayout;
import com.parse.ParseException;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class CreatePoemFragment extends Fragment implements SearchAdapter.EventListener {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private ArrayList<String> poemLines;
    private String poemLine;
    private LinearLayout poemLayout;
    private ImageView ivForwardArrow;
    private String previousChipText;
    private ImageView ivAdd;
    private ArrayList<String> generatedLines;
    private ImageView ivBack;
    protected SearchAdapter adapter;
    protected List<String> allFriendsLines;
    private RecyclerView rvFriendsLines;
    private TextView tvLinesCount;
    private String linesCount;
    private DragLinearLayout dragLinearLayout;
    private EditText etSearch;
    private LottieAnimationView lottieAnimationView;
    private TextView tvInstructions;
    private ImageView ivSearch;
    private ChipGroup chipGroup;

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
            generatedLines = bundle.getStringArrayList("GeneratedLines");
            poemLayout = view.findViewById(R.id.poemLayout);
            lottieAnimationView = view.findViewById(R.id.lottieLoad);
            ivForwardArrow = view.findViewById(R.id.ivForwardArrow2);
            allFriendsLines = new ArrayList<>();
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
            ivBack = view.findViewById(R.id.ivMinus);
            tvLinesCount = view.findViewById(R.id.tvLinesCount);
            etSearch = view.findViewById(R.id.etSearch);
            tvInstructions = view.findViewById(R.id.tvInstructions);
            ivSearch = view.findViewById(R.id.ivSearch);
            chipGroup = view.findViewById(R.id.chipGroup);
            ivSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onSearchClicked();
                }
            });
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
                try {
                    onAddClicked();
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void onAddClicked() throws ExecutionException, InterruptedException {
        showBackArrow();
    }

    private void onSearchClicked() {
        if (chipGroup.getChildCount() < 44) {
            Chip chip = new Chip(getContext());
            chip.setText(etSearch.getText());
            // can set icon to show profile pic as well
            chip.setCloseIconVisible(true);
            chip.setCheckable(false);
            chip.setOnCloseIconClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (chipGroup.getChildCount() == 1) {
                        adapter.clear();
                        etSearch.setText("");
                        showSoftKeyboard(getActivity());
                    }
                    chipGroup.removeView(chip);
                }
            });
            chipGroup.addView(chip);
            chipGroup.setVisibility(View.VISIBLE);
        }
        hideSoftKeyboard(requireActivity());
        lottieAnimationView.setVisibility(View.VISIBLE);
        if (allFriendsLines.size() > 0 && etSearch.getText().toString().equals(previousChipText)) {
            lottieAnimationView.setVisibility(View.GONE);
        } else {
            runQuery();
        }
    }

    private void runQuery() {
            ExecutorService service = Executors.newSingleThreadExecutor();
            service.execute(new Runnable() {
                @Override
                public void run() {
                    Query query = new Query(etSearch.getText().toString());
                    try {
                        query.call(new Runnable() {
                            @Override
                            public void run() {
                                if (adapter != null && !etSearch.getText().toString().equals(previousChipText)) {
                                    previousChipText = etSearch.getText().toString();
                                    adapter.clear();
                                    adapter.addAll(query.getFriendsLines());
                                    lottieAnimationView.setVisibility(View.GONE);
                                } else {
                                    previousChipText = etSearch.getText().toString();
                                    allFriendsLines.addAll(query.getFriendsLines());
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            setUpAdapter();
                                        }
                                    });
                                }
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
    }

    private void setUpAdapter() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getView().getContext());
        rvFriendsLines.setLayoutManager(linearLayoutManager);
        adapter = new SearchAdapter(getView().getContext(), allFriendsLines, this);
        rvFriendsLines.setAdapter(adapter);
        lottieAnimationView.setVisibility(View.GONE);
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

    private void showBackArrow() {
        poemLayout.setVisibility(View.GONE);
        ivAdd.setVisibility(View.GONE);
        tvInstructions.setVisibility(View.GONE);
        etSearch.setVisibility(View.VISIBLE);
        ivBack.setVisibility(View.VISIBLE);
        rvFriendsLines.setVisibility(View.VISIBLE);
        ivSearch.setVisibility(View.VISIBLE);
        chipGroup.setVisibility(View.VISIBLE);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToAdd();
            }
        });
    }

    private void switchToAdd() {
        poemLayout.setVisibility(View.VISIBLE);
        rvFriendsLines.setVisibility(View.GONE);
        tvInstructions.setVisibility(View.VISIBLE);
        etSearch.setVisibility(View.GONE);
        ivAdd.setVisibility(View.VISIBLE);
        ivBack.setVisibility(View.GONE);
        ivSearch.setVisibility(View.GONE);
        chipGroup.setVisibility(View.GONE);
    }

    private static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        if(inputMethodManager.isAcceptingText()) {
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(),
                    0);
        }
    }

    private void showSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        if(inputMethodManager.isAcceptingText()) {
            inputMethodManager.showSoftInput(etSearch, 0);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity)getActivity()).getSupportActionBar().hide();
    }
    @Override
    public void onStop() {
        super.onStop();
        ((AppCompatActivity)getActivity()).getSupportActionBar().show();
    }
}
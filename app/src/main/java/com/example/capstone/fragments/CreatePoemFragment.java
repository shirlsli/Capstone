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
import com.example.capstone.models.User;
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

import smartdevelop.ir.eram.showcaseviewlib.GuideView;
import smartdevelop.ir.eram.showcaseviewlib.config.DismissType;
import smartdevelop.ir.eram.showcaseviewlib.listener.GuideListener;


public class CreatePoemFragment extends Fragment implements SearchAdapter.EventListener {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private ArrayList<String> poemLines;
    private String poemLine;
    private LinearLayout poemLayout;
    private ImageView ivForwardArrow;
    private ArrayList<String> previousChipText;
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
    private ImageView ivSearch;
    private ChipGroup chipGroup;
    private Chip allFriends;
    private Chip suggested;
    private ArrayList<String> chips;
    private ImageView ivCheck;
    private ArrayList<TextView> deletedLines;
    private boolean selectOn = false;
    private boolean activateTutorial;
    private User user;

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
            activateTutorial = bundle.getBoolean("activateTutorial");
            user = bundle.getParcelable("user");
            initialize(view);
            linesCount = "/16 lines";
            try {
                createPoem();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (activateTutorial) {
                tutorial();
            }
            ivForwardArrow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bundleToConfirmScreen();
                }
            });
        }
    }

    private void initialize(View view) {
        poemLayout = view.findViewById(R.id.poemLayout);
        lottieAnimationView = view.findViewById(R.id.lottieLoad);
        ivForwardArrow = view.findViewById(R.id.ivForwardArrow2);
        allFriendsLines = new ArrayList<>();
        chips = new ArrayList<>();
        previousChipText = new ArrayList<>();
        deletedLines = new ArrayList<>();
        ivAdd = view.findViewById(R.id.ivAdd);

        ivBack = view.findViewById(R.id.ivMinus);
        tvLinesCount = view.findViewById(R.id.tvLinesCount);
        etSearch = view.findViewById(R.id.etSearch);
        ivSearch = view.findViewById(R.id.ivSearch);
        chipGroup = view.findViewById(R.id.chipGroup);
        ivCheck = view.findViewById(R.id.ivCheck);
        ivSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeNewChip();
                onSearchClicked();
            }
        });
        dragLinearLayout = view.findViewById(R.id.dragDropContainer);
        dragLinearLayout.setOnViewSwapListener(new DragLinearLayout.OnViewSwapListener() {
            @Override
            public void onSwap(View firstView, int firstPosition,
                               View secondView, int secondPosition) {
                swapPoemLines(firstView, secondView);
            }
        });
    }

    private void bundleToConfirmScreen() {
        Fragment confirmPoemFragment = new ConfirmPoemFragment();
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("Poem", poemLines);
        bundle.putString("PoemLine", poemLine);
        bundle.putBoolean("activateTutorial", activateTutorial);
        confirmPoemFragment.setArguments(bundle);
        getParentFragmentManager().beginTransaction().replace(R.id.flContainer, confirmPoemFragment).addToBackStack( "generate_poem" ).commit();
    }

    private void swapPoemLines(View firstView, View secondView) {
        int firstIndex = poemLines.indexOf(((TextView) firstView).getText().toString());
        int secondIndex = poemLines.indexOf(((TextView) secondView).getText().toString());
        if (firstIndex >= 0 && secondIndex >= 0) {
            String temp = poemLines.get(secondIndex);
            poemLines.set(secondIndex, poemLines.get(firstIndex));
            poemLines.set(firstIndex, temp);
        }
    }

    private void createPoem() throws ParseException {
        allFriends = new Chip(getContext());
        formatChip(allFriends, "All Friends");
        etSearch.setText("");
        suggested = new Chip(getContext());
        formatChip(suggested, "Suggested");
        poemLayout.setVisibility(View.VISIBLE);
        for (int i = 0; i < poemLines.size(); i++) {
            formatPoemLineTextView(poemLines.get(i));
        }
        if (poemLines.size() == 0) {
            formatPoemLineTextView(poemLine);
            poemLines.add(poemLine);
        }
        String temp = poemLines.size() + linesCount;
        tvLinesCount.setText(temp);
        ivAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBackArrow();
                onSearchClicked();
                runQuery();
            }
        });
    }

    private void formatChip(Chip chip, String text) { // Stretch Goal: set icon to show profile pic as well
        chip.setText(text);
        chip.setCloseIconVisible(false);
        chip.setCheckable(false);
        chip.setClickable(false);
    }

    private void formatPoemLineTextView(String poemLine) {
        TextView tvTemp = new TextView(getContext());
        tvTemp.setText(poemLine);
        tvTemp.setTextColor(getResources().getColor(R.color.gray));
        setLayout(tvTemp);
        dragLinearLayout.addView(tvTemp);
        dragLinearLayout.setViewDraggable(tvTemp, tvTemp);
        if (poemLine != this.poemLine) {
            tvTemp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    singularDelete(tvTemp);
                }
            });
            tvTemp.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    multiDelete(tvTemp);
                    return true;
                }
            });
        }
    }

    private void onSearchClicked() {
        loadingScreen();
        if (allFriendsLines.size() > 0 && previousChipText.containsAll(chips)) {
            lottieAnimationView.setVisibility(View.GONE);
            rvFriendsLines.setVisibility(View.VISIBLE);
        } else {
            runQuery();
        }
    }

    private void loadingScreen() {
        rvFriendsLines.setVisibility(View.INVISIBLE);
        hideSoftKeyboard(requireActivity());
        lottieAnimationView.setVisibility(View.VISIBLE);
    }

    private void makeNewChip() {
        if (chipGroup.getChildCount() < 44 && !etSearch.getText().toString().equals("")) {
            Chip chip = new Chip(getContext());
            chip.setText(etSearch.getText().toString());
            chips.add(etSearch.getText().toString());
            etSearch.setText("");
            // can set icon to show profile pic as well
            chip.setCloseIconVisible(true);
            chip.setCheckable(false);
            chip.setClickable(false);
            chip.setOnCloseIconClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    chipGroup.removeView(chip);
                    chips.remove(chip.getText().toString());
                    if (chipGroup.getChildCount() == 0) {
                        etSearch.setText("");
                        showSoftKeyboard(getActivity());
                        chipGroup.addView(allFriends);
                    }
                    loadingScreen();
                    runQuery();
                }
            });
            chipGroup.addView(chip);
            chipGroup.removeView(allFriends);
        }
    }

    private void runQuery() {
            ExecutorService service = Executors.newSingleThreadExecutor();
            service.execute(new Runnable() {
                @Override
                public void run() {
                    Query query = new Query(user, chips);
                    try {
                        query.call(new Runnable() {
                            @Override
                            public void run() {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (adapter != null && (!chips.containsAll(previousChipText) || !previousChipText.containsAll(chips))) {
                                                adapter.clear();
                                                if (query.getFriendsLines().size() == 0) {
                                                    allFriendsLines.addAll(generatedLines);
                                                } else {
                                                    adapter.addAll(query.getFriendsLines());
                                                }
                                                lottieAnimationView.setVisibility(View.GONE);
                                                rvFriendsLines.setVisibility(View.VISIBLE);
                                            } else {
                                                allFriendsLines.removeAll(allFriendsLines);
                                                if (query.getFriendsLines().size() == 0) {
                                                    chipGroup.removeAllViews();
                                                    chipGroup.addView(suggested);
                                                    allFriendsLines.addAll(generatedLines);
                                                } else if (chips.size() == 0) {
                                                    chipGroup.removeAllViews();
                                                    chipGroup.addView(allFriends);
                                                    allFriendsLines.addAll(query.getFriendsLines());
                                                }
                                                setUpAdapter();
                                            }
                                            previousChipText.removeAll(previousChipText);
                                            previousChipText.addAll(chips);
                                        }
                                    });
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
        adapter = new SearchAdapter(getView().getContext(), allFriendsLines, this, this);
        rvFriendsLines.setAdapter(adapter);
        lottieAnimationView.setVisibility(View.GONE);
        rvFriendsLines.setVisibility(View.VISIBLE);
    }

    @Override
    public void onEvent(ArrayList<String> data) {
        if (adapter.getSelectOn()) {
            ivSearch.setVisibility(View.GONE);
            ivCheck.setVisibility(View.VISIBLE);
            ivCheck.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ivCheck.setVisibility(View.GONE);
                    adapter.setSelectOn(false);
                    selectFriendLine(data);
                }
            });
        } else {
            ivSearch.setVisibility(View.VISIBLE);
            selectFriendLine(data);
        }
    }

    private void selectFriendLine(ArrayList<String> lines) {
        for (int i = 0; i < lines.size(); i++) {
            if (poemLines.size() < 16) {
                tvLinesCount.setTextColor(getResources().getColor(R.color.gray));
                poemLines.add(lines.get(i));
                TextView tvTemp = new TextView(getContext());
                tvTemp.setText(poemLines.get(poemLines.size() - 1));
                tvTemp.setTextColor(getResources().getColor(R.color.gray));
                tvTemp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        singularDelete(tvTemp);
                    }
                });
                tvTemp.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        multiDelete(tvTemp);
                        return true;
                    }
                });
                setLayout(tvTemp);
                dragLinearLayout.addView(tvTemp);
                dragLinearLayout.setViewDraggable(tvTemp, tvTemp);
            } else {
                tvLinesCount.setTextColor(getResources().getColor(R.color.purple_500));
            }
        }
        String temp = poemLines.size() + linesCount;
        tvLinesCount.setText(temp);
        switchToAdd();
    }

    private void singularDelete(TextView tvTemp) {
        try {
            deletedLines.add(tvTemp);
            if (!selectOn) {
                deletePoemLine(deletedLines);
            } else {
                tvTemp.setTextColor(getResources().getColor(R.color.light_gray));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void multiDelete(TextView tvTemp) {
        selectOn = true;
        deletedLines.add(tvTemp);
        ivCheck.setVisibility(View.VISIBLE);
        ivCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    deletePoemLine(deletedLines);
                    selectOn = false;
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
        ivAdd.setVisibility(View.GONE);
        tvTemp.setTextColor(getResources().getColor(R.color.light_gray));
    }

    private void deletePoemLine(ArrayList<TextView> textViews) throws ParseException {
        for (int i = 0; i < textViews.size(); i++) {
            poemLines.remove(textViews.get(i).getText().toString());
            dragLinearLayout.removeDragView(textViews.get(i));
        }
        String temp = poemLines.size() + linesCount;
        tvLinesCount.setText(temp);
        ivCheck.setVisibility(View.GONE);
        ivAdd.setVisibility(View.VISIBLE);
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
        etSearch.setVisibility(View.GONE);
        ivAdd.setVisibility(View.VISIBLE);
        ivBack.setVisibility(View.GONE);
        ivSearch.setVisibility(View.GONE);
        chipGroup.setVisibility(View.GONE);
    }

    private void tutorial() {
        new GuideView.Builder(getContext())
                .setTitle("Add poem lines!")
                .setContentText("Add your friends' or suggested poem lines \n" +
                        "to your poem by tapping the add button.")
                .setTargetView(ivAdd)
                .setDismissType(DismissType.targetView)
                .setGuideListener(new GuideListener() {
                    @Override
                    public void onDismiss(View view) {
                        new GuideView.Builder(getContext())
                                .setTitle("Filter poem lines by friend(s)!")
                                .setContentText("Type in your friend(s)'s username \n" +
                                        "to spotlight their poem lines.")
                                .setTargetView(etSearch)
                                .setDismissType(DismissType.targetView)
                                .setGuideListener(new GuideListener() {
                                    @Override
                                    public void onDismiss(View view) {
                                        new GuideView.Builder(getContext())
                                                .setTitle("Tap to filter!")
                                                .setContentText("Tapping this will kickstart the filtering \n" +
                                                        "and will add a tag with your friend(s)'s username.")
                                                .setTargetView(ivSearch)
                                                .setDismissType(DismissType.targetView)
                                                .setGuideListener(new GuideListener() {
                                                    @Override
                                                    public void onDismiss(View view) {
                                                        poemLinesTutorial();
                                                    }
                                                })
                                                .build()
                                                .show();
                                    }
                                })
                                .build()
                                .show();
                    }
                })
                .build()
                .show();
    }

    private void poemLinesTutorial() {
        new GuideView.Builder(getContext())
                .setTitle("Our suggested poem lines for you!")
                .setContentText("Since you currently have no friends, \n" +
                        "we will provide you with suggested poem lines.")
                .setTargetView(suggested)
                .setDismissType(DismissType.targetView)
                .setGuideListener(new GuideListener() {
                    @Override
                    public void onDismiss(View view) {
                        new GuideView.Builder(getContext())
                                .setTitle("Tap or press to select a poem line!")
                                .setContentText("Tap to select a singular poem line and \n" +
                                        "press on a poem line to activate multi-selection mode. \n" +
                                        "Activating multi-selection mode will allow you\n" +
                                        "to select multiple lines in addition to the one you pressed.")
                                .setTargetView(rvFriendsLines.getChildAt(0))
                                .setDismissType(DismissType.targetView)
                                .setGuideListener(new GuideListener() {
                                    @Override
                                    public void onDismiss(View view) {
                                        new GuideView.Builder(getContext())
                                                .setTitle("Tap and drag to rearrange poem lines!")
                                                .setContentText("Tap and drag a singular poem line to \n" +
                                                        "rearrange the order of the lines in your poem.")
                                                .setTargetView(dragLinearLayout.getChildAt(0))
                                                .setDismissType(DismissType.targetView)
                                                .setGuideListener(new GuideListener() {
                                                    @Override
                                                    public void onDismiss(View view) {
                                                        moveLinesTutorial();
                                                    }
                                                })
                                                .build()
                                                .show();
                                    }
                                })
                                .build()
                                .show();
                    }
                })
                .build()
                .show();
    }

    private void moveLinesTutorial() {
        new GuideView.Builder(getContext())
                .setTitle("Tap or press to delete a poem line!")
                .setContentText("Tap to delete a singular poem line and \n" +
                        "press on a poem line to activate multi-deletion mode.\n" +
                        "Activating multi-deletion mode will allow you\n" +
                        "to delete multiple lines in addition to the one you pressed.")
                .setTargetView(dragLinearLayout.getChildAt(1))
                .setDismissType(DismissType.targetView)
                .setGuideListener(new GuideListener() {
                    @Override
                    public void onDismiss(View view) {
                        new GuideView.Builder(getContext())
                                .setTitle("Maximum of 16 lines per poem!")
                                .setContentText("The maximum amount of lines in \n" +
                                        "a poem is 16 poem lines.")
                                .setTargetView(tvLinesCount)
                                .setDismissType(DismissType.targetView)
                                .setGuideListener(new GuideListener() {
                                    @Override
                                    public void onDismiss(View view) {
                                        new GuideView.Builder(getContext())
                                                .setTitle("Tap the forward arrow to proceed!")
                                                .setContentText("When you're done editing your poem, \n" +
                                                        "tap the forward arrow to go to the Poem Confirmation Screen.")
                                                .setTargetView(ivForwardArrow)
                                                .setDismissType(DismissType.targetView)
                                                .build()
                                                .show();
                                        activateTutorial = false;
                                    }
                                })
                                .build()
                                .show();
                    }
                })
                .build()
                .show();
    }

    private static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        View focusedView = activity.getCurrentFocus();
        if(focusedView != null && inputMethodManager.isAcceptingText()) {
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
package com.example.capstone.fragments;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.example.capstone.R;
import com.example.capstone.SearchAdapter;
import com.example.capstone.models.Line;
import com.example.capstone.models.OpenAIThread;
import com.example.capstone.models.Poem;
import com.example.capstone.models.User;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import smartdevelop.ir.eram.showcaseviewlib.GuideView;
import smartdevelop.ir.eram.showcaseviewlib.config.DismissType;
import smartdevelop.ir.eram.showcaseviewlib.listener.GuideListener;

public class GenerateFragment extends Fragment implements SearchAdapter.EventListener {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private EditText etUserInput;
    private String poemLine;
    private ImageView ivForwardArrow;
    protected SearchAdapter adapter;
    protected List<String> allGeneratedLines;
    private RecyclerView rvGeneratedLines;
    private LottieAnimationView lottieAnimationView;
    private ArrayList<String> generatedLines;
    private boolean activateTutorial = false;
    private User user;
    private static final String TAG = "GenerateFragment";


    public GenerateFragment() {
        // Required empty public constructor
    }

    public static GenerateFragment newInstance(String param1, String param2) {
        GenerateFragment fragment = new GenerateFragment();
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
        return inflater.inflate(R.layout.fragment_generate, container, false);
    }
    // allow user to choose to make their poem line public
    // add friend's username to their poem line in edit poem screen
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            activateTutorial = bundle.getBoolean("activateTutorial");
        }
        etUserInput = view.findViewById(R.id.etUserInput);
        etUserInput.setVisibility(View.VISIBLE);
        lottieAnimationView = view.findViewById(R.id.lottieLoad);
        rvGeneratedLines = view.findViewById(R.id.rvGeneratedLines);
        ivForwardArrow = view.findViewById(R.id.ivForwardArrow);
        if (activateTutorial) {
            new GuideView.Builder(getContext())
                    .setTitle("Give us a prompt!")
                    .setContentText("Enter a word, phrase, or sentence.")
                    .setTargetView(etUserInput)
                    .setDismissType(DismissType.targetView)
                    .setGuideListener(new GuideListener() {
                        @Override
                        public void onDismiss(View view) {
                            new GuideView.Builder(getContext())
                                    .setTitle("Generate and proceed!")
                                    .setContentText("After you generate a poem line, \n" +
                                            "tap the forward arrow to proceed.")
                                    .setTargetView(ivForwardArrow)
                                    .setDismissType(DismissType.targetView)
                                    .build()
                                    .show();
                        }
                    })
                    .build()
                    .show();
        }
        ivForwardArrow.setOnClickListener(new View.OnClickListener() {
            // need to identify if the word is a real word
            @Override
            public void onClick(View v) {
                try {
                    generatePrompts();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void generatePrompts() throws InterruptedException {
        String prompt = etUserInput.getText().toString().replaceAll("[^a-zA-Z0-9]", "");
        if (prompt.length() > 0) {
            hideSoftKeyboard(getActivity());
            if (generatedLines != null) {
                adapter.clear();
                generatedLines = null;
            }
            lottieAnimationView.setVisibility(View.VISIBLE);
            ivForwardArrow.setVisibility(View.GONE);
            ExecutorService service = Executors.newSingleThreadExecutor();
            service.execute(new Runnable() {
                @Override
                public void run() {
                    OpenAIThread openAIThread = new OpenAIThread(etUserInput.getText().toString());
                    openAIThread.runCallback(new Runnable() {
                        @Override
                        public void run() {
                            generatedLines = openAIThread.getGeneratedLines();
                            allGeneratedLines = generatedLines.subList(1, generatedLines.size());
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (adapter != null) {
                                        adapter.clear();
                                        adapter.addAll(allGeneratedLines);
                                    } else {
                                        displayPoemLines(allGeneratedLines);
                                    }
                                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getView().getContext());
                                    rvGeneratedLines.setLayoutManager(linearLayoutManager);
                                    rvGeneratedLines.setAdapter(adapter);
                                    lottieAnimationView.setVisibility(View.GONE);
                                    rvGeneratedLines.setVisibility(View.VISIBLE);
                                    ivForwardArrow.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                    });
                }
            });
        } else {
            showErrorMessage();
        }
    }

    private void showErrorMessage() {
        Toast.makeText(getActivity(), "Please enter a legitimate word, phrase, or sentence!",
                Toast.LENGTH_LONG).show();
        etUserInput.setText("");
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        if(inputMethodManager.isAcceptingText()){
            inputMethodManager.hideSoftInputFromWindow(
                    activity.getCurrentFocus().getWindowToken(),
                    0
            );
        }
    }

    private void displayPoemLines(List<String> generatedLines) {
        adapter = new SearchAdapter(getView().getContext(), generatedLines, this);
    }

    public void createPoemLine(String line, ArrayList<String> generatedLines) {
        try {
            String prompt = etUserInput.getText().toString();
            etUserInput.setText(line);
            ParseQuery<User> currentUserQuery = ParseQuery.getQuery(User.class);
            currentUserQuery.include(User.KEY_FRIENDS);
            currentUserQuery.whereEqualTo("objectId", ParseUser.getCurrentUser().getObjectId());
            currentUserQuery.getFirstInBackground(new GetCallback<User>() {
                @Override
                public void done(User currentUser, ParseException e) {
                    user = currentUser;
                }
            });
            ivForwardArrow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // goes to Create Poem Fragment
                    ArrayList<String> poemLines = new ArrayList<>();
                    Fragment createPoemFragment = new CreatePoemFragment();
                    Bundle bundle = new Bundle();
                    bundle.putStringArrayList("Poem", poemLines);
                    bundle.putString("Line", line);
                    bundle.putString("Prompt", prompt);
                    bundle.putParcelable("user", user);
                    bundle.putStringArrayList("GeneratedLines", generatedLines);
                    bundle.putBoolean("activateTutorial", activateTutorial);
                    createPoemFragment.setArguments(bundle);
                    getParentFragmentManager().beginTransaction().replace(R.id.flContainer, createPoemFragment).addToBackStack( "generate_poem" ).commit();
                }
            });
            Log.i(TAG, "poem line creation success! " + poemLine);
        } catch (Exception exception) {
            Log.e(TAG, "poem line creation failed :(", exception);
        }
    }

    @Override
    public void onEvent(ArrayList<String> data) {
        createPoemLine(data.get(0), new ArrayList<String>(allGeneratedLines));
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
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

import com.airbnb.lottie.LottieAnimationView;
import com.example.capstone.R;
import com.example.capstone.SearchAdapter;
import com.example.capstone.models.Line;
import com.example.capstone.models.OpenAIThread;
import com.example.capstone.models.Poem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
// add instructions for generate
    // move hint in etSearch to a TextView label for instruction - done
    // tell user that they can drag and drop, tap to delete, and tap to add
    // move etSearch to visible when add is clicked; replace current etSearch with instructions/guidance text - done
    // allow user to choose to make their poem line public
    // add friend's username to their poem line in edit poem screen
    // rephrase line count: Max 16 lines?
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        etUserInput = view.findViewById(R.id.etUserInput);
        etUserInput.setVisibility(View.VISIBLE);
        lottieAnimationView = view.findViewById(R.id.lottieLoad);
        rvGeneratedLines = view.findViewById(R.id.rvGeneratedLines);
        ivForwardArrow = view.findViewById(R.id.ivForwardArrow);
        ivForwardArrow.setOnClickListener(new View.OnClickListener() {
            // need to identify if the word is a real word
            @Override
            public void onClick(View v) {
                try {
                    generatePrompts(v);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void generatePrompts(View view) throws InterruptedException {
        if (etUserInput.getText().toString().length() > 0) {
            hideSoftKeyboard(getActivity());
            if (generatedLines != null) {
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
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    lottieAnimationView.setVisibility(View.GONE);
                                    ivForwardArrow.setVisibility(View.VISIBLE);
                                    displayPoemLines(generatedLines);
                                }
                            });
                        }
                    });
                }
            });
        }
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

    private void displayPoemLines(ArrayList<String> generatedLines) {
        allGeneratedLines = generatedLines.subList(1, generatedLines.size());
        adapter = new SearchAdapter(getView().getContext(), allGeneratedLines, this, this);
        rvGeneratedLines.setAdapter(adapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getView().getContext());
        rvGeneratedLines.setLayoutManager(linearLayoutManager);
    }

    public void createPoemLine(String line, ArrayList<String> generatedLines) {
        try {
            String prompt = etUserInput.getText().toString();
            etUserInput.setText(line);
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
                    bundle.putStringArrayList("GeneratedLines", generatedLines);
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
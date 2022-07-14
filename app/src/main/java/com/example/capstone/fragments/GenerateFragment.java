package com.example.capstone.fragments;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.capstone.R;
import com.example.capstone.models.Line;
import com.example.capstone.models.OpenAIThread;
import com.example.capstone.models.Poem;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GenerateFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private EditText etUserInput;
    private LinearLayout linearLayout;
    private String poemLine;
    private ImageView ivForwardArrow;
    private ProgressBar pb;


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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        etUserInput = view.findViewById(R.id.etUserInput);
        etUserInput.setVisibility(View.VISIBLE);
        linearLayout = view.findViewById(R.id.friendsLinesLayout);
        pb = view.findViewById(R.id.pbLoading);
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
            pb.setVisibility(ProgressBar.VISIBLE);
            ivForwardArrow.setVisibility(View.GONE);
            ExecutorService service = Executors.newSingleThreadExecutor();
            service.execute(new Runnable() {
                @Override
                public void run() {
                    OpenAIThread openAIThread = new OpenAIThread(etUserInput.getText().toString());
                    openAIThread.runCallback(new Runnable() {
                        @Override
                        public void run() {
                            String[] generatedLines = openAIThread.getGeneratedLines();
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    pb.setVisibility(ProgressBar.GONE);
                                    ivForwardArrow.setVisibility(View.VISIBLE);
                                    displayPoemLines(generatedLines, view);
                                }
                            });
                        }
                    });
                }
            });
        } else {
            linearLayout.setVisibility(View.GONE);
            linearLayout.removeAllViews();
        }
    }

    private void displayPoemLines(String[] generatedLines, View view) {
        if (generatedLines != null) {
            for( int i = 2; i < generatedLines.length; i++ )
            {
                TextView tvTestString = new TextView(view.getContext());
                tvTestString.setText(generatedLines[i]);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(0,0,0,20);
                tvTestString.setLayoutParams(params);
                tvTestString.setTextSize(20);
                tvTestString.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        tvTestString.setTextColor(getResources().getColor(R.color.gray));
                        createPoemLine(tvTestString, generatedLines);
                    }
                });
                linearLayout.addView(tvTestString);
            }
            linearLayout.setVisibility(View.VISIBLE);
        }
    }

    public void createPoemLine(TextView tvTestString, String[] generatedLines) {
        try {
            poemLine = tvTestString.getText().toString();
            String prompt = etUserInput.getText().toString();
            etUserInput.setText(tvTestString.getText().toString());
            ivForwardArrow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                        // goes to Create Poem Fragment
                        Fragment createPoemFragment = new CreatePoemFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("Line", poemLine);
                        bundle.putString("Prompt", prompt);
                        bundle.putStringArray("GeneratedLines", generatedLines);
                        createPoemFragment.setArguments(bundle);
                        getParentFragmentManager().beginTransaction().replace(R.id.flContainer, createPoemFragment).addToBackStack( "generate_poem" ).commit();
                }
            });
            Log.i("poem_line_creation_test", "poem line creation success! " + poemLine);
        } catch (Exception exception) {
            Log.e("poem_line_creation_test", "poem line creation failed :(", exception);
        }
    }

}
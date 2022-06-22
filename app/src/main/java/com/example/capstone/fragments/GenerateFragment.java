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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.capstone.R;
import com.example.capstone.models.Line;
import com.example.capstone.models.Poem;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class GenerateFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private EditText etUserInput;
    private Button bGenerate;
    private LinearLayout linearLayout;
    private Line poemLine;
    private Button bPublish;

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
        bGenerate = view.findViewById(R.id.bGenerate);
        linearLayout = view.findViewById(R.id.linearLayout);
        bPublish = view.findViewById(R.id.bPublish);

        bGenerate.setOnClickListener(new View.OnClickListener() {
            // need to identify if the word is a real word
            @Override
            public void onClick(View v) {
               generatePrompts(v);
            }
        });
    }

    public void generatePrompts(View view) {
        if (etUserInput.getText().toString().length() > 0) {
            // skeleton: generate hello world
            String[] textArray = {"hello world", "hello there", "hello everyone", "hello all"};

            if (linearLayout.getVisibility() != View.VISIBLE) {
                linearLayout.setVisibility(View.VISIBLE);
                for( int i = 0; i < textArray.length; i++ )
                {
                    TextView textView = new TextView(view.getContext());
                    textView.setText(textArray[i]);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    params.setMargins(0,0,0,20);
                    textView.setLayoutParams(params);
                    textView.setTextSize(20);
                    // if user taps on generated textview, go to next screen
                    textView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            createPoemLine(textView);

                        }
                    });
                    linearLayout.addView(textView);
                }
            }
        } else {
            linearLayout.setVisibility(View.GONE);
            linearLayout.removeAllViews();
        }
        // Calls open ai on inputted word
    }

    public void createPoemLine(TextView textView) {
        try {
            poemLine = new Line();
            poemLine.setPoemLine(textView.getText().toString());
            poemLine.setAuthor(ParseUser.getCurrentUser());
            bPublish.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    savePoemLine();
                }
            });
            Log.i("poem_line_creation_test", "poem line creation success! " + poemLine.getPoemLine());
        } catch (Exception exception) {
            Log.e("poem_line_creation_test", "poem line creation failed :(", exception);
        }
    }

    public void savePoemLine() {
            // if user is the first friend to post poem line, create a new poem
            poemLine.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        Log.e("line_saved_test", "Poem line has not been saved", e);
                    } else {
                        Log.i("line_saved_test", "Poem line has been saved!");
                    }
                }
            });
            createPoem();
    }

    public void createPoem() {
        Poem poem = new Poem();
        // else fetch poem created for today and add to that
        // current issue: poem is null, cannot add to a null object
        poem.addAuthor(poemLine.getAuthor());
        poem.setPoemLines(poemLine);
        poem.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.i("poem_creation_test", "Poem created success!");
                } else {
                    Log.e("poem_creation_test", "Poem created failed :(", e);
                }
            }
        });
    }

}
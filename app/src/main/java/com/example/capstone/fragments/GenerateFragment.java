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
import android.widget.Toast;

import com.example.capstone.R;
import com.example.capstone.models.Poem;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class GenerateFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private EditText etUserInput;
    private Button bGenerate;
    private LinearLayout linearLayout;
    private String poemLine;
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
                    TextView tvTestString = new TextView(view.getContext());
                    tvTestString.setText(textArray[i]);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    params.setMargins(0,0,0,20);
                    tvTestString.setLayoutParams(params);
                    tvTestString.setTextSize(20);
                    // if user taps on generated textview, go to next screen
                    tvTestString.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            createPoemLine(tvTestString);
                        }
                    });
                    linearLayout.addView(tvTestString);
                }
            }
        } else {
            linearLayout.setVisibility(View.GONE);
            linearLayout.removeAllViews();
        }
        // Calls open ai on inputted word
    }

    public void createPoemLine(TextView tvTestString) {
        try {
            poemLine = "";
            poemLine += tvTestString.getText().toString();
            etUserInput.setText(tvTestString.getText().toString());
            bPublish.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    createPoem();
                }
            });
            Log.i("poem_line_creation_test", "poem line creation success! " + poemLine);
        } catch (Exception exception) {
            Log.e("poem_line_creation_test", "poem line creation failed :(", exception);
        }
    }

    public void createPoem() {
        // if user is the first friend to post poem line, create a new poem (for now, just creates a poem everytime)
        Poem poem = new Poem();
        // otherwise just update poem
        poem.addAuthor(ParseUser.getCurrentUser());
        poem.updatePoem(poemLine);
        poem.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Toast.makeText(getActivity(), "Your poem line was added to today's poem!",
                            Toast.LENGTH_LONG).show();
                    Fragment poemDetailsFragment = new PoemDetailsFragment();
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("Poem", poem);
                    poemDetailsFragment.setArguments(bundle);
                    getParentFragmentManager().beginTransaction().replace(R.id.flContainer, poemDetailsFragment).addToBackStack( "generate_poem" ).commit();
                } else {
                    Log.e("poem_creation_test", "Poem created failed :(", e);
                    Toast.makeText(getActivity(), "Your poem line was not saved to today's poem :(",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

}
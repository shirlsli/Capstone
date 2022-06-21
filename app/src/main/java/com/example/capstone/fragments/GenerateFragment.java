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
import com.parse.ParseException;
import com.parse.ParseUser;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GenerateFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GenerateFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private EditText etUserInput;
    private Button bGenerate;
    private LinearLayout linearLayout;

    public GenerateFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment GenerateFragment.
     */
    // TODO: Rename and change types and number of parameters
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

        bGenerate.setOnClickListener(new View.OnClickListener() {
            // need to identify if the word is a real word
            @Override
            public void onClick(View v) {
                if (etUserInput.getText().toString().length() > 0) {
                    // skeleton: generate hello world
                    String[] textArray = {"hello world", "hello there", "hello everyone", "hello all"};

                    if (linearLayout.getVisibility() != View.VISIBLE) {
                        linearLayout.setVisibility(View.VISIBLE);
                        for( int i = 0; i < textArray.length; i++ )
                        {
                            TextView textView = new TextView(v.getContext());
                            textView.setText(textArray[i]);
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            params.setMargins(0,0,0,20);
                            textView.setLayoutParams(params);
                            textView.setTextSize(20);
                            textView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    try {
                                        Line poemLine = new Line();
                                        poemLine.setPoemLine(textView.getText().toString());
                                        poemLine.setAuthor(ParseUser.getCurrentUser());
                                        Log.i("poem_line_creation_test", "poem line creation success!");
                                    } catch (Exception exception) {
                                        Log.i("poem_line_creation_test", "poem line creation failed :(");
                                    }

                                }
                            });
                            linearLayout.addView(textView);
                        }
                        // if user taps on generated textview, go to next screen
                    }
                } else {
                    linearLayout.setVisibility(View.GONE);
                    linearLayout.removeAllViews();
                }
                // Calls open ai on inputted word

            }
        });
    }

}
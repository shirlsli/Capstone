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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.capstone.R;
import com.example.capstone.models.Line;
import com.example.capstone.models.Poem;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;

import smartdevelop.ir.eram.showcaseviewlib.GuideView;
import smartdevelop.ir.eram.showcaseviewlib.config.DismissType;
import smartdevelop.ir.eram.showcaseviewlib.config.Gravity;

public class ConfirmPoemFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private ArrayList<String> poemLines;
    private LinearLayout linearLayout;
    private TextView tvPrompt;
    private Button bPublish;
    private String poemLine;
    private static final String TAG = "ConfirmPoemFragments";

    private String mParam1;
    private String mParam2;

    public ConfirmPoemFragment() {
        // Required empty public constructor
    }

    public static ConfirmPoemFragment newInstance(String param1, String param2) {
        ConfirmPoemFragment fragment = new ConfirmPoemFragment();
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
        return inflater.inflate(R.layout.fragment_confirm_poem, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            poemLines = bundle.getStringArrayList("Poem");
            poemLine = bundle.getString("PoemLine");
            linearLayout = view.findViewById(R.id.friendsLinesLayout);
            tvPrompt = view.findViewById(R.id.tvPoemConfirmation);
            bPublish = view.findViewById(R.id.bPost);
            poemConfirmScreen();
        }
    }

    private void setPoemConfirmUI() {
        tvPrompt.setText(R.string.poemConfirmation);
        tvPrompt.setVisibility(View.VISIBLE);
        linearLayout.setVisibility(View.VISIBLE);
        for (int i = 0; i < poemLines.size(); i++) {
            TextView tvPoem = new TextView(getContext());
            tvPoem.setText(poemLines.get(i));
            if (i == 4 || i == 8 || i == 12) {
                TextView tvBlank = new TextView(getContext());
                linearLayout.addView(tvBlank);
            }
            linearLayout.addView(tvPoem);
        }
        // set publish visibility to visible
        bPublish.setVisibility(View.VISIBLE);
    }

    private void poemConfirmScreen() {
        // change add friends' poem lines textview to contain text: Are you done creating your poem?
        setPoemConfirmUI();
        bPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Poem poem = new Poem();
                int userLineIdx = poemLines.indexOf(poemLine);
                for (int i = 0; i < poemLines.size(); i++) {
                    Line line = new Line();
                    line.setPoemLine(poemLines.get(i));
                    if (i == userLineIdx) {
                        line.setAuthor(ParseUser.getCurrentUser());
                    }
                    poem.updatePoem(line);
                }
                poem.addAuthor(ParseUser.getCurrentUser());
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
                            Log.e(TAG, "Poem created failed :(", e);
                            Toast.makeText(getActivity(), "Your poem line was not saved to today's poem :(",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }
}
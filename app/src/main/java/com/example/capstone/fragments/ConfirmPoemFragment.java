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
import com.example.capstone.models.Poem;
import com.parse.ParseException;
import com.parse.SaveCallback;

public class ConfirmPoemFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private Poem poem;
    private LinearLayout linearLayout;
    private TextView tvPrompt;
    private Button bPublish;

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
            poem = bundle.getParcelable("Poem");
            linearLayout = view.findViewById(R.id.friendsLinesLayout);
            tvPrompt = view.findViewById(R.id.tvPoemConfirmation);
            bPublish = view.findViewById(R.id.bPublish);
            poemConfirmScreen();
        }
    }

    private void poemConfirmScreen() {
        // change add friends' poem lines textview to contain text: Are you done creating your poem?
        tvPrompt.setText(R.string.poemConfirmation);
        tvPrompt.setVisibility(View.VISIBLE);
        linearLayout.setVisibility(View.VISIBLE);
        for (int i = 0; i < poem.getPoemLines().size(); i++) {
            TextView tvPoem = new TextView(getContext());
            tvPoem.setText(poem.getPoemLines().get(i).getPoemLine());
            linearLayout.addView(tvPoem);
        }
        // set publish visibility to visible
        bPublish.setVisibility(View.VISIBLE);
        bPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        });
    }
}
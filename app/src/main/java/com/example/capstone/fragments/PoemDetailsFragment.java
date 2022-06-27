package com.example.capstone.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.capstone.R;
import com.example.capstone.models.Line;
import com.example.capstone.models.Poem;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.util.List;

public class PoemDetailsFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private LinearLayout poemLayout;
    private Poem poem;

    public PoemDetailsFragment() {
        // Required empty public constructor
    }

    public static PoemDetailsFragment newInstance(String param1, String param2) {
        PoemDetailsFragment fragment = new PoemDetailsFragment();
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
        return inflater.inflate(R.layout.fragment_poem_details, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        poemLayout = view.findViewById(R.id.poemLayout);
        // Skeleton: everyone's poem lines get broken into stanzas (one giant poem a day)
        // Stretch: want to query poem containing all of user's friends
        // maybe mark a poem with boolean signifying today?
        Bundle bundle = getArguments();
        if (bundle != null) {
            poem = bundle.getParcelable("Poem");
        } else {
            Log.i("bundle_null", "Bundle is null");
        }
        for (int i = 0; i < poem.getPoemLines().size(); i++) {
            TextView tvNewLine = new TextView(view.getContext());
            tvNewLine.setText(poem.getPoemLines().get(i).getPoemLine());
            poemLayout.addView(tvNewLine);
        }
    }
}
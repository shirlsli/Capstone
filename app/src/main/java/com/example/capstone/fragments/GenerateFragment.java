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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.capstone.R;
import com.example.capstone.models.Line;
import com.example.capstone.models.OpenAIThread;
import com.example.capstone.models.Poem;
import com.example.capstone.models.User;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.theokanning.openai.OpenAiService;
import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.engine.Engine;
import com.theokanning.openai.search.SearchRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GenerateFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private EditText etUserInput;
    private LinearLayout linearLayout;
    private Line poemLine;
    private Button bPublish;
    private TextView tvPrompt;
    private ImageView ivForwardArrow;

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
        linearLayout = view.findViewById(R.id.linearLayout);
        bPublish = view.findViewById(R.id.bPublish);
        tvPrompt = view.findViewById(R.id.tvPrompt);
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
            OpenAIThread openAIThread = new OpenAIThread(etUserInput.getText().toString());
            openAIThread.start();
            // skeleton: generate hello world
            openAIThread.join();
            String[] generatedLines = openAIThread.getGeneratedLines();
            if (linearLayout.getVisibility() != View.VISIBLE) {
                linearLayout.setVisibility(View.VISIBLE);
                for( int i = 2; i < generatedLines.length; i++ )
                {
                    TextView tvTestString = new TextView(view.getContext());
                    tvTestString.setText(generatedLines[i]);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    params.setMargins(0,0,0,20);
                    tvTestString.setLayoutParams(params);
                    tvTestString.setTextSize(20);
                    // if user taps on generated textview, go to next screen
                    tvTestString.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            tvTestString.setTextColor(getResources().getColor(R.color.gray));
                            createPoemLine(tvTestString);
                        }
                    });
                    linearLayout.addView(tvTestString);
                }
            }
        } else {
            linearLayout.setVisibility(View.GONE);
            linearLayout.removeAllViews();
            bPublish.setVisibility(View.GONE);
        }
        // Calls open ai on inputted word
    }

    public void createPoemLine(TextView tvTestString) {
        try {
            poemLine = new Line();
            poemLine.setPoemLine(tvTestString.getText().toString());
            etUserInput.setText(tvTestString.getText().toString());
            ivForwardArrow.setVisibility(View.VISIBLE);
            ivForwardArrow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        // need to check if on add your friends' poem lines screen
                        createPoem();
                    } catch (ParseException e) {
                        Log.e("create_poem_error", "ParseException", e);
                    }
                }
            });
            Log.i("poem_line_creation_test", "poem line creation success! " + poemLine);
        } catch (Exception exception) {
            Log.e("poem_line_creation_test", "poem line creation failed :(", exception);
        }
    }

    public void createPoem() throws ParseException {
        // brand new query for the current user
        ParseQuery<User> currentUserQuery = ParseQuery.getQuery(User.class);
        currentUserQuery.include(User.KEY_FRIENDS); // TODO: see if we can remove this?
        currentUserQuery.whereEqualTo("objectId", ParseUser.getCurrentUser().getObjectId());
        currentUserQuery.findInBackground(new FindCallback<User>() {
            @Override
            public void done(List<User> objects, ParseException e) {
                ParseQuery<Line> poemLineQuery = ParseQuery.getQuery(Line.class);
                linearLayout.removeAllViews();
                Poem poem = new Poem();
                poemLine.setAuthor(ParseUser.getCurrentUser());
                poem.addAuthor(ParseUser.getCurrentUser());
                if (objects != null && objects.get(0).getFriends() != null) {
                    poemLineQuery.whereContainedIn(Line.KEY_AUTHOR, objects.get(0).getFriends());
                    poemLineQuery.setLimit(5);
                    poemLineQuery.addDescendingOrder("createdAt");
                    poemLineQuery.include(Line.KEY_AUTHOR);
                    poemLineQuery.include(Line.KEY_POEM_LINE);
                    poemLineQuery.findInBackground(new FindCallback<Line>() {
                        @Override
                        public void done(List<Line> friendLines, ParseException e) {
                            if (e != null) {
                                Log.e("tag", friendLines.toString(), e);
                            } else {
                                poem.updatePoem(poemLine);
                                tvPrompt.setVisibility(View.VISIBLE);
                                bPublish.setVisibility(View.GONE);
                                addFriendLines(friendLines, poem);
                            }
                        }
                    });
                } else {
                    TextView tvNoFriends = new TextView(getContext());
                    tvNoFriends.setText(R.string.noFriendsPrompt);
                    tvNoFriends.setTextSize(20);
                    linearLayout.addView(tvNoFriends);
                    // make a method that generates TextViews into a list to add to linearLayout
                }
                ivForwardArrow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        poemConfirmScreen(poem);
                    }
                });
            }
        });
    }


    private void poemConfirmScreen(Poem poem) {
        // change add friends' poem lines textview to contain text: Are you done creating your poem?
        tvPrompt.setText(R.string.poemConfirmation);
        // remove linearlayout's textviews with one new textview (textview content: etUserInput text)
        TextView tvPoem = new TextView(getContext());
        tvPoem.setText(etUserInput.getText());
        linearLayout.removeAllViews();
        linearLayout.addView(tvPoem);
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
        // set forward arrow visibility to gone
        ivForwardArrow.setVisibility(View.GONE);
        // set editext to gone? would need to set it to visible every time
        etUserInput.setText("");
        etUserInput.setVisibility(View.GONE);
    }

    private void addFriendLines(List<Line> friendLines, Poem poem) {
        for (int i = 0; i < friendLines.size(); i++) {
            Line friendLine = new Line();
            friendLine.setAuthor(friendLines.get(i).getAuthor());
            friendLine.setPoemLine(friendLines.get(i).getPoemLine());
            TextView tvTestString = new TextView(getContext());
            tvTestString.setText(friendLine.getPoemLine());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(16,0,0,20);
            tvTestString.setLayoutParams(params);
            tvTestString.setTextSize(20);
            tvTestString.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tvTestString.setTextColor(getResources().getColor(R.color.gray));
                    selectFriendLine(friendLine, poem);
                }
            });
            linearLayout.addView(tvTestString);
        }
    }

    private void selectFriendLine(Line friendLine, Poem poem) {
        poem.updatePoem(friendLine);
        String curPoem = etUserInput.getText().toString() + "\n" + friendLine.getPoemLine();
        etUserInput.setText(curPoem);
    }

}
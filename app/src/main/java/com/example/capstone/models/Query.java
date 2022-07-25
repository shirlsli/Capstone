package com.example.capstone.models;

import android.util.Log;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class Query {

    private static final String TAG = "Query";
    private ArrayList<String> userInput;
    private List<String> friendsLines;
    private User currentUser;

    public Query(User currentUser, ArrayList<String> userInput) {
        this.currentUser = currentUser;
        this.userInput = userInput;
    }

    public List<String> getFriendsLines() {
        return friendsLines;
    }

    public void call(Runnable callback) throws Exception {
        friendsLines = new ArrayList<>();
        if (currentUser != null && currentUser.getFriends() != null) {
            // query User with username: etSearch's input
            ParseQuery<ParseUser> userQuery = ParseQuery.getQuery(ParseUser.class);
            userQuery.whereContainedIn("username", userInput);
            userQuery.findInBackground(new FindCallback<ParseUser>() {
                @Override
                public void done(List<ParseUser> friends, ParseException e) {
                    if (e != null) {
                        Log.e(TAG, friends.toString(), e);
                    } else {
                        ParseQuery<Line> poemLineQuery = ParseQuery.getQuery(Line.class);
                        poemLineQuery.include(Line.KEY_AUTHOR);
                        poemLineQuery.include(Line.KEY_POEM_LINE);
                        poemLineQuery.whereContainedIn(Line.KEY_AUTHOR, currentUser.getFriends());
                        poemLineQuery.setLimit(20);
                        poemLineQuery.addDescendingOrder("createdAt");
                        poemLineQuery.findInBackground(new FindCallback<Line>() {
                            @Override
                            public void done(List<Line> objects, ParseException e) {
                                if (e != null) {
                                    Log.e(TAG, objects.toString(), e);
                                } else {
                                    if (userInput.size() == 0) {
                                        prepareCallback(objects, callback); // case: user inputted is not current user's friend
                                    } else {
                                        ParseQuery<Line> friendLineQuery = ParseQuery.getQuery(Line.class);
                                        friendLineQuery.whereContainedIn(Line.KEY_AUTHOR, friends);
                                        friendLineQuery.include(Line.KEY_POEM_LINE);
                                        friendLineQuery.setLimit(20);
                                        friendLineQuery.addDescendingOrder("createdAt");
                                        friendLineQuery.findInBackground(new FindCallback<Line>() {
                                            @Override
                                            public void done(List<Line> friendLines, ParseException e) {
                                                prepareCallback(friendLines, callback);
                                            }
                                        });
                                    }
                                }
                            }
                        });
                    }
                }
            });
        } else { // case: user does not exists
            ArrayList<Line> temp = new ArrayList<>();
            prepareCallback(temp, callback);
        }
    }

    private void prepareCallback(List<Line> friendLines, Runnable callback) {
        for (int i = 0; i < friendLines.size(); i++) {
            friendsLines.add(friendLines.get(i).getPoemLine());
        }
        callback.run();
    }

}

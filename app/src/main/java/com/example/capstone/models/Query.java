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
    private ArrayList<String> friendsLines;
    private String userInput;

    public Query(String userInput) {
        this.userInput = userInput;
    }

    public ArrayList<String> getFriendsLines() {
        return friendsLines;
    }

    public void call(Runnable callback) throws Exception {
        friendsLines = new ArrayList<>();
        ParseQuery<User> currentUserQuery = ParseQuery.getQuery(User.class);
        currentUserQuery.include(User.KEY_FRIENDS);
        currentUserQuery.whereEqualTo("objectId", ParseUser.getCurrentUser().getObjectId());
        currentUserQuery.getFirstInBackground(new GetCallback<User>() {
            @Override
            public void done(User currentUser, ParseException e) {
                if (currentUser != null && currentUser.getFriends() != null) {
                    if (!userInput.isEmpty()) {
                        // query User with username: etSearch's input
                        ParseQuery<ParseUser> userQuery = ParseQuery.getQuery(ParseUser.class);
                        userQuery.whereEqualTo("username", userInput);
                        userQuery.getFirstInBackground(new GetCallback<ParseUser>() {
                            @Override
                            public void done(ParseUser friend, ParseException e) {
                                if (e != null) {
                                    Log.e(TAG, friend.toString(), e);
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
                                                ParseQuery<Line> friendLineQuery = ParseQuery.getQuery(Line.class);
                                                friendLineQuery.whereEqualTo(Line.KEY_AUTHOR, friend);
                                                friendLineQuery.include(Line.KEY_POEM_LINE);
                                                friendLineQuery.setLimit(20);
                                                friendLineQuery.addDescendingOrder("createdAt");
                                                friendLineQuery.findInBackground(new FindCallback<Line>() {
                                                    @Override
                                                    public void done(List<Line> friendLines, ParseException e) {
                                                        for (int i = 0; i < friendLines.size(); i++) {
                                                            friendsLines.add(friendLines.get(i).getPoemLine());
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    });
                                }
                            }
                        });
                    }
                }
            }
        });
        callback.run();
    }
}

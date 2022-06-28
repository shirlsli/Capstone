package com.example.capstone.models;

import android.os.Parcelable;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@ParseClassName("Poem")
public class Poem extends ParseObject implements Parcelable {

    public static final String KEY_AUTHORS = "authors";
    public static final String KEY_POEM_LINES = "poemLines";
    private String poemString;

    public List<ParseUser> getAuthors() {
        return getList(KEY_AUTHORS);
    }

    public void addAuthor(ParseUser user) {
        add(KEY_AUTHORS, user);
    }

    public List<String> getPoemLines() { return getList(KEY_POEM_LINES); }

    // we want the user to have the ability to edit their poem line
    // need to save index of poem line in poem somewhere
    // know for sure that poem line should be the object saving its index
    // having an index would be more runtime efficient than iterating through the poem and finding the line with matching user
    // constant vs linear time (depending on scope of user's friends)
    // a poem line belongs to a poem when their createdAt and updatedAt dates/times match within 24 hours
    // that means from 12:00 am to 11:59 pm
    // skeleton: time zone PDT
    // for now, just add it to the end of the poem
    public void updatePoem(String poemLine) {
        add(KEY_POEM_LINES, poemLine);
    }
}
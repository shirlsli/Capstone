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
public class Poem extends ParseObject {

    public static final String KEY_AUTHORS = "authors";
    public static final String KEY_POEM_LINES = "poemLines";

    public List<ParseUser> getAuthors() {
        return getList(KEY_AUTHORS);
    }

    public void addAuthor(ParseUser user) {
        add(KEY_AUTHORS, user);
    }

    public List<Line> getPoemLines() { return getList(KEY_POEM_LINES); }

    public void updatePoem(Line poemLine) { add(KEY_POEM_LINES, poemLine); }

}
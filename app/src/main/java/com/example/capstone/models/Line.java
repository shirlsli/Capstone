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

@ParseClassName("Line")
public class Line extends ParseObject {

    public static final String KEY_AUTHOR = "author";
    public static final String KEY_POEM_LINE = "poemLine";

    public ParseUser getAuthor() {
        return getParseUser(KEY_AUTHOR);
    }

    public void setAuthor(ParseUser user) {
        put(KEY_AUTHOR, user);
    }

    public String getPoemLine() { return getString(KEY_POEM_LINE); }

    public void setPoemLine(String poemLine) { put(KEY_POEM_LINE, poemLine); }
}
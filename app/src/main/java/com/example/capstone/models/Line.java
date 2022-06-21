package com.example.capstone.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Line")
public class Line extends ParseObject {

    public static final String KEY_AUTHOR = "author";
    public static final String KEY_POEM_LINE = "poemLine";

    public ParseUser getAuthor() { return getParseUser(KEY_AUTHOR); }

    public void setAuthor(ParseUser user) { put(KEY_AUTHOR, user); }

    public String getPoemLine() { return getString(KEY_POEM_LINE); }

    public void setPoemLine(String poemLine) { put(KEY_POEM_LINE, poemLine); }

}

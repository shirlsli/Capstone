package com.example.capstone.models;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.json.JSONArray;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@ParseClassName("Poem")
public class Poem extends ParseObject {

    public static final String KEY_AUTHORS = "authors";
    public static final String KEY_POEM_LINES = "poemLines";

    private List<ParseUser> authors;
    private List<Line> poemLines;

    public Poem() {
        authors = new ArrayList<>();
        poemLines = new ArrayList<>();
    }

    public List<ParseUser> getAuthors() {
//        for (int i = 0; i < jsonArray.length(); i++) {
//            authors.add(fromJson(jsonArray.getJSONObject(i)));
//        }
        return authors;
    }

    public void addAuthor(ParseUser user) {
        put(KEY_AUTHORS, user);
    }

    public List<Line> getPoemLines() {
        JSONArray pL = getJSONArray(KEY_POEM_LINES);
        // very inefficient when scaling
//        if (pL != null) {
//            for (int i = 0; i < pL.length(); i++) {
//                authors.set(i, pL.getLine(i));
//            }
//        }
        return getList(KEY_POEM_LINES);
    }

    // we want the user to have the ability to edit their poem line
    // need to save index of poem line in poem somewhere
    // know for sure that poem line should be the object saving its index
    // having an index would be more runtime efficient than iterating through the poem and finding the line with matching user
    // constant vs linear time (depending on scope of user's friends)
    // a poem line belongs to a poem when their createdAt and updatedAt dates/times match within 24 hours
    // that means from 12:00 am to 11:59 pm
    // skeleton: time zone PDT
    // for now, just add it to the end of the poem
    public void updatePoem(Line poemLine) {
        poemLines.add(poemLine);
        put(KEY_POEM_LINES, poemLine);
    }
}
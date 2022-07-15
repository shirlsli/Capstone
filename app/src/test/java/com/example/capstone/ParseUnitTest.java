package com.example.capstone;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import android.util.Log;
import android.view.View;

import com.example.capstone.models.Line;
import com.example.capstone.models.OpenAIThread;
import com.example.capstone.models.Poem;
import com.example.capstone.models.Post;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ParseUnitTest {

    @Test
    public void parseQueryLines() throws InterruptedException {
        ParseQuery<Line> poemLineQuery = ParseQuery.getQuery(Line.class);
        poemLineQuery.setLimit(5);
        poemLineQuery.addDescendingOrder("createdAt");
        poemLineQuery.include(Line.KEY_AUTHOR);
        poemLineQuery.include(Line.KEY_POEM_LINE);
        poemLineQuery.findInBackground(new FindCallback<Line>() {
            @Override
            public void done(List<Line> objects, ParseException e) {
                assertNotNull(objects);
                assertNull(e);
            }
        });
    }

    @Test
    public void parseQueryPoems() throws InterruptedException {
        ParseQuery<Poem> poemQuery = ParseQuery.getQuery(Poem.class);
        poemQuery.setLimit(5);
        poemQuery.addDescendingOrder("createdAt");
        poemQuery.include(Line.KEY_AUTHOR);
        poemQuery.include(Line.KEY_POEM_LINE);
        poemQuery.findInBackground(new FindCallback<Poem>() {
            @Override
            public void done(List<Poem> objects, ParseException e) {
                assertNotNull(objects);
                assertNull(e);
            }
        });
    }

    @Test
    public void parseQueryPosts() throws InterruptedException {
        ParseQuery<Post> postQuery = ParseQuery.getQuery(Post.class);
        postQuery.setLimit(5);
        postQuery.addDescendingOrder("createdAt");
        postQuery.include(Line.KEY_AUTHOR);
        postQuery.include(Line.KEY_POEM_LINE);
        postQuery.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> objects, ParseException e) {
                assertNotNull(objects);
                assertNull(e);
            }
        });
    }
}
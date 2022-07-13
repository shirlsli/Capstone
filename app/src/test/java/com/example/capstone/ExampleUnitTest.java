package com.example.capstone;

import org.junit.Test;

import static org.junit.Assert.*;

import com.example.capstone.models.OpenAIThread;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    /**
     * Tests OpenAI API call and on "test" prompt and checks if fetched value is null
     */
    public void openAI() throws InterruptedException {
        OpenAIThread openAIThread = new OpenAIThread("test");
        openAIThread.runCallback(new Runnable() {
            @Override
            public void run() {
                assertNotNull(openAIThread.getGeneratedLines());
            }
        });
    }
}
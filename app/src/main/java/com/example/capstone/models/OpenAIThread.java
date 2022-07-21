package com.example.capstone.models;

import android.util.Log;

import com.example.capstone.BuildConfig;
import com.theokanning.openai.OpenAiService;
import com.theokanning.openai.completion.CompletionChoice;
import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.engine.Engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class OpenAIThread {
    private String prompt;
    private String[] generatedLines;
    private String[] splitLines;
    private ArrayList<String> editedLines;

    public OpenAIThread(String prompt) {
        this.prompt = prompt;
        generatedLines = new String[1];
        editedLines = new ArrayList<>();
    }

    public ArrayList<String> getGeneratedLines() {
        return editedLines;
    }

    public void runCallback(Runnable callback)
    {
        OpenAiService service = new OpenAiService(BuildConfig.OPENAI_API_KEY);

        System.out.println("\nGetting da vinci engine...");
        Engine davinci = service.getEngine("text-davinci-002");
        System.out.println(davinci);

        System.out.println("\nCreating completion...");
        CompletionRequest completionRequest = CompletionRequest.builder()
                .prompt("write a poem with the word \"" + prompt + "\" in it")
                .temperature(0.7)
                .maxTokens(256)
                .topP(1.0)
                .frequencyPenalty(1.0)
                .presencePenalty(0.0)
                .build();
        // asynch call that gets treated as synchronous
        List<CompletionChoice> choices = service.createCompletion("text-davinci-002", completionRequest).getChoices();
        generatedLines[0] = choices.get(0).toString();
        splitLines = generatedLines[0].split("\n");
        splitLines[splitLines.length - 1] = splitLines[splitLines.length - 1].split(",")[0];
        editedLines.addAll(Arrays.asList(splitLines));
        editedLines.removeAll(Collections.singleton(""));
        for (int i = 0; i < editedLines.size(); i++) {
            Log.i("checking", editedLines.get(i));
        }
        // Run callback
        callback.run();
    }

}

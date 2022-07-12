package com.example.capstone.models;

import android.util.Log;

import com.example.capstone.BuildConfig;
import com.theokanning.openai.OpenAiService;
import com.theokanning.openai.completion.CompletionChoice;
import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.engine.Engine;

import java.util.ArrayList;
import java.util.List;

public class OpenAIThread extends Thread {
    // probably want to make a constructor holding the conditions needed for poem line generation
    private String prompt;
    private String[] generatedLines;
    private String[] splitLines;

    public OpenAIThread(String prompt) {
        this.prompt = prompt;
        generatedLines = new String[1];
    }

    public String[] getGeneratedLines() {
        return splitLines;
    }

    @Override
    public void run() {
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
        List<CompletionChoice> choices = service.createCompletion("text-davinci-002", completionRequest).getChoices();
        generatedLines[0] = choices.get(0).toString();
        splitLines = generatedLines[0].split("\n");
        splitLines[splitLines.length - 1] = splitLines[splitLines.length - 1].split(",")[0];
    }
}

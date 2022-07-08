package com.example.capstone.models;

import android.util.Log;

import com.theokanning.openai.OpenAiService;
import com.theokanning.openai.completion.CompletionChoice;
import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.engine.Engine;

import java.util.ArrayList;
import java.util.List;

public class OpenAIThread extends Thread {
    // probably want to make a constructor holding the conditions needed for poem line generation
    private String prompt;
    private ArrayList<String> generatedLines;

    public OpenAIThread(String prompt) {
        this.prompt = prompt;
        generatedLines = new ArrayList<>();
    }

    public ArrayList<String> getGeneratedLines() {
        return generatedLines;
    }

    @Override
    public void run() {
        // "${OPENAI_API_KEY}"
        // tested not using api key string as token and it got an error :(
        OpenAiService service = new OpenAiService("");

        System.out.println("\nGetting da vinci engine...");
        Engine davinci = service.getEngine("davinci");
        System.out.println(davinci);

        System.out.println("\nCreating completion...");
        CompletionRequest completionRequest = CompletionRequest.builder()
                .prompt("write a poem with the word \"" + prompt + "\" in it")
                .temperature(0.7)
                .maxTokens(128)
                .topP(1.0)
                .frequencyPenalty(0.0)
                .presencePenalty(0.0)
                .build();
        List<CompletionChoice> choices = service.createCompletion("davinci", completionRequest).getChoices();
        for (int i = 0; i < choices.size(); i++) {
            generatedLines.add(choices.get(i).toString());
            Log.i("openai_poem_lines_test", generatedLines.get(i));
        }
    }
}

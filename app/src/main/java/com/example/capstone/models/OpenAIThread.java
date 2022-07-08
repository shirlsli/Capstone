package com.example.capstone.models;

import com.theokanning.openai.OpenAiService;
import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.engine.Engine;

public class OpenAIThread extends Thread {
    // probably want to make a constructor holding the conditions needed for poem line generation
    private String prompt;

    public OpenAIThread(String prompt) {
        this.prompt = prompt;
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
        service.createCompletion("davinci", completionRequest).getChoices().forEach(System.out::println);
    }
}

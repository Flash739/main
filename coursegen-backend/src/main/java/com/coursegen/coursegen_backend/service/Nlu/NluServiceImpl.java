package com.coursegen.coursegen_backend.service.Nlu;

import com.coursegen.coursegen_backend.client.OpenAIClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NluServiceImpl implements NluService{

    @Autowired
    private OpenAIClient openAIClient;

    @Override
    public String extractIntent(String prompt) {
        String taskPrompt = "Extract the intent from this input: \"" + prompt + "\". Intent should be a single word like 'generate-course'.";
        return openAIClient.extractFromLLM(taskPrompt).block();
    }

    @Override
    public String extractTopic(String prompt) {
        String topicPrompt = "Extract the topic from this input: \"" + prompt + "\". Topic should be a specific subject like 'React', 'Docker', 'Kubernetes' and do not include any short form in returned topic just Full form of topic.";
        return openAIClient.extractFromLLM(topicPrompt).block();
    }

    @Override
    public String formatPrompt(String intent, String topic, String userPrompt) {
        return "You are an expert at creating detailed course outlines. Generate a full course curriculum on \"" + topic + "\".";
    }
}

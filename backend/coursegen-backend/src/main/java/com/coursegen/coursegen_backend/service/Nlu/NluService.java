package com.coursegen.coursegen_backend.service.Nlu;

public interface NluService {
    String extractIntent(String prompt);
    String extractTopic(String prompt);
    String formatPrompt(String intent,String topic,String prompt);
}

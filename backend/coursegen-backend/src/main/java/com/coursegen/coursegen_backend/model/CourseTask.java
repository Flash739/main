package com.coursegen.coursegen_backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "course_tasks")
public class CourseTask {

    @Id
    private String id;

    private String userPrompt;        // Raw user input
    private String intent;            // e.g. "generate-course"
    private String topic;             // e.g. "MongoDB"
    private String formattedPrompt;   // LLM-ready prompt
    private String status;            // PENDING, COMPLETED, FAILED
    private String result;            // JSON stringified course result

    // --- Getters and Setters ---
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserPrompt() {
        return userPrompt;
    }

    public void setUserPrompt(String userPrompt) {
        this.userPrompt = userPrompt;
    }

    public String getIntent() {
        return intent;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getFormattedPrompt() {
        return formattedPrompt;
    }

    public void setFormattedPrompt(String formattedPrompt) {
        this.formattedPrompt = formattedPrompt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}

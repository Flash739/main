package com.coursegen.coursegen_backend.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "courses")
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CourseOutline {

    @Id
    private String id;         // taskId from frontend
    private String topic;
    private String status;     // OUTLINE_READY, DETAILS_READY, COMPLETED

    private List<Section> sections;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Section {
        private String title;
        private String description;
        private boolean lazyLoaded;

        private List<Subsection> subsections;
        private List<Quiz> quizzes;
        private List<Video> videos;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Subsection {
        private String title;
        private String description;
        private List<Video> videos;
        private List<Quiz> quiz;
        private boolean lazyLoaded;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Quiz {
        private String question;
        private List<String> options;
        private int correctOptionIndex;
        private boolean lazyLoaded;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Video {
        private String title;
        private String url;
        private String description;
    }
}
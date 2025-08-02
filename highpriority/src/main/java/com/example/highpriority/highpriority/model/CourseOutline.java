package com.example.highpriority.highpriority.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "courses")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseOutline {

    @Id
    private String id;         // taskId from frontend
    private String topic;
    private String status;     // OUTLINE_READY, DETAILS_READY, COMPLETED

    private List<Section> sections;

    public Section findSection(String title) {
        if (sections == null) return null;
        for (Section section : sections) {
            if (section.getTitle().equalsIgnoreCase(title)) {
                return section;
            }
        }
        return null;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Section {
        private String title;
        private String description;
        private boolean lazyLoaded;

        private List<Subsection> subsections;
        private List<Quiz> quizzes;
        private List<Video> videos;

        public Subsection findSubsection(String title) {
            if (subsections == null) return null;
            for (Subsection subsection : subsections) {
                if (subsection.getTitle().equalsIgnoreCase(title)) {
                    return subsection;
                }
            }
            return null;
        }


    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
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
    public static class Quiz {
        private String question;
        private List<String> options;
        private int correctOptionIndex;
        private boolean lazyLoaded;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Video {
        private String title;
        private String url;
        private String description;
    }
}
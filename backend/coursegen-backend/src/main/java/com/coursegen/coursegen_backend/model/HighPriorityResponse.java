package com.coursegen.coursegen_backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HighPriorityResponse {
        private CourseOutline.Section section;
        private CourseOutline.Subsection subsection;

        // constructors, getters, setters
    }

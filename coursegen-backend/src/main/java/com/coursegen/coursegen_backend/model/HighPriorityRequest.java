package com.coursegen.coursegen_backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HighPriorityRequest {
    private String taskId;
    private String sectionTitle;
    private String subsectionTitle; // Nullable
}

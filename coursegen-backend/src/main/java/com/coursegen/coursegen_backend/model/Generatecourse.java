package com.coursegen.coursegen_backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Generatecourse {
    private String prompt;
    private String userid;
}

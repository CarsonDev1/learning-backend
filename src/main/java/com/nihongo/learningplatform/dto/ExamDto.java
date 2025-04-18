package com.nihongo.learningplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamDto {
    private Long id;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @Positive(message = "Time limit must be positive")
    private int timeLimit;

    @Positive(message = "Passing score must be positive")
    private int passingScore;

    private boolean isAiGenerated;

    @NotNull(message = "Course ID is required")
    private Long courseId;

    private List<QuestionDto> questions;
}
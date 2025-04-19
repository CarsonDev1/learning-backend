package com.nihongo.learningplatform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MockExamDto {
    private Long id;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @Positive(message = "Time limit must be positive")
    private int timeLimit;

    @Positive(message = "Passing score must be positive")
    private int passingScore;

    private boolean isAiGenerated;

    @NotBlank(message = "Level is required")
    private String level;

    private Long courseId;

    private String courseName;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private List<QuestionDto> questions;
}

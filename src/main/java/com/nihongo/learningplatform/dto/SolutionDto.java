package com.nihongo.learningplatform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SolutionDto {
    private Long id;

    @NotBlank(message = "Content is required")
    private String content;

    private String explanation;
    private Long questionId;
    private Long speechExerciseId;
    private boolean visible;
    private boolean availableAfterSubmission;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
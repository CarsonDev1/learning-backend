package com.nihongo.learningplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseDto {
    private Long id;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;
    private boolean isAiGenerated;

    @NotNull(message = "Course ID is required")
    private Long courseId;

    private List<QuestionDto> questions;
}
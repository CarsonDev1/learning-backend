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
public class ModuleDto {
    private Long id;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    private int orderIndex;

    @Positive(message = "Duration must be positive")
    private int durationMinutes;

    @NotNull(message = "Course ID is required")
    private Long courseId;

    private String courseName;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private List<LessonDto> lessons;
}
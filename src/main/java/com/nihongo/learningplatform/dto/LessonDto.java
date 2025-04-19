package com.nihongo.learningplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LessonDto {
    private Long id;

    @NotBlank(message = "Title is required")
    private String title;

    private Long courseId;

    @NotBlank(message = "Content is required")
    private String content;

    private String videoUrl;
    private String videoPublicId;
    private int orderIndex;

    @NotNull(message = "Module ID is required")
    private Long moduleId;
}
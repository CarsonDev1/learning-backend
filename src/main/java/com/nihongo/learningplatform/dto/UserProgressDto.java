package com.nihongo.learningplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProgressDto {
    private Long id;

    @NotNull(message = "User ID is required")
    private Long userId;

    private Long lessonId;
    private Long exerciseId;
    private Long examId;
    private Long speechExerciseId;

    private boolean completed;
    private Integer score;
    private String userAudioUrl;
    private Float pronunciationScore;
}

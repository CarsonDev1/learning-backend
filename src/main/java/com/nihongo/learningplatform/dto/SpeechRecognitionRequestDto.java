package com.nihongo.learningplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpeechRecognitionRequestDto {
    @NotBlank(message = "Audio data is required")
    private String audioData; // Base64 encoded audio

    @NotNull(message = "Speech exercise ID is required")
    private Long speechExerciseId;
}
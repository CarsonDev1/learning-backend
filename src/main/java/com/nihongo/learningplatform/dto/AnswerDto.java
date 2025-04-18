package com.nihongo.learningplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnswerDto {
    private Long id;

    @NotBlank(message = "Content is required")
    private String content;

    private boolean isCorrect;

    @NotNull(message = "Question ID is required")
    private Long questionId;
}
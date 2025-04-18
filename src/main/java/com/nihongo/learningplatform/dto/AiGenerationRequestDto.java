package com.nihongo.learningplatform.dto;

import com.nihongo.learningplatform.entity.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiGenerationRequestDto {
    @NotBlank(message = "Topic is required")
    private String topic;

    @NotNull(message = "Difficulty level is required")
    private String difficultyLevel; // N5, N4, N3, N2, N1

    @Min(value = 1, message = "Number of questions must be at least 1")
    private int numberOfQuestions;

    private List<QuestionType> questionTypes;

    @NotNull(message = "Course ID is required")
    private Long courseId;

    private boolean isExam;
}
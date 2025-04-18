package com.nihongo.learningplatform.dto;

import com.nihongo.learningplatform.entity.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDto {
    private Long id;

    @NotBlank(message = "Content is required")
    private String content;

    @NotNull(message = "Question type is required")
    private QuestionType type;

    private String audioUrl;
    private Long exerciseId;
    private Long examId;

    private List<AnswerDto> answers;
}
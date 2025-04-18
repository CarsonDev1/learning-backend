package com.nihongo.learningplatform.service;

import com.nihongo.learningplatform.dto.QuestionDto;
import com.nihongo.learningplatform.entity.Question;
import com.nihongo.learningplatform.entity.QuestionType;

import java.util.List;

public interface QuestionService {
    QuestionDto createQuestion(QuestionDto questionDto);
    Question createQuestionForExercise(QuestionDto questionDto);
    Question createQuestionForExam(QuestionDto questionDto);
    QuestionDto getQuestionById(Long id);
    List<QuestionDto> getQuestionsByExercise(Long exerciseId);
    List<QuestionDto> getQuestionsByExam(Long examId);
    List<QuestionDto> getQuestionsByType(QuestionType type);
    QuestionDto updateQuestion(Long id, QuestionDto questionDto);
    void deleteQuestion(Long id);
    Question getQuestionEntityById(Long id);
}
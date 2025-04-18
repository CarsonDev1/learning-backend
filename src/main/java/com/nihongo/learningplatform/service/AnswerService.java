package com.nihongo.learningplatform.service;

import com.nihongo.learningplatform.dto.AnswerDto;
import com.nihongo.learningplatform.entity.Answer;

import java.util.List;

public interface AnswerService {
    AnswerDto createAnswerDto(AnswerDto answerDto);
    Answer createAnswer(AnswerDto answerDto);
    AnswerDto getAnswerById(Long id);
    List<AnswerDto> getAnswersByQuestion(Long questionId);
    List<AnswerDto> getCorrectAnswersByQuestion(Long questionId);
    AnswerDto updateAnswer(Long id, AnswerDto answerDto);
    void deleteAnswer(Long id);
    Answer getAnswerEntityById(Long id);
}
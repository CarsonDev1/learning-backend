package com.nihongo.learningplatform.service.impl;

import com.nihongo.learningplatform.dto.AnswerDto;
import com.nihongo.learningplatform.entity.Answer;
import com.nihongo.learningplatform.entity.Question;
import com.nihongo.learningplatform.exception.ResourceNotFoundException;
import com.nihongo.learningplatform.repository.AnswerRepository;
import com.nihongo.learningplatform.service.AnswerService;
import com.nihongo.learningplatform.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnswerServiceImpl implements AnswerService {

    private final AnswerRepository answerRepository;
    private final QuestionService questionService;

    @Autowired
    public AnswerServiceImpl(AnswerRepository answerRepository,
                             @Lazy QuestionService questionService) {
        this.answerRepository = answerRepository;
        this.questionService = questionService;
    }

    @Override
    @Transactional
    public AnswerDto createAnswerDto(AnswerDto answerDto) {
        Answer answer = createAnswer(answerDto);
        return mapToDto(answer);
    }

    @Override
    @Transactional
    public Answer createAnswer(AnswerDto answerDto) {
        Question question = questionService.getQuestionEntityById(answerDto.getQuestionId());

        Answer answer = new Answer();
        answer.setContent(answerDto.getContent());
        answer.setCorrect(answerDto.isCorrect());
        answer.setQuestion(question);

        return answerRepository.save(answer);
    }

    @Override
    public AnswerDto getAnswerById(Long id) {
        Answer answer = answerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Answer not found with id: " + id));
        return mapToDto(answer);
    }

    @Override
    public List<AnswerDto> getAnswersByQuestion(Long questionId) {
        Question question = questionService.getQuestionEntityById(questionId);
        List<Answer> answers = answerRepository.findByQuestion(question);
        return answers.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<AnswerDto> getCorrectAnswersByQuestion(Long questionId) {
        Question question = questionService.getQuestionEntityById(questionId);
        List<Answer> answers = answerRepository.findByQuestionAndIsCorrect(question, true);
        return answers.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AnswerDto updateAnswer(Long id, AnswerDto answerDto) {
        Answer answer = answerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Answer not found with id: " + id));

        answer.setContent(answerDto.getContent());
        answer.setCorrect(answerDto.isCorrect());

        Answer updatedAnswer = answerRepository.save(answer);
        return mapToDto(updatedAnswer);
    }

    @Override
    @Transactional
    public void deleteAnswer(Long id) {
        if (!answerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Answer not found with id: " + id);
        }
        answerRepository.deleteById(id);
    }

    @Override
    public Answer getAnswerEntityById(Long id) {
        return answerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Answer not found with id: " + id));
    }

    // Helper method to map Answer entity to AnswerDto
    private AnswerDto mapToDto(Answer answer) {
        AnswerDto answerDto = new AnswerDto();
        answerDto.setId(answer.getId());
        answerDto.setContent(answer.getContent());
        answerDto.setCorrect(answer.isCorrect());
        answerDto.setQuestionId(answer.getQuestion().getId());
        return answerDto;
    }
}
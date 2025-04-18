package com.nihongo.learningplatform.service.impl;

import com.nihongo.learningplatform.dto.AnswerDto;
import com.nihongo.learningplatform.dto.QuestionDto;
import com.nihongo.learningplatform.entity.*;
import com.nihongo.learningplatform.exception.ResourceNotFoundException;
import com.nihongo.learningplatform.repository.QuestionRepository;
import com.nihongo.learningplatform.service.AnswerService;
import com.nihongo.learningplatform.service.ExamService;
import com.nihongo.learningplatform.service.ExerciseService;
import com.nihongo.learningplatform.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuestionServiceImpl implements QuestionService {

    private final QuestionRepository questionRepository;
    private final ExerciseService exerciseService;
    private final ExamService examService;
    private final AnswerService answerService;

    @Autowired
    public QuestionServiceImpl(QuestionRepository questionRepository,
                               @Lazy ExerciseService exerciseService,
                               @Lazy ExamService examService,
                               @Lazy AnswerService answerService) {
        this.questionRepository = questionRepository;
        this.exerciseService = exerciseService;
        this.examService = examService;
        this.answerService = answerService;
    }

    @Override
    @Transactional
    public QuestionDto createQuestion(QuestionDto questionDto) {
        Question question = new Question();
        question.setContent(questionDto.getContent());
        question.setType(questionDto.getType());
        question.setAudioUrl(questionDto.getAudioUrl());

        // Link to exercise or exam
        if (questionDto.getExerciseId() != null) {
            Exercise exercise = exerciseService.getExerciseEntityById(questionDto.getExerciseId());
            question.setExercise(exercise);
        } else if (questionDto.getExamId() != null) {
            Exam exam = examService.getExamEntityById(questionDto.getExamId());
            question.setExam(exam);
        }

        Question savedQuestion = questionRepository.save(question);

        // Create answers for the question if any
        if (questionDto.getAnswers() != null && !questionDto.getAnswers().isEmpty()) {
            List<Answer> answers = new ArrayList<>();
            for (AnswerDto answerDto : questionDto.getAnswers()) {
                answerDto.setQuestionId(savedQuestion.getId());
                Answer answer = answerService.createAnswer(answerDto);
                answers.add(answer);
            }
        }

        return mapToDto(savedQuestion);
    }

    @Override
    @Transactional
    public Question createQuestionForExercise(QuestionDto questionDto) {
        Question question = new Question();
        question.setContent(questionDto.getContent());
        question.setType(questionDto.getType());
        question.setAudioUrl(questionDto.getAudioUrl());

        Exercise exercise = exerciseService.getExerciseEntityById(questionDto.getExerciseId());
        question.setExercise(exercise);

        Question savedQuestion = questionRepository.save(question);

        // Create answers for the question if any
        if (questionDto.getAnswers() != null && !questionDto.getAnswers().isEmpty()) {
            for (AnswerDto answerDto : questionDto.getAnswers()) {
                answerDto.setQuestionId(savedQuestion.getId());
                answerService.createAnswer(answerDto);
            }
        }

        return savedQuestion;
    }

    @Override
    @Transactional
    public Question createQuestionForExam(QuestionDto questionDto) {
        Question question = new Question();
        question.setContent(questionDto.getContent());
        question.setType(questionDto.getType());
        question.setAudioUrl(questionDto.getAudioUrl());

        Exam exam = examService.getExamEntityById(questionDto.getExamId());
        question.setExam(exam);

        Question savedQuestion = questionRepository.save(question);

        // Create answers for the question if any
        if (questionDto.getAnswers() != null && !questionDto.getAnswers().isEmpty()) {
            for (AnswerDto answerDto : questionDto.getAnswers()) {
                answerDto.setQuestionId(savedQuestion.getId());
                answerService.createAnswer(answerDto);
            }
        }

        return savedQuestion;
    }

    @Override
    public QuestionDto getQuestionById(Long id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + id));

        QuestionDto questionDto = mapToDto(question);

        // Load answers for this question
        List<AnswerDto> answers = answerService.getAnswersByQuestion(id);
        questionDto.setAnswers(answers);

        return questionDto;
    }

    @Override
    public List<QuestionDto> getQuestionsByExercise(Long exerciseId) {
        Exercise exercise = exerciseService.getExerciseEntityById(exerciseId);
        List<Question> questions = questionRepository.findByExercise(exercise);

        return questions.stream()
                .map(question -> {
                    QuestionDto dto = mapToDto(question);
                    // Load answers for each question
                    List<AnswerDto> answers = answerService.getAnswersByQuestion(question.getId());
                    dto.setAnswers(answers);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<QuestionDto> getQuestionsByExam(Long examId) {
        Exam exam = examService.getExamEntityById(examId);
        List<Question> questions = questionRepository.findByExam(exam);

        return questions.stream()
                .map(question -> {
                    QuestionDto dto = mapToDto(question);
                    // Load answers for each question
                    List<AnswerDto> answers = answerService.getAnswersByQuestion(question.getId());
                    dto.setAnswers(answers);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<QuestionDto> getQuestionsByType(QuestionType type) {
        List<Question> questions = questionRepository.findByType(type);

        return questions.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public QuestionDto updateQuestion(Long id, QuestionDto questionDto) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + id));

        question.setContent(questionDto.getContent());
        question.setType(questionDto.getType());
        question.setAudioUrl(questionDto.getAudioUrl());

        Question updatedQuestion = questionRepository.save(question);

        // Update answers if provided
        if (questionDto.getAnswers() != null && !questionDto.getAnswers().isEmpty()) {
            for (AnswerDto answerDto : questionDto.getAnswers()) {
                if (answerDto.getId() != null) {
                    answerService.updateAnswer(answerDto.getId(), answerDto);
                } else {
                    answerDto.setQuestionId(id);
                    answerService.createAnswer(answerDto);
                }
            }
        }

        return getQuestionById(id); // Return with updated answers
    }

    @Override
    @Transactional
    public void deleteQuestion(Long id) {
        if (!questionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Question not found with id: " + id);
        }
        questionRepository.deleteById(id);
    }

    @Override
    public Question getQuestionEntityById(Long id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + id));
    }

    // Helper method to map Question entity to QuestionDto
    private QuestionDto mapToDto(Question question) {
        QuestionDto questionDto = new QuestionDto();
        questionDto.setId(question.getId());
        questionDto.setContent(question.getContent());
        questionDto.setType(question.getType());
        questionDto.setAudioUrl(question.getAudioUrl());

        if (question.getExercise() != null) {
            questionDto.setExerciseId(question.getExercise().getId());
        }

        if (question.getExam() != null) {
            questionDto.setExamId(question.getExam().getId());
        }

        return questionDto;
    }
}
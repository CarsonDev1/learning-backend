package com.nihongo.learningplatform.service.impl;

import com.nihongo.learningplatform.dto.SolutionDto;
import com.nihongo.learningplatform.entity.*;
import com.nihongo.learningplatform.exception.ResourceNotFoundException;
import com.nihongo.learningplatform.repository.SolutionRepository;
import com.nihongo.learningplatform.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SolutionServiceImpl implements SolutionService {

    private final SolutionRepository solutionRepository;
    private final QuestionService questionService;
    private final SpeechExerciseService speechExerciseService;
    private final UserProgressService userProgressService;
    private final EnrollmentService enrollmentService;

    @Autowired
    public SolutionServiceImpl(SolutionRepository solutionRepository,
                               @Lazy QuestionService questionService,
                               @Lazy SpeechExerciseService speechExerciseService,
                               @Lazy UserProgressService userProgressService,
                               @Lazy EnrollmentService enrollmentService) {
        this.solutionRepository = solutionRepository;
        this.questionService = questionService;
        this.speechExerciseService = speechExerciseService;
        this.userProgressService = userProgressService;
        this.enrollmentService = enrollmentService;
    }

    @Override
    @Transactional
    public SolutionDto createSolution(SolutionDto solutionDto) {
        Solution solution = new Solution();
        solution.setContent(solutionDto.getContent());
        solution.setExplanation(solutionDto.getExplanation());
        solution.setVisible(solutionDto.isVisible());
        solution.setAvailableAfterSubmission(solutionDto.isAvailableAfterSubmission());

        // Link to question or speech exercise
        if (solutionDto.getQuestionId() != null) {
            Question question = questionService.getQuestionEntityById(solutionDto.getQuestionId());
            solution.setQuestion(question);
        } else if (solutionDto.getSpeechExerciseId() != null) {
            SpeechExercise speechExercise = speechExerciseService.getSpeechExerciseEntityById(solutionDto.getSpeechExerciseId());
            solution.setSpeechExercise(speechExercise);
        } else {
            throw new IllegalArgumentException("Solution must be linked to a question or speech exercise");
        }

        Solution savedSolution = solutionRepository.save(solution);
        return mapToDto(savedSolution);
    }

    @Override
    public SolutionDto getSolutionById(Long id) {
        Solution solution = solutionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solution not found with id: " + id));
        return mapToDto(solution);
    }

    @Override
    public SolutionDto getSolutionByQuestionId(Long questionId) {
        Question question = questionService.getQuestionEntityById(questionId);
        Solution solution = solutionRepository.findByQuestion(question)
                .orElseThrow(() -> new ResourceNotFoundException("Solution not found for question id: " + questionId));
        return mapToDto(solution);
    }

    @Override
    public SolutionDto getSolutionBySpeechExerciseId(Long speechExerciseId) {
        SpeechExercise speechExercise = speechExerciseService.getSpeechExerciseEntityById(speechExerciseId);
        Solution solution = solutionRepository.findBySpeechExercise(speechExercise)
                .orElseThrow(() -> new ResourceNotFoundException("Solution not found for speech exercise id: " + speechExerciseId));
        return mapToDto(solution);
    }

    @Override
    public List<SolutionDto> getSolutionsByExerciseId(Long exerciseId) {
        List<Solution> solutions = solutionRepository.findByExerciseId(exerciseId);
        return solutions.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SolutionDto> getSolutionsByExamId(Long examId) {
        List<Solution> solutions = solutionRepository.findByExamId(examId);
        return solutions.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SolutionDto> getVisibleSolutions() {
        List<Solution> solutions = solutionRepository.findByVisible(true);
        return solutions.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SolutionDto updateSolution(Long id, SolutionDto solutionDto) {
        Solution solution = solutionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solution not found with id: " + id));

        solution.setContent(solutionDto.getContent());
        solution.setExplanation(solutionDto.getExplanation());
        solution.setVisible(solutionDto.isVisible());
        solution.setAvailableAfterSubmission(solutionDto.isAvailableAfterSubmission());

        // Update links if needed
        if (solutionDto.getQuestionId() != null &&
                (solution.getQuestion() == null || !solution.getQuestion().getId().equals(solutionDto.getQuestionId()))) {
            Question question = questionService.getQuestionEntityById(solutionDto.getQuestionId());
            solution.setQuestion(question);
            solution.setSpeechExercise(null);
        } else if (solutionDto.getSpeechExerciseId() != null &&
                (solution.getSpeechExercise() == null || !solution.getSpeechExercise().getId().equals(solutionDto.getSpeechExerciseId()))) {
            SpeechExercise speechExercise = speechExerciseService.getSpeechExerciseEntityById(solutionDto.getSpeechExerciseId());
            solution.setSpeechExercise(speechExercise);
            solution.setQuestion(null);
        }

        Solution updatedSolution = solutionRepository.save(solution);
        return mapToDto(updatedSolution);
    }

    @Override
    @Transactional
    public SolutionDto showSolution(Long id) {
        Solution solution = solutionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solution not found with id: " + id));

        solution.setVisible(true);
        Solution updatedSolution = solutionRepository.save(solution);
        return mapToDto(updatedSolution);
    }

    @Override
    @Transactional
    public SolutionDto hideSolution(Long id) {
        Solution solution = solutionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solution not found with id: " + id));

        solution.setVisible(false);
        Solution updatedSolution = solutionRepository.save(solution);
        return mapToDto(updatedSolution);
    }

    @Override
    @Transactional
    public SolutionDto setAvailableAfterSubmission(Long id, boolean available) {
        Solution solution = solutionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solution not found with id: " + id));

        solution.setAvailableAfterSubmission(available);
        Solution updatedSolution = solutionRepository.save(solution);
        return mapToDto(updatedSolution);
    }

    @Override
    @Transactional
    public void deleteSolution(Long id) {
        if (!solutionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Solution not found with id: " + id);
        }
        solutionRepository.deleteById(id);
    }

    @Override
    public Solution getSolutionEntityById(Long id) {
        return solutionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solution not found with id: " + id));
    }

    @Override
    public boolean isUserAllowedToViewSolution(Long solutionId, Long userId, Long courseId) {
        Solution solution = getSolutionEntityById(solutionId);

        // If solution is not visible, user can't see it
        if (!solution.isVisible()) {
            return false;
        }

        // Check if user is enrolled in the course
        boolean isEnrolled = enrollmentService.isStudentEnrolledInCourse(userId, courseId);
        if (!isEnrolled) {
            return false;
        }

        // If solution is available to all enrolled users, allow access
        if (!solution.isAvailableAfterSubmission()) {
            return true;
        }

        // If solution requires submission first, check if user has submitted
        if (solution.getQuestion() != null) {
            Question question = solution.getQuestion();

            // Check if user has attempted the exercise or exam
            if (question.getExercise() != null) {
                try {
                    userProgressService.getUserProgressByUserAndExercise(userId, question.getExercise().getId());
                    return true;
                } catch (ResourceNotFoundException e) {
                    return false;
                }
            } else if (question.getExam() != null) {
                try {
                    userProgressService.getUserProgressByUserAndExam(userId, question.getExam().getId());
                    return true;
                } catch (ResourceNotFoundException e) {
                    return false;
                }
            }
        } else if (solution.getSpeechExercise() != null) {
            // Check if user has attempted the speech exercise
            try {
                userProgressService.getUserProgressByUserAndSpeechExercise(userId, solution.getSpeechExercise().getId());
                return true;
            } catch (ResourceNotFoundException e) {
                return false;
            }
        }

        return false;
    }

    // Helper method to map Solution entity to SolutionDto
    private SolutionDto mapToDto(Solution solution) {
        SolutionDto solutionDto = new SolutionDto();
        solutionDto.setId(solution.getId());
        solutionDto.setContent(solution.getContent());
        solutionDto.setExplanation(solution.getExplanation());
        solutionDto.setVisible(solution.isVisible());
        solutionDto.setAvailableAfterSubmission(solution.isAvailableAfterSubmission());
        solutionDto.setCreatedAt(solution.getCreatedAt());
        solutionDto.setUpdatedAt(solution.getUpdatedAt());

        if (solution.getQuestion() != null) {
            solutionDto.setQuestionId(solution.getQuestion().getId());
        }

        if (solution.getSpeechExercise() != null) {
            solutionDto.setSpeechExerciseId(solution.getSpeechExercise().getId());
        }

        return solutionDto;
    }
}
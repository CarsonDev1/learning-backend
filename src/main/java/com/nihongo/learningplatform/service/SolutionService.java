package com.nihongo.learningplatform.service;

import com.nihongo.learningplatform.dto.SolutionDto;
import com.nihongo.learningplatform.entity.Solution;

import java.util.List;

public interface SolutionService {
    SolutionDto createSolution(SolutionDto solutionDto);
    SolutionDto getSolutionById(Long id);
    SolutionDto getSolutionByQuestionId(Long questionId);
    SolutionDto getSolutionBySpeechExerciseId(Long speechExerciseId);
    List<SolutionDto> getSolutionsByExerciseId(Long exerciseId);
    List<SolutionDto> getSolutionsByExamId(Long examId);
    List<SolutionDto> getVisibleSolutions();
    SolutionDto updateSolution(Long id, SolutionDto solutionDto);
    SolutionDto showSolution(Long id);
    SolutionDto hideSolution(Long id);
    SolutionDto setAvailableAfterSubmission(Long id, boolean available);
    void deleteSolution(Long id);
    Solution getSolutionEntityById(Long id);
    boolean isUserAllowedToViewSolution(Long solutionId, Long userId, Long courseId);
}
package com.nihongo.learningplatform.service;

import com.nihongo.learningplatform.dto.ExerciseDto;
import com.nihongo.learningplatform.entity.Exercise;

import java.util.List;

public interface ExerciseService {
    ExerciseDto createExercise(ExerciseDto exerciseDto);
    ExerciseDto getExerciseById(Long id);
    List<ExerciseDto> getExercisesByCourse(Long courseId);
    List<ExerciseDto> getAiGeneratedExercises();
    ExerciseDto updateExercise(Long id, ExerciseDto exerciseDto);
    void deleteExercise(Long id);
    Exercise getExerciseEntityById(Long id);
}
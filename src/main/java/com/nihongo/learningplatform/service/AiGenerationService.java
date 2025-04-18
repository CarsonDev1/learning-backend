package com.nihongo.learningplatform.service;

import com.nihongo.learningplatform.dto.AiGenerationRequestDto;
import com.nihongo.learningplatform.dto.ExamDto;
import com.nihongo.learningplatform.dto.ExerciseDto;

public interface AiGenerationService {
    ExerciseDto generateExercise(AiGenerationRequestDto requestDto);
    ExamDto generateExam(AiGenerationRequestDto requestDto);
}
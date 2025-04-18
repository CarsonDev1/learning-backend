package com.nihongo.learningplatform.service;

import com.nihongo.learningplatform.dto.SpeechExerciseDto;
import com.nihongo.learningplatform.entity.SpeechExercise;

import java.util.List;

public interface SpeechExerciseService {
    SpeechExerciseDto createSpeechExercise(SpeechExerciseDto speechExerciseDto);
    SpeechExerciseDto getSpeechExerciseById(Long id);
    List<SpeechExerciseDto> getSpeechExercisesByLesson(Long lessonId);
    SpeechExerciseDto updateSpeechExercise(Long id, SpeechExerciseDto speechExerciseDto);
    void deleteSpeechExercise(Long id);
    SpeechExercise getSpeechExerciseEntityById(Long id);
}
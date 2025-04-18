package com.nihongo.learningplatform.service;

import com.nihongo.learningplatform.dto.UserProgressDto;
import com.nihongo.learningplatform.entity.UserProgress;

import java.util.List;

public interface UserProgressService {
    UserProgressDto createOrUpdateLessonProgress(Long userId, Long lessonId, boolean completed);
    UserProgressDto createOrUpdateExerciseProgress(Long userId, Long exerciseId, Integer score);
    UserProgressDto createOrUpdateExamProgress(Long userId, Long examId, Integer score);
    UserProgressDto createOrUpdateSpeechExerciseProgress(Long userId, Long speechExerciseId, String userAudioUrl, Float pronunciationScore);
    UserProgressDto getUserProgressById(Long id);
    List<UserProgressDto> getUserProgressByUser(Long userId);
    UserProgressDto getUserProgressByUserAndLesson(Long userId, Long lessonId);
    UserProgressDto getUserProgressByUserAndExercise(Long userId, Long exerciseId);
    UserProgressDto getUserProgressByUserAndExam(Long userId, Long examId);
    UserProgressDto getUserProgressByUserAndSpeechExercise(Long userId, Long speechExerciseId);
    List<UserProgressDto> getCompletedProgressByUser(Long userId);
    void deleteUserProgress(Long id);
}

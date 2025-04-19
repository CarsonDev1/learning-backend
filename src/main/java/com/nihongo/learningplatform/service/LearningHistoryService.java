package com.nihongo.learningplatform.service;

import com.nihongo.learningplatform.dto.LearningHistoryDto;
import com.nihongo.learningplatform.dto.PageResponseDto;
import com.nihongo.learningplatform.entity.ActivityType;
import com.nihongo.learningplatform.entity.LearningHistory;

import java.time.LocalDateTime;
import java.util.List;

public interface LearningHistoryService {
    LearningHistoryDto createLearningHistory(LearningHistoryDto learningHistoryDto);
    LearningHistoryDto getLearningHistoryById(Long id);
    List<LearningHistoryDto> getLearningHistoriesByUser(Long userId);
    PageResponseDto<LearningHistoryDto> getLearningHistoriesByUserPaginated(Long userId, int pageNo, int pageSize);
    List<LearningHistoryDto> getLearningHistoriesByUserAndActivityType(Long userId, ActivityType activityType);
    List<LearningHistoryDto> getLearningHistoriesByUserAndCourse(Long userId, Long courseId);
    List<LearningHistoryDto> getLearningHistoriesByUserAndDateRange(Long userId, LocalDateTime start, LocalDateTime end);
    void deleteOldLearningHistories(LocalDateTime before);

    // Helper methods to record specific activities
    void recordCourseEnrollment(Long userId, Long courseId);
    void recordLessonCompleted(Long userId, Long lessonId);
    void recordExerciseCompleted(Long userId, Long exerciseId, Integer score);
    void recordExamCompleted(Long userId, Long examId, Integer score, Boolean passed);
    void recordMockExamCompleted(Long userId, Long mockExamId, Integer score, Boolean passed);
    void recordCertificateEarned(Long userId, Long courseId, Long certificateId);
    void recordLogin(Long userId);
    void recordPurchase(Long userId, Long paymentId, String details);
    void recordCommentPosted(Long userId, Long lessonId, Long commentId);
    void recordReviewPosted(Long userId, Long courseId, Long reviewId);
}

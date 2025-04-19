package com.nihongo.learningplatform.repository;

import com.nihongo.learningplatform.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LearningHistoryRepository extends JpaRepository<LearningHistory, Long> {
    List<LearningHistory> findByUser(User user);
    Page<LearningHistory> findByUser(User user, Pageable pageable);
    List<LearningHistory> findByUserAndActivityType(User user, ActivityType activityType);
    List<LearningHistory> findByUserAndCourse(User user, Course course);
    List<LearningHistory> findByUserAndTimestampBetween(User user, LocalDateTime start, LocalDateTime end);
}
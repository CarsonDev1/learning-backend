package com.nihongo.learningplatform.repository;

import com.nihongo.learningplatform.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserProgressRepository extends JpaRepository<UserProgress, Long> {
    List<UserProgress> findByUser(User user);

    Optional<UserProgress> findByUserAndLesson(User user, Lesson lesson);
    Optional<UserProgress> findByUserAndExercise(User user, Exercise exercise);
    Optional<UserProgress> findByUserAndExam(User user, Exam exam);
    Optional<UserProgress> findByUserAndSpeechExercise(User user, SpeechExercise speechExercise);

    List<UserProgress> findByUserAndCompleted(User user, boolean completed);
}
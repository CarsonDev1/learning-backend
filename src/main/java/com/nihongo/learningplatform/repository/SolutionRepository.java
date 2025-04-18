package com.nihongo.learningplatform.repository;

import com.nihongo.learningplatform.entity.Question;
import com.nihongo.learningplatform.entity.Solution;
import com.nihongo.learningplatform.entity.SpeechExercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SolutionRepository extends JpaRepository<Solution, Long> {
    Optional<Solution> findByQuestion(Question question);

    Optional<Solution> findBySpeechExercise(SpeechExercise speechExercise);

    List<Solution> findByVisible(boolean visible);

    @Query("SELECT s FROM Solution s JOIN s.question q WHERE q.exercise.id = ?1")
    List<Solution> findByExerciseId(Long exerciseId);

    @Query("SELECT s FROM Solution s JOIN s.question q WHERE q.exam.id = ?1")
    List<Solution> findByExamId(Long examId);
}
package com.nihongo.learningplatform.repository;

import com.nihongo.learningplatform.entity.Exam;
import com.nihongo.learningplatform.entity.Exercise;
import com.nihongo.learningplatform.entity.Question;
import com.nihongo.learningplatform.entity.QuestionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByExercise(Exercise exercise);
    List<Question> findByExam(Exam exam);
    List<Question> findByType(QuestionType type);
}
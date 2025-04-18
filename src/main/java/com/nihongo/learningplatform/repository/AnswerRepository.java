package com.nihongo.learningplatform.repository;

import com.nihongo.learningplatform.entity.Answer;
import com.nihongo.learningplatform.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {
    List<Answer> findByQuestion(Question question);
    List<Answer> findByQuestionAndIsCorrect(Question question, boolean isCorrect);
}
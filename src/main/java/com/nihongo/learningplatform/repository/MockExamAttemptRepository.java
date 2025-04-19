package com.nihongo.learningplatform.repository;

import com.nihongo.learningplatform.entity.MockExam;
import com.nihongo.learningplatform.entity.MockExamAttempt;
import com.nihongo.learningplatform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MockExamAttemptRepository extends JpaRepository<MockExamAttempt, Long> {
    List<MockExamAttempt> findByUser(User user);
    List<MockExamAttempt> findByMockExam(MockExam mockExam);
    List<MockExamAttempt> findByUserAndMockExam(User user, MockExam mockExam);

    @Query("SELECT MAX(mea.score) FROM MockExamAttempt mea WHERE mea.user = ?1 AND mea.mockExam = ?2")
    Integer findHighestScoreByUserAndMockExam(User user, MockExam mockExam);
}
package com.nihongo.learningplatform.repository;

import com.nihongo.learningplatform.entity.Course;
import com.nihongo.learningplatform.entity.MockExam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MockExamRepository extends JpaRepository<MockExam, Long> {
    List<MockExam> findByCourse(Course course);
    List<MockExam> findByLevel(String level);
    List<MockExam> findByCourseIsNull();
}
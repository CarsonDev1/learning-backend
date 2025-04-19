package com.nihongo.learningplatform.repository;

import com.nihongo.learningplatform.entity.Course;
import com.nihongo.learningplatform.entity.CourseCombo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseComboRepository extends JpaRepository<CourseCombo, Long> {
    List<CourseCombo> findByActiveTrue();
    List<CourseCombo> findByCourses_Id(Long courseId);
}
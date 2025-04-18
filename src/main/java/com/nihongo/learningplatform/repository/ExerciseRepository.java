package com.nihongo.learningplatform.repository;

import com.nihongo.learningplatform.entity.Course;
import com.nihongo.learningplatform.entity.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, Long> {
    List<Exercise> findByCourse(Course course);
    List<Exercise> findByIsAiGenerated(boolean isAiGenerated);
}
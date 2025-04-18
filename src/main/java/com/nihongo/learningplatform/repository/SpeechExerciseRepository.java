package com.nihongo.learningplatform.repository;

import com.nihongo.learningplatform.entity.Lesson;
import com.nihongo.learningplatform.entity.SpeechExercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpeechExerciseRepository extends JpaRepository<SpeechExercise, Long> {
    List<SpeechExercise> findByLesson(Lesson lesson);
}
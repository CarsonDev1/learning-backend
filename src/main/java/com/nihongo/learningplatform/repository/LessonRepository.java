package com.nihongo.learningplatform.repository;

import com.nihongo.learningplatform.entity.Lesson;
import com.nihongo.learningplatform.entity.Module;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findByModuleOrderByOrderIndex(Module module);
}

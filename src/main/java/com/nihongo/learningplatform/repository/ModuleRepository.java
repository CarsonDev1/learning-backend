package com.nihongo.learningplatform.repository;

import com.nihongo.learningplatform.entity.Course;
import com.nihongo.learningplatform.entity.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Long> {
    List<Module> findByCourseOrderByOrderIndex(Course course);
}

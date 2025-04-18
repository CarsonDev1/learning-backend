package com.nihongo.learningplatform.repository;

import com.nihongo.learningplatform.entity.Course;
import com.nihongo.learningplatform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByInstructor(User instructor);
    List<Course> findByApprovedAndActive(boolean approved, boolean active);
    List<Course> findByApproved(boolean approved);
    List<Course> findByActive(boolean active);
    List<Course> findByLevel(String level);

    @Query("SELECT c FROM Course c WHERE c.title LIKE %?1% OR c.description LIKE %?1%")

    List<Course> searchCourses(String keyword);
}

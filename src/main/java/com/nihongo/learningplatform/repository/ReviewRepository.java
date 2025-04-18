package com.nihongo.learningplatform.repository;

import com.nihongo.learningplatform.entity.Course;
import com.nihongo.learningplatform.entity.Review;
import com.nihongo.learningplatform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByCourse(Course course);
    List<Review> findByUser(User user);
    Optional<Review> findByUserAndCourse(User user, Course course);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.course = ?1")
    Double getAverageRatingForCourse(Course course);
}
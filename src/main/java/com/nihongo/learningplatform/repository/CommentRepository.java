package com.nihongo.learningplatform.repository;

import com.nihongo.learningplatform.entity.Comment;
import com.nihongo.learningplatform.entity.Lesson;
import com.nihongo.learningplatform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByLesson(Lesson lesson);
    List<Comment> findByUser(User user);
    List<Comment> findByLessonOrderByCreatedAtDesc(Lesson lesson);
}
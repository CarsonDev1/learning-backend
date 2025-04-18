package com.nihongo.learningplatform.service.impl;

import com.nihongo.learningplatform.dto.CommentDto;
import com.nihongo.learningplatform.entity.Comment;
import com.nihongo.learningplatform.entity.Lesson;
import com.nihongo.learningplatform.entity.User;
import com.nihongo.learningplatform.exception.ResourceNotFoundException;
import com.nihongo.learningplatform.repository.CommentRepository;
import com.nihongo.learningplatform.service.CommentService;
import com.nihongo.learningplatform.service.LessonService;
import com.nihongo.learningplatform.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserService userService;
    private final LessonService lessonService;

    @Autowired
    public CommentServiceImpl(CommentRepository commentRepository,
                              UserService userService,
                              LessonService lessonService) {
        this.commentRepository = commentRepository;
        this.userService = userService;
        this.lessonService = lessonService;
    }

    @Override
    @Transactional
    public CommentDto createComment(CommentDto commentDto) {
        User user = userService.getUserEntityById(commentDto.getUserId());
        Lesson lesson = lessonService.getLessonEntityById(commentDto.getLessonId());

        Comment comment = new Comment();
        comment.setContent(commentDto.getContent());
        comment.setUser(user);
        comment.setLesson(lesson);

        Comment savedComment = commentRepository.save(comment);
        return mapToDto(savedComment);
    }

    @Override
    public CommentDto getCommentById(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + id));
        return mapToDto(comment);
    }

    @Override
    public List<CommentDto> getCommentsByLesson(Long lessonId) {
        Lesson lesson = lessonService.getLessonEntityById(lessonId);
        List<Comment> comments = commentRepository.findByLessonOrderByCreatedAtDesc(lesson);
        return comments.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommentDto> getCommentsByUser(Long userId) {
        User user = userService.getUserEntityById(userId);
        List<Comment> comments = commentRepository.findByUser(user);
        return comments.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto updateComment(Long id, CommentDto commentDto) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + id));

        comment.setContent(commentDto.getContent());

        Comment updatedComment = commentRepository.save(comment);
        return mapToDto(updatedComment);
    }

    @Override
    @Transactional
    public void deleteComment(Long id) {
        if (!commentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Comment not found with id: " + id);
        }
        commentRepository.deleteById(id);
    }

    // Helper method to map Comment entity to CommentDto
    private CommentDto mapToDto(Comment comment) {
        CommentDto commentDto = new CommentDto();
        commentDto.setId(comment.getId());
        commentDto.setContent(comment.getContent());
        commentDto.setCreatedAt(comment.getCreatedAt());
        commentDto.setUserId(comment.getUser().getId());
        commentDto.setLessonId(comment.getLesson().getId());
        commentDto.setUsername(comment.getUser().getUsername());
        return commentDto;
    }
}
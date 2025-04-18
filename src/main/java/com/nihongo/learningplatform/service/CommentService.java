package com.nihongo.learningplatform.service;

import com.nihongo.learningplatform.dto.CommentDto;
import com.nihongo.learningplatform.entity.Comment;

import java.util.List;

public interface CommentService {
    CommentDto createComment(CommentDto commentDto);
    CommentDto getCommentById(Long id);
    List<CommentDto> getCommentsByLesson(Long lessonId);
    List<CommentDto> getCommentsByUser(Long userId);
    CommentDto updateComment(Long id, CommentDto commentDto);
    void deleteComment(Long id);
}
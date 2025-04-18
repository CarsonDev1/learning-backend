package com.nihongo.learningplatform.service;

import com.nihongo.learningplatform.dto.LessonDto;
import com.nihongo.learningplatform.entity.Course;
import com.nihongo.learningplatform.entity.Lesson;

import java.util.List;

public interface LessonService {
    LessonDto createLesson(LessonDto lessonDto);
    LessonDto getLessonById(Long id);
    List<LessonDto> getLessonsByCourse(Long courseId);
    LessonDto updateLesson(Long id, LessonDto lessonDto);
    void deleteLesson(Long id);
    Lesson getLessonEntityById(Long id);
}
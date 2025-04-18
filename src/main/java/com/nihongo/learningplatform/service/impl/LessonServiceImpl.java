package com.nihongo.learningplatform.service.impl;

import com.nihongo.learningplatform.dto.LessonDto;
import com.nihongo.learningplatform.entity.Course;
import com.nihongo.learningplatform.entity.Lesson;
import com.nihongo.learningplatform.exception.ResourceNotFoundException;
import com.nihongo.learningplatform.repository.LessonRepository;
import com.nihongo.learningplatform.service.CourseService;
import com.nihongo.learningplatform.service.LessonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LessonServiceImpl implements LessonService {

    private final LessonRepository lessonRepository;
    private final CourseService courseService;

    @Autowired
    public LessonServiceImpl(LessonRepository lessonRepository, CourseService courseService) {
        this.lessonRepository = lessonRepository;
        this.courseService = courseService;
    }

    @Override
    @Transactional
    public LessonDto createLesson(LessonDto lessonDto) {
        Course course = courseService.getCourseEntityById(lessonDto.getCourseId());

        Lesson lesson = new Lesson();
        lesson.setTitle(lessonDto.getTitle());
        lesson.setContent(lessonDto.getContent());
        lesson.setVideoUrl(lessonDto.getVideoUrl());
        lesson.setOrderIndex(lessonDto.getOrderIndex());
        lesson.setCourse(course);

        Lesson savedLesson = lessonRepository.save(lesson);
        return mapToDto(savedLesson);
    }

    @Override
    public LessonDto getLessonById(Long id) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with id: " + id));
        return mapToDto(lesson);
    }

    @Override
    public List<LessonDto> getLessonsByCourse(Long courseId) {
        Course course = courseService.getCourseEntityById(courseId);
        List<Lesson> lessons = lessonRepository.findByCourseOrderByOrderIndex(course);
        return lessons.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public LessonDto updateLesson(Long id, LessonDto lessonDto) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with id: " + id));

        lesson.setTitle(lessonDto.getTitle());
        lesson.setContent(lessonDto.getContent());
        lesson.setVideoUrl(lessonDto.getVideoUrl());
        lesson.setOrderIndex(lessonDto.getOrderIndex());

        Lesson updatedLesson = lessonRepository.save(lesson);
        return mapToDto(updatedLesson);
    }

    @Override
    @Transactional
    public void deleteLesson(Long id) {
        if (!lessonRepository.existsById(id)) {
            throw new ResourceNotFoundException("Lesson not found with id: " + id);
        }
        lessonRepository.deleteById(id);
    }

    @Override
    public Lesson getLessonEntityById(Long id) {
        return lessonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with id: " + id));
    }

    // Helper method to map Lesson entity to LessonDto
    private LessonDto mapToDto(Lesson lesson) {
        LessonDto lessonDto = new LessonDto();
        lessonDto.setId(lesson.getId());
        lessonDto.setTitle(lesson.getTitle());
        lessonDto.setContent(lesson.getContent());
        lessonDto.setVideoUrl(lesson.getVideoUrl());
        lessonDto.setOrderIndex(lesson.getOrderIndex());
        lessonDto.setCourseId(lesson.getCourse().getId());
        return lessonDto;
    }
}
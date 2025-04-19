package com.nihongo.learningplatform.service.impl;

import com.nihongo.learningplatform.dto.LessonDto;
import com.nihongo.learningplatform.entity.Course;
import com.nihongo.learningplatform.entity.Lesson;
import com.nihongo.learningplatform.entity.Module;
import com.nihongo.learningplatform.exception.ResourceNotFoundException;
import com.nihongo.learningplatform.repository.LessonRepository;
import com.nihongo.learningplatform.service.CloudinaryService;
import com.nihongo.learningplatform.service.CourseService;
import com.nihongo.learningplatform.service.LessonService;
import com.nihongo.learningplatform.service.ModuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LessonServiceImpl implements LessonService {

    private final LessonRepository lessonRepository;
    private final CourseService courseService;
    private final ModuleService moduleService;

    @Autowired
    public LessonServiceImpl(LessonRepository lessonRepository, CourseService courseService, ModuleService moduleService) {
        this.lessonRepository = lessonRepository;
        this.courseService = courseService;
        this.moduleService = moduleService;
    }

    @Override
    @Transactional
    public LessonDto createLesson(LessonDto lessonDto) {
        Module module = moduleService.getModuleEntityById(lessonDto.getModuleId());

        Lesson lesson = new Lesson();
        lesson.setTitle(lessonDto.getTitle());
        lesson.setContent(lessonDto.getContent());
        lesson.setVideoUrl(lessonDto.getVideoUrl());
        lesson.setOrderIndex(lessonDto.getOrderIndex());
        lesson.setModule(module);

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
        List<Module> modules = new ArrayList<>(course.getModules());
        return modules.stream()
                .flatMap(module -> lessonRepository.findByModuleOrderByOrderIndex(module).stream())
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<LessonDto> getLessonsByModule(Long moduleId) {
        Module module = moduleService.getModuleEntityById(moduleId);
        List<Lesson> lessons = lessonRepository.findByModuleOrderByOrderIndex(module);
        return lessons.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Autowired
    private CloudinaryService cloudinaryService;

    @Override
    @Transactional
    public LessonDto updateLesson(Long id, LessonDto lessonDto) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with id: " + id));

        if (lessonDto.getVideoUrl() != null && !lessonDto.getVideoUrl().equals(lesson.getVideoUrl())
                && lesson.getVideoPublicId() != null) {
            cloudinaryService.deleteFile(lesson.getVideoPublicId(), "video");
        }

        lesson.setTitle(lessonDto.getTitle());
        lesson.setContent(lessonDto.getContent());
        lesson.setVideoUrl(lessonDto.getVideoUrl());
        lesson.setVideoPublicId(lessonDto.getVideoPublicId());
        lesson.setOrderIndex(lessonDto.getOrderIndex());

        Lesson updatedLesson = lessonRepository.save(lesson);
        return mapToDto(updatedLesson);
    }

    @Override
    @Transactional
    public void deleteLesson(Long id) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with id: " + id));
        if (!lessonRepository.existsById(id)) {
            throw new ResourceNotFoundException("Lesson not found with id: " + id);
        }
        if (lesson.getVideoPublicId() != null) {
            cloudinaryService.deleteFile(lesson.getVideoPublicId(), "video");
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
        lessonDto.setModuleId(lesson.getModule().getId()); // ✅ moduleId chứ không phải courseId
        return lessonDto;
    }
}

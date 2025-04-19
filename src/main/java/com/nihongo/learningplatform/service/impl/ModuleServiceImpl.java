package com.nihongo.learningplatform.service.impl;

import com.nihongo.learningplatform.dto.LessonDto;
import com.nihongo.learningplatform.dto.ModuleDto;
import com.nihongo.learningplatform.entity.Course;
import com.nihongo.learningplatform.entity.Module;
import com.nihongo.learningplatform.exception.ResourceNotFoundException;
import com.nihongo.learningplatform.repository.ModuleRepository;
import com.nihongo.learningplatform.service.CourseService;
import com.nihongo.learningplatform.service.LessonService;
import com.nihongo.learningplatform.service.ModuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ModuleServiceImpl implements ModuleService {

    private final ModuleRepository moduleRepository;
    private final CourseService courseService;
    private final LessonService lessonService;

    @Autowired
    public ModuleServiceImpl(ModuleRepository moduleRepository,
                             CourseService courseService,
                             @Lazy LessonService lessonService) {
        this.moduleRepository = moduleRepository;
        this.courseService = courseService;
        this.lessonService = lessonService;
    }

    @Override
    @Transactional
    public ModuleDto createModule(ModuleDto moduleDto) {
        Course course = courseService.getCourseEntityById(moduleDto.getCourseId());

        Module module = new Module();
        module.setTitle(moduleDto.getTitle());
        module.setDescription(moduleDto.getDescription());
        module.setOrderIndex(moduleDto.getOrderIndex());
        module.setDurationMinutes(moduleDto.getDurationMinutes());
        module.setCourse(course);

        Module savedModule = moduleRepository.save(module);
        return mapToDto(savedModule);
    }

    @Override
    public ModuleDto getModuleById(Long id) {
        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found with id: " + id));

        ModuleDto moduleDto = mapToDto(module);

        // Load lessons for this module
        List<LessonDto> lessons = lessonService.getLessonsByModule(id);
        moduleDto.setLessons(lessons);

        return moduleDto;
    }

    @Override
    public List<ModuleDto> getModulesByCourse(Long courseId) {
        Course course = courseService.getCourseEntityById(courseId);
        List<Module> modules = moduleRepository.findByCourseOrderByOrderIndex(course);

        return modules.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ModuleDto updateModule(Long id, ModuleDto moduleDto) {
        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found with id: " + id));

        module.setTitle(moduleDto.getTitle());
        module.setDescription(moduleDto.getDescription());
        module.setOrderIndex(moduleDto.getOrderIndex());
        module.setDurationMinutes(moduleDto.getDurationMinutes());

        Module updatedModule = moduleRepository.save(module);
        return mapToDto(updatedModule);
    }

    @Override
    @Transactional
    public void deleteModule(Long id) {
        if (!moduleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Module not found with id: " + id);
        }
        moduleRepository.deleteById(id);
    }

    @Override
    public Module getModuleEntityById(Long id) {
        return moduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found with id: " + id));
    }

    // Helper method to map Module entity to ModuleDto
    private ModuleDto mapToDto(Module module) {
        ModuleDto moduleDto = new ModuleDto();
        moduleDto.setId(module.getId());
        moduleDto.setTitle(module.getTitle());
        moduleDto.setDescription(module.getDescription());
        moduleDto.setOrderIndex(module.getOrderIndex());
        moduleDto.setDurationMinutes(module.getDurationMinutes());
        moduleDto.setCourseId(module.getCourse().getId());
        moduleDto.setCourseName(module.getCourse().getTitle());
        moduleDto.setCreatedAt(module.getCreatedAt());
        moduleDto.setUpdatedAt(module.getUpdatedAt());
        return moduleDto;
    }
}
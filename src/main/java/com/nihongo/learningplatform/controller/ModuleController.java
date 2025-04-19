package com.nihongo.learningplatform.controller;

import com.nihongo.learningplatform.dto.ApiResponseDto;
import com.nihongo.learningplatform.dto.ModuleDto;
import com.nihongo.learningplatform.entity.Course;
import com.nihongo.learningplatform.entity.User;
import com.nihongo.learningplatform.service.CourseService;
import com.nihongo.learningplatform.service.ModuleService;
import com.nihongo.learningplatform.service.UserService;
import com.nihongo.learningplatform.service.EnrollmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ModuleController {

    private final ModuleService moduleService;
    private final CourseService courseService;
    private final UserService userService;
    private final EnrollmentService enrollmentService;

    @Autowired
    public ModuleController(ModuleService moduleService,
                            CourseService courseService,
                            UserService userService, EnrollmentService enrollmentService, EnrollmentService enrollmentService1) {
        this.moduleService = moduleService;
        this.courseService = courseService;
        this.userService = userService;
        this.enrollmentService = enrollmentService1;
    }

    // Student endpoints

    @GetMapping("/student/courses/{courseId}/modules")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<ApiResponseDto> getModulesByCourseForStudent(@PathVariable Long courseId, Principal principal) {
        User user = userService.getUserEntityByUsername(principal.getName());

        // Check if the user is enrolled in the course or is the instructor or an admin
        Course course = courseService.getCourseEntityById(courseId);
        boolean isInstructor = course.getInstructor().getId().equals(user.getId());
        boolean isAdmin = user.getRole().name().equals("ADMIN");
        boolean isEnrolled = enrollmentService.isStudentEnrolledInCourse(user.getId(), courseId);

        if (!isInstructor && !isAdmin && !isEnrolled) {
            ApiResponseDto apiResponse = new ApiResponseDto(
                    false,
                    "You are not enrolled in this course",
                    null,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
        }

        List<ModuleDto> modules = moduleService.getModulesByCourse(courseId);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Modules retrieved successfully",
                modules,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/student/modules/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<ApiResponseDto> getModuleForStudent(@PathVariable Long id, Principal principal) {
        User user = userService.getUserEntityByUsername(principal.getName());
        ModuleDto module = moduleService.getModuleById(id);

        // Check if the user is enrolled in the course or is the instructor or an admin
        Course course = courseService.getCourseEntityById(module.getCourseId());
        boolean isInstructor = course.getInstructor().getId().equals(user.getId());
        boolean isAdmin = user.getRole().name().equals("ADMIN");
        boolean isEnrolled = enrollmentService.isStudentEnrolledInCourse(user.getId(), module.getCourseId());

        if (!isInstructor && !isAdmin && !isEnrolled) {
            ApiResponseDto apiResponse = new ApiResponseDto(
                    false,
                    "You are not enrolled in this course",
                    null,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
        }

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Module retrieved successfully",
                module,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    // Instructor endpoints

    @PostMapping("/instructor/courses/{courseId}/modules")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponseDto> createModule(@PathVariable Long courseId,
                                                       @Valid @RequestBody ModuleDto moduleDto,
                                                       Principal principal) {
        User instructor = userService.getUserEntityByUsername(principal.getName());

        // Verify that the course belongs to the instructor
        Course course = courseService.getCourseEntityById(courseId);
        boolean isAdmin = instructor.getRole().name().equals("ADMIN");

        if (!isAdmin && !course.getInstructor().getId().equals(instructor.getId())) {
            ApiResponseDto apiResponse = new ApiResponseDto(
                    false,
                    "You are not authorized to add modules to this course",
                    null,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
        }

        moduleDto.setCourseId(courseId);
        ModuleDto createdModule = moduleService.createModule(moduleDto);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Module created successfully",
                createdModule,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @PutMapping("/instructor/modules/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponseDto> updateModule(@PathVariable Long id,
                                                       @Valid @RequestBody ModuleDto moduleDto,
                                                       Principal principal) {
        User instructor = userService.getUserEntityByUsername(principal.getName());

        // Verify that the module's course belongs to the instructor
        ModuleDto existingModule = moduleService.getModuleById(id);
        Course course = courseService.getCourseEntityById(existingModule.getCourseId());
        boolean isAdmin = instructor.getRole().name().equals("ADMIN");

        if (!isAdmin && !course.getInstructor().getId().equals(instructor.getId())) {
            ApiResponseDto apiResponse = new ApiResponseDto(
                    false,
                    "You are not authorized to update this module",
                    null,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
        }

        moduleDto.setId(id);
        moduleDto.setCourseId(existingModule.getCourseId());
        ModuleDto updatedModule = moduleService.updateModule(id, moduleDto);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Module updated successfully",
                updatedModule,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @DeleteMapping("/instructor/modules/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponseDto> deleteModule(@PathVariable Long id, Principal principal) {
        User instructor = userService.getUserEntityByUsername(principal.getName());

        // Verify that the module's course belongs to the instructor
        ModuleDto existingModule = moduleService.getModuleById(id);
        Course course = courseService.getCourseEntityById(existingModule.getCourseId());
        boolean isAdmin = instructor.getRole().name().equals("ADMIN");

        if (!isAdmin && !course.getInstructor().getId().equals(instructor.getId())) {
            ApiResponseDto apiResponse = new ApiResponseDto(
                    false,
                    "You are not authorized to delete this module",
                    null,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
        }

        moduleService.deleteModule(id);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Module deleted successfully",
                null,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }
}
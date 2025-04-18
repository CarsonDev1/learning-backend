package com.nihongo.learningplatform.controller;

import com.nihongo.learningplatform.dto.ApiResponseDto;
import com.nihongo.learningplatform.dto.CourseDto;
import com.nihongo.learningplatform.dto.LessonDto;
import com.nihongo.learningplatform.entity.User;
import com.nihongo.learningplatform.service.CourseService;
import com.nihongo.learningplatform.service.EnrollmentService;
import com.nihongo.learningplatform.service.LessonService;
import com.nihongo.learningplatform.service.UserService;
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
public class LessonController {

    private final LessonService lessonService;
    private final CourseService courseService;
    private final UserService userService;
    private final EnrollmentService enrollmentService;

    @Autowired
    public LessonController(LessonService lessonService,
                            CourseService courseService,
                            UserService userService,
                            EnrollmentService enrollmentService) {
        this.lessonService = lessonService;
        this.courseService = courseService;
        this.userService = userService;
        this.enrollmentService = enrollmentService;
    }

    // Student endpoints

    @GetMapping("/student/courses/{courseId}/lessons")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<ApiResponseDto> getLessonsByCourseForStudent(@PathVariable Long courseId, Principal principal) {
        User user = userService.getUserEntityByUsername(principal.getName());

        // Check if the user is enrolled in the course or is the instructor or an admin
        CourseDto course = courseService.getCourseById(courseId);
        boolean isInstructor = course.getInstructorId().equals(user.getId());
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

        List<LessonDto> lessons = lessonService.getLessonsByCourse(courseId);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Lessons retrieved successfully",
                lessons,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/student/lessons/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<ApiResponseDto> getLessonForStudent(@PathVariable Long id, Principal principal) {
        User user = userService.getUserEntityByUsername(principal.getName());
        LessonDto lesson = lessonService.getLessonById(id);

        // Check if the user is enrolled in the course or is the instructor or an admin
        CourseDto course = courseService.getCourseById(lesson.getCourseId());
        boolean isInstructor = course.getInstructorId().equals(user.getId());
        boolean isAdmin = user.getRole().name().equals("ADMIN");
        boolean isEnrolled = enrollmentService.isStudentEnrolledInCourse(user.getId(), lesson.getCourseId());

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
                "Lesson retrieved successfully",
                lesson,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    // Instructor endpoints

    @PostMapping("/instructor/courses/{courseId}/lessons")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponseDto> createLesson(@PathVariable Long courseId,
                                                       @Valid @RequestBody LessonDto lessonDto,
                                                       Principal principal) {
        // Verify that the course belongs to the instructor
        CourseDto course = courseService.getCourseById(courseId);
        User instructor = userService.getUserEntityByUsername(principal.getName());

        if (!course.getInstructorId().equals(instructor.getId()) && !instructor.getRole().name().equals("ADMIN")) {
            ApiResponseDto apiResponse = new ApiResponseDto(
                    false,
                    "You are not authorized to add lessons to this course",
                    null,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
        }

        lessonDto.setCourseId(courseId);
        LessonDto createdLesson = lessonService.createLesson(lessonDto);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Lesson created successfully",
                createdLesson,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @GetMapping("/instructor/courses/{courseId}/lessons")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponseDto> getLessonsByCourseForInstructor(@PathVariable Long courseId, Principal principal) {
        // Verify that the course belongs to the instructor
        CourseDto course = courseService.getCourseById(courseId);
        User instructor = userService.getUserEntityByUsername(principal.getName());

        if (!course.getInstructorId().equals(instructor.getId()) && !instructor.getRole().name().equals("ADMIN")) {
            ApiResponseDto apiResponse = new ApiResponseDto(
                    false,
                    "You are not authorized to view lessons for this course",
                    null,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
        }

        List<LessonDto> lessons = lessonService.getLessonsByCourse(courseId);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Lessons retrieved successfully",
                lessons,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PutMapping("/instructor/lessons/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponseDto> updateLesson(@PathVariable Long id,
                                                       @Valid @RequestBody LessonDto lessonDto,
                                                       Principal principal) {
        // Verify that the lesson's course belongs to the instructor
        LessonDto existingLesson = lessonService.getLessonById(id);
        CourseDto course = courseService.getCourseById(existingLesson.getCourseId());
        User instructor = userService.getUserEntityByUsername(principal.getName());

        if (!course.getInstructorId().equals(instructor.getId()) && !instructor.getRole().name().equals("ADMIN")) {
            ApiResponseDto apiResponse = new ApiResponseDto(
                    false,
                    "You are not authorized to update this lesson",
                    null,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
        }

        lessonDto.setId(id);
        lessonDto.setCourseId(existingLesson.getCourseId());
        LessonDto updatedLesson = lessonService.updateLesson(id, lessonDto);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Lesson updated successfully",
                updatedLesson,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @DeleteMapping("/instructor/lessons/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponseDto> deleteLesson(@PathVariable Long id, Principal principal) {
        // Verify that the lesson's course belongs to the instructor
        LessonDto existingLesson = lessonService.getLessonById(id);
        CourseDto course = courseService.getCourseById(existingLesson.getCourseId());
        User instructor = userService.getUserEntityByUsername(principal.getName());

        if (!course.getInstructorId().equals(instructor.getId()) && !instructor.getRole().name().equals("ADMIN")) {
            ApiResponseDto apiResponse = new ApiResponseDto(
                    false,
                    "You are not authorized to delete this lesson",
                    null,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
        }

        lessonService.deleteLesson(id);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Lesson deleted successfully",
                null,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    // Admin endpoints

    @GetMapping("/admin/lessons/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto> getLessonById(@PathVariable Long id) {
        LessonDto lesson = lessonService.getLessonById(id);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Lesson retrieved successfully",
                lesson,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }
}
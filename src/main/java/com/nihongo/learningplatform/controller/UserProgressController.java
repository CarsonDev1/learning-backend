package com.nihongo.learningplatform.controller;

import com.nihongo.learningplatform.dto.*;
import com.nihongo.learningplatform.entity.User;
import com.nihongo.learningplatform.service.*;
import com.nihongo.learningplatform.service.EnrollmentService;
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
public class UserProgressController {

    private final UserProgressService userProgressService;
    private final UserService userService;
    private final CourseService courseService;
    private final LessonService lessonService;
    private final ExerciseService exerciseService;
    private final ExamService examService;
    private final EnrollmentService enrollmentService;

    @Autowired
    public UserProgressController(UserProgressService userProgressService,
                                  UserService userService,
                                  CourseService courseService,
                                  LessonService lessonService,
                                  ExerciseService exerciseService,
                                  ExamService examService,
                                  EnrollmentService enrollmentService) {
        this.userProgressService = userProgressService;
        this.userService = userService;
        this.courseService = courseService;
        this.lessonService = lessonService;
        this.exerciseService = exerciseService;
        this.examService = examService;
        this.enrollmentService = enrollmentService;
    }

    // Student endpoints for tracking progress

    @GetMapping("/student/progress")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<ApiResponseDto> getUserProgress(Principal principal) {
        User user = userService.getUserEntityByUsername(principal.getName());
        List<UserProgressDto> progressList = userProgressService.getUserProgressByUser(user.getId());

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "User progress retrieved successfully",
                progressList,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/student/progress/completed")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<ApiResponseDto> getCompletedUserProgress(Principal principal) {
        User user = userService.getUserEntityByUsername(principal.getName());
        List<UserProgressDto> progressList = userProgressService.getCompletedProgressByUser(user.getId());

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Completed user progress retrieved successfully",
                progressList,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/student/lessons/{lessonId}/progress")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<ApiResponseDto> getLessonProgress(@PathVariable Long lessonId, Principal principal) {
        User user = userService.getUserEntityByUsername(principal.getName());

        try {
            UserProgressDto progress = userProgressService.getUserProgressByUserAndLesson(user.getId(), lessonId);

            ApiResponseDto apiResponse = new ApiResponseDto(
                    true,
                    "Lesson progress retrieved successfully",
                    progress,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponseDto apiResponse = new ApiResponseDto(
                    false,
                    "No progress found for this lesson",
                    null,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/student/lessons/{lessonId}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<ApiResponseDto> completeLesson(@PathVariable Long lessonId, Principal principal) {
        User user = userService.getUserEntityByUsername(principal.getName());
        LessonDto lesson = lessonService.getLessonById(lessonId);

        // Check if the user is enrolled in the course
        CourseDto course = courseService.getCourseById(lesson.getCourseId());
        boolean isAdmin = user.getRole().name().equals("ADMIN");
        boolean isInstructor = user.getRole().name().equals("INSTRUCTOR") ||
                course.getInstructorId().equals(user.getId());

        if (!isAdmin && !isInstructor) {
            // For regular students, check enrollment
            boolean isEnrolled = enrollmentService.isStudentEnrolledInCourse(user.getId(), lesson.getCourseId());
            if (!isEnrolled) {
                ApiResponseDto apiResponse = new ApiResponseDto(
                        false,
                        "You must be enrolled in this course to mark lessons as completed",
                        null,
                        LocalDateTime.now()
                );

                return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
            }
        }

        UserProgressDto progress = userProgressService.createOrUpdateLessonProgress(
                user.getId(), lessonId, true);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Lesson marked as completed",
                progress,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/student/exercises/{exerciseId}/progress")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<ApiResponseDto> getExerciseProgress(@PathVariable Long exerciseId, Principal principal) {
        User user = userService.getUserEntityByUsername(principal.getName());

        try {
            UserProgressDto progress = userProgressService.getUserProgressByUserAndExercise(user.getId(), exerciseId);

            ApiResponseDto apiResponse = new ApiResponseDto(
                    true,
                    "Exercise progress retrieved successfully",
                    progress,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponseDto apiResponse = new ApiResponseDto(
                    false,
                    "No progress found for this exercise",
                    null,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/student/exams/{examId}/progress")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<ApiResponseDto> getExamProgress(@PathVariable Long examId, Principal principal) {
        User user = userService.getUserEntityByUsername(principal.getName());

        try {
            UserProgressDto progress = userProgressService.getUserProgressByUserAndExam(user.getId(), examId);

            ApiResponseDto apiResponse = new ApiResponseDto(
                    true,
                    "Exam progress retrieved successfully",
                    progress,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponseDto apiResponse = new ApiResponseDto(
                    false,
                    "No progress found for this exam",
                    null,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/student/speech-exercises/{speechExerciseId}/progress")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<ApiResponseDto> getSpeechExerciseProgress(@PathVariable Long speechExerciseId, Principal principal) {
        User user = userService.getUserEntityByUsername(principal.getName());

        try {
            UserProgressDto progress = userProgressService.getUserProgressByUserAndSpeechExercise(user.getId(), speechExerciseId);

            ApiResponseDto apiResponse = new ApiResponseDto(
                    true,
                    "Speech exercise progress retrieved successfully",
                    progress,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponseDto apiResponse = new ApiResponseDto(
                    false,
                    "No progress found for this speech exercise",
                    null,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
        }
    }

    // Admin endpoints for managing progress

    @GetMapping("/admin/users/{userId}/progress")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto> getUserProgressByAdmin(@PathVariable Long userId) {
        List<UserProgressDto> progressList = userProgressService.getUserProgressByUser(userId);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "User progress retrieved successfully",
                progressList,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @DeleteMapping("/admin/progress/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto> deleteUserProgress(@PathVariable Long id) {
        userProgressService.deleteUserProgress(id);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "User progress deleted successfully",
                null,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }
}


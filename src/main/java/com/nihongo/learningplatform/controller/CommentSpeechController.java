package com.nihongo.learningplatform.controller;

import com.nihongo.learningplatform.dto.*;
import com.nihongo.learningplatform.entity.User;
import com.nihongo.learningplatform.service.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CommentSpeechController {

    private final CommentService commentService;
    private final SpeechExerciseService speechExerciseService;
    private final SpeechRecognitionService speechRecognitionService;
    private final LessonService lessonService;
    private final CourseService courseService;
    private final UserService userService;
    private final EnrollmentService enrollmentService;
    private final UserProgressService userProgressService;

    @Autowired
    public CommentSpeechController(@Lazy CommentService commentService,
                                   SpeechExerciseService speechExerciseService,
                                   SpeechRecognitionService speechRecognitionService,
                                   @Lazy LessonService lessonService,
                                   @Lazy CourseService courseService,
                                   UserService userService,
                                   EnrollmentService enrollmentService,
                                   @Lazy UserProgressService userProgressService) {
        this.commentService = commentService;
        this.speechExerciseService = speechExerciseService;
        this.speechRecognitionService = speechRecognitionService;
        this.lessonService = lessonService;
        this.courseService = courseService;
        this.userService = userService;
        this.enrollmentService = enrollmentService;
        this.userProgressService = userProgressService;
    }

    // Comment endpoints

    @PostMapping("/student/lessons/{lessonId}/comments")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<ApiResponseDto> createComment(@PathVariable Long lessonId,
                                                        @Valid @RequestBody CommentDto commentDto,
                                                        Principal principal) {
        User user = userService.getUserEntityByUsername(principal.getName());
        LessonDto lesson = lessonService.getLessonById(lessonId);

        // Check if the user is enrolled in the course or is the instructor or an admin
        CourseDto course = courseService.getCourseById(lesson.getCourseId());
        boolean isInstructor = course.getInstructorId().equals(user.getId());
        boolean isAdmin = user.getRole().name().equals("ADMIN");
        boolean isEnrolled = enrollmentService.isStudentEnrolledInCourse(user.getId(), lesson.getCourseId());

        if (!isInstructor && !isAdmin && !isEnrolled) {
            ApiResponseDto apiResponse = new ApiResponseDto(
                    false,
                    "You must be enrolled in this course to comment",
                    null,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
        }

        commentDto.setUserId(user.getId());
        commentDto.setLessonId(lessonId);
        CommentDto createdComment = commentService.createComment(commentDto);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Comment added successfully",
                createdComment,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @GetMapping("/student/lessons/{lessonId}/comments")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<ApiResponseDto> getLessonComments(@PathVariable Long lessonId, Principal principal) {
        User user = userService.getUserEntityByUsername(principal.getName());
        LessonDto lesson = lessonService.getLessonById(lessonId);

        // Check if the user is enrolled in the course or is the instructor or an admin
        CourseDto course = courseService.getCourseById(lesson.getCourseId());
        boolean isInstructor = course.getInstructorId().equals(user.getId());
        boolean isAdmin = user.getRole().name().equals("ADMIN");
        boolean isEnrolled = enrollmentService.isStudentEnrolledInCourse(user.getId(), lesson.getCourseId());

        if (!isInstructor && !isAdmin && !isEnrolled) {
            ApiResponseDto apiResponse = new ApiResponseDto(
                    false,
                    "You must be enrolled in this course to view comments",
                    null,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
        }

        List<CommentDto> comments = commentService.getCommentsByLesson(lessonId);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Comments retrieved successfully",
                comments,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PutMapping("/student/comments/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<ApiResponseDto> updateComment(@PathVariable Long id,
                                                        @Valid @RequestBody CommentDto commentDto,
                                                        Principal principal) {
        User user = userService.getUserEntityByUsername(principal.getName());
        CommentDto existingComment = commentService.getCommentById(id);

        // Check if the comment belongs to the user or the user is an admin
        boolean isAdmin = user.getRole().name().equals("ADMIN");
        boolean isOwner = existingComment.getUserId().equals(user.getId());

        if (!isAdmin && !isOwner) {
            ApiResponseDto apiResponse = new ApiResponseDto(
                    false,
                    "You are not authorized to update this comment",
                    null,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
        }

        commentDto.setId(id);
        commentDto.setUserId(existingComment.getUserId());
        commentDto.setLessonId(existingComment.getLessonId());
        CommentDto updatedComment = commentService.updateComment(id, commentDto);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Comment updated successfully",
                updatedComment,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @DeleteMapping("/student/comments/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<ApiResponseDto> deleteComment(@PathVariable Long id, Principal principal) {
        User user = userService.getUserEntityByUsername(principal.getName());
        CommentDto existingComment = commentService.getCommentById(id);

        // Check if the comment belongs to the user or the user is an admin/instructor
        boolean isAdmin = user.getRole().name().equals("ADMIN");
        boolean isInstructor = user.getRole().name().equals("INSTRUCTOR");
        boolean isOwner = existingComment.getUserId().equals(user.getId());

        if (!isAdmin && !isInstructor && !isOwner) {
            ApiResponseDto apiResponse = new ApiResponseDto(
                    false,
                    "You are not authorized to delete this comment",
                    null,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
        }

        commentService.deleteComment(id);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Comment deleted successfully",
                null,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    // Speech Exercise endpoints

    @GetMapping("/student/lessons/{lessonId}/speech-exercises")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<ApiResponseDto> getSpeechExercisesByLesson(@PathVariable Long lessonId, Principal principal) {
        User user = userService.getUserEntityByUsername(principal.getName());
        LessonDto lesson = lessonService.getLessonById(lessonId);

        // Check if the user is enrolled in the course or is the instructor or an admin
        CourseDto course = courseService.getCourseById(lesson.getCourseId());
        boolean isInstructor = course.getInstructorId().equals(user.getId());
        boolean isAdmin = user.getRole().name().equals("ADMIN");
        boolean isEnrolled = enrollmentService.isStudentEnrolledInCourse(user.getId(), lesson.getCourseId());

        if (!isInstructor && !isAdmin && !isEnrolled) {
            ApiResponseDto apiResponse = new ApiResponseDto(
                    false,
                    "You must be enrolled in this course to access speech exercises",
                    null,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
        }

        List<SpeechExerciseDto> speechExercises = speechExerciseService.getSpeechExercisesByLesson(lessonId);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Speech exercises retrieved successfully",
                speechExercises,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/student/speech-exercises/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<ApiResponseDto> getSpeechExercise(@PathVariable Long id, Principal principal) {
        User user = userService.getUserEntityByUsername(principal.getName());
        SpeechExerciseDto speechExercise = speechExerciseService.getSpeechExerciseById(id);
        LessonDto lesson = lessonService.getLessonById(speechExercise.getLessonId());

        // Check if the user is enrolled in the course or is the instructor or an admin
        CourseDto course = courseService.getCourseById(lesson.getCourseId());
        boolean isInstructor = course.getInstructorId().equals(user.getId());
        boolean isAdmin = user.getRole().name().equals("ADMIN");
        boolean isEnrolled = enrollmentService.isStudentEnrolledInCourse(user.getId(), lesson.getCourseId());

        if (!isInstructor && !isAdmin && !isEnrolled) {
            ApiResponseDto apiResponse = new ApiResponseDto(
                    false,
                    "You must be enrolled in this course to access speech exercises",
                    null,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
        }

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Speech exercise retrieved successfully",
                speechExercise,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PostMapping("/student/speech-exercises/{id}/recognize")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<ApiResponseDto> recognizeSpeech(@PathVariable Long id,
                                                          @Valid @RequestBody SpeechRecognitionRequestDto requestDto,
                                                          Principal principal) {
        User user = userService.getUserEntityByUsername(principal.getName());
        SpeechExerciseDto speechExercise = speechExerciseService.getSpeechExerciseById(id);
        LessonDto lesson = lessonService.getLessonById(speechExercise.getLessonId());

        // Check if the user is enrolled in the course or is the instructor or an admin
        CourseDto course = courseService.getCourseById(lesson.getCourseId());
        boolean isInstructor = course.getInstructorId().equals(user.getId());
        boolean isAdmin = user.getRole().name().equals("ADMIN");
        boolean isEnrolled = enrollmentService.isStudentEnrolledInCourse(user.getId(), lesson.getCourseId());

        if (!isInstructor && !isAdmin && !isEnrolled) {
            ApiResponseDto apiResponse = new ApiResponseDto(
                    false,
                    "You must be enrolled in this course to use speech recognition",
                    null,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
        }

        requestDto.setSpeechExerciseId(id);
        Float pronunciationScore = speechRecognitionService.recognizeAndEvaluateSpeech(requestDto);

        // Save progress
        UserProgressDto progress = userProgressService.createOrUpdateSpeechExerciseProgress(
                user.getId(), id, requestDto.getAudioData(), pronunciationScore);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Speech recognition completed",
                Map.of("pronunciationScore", pronunciationScore, "progress", progress),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    // Instructor endpoints for speech exercises

    @PostMapping("/instructor/lessons/{lessonId}/speech-exercises")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponseDto> createSpeechExercise(@PathVariable Long lessonId,
                                                               @Valid @RequestBody SpeechExerciseDto speechExerciseDto,
                                                               Principal principal) {
        User instructor = userService.getUserEntityByUsername(principal.getName());
        LessonDto lesson = lessonService.getLessonById(lessonId);

        // Check if the instructor owns the course
        CourseDto course = courseService.getCourseById(lesson.getCourseId());
        boolean isAdmin = instructor.getRole().name().equals("ADMIN");
        boolean isCourseOwner = course.getInstructorId().equals(instructor.getId());

        if (!isAdmin && !isCourseOwner) {
            ApiResponseDto apiResponse = new ApiResponseDto(
                    false,
                    "You are not authorized to add speech exercises to this lesson",
                    null,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
        }

        speechExerciseDto.setLessonId(lessonId);
        SpeechExerciseDto createdSpeechExercise = speechExerciseService.createSpeechExercise(speechExerciseDto);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Speech exercise created successfully",
                createdSpeechExercise,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @PutMapping("/instructor/speech-exercises/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponseDto> updateSpeechExercise(@PathVariable Long id,
                                                               @Valid @RequestBody SpeechExerciseDto speechExerciseDto,
                                                               Principal principal) {
        User instructor = userService.getUserEntityByUsername(principal.getName());
        SpeechExerciseDto existingSpeechExercise = speechExerciseService.getSpeechExerciseById(id);
        LessonDto lesson = lessonService.getLessonById(existingSpeechExercise.getLessonId());

        // Check if the instructor owns the course
        CourseDto course = courseService.getCourseById(lesson.getCourseId());
        boolean isAdmin = instructor.getRole().name().equals("ADMIN");
        boolean isCourseOwner = course.getInstructorId().equals(instructor.getId());

        if (!isAdmin && !isCourseOwner) {
            ApiResponseDto apiResponse = new ApiResponseDto(
                    false,
                    "You are not authorized to update this speech exercise",
                    null,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
        }

        speechExerciseDto.setId(id);
        speechExerciseDto.setLessonId(existingSpeechExercise.getLessonId());
        SpeechExerciseDto updatedSpeechExercise = speechExerciseService.updateSpeechExercise(id, speechExerciseDto);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Speech exercise updated successfully",
                updatedSpeechExercise,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @DeleteMapping("/instructor/speech-exercises/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponseDto> deleteSpeechExercise(@PathVariable Long id, Principal principal) {
        User instructor = userService.getUserEntityByUsername(principal.getName());
        SpeechExerciseDto existingSpeechExercise = speechExerciseService.getSpeechExerciseById(id);
        LessonDto lesson = lessonService.getLessonById(existingSpeechExercise.getLessonId());

        // Check if the instructor owns the course
        CourseDto course = courseService.getCourseById(lesson.getCourseId());
        boolean isAdmin = instructor.getRole().name().equals("ADMIN");
        boolean isCourseOwner = course.getInstructorId().equals(instructor.getId());

        if (!isAdmin && !isCourseOwner) {
            ApiResponseDto apiResponse = new ApiResponseDto(
                    false,
                    "You are not authorized to delete this speech exercise",
                    null,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
        }

        speechExerciseService.deleteSpeechExercise(id);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Speech exercise deleted successfully",
                null,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }
}
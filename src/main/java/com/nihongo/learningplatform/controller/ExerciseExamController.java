package com.nihongo.learningplatform.controller;

import com.nihongo.learningplatform.dto.*;
import com.nihongo.learningplatform.entity.User;
import com.nihongo.learningplatform.service.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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
public class ExerciseExamController {

    private final ExerciseService exerciseService;
    private final ExamService examService;
    private final CourseService courseService;
    private final UserService userService;
    private final EnrollmentService enrollmentService;
    private final UserProgressService userProgressService;
    private final AiGenerationService aiGenerationService;

    @Autowired
    public ExerciseExamController(ExerciseService exerciseService,
                                  ExamService examService,
                                  CourseService courseService,
                                  UserService userService,
                                  EnrollmentService enrollmentService,
                                  UserProgressService userProgressService,
                                  AiGenerationService aiGenerationService) {
        this.exerciseService = exerciseService;
        this.examService = examService;
        this.courseService = courseService;
        this.userService = userService;
        this.enrollmentService = enrollmentService;
        this.userProgressService = userProgressService;
        this.aiGenerationService = aiGenerationService;
    }

    // Student endpoints for exercises

    @GetMapping("/student/courses/{courseId}/exercises")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<ApiResponseDto> getExercisesByCourseForStudent(@PathVariable Long courseId, Principal principal) {
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

        List<ExerciseDto> exercises = exerciseService.getExercisesByCourse(courseId);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Exercises retrieved successfully",
                exercises,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/student/exercises/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<ApiResponseDto> getExerciseForStudent(@PathVariable Long id, Principal principal) {
        User user = userService.getUserEntityByUsername(principal.getName());
        ExerciseDto exercise = exerciseService.getExerciseById(id);

        // Check if the user is enrolled in the course or is the instructor or an admin
        CourseDto course = courseService.getCourseById(exercise.getCourseId());
        boolean isInstructor = course.getInstructorId().equals(user.getId());
        boolean isAdmin = user.getRole().name().equals("ADMIN");
        boolean isEnrolled = enrollmentService.isStudentEnrolledInCourse(user.getId(), exercise.getCourseId());

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
                "Exercise retrieved successfully",
                exercise,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PostMapping("/student/exercises/{id}/submit")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<ApiResponseDto> submitExercise(@PathVariable Long id,
                                                         @RequestBody Map<Long, List<Long>> answers,
                                                         Principal principal) {
        User user = userService.getUserEntityByUsername(principal.getName());
        ExerciseDto exercise = exerciseService.getExerciseById(id);

        // Check if the user is enrolled in the course
        boolean isEnrolled = enrollmentService.isStudentEnrolledInCourse(user.getId(), exercise.getCourseId());

        if (!isEnrolled && !user.getRole().name().equals("ADMIN") && !user.getRole().name().equals("INSTRUCTOR")) {
            ApiResponseDto apiResponse = new ApiResponseDto(
                    false,
                    "You are not enrolled in this course",
                    null,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
        }

        // Calculate score
        int totalQuestions = exercise.getQuestions().size();
        int correctAnswers = 0;

        for (QuestionDto question : exercise.getQuestions()) {
            List<Long> selectedAnswerIds = answers.getOrDefault(question.getId(), List.of());
            List<Long> correctAnswerIds = question.getAnswers().stream()
                    .filter(AnswerDto::isCorrect)
                    .map(AnswerDto::getId)
                    .toList();

            if (selectedAnswerIds.size() == correctAnswerIds.size() &&
                    selectedAnswerIds.containsAll(correctAnswerIds)) {
                correctAnswers++;
            }
        }

        int score = (totalQuestions > 0) ? (correctAnswers * 100) / totalQuestions : 0;

        // Save progress
        UserProgressDto progress = userProgressService.createOrUpdateExerciseProgress(
                user.getId(), id, score);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Exercise submitted successfully",
                Map.of("score", score, "progress", progress),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    // Student endpoints for exams

    @GetMapping("/student/courses/{courseId}/exams")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<ApiResponseDto> getExamsByCourseForStudent(@PathVariable Long courseId, Principal principal) {
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

        List<ExamDto> exams = examService.getExamsByCourse(courseId);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Exams retrieved successfully",
                exams,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/student/exams/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<ApiResponseDto> getExamForStudent(@PathVariable Long id, Principal principal) {
        User user = userService.getUserEntityByUsername(principal.getName());
        ExamDto exam = examService.getExamById(id);

        // Check if the user is enrolled in the course or is the instructor or an admin
        CourseDto course = courseService.getCourseById(exam.getCourseId());
        boolean isInstructor = course.getInstructorId().equals(user.getId());
        boolean isAdmin = user.getRole().name().equals("ADMIN");
        boolean isEnrolled = enrollmentService.isStudentEnrolledInCourse(user.getId(), exam.getCourseId());

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
                "Exam retrieved successfully",
                exam,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PostMapping("/student/exams/{id}/submit")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<ApiResponseDto> submitExam(@PathVariable Long id,
                                                     @RequestBody Map<Long, List<Long>> answers,
                                                     Principal principal) {
        User user = userService.getUserEntityByUsername(principal.getName());
        ExamDto exam = examService.getExamById(id);

        // Check if the user is enrolled in the course
        boolean isEnrolled = enrollmentService.isStudentEnrolledInCourse(user.getId(), exam.getCourseId());

        if (!isEnrolled && !user.getRole().name().equals("ADMIN") && !user.getRole().name().equals("INSTRUCTOR")) {
            ApiResponseDto apiResponse = new ApiResponseDto(
                    false,
                    "You are not enrolled in this course",
                    null,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
        }

        // Calculate score
        int totalQuestions = exam.getQuestions().size();
        int correctAnswers = 0;

        for (QuestionDto question : exam.getQuestions()) {
            List<Long> selectedAnswerIds = answers.getOrDefault(question.getId(), List.of());
            List<Long> correctAnswerIds = question.getAnswers().stream()
                    .filter(AnswerDto::isCorrect)
                    .map(AnswerDto::getId)
                    .toList();

            if (selectedAnswerIds.size() == correctAnswerIds.size() &&
                    selectedAnswerIds.containsAll(correctAnswerIds)) {
                correctAnswers++;
            }
        }

        int score = (totalQuestions > 0) ? (correctAnswers * 100) / totalQuestions : 0;
        boolean passed = score >= exam.getPassingScore();

        // Save progress
        UserProgressDto progress = userProgressService.createOrUpdateExamProgress(
                user.getId(), id, score);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                passed ? "Exam passed successfully" : "Exam completed",
                Map.of("score", score, "passed", passed, "progress", progress),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    // Instructor endpoints for exercises

    @PostMapping("/instructor/courses/{courseId}/exercises")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponseDto> createExercise(@PathVariable Long courseId,
                                                         @Valid @RequestBody ExerciseDto exerciseDto,
                                                         Principal principal) {
        // Verify that the course belongs to the instructor
        CourseDto course = courseService.getCourseById(courseId);
        User instructor = userService.getUserEntityByUsername(principal.getName());

        if (!course.getInstructorId().equals(instructor.getId()) && !instructor.getRole().name().equals("ADMIN")) {
            ApiResponseDto apiResponse = new ApiResponseDto(
                    false,
                    "You are not authorized to add exercises to this course",
                    null,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
        }

        exerciseDto.setCourseId(courseId);
        ExerciseDto createdExercise = exerciseService.createExercise(exerciseDto);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Exercise created successfully",
                createdExercise,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @PostMapping("/instructor/courses/{courseId}/generate-exercise")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponseDto> generateExercise(@PathVariable Long courseId,
                                                           @Valid @RequestBody AiGenerationRequestDto requestDto,
                                                           Principal principal) {
        // Verify that the course belongs to the instructor
        CourseDto course = courseService.getCourseById(courseId);
        User instructor = userService.getUserEntityByUsername(principal.getName());

        if (!course.getInstructorId().equals(instructor.getId()) && !instructor.getRole().name().equals("ADMIN")) {
            ApiResponseDto apiResponse = new ApiResponseDto(
                    false,
                    "You are not authorized to add exercises to this course",
                    null,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
        }

        requestDto.setCourseId(courseId);
        ExerciseDto generatedExercise = aiGenerationService.generateExercise(requestDto);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Exercise generated successfully",
                generatedExercise,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @PutMapping("/instructor/exercises/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponseDto> updateExercise(@PathVariable Long id,
                                                         @Valid @RequestBody ExerciseDto exerciseDto,
                                                         Principal principal) {
        // Verify that the exercise's course belongs to the instructor
        ExerciseDto existingExercise = exerciseService.getExerciseById(id);
        CourseDto course = courseService.getCourseById(existingExercise.getCourseId());
        User instructor = userService.getUserEntityByUsername(principal.getName());

        if (!course.getInstructorId().equals(instructor.getId()) && !instructor.getRole().name().equals("ADMIN")) {
            ApiResponseDto apiResponse = new ApiResponseDto(
                    false,
                    "You are not authorized to update this exercise",
                    null,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
        }

        exerciseDto.setId(id);
        exerciseDto.setCourseId(existingExercise.getCourseId());
        ExerciseDto updatedExercise = exerciseService.updateExercise(id, exerciseDto);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Exercise updated successfully",
                updatedExercise,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @DeleteMapping("/instructor/exercises/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponseDto> deleteExercise(@PathVariable Long id, Principal principal) {
        // Verify that the exercise's course belongs to the instructor
        ExerciseDto existingExercise = exerciseService.getExerciseById(id);
        CourseDto course = courseService.getCourseById(existingExercise.getCourseId());
        User instructor = userService.getUserEntityByUsername(principal.getName());

        if (!course.getInstructorId().equals(instructor.getId()) && !instructor.getRole().name().equals("ADMIN")) {
            ApiResponseDto apiResponse = new ApiResponseDto(
                    false,
                    "You are not authorized to delete this exercise",
                    null,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
        }

        exerciseService.deleteExercise(id);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Exercise deleted successfully",
                null,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    // Instructor endpoints for exams

    @PostMapping("/instructor/courses/{courseId}/exams")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponseDto> createExam(@PathVariable Long courseId,
                                                     @Valid @RequestBody ExamDto examDto,
                                                     Principal principal) {
        // Verify that the course belongs to the instructor
        CourseDto course = courseService.getCourseById(courseId);
        User instructor = userService.getUserEntityByUsername(principal.getName());

        if (!course.getInstructorId().equals(instructor.getId()) && !instructor.getRole().name().equals("ADMIN")) {
            ApiResponseDto apiResponse = new ApiResponseDto(
                    false,
                    "You are not authorized to add exams to this course",
                    null,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
        }

        examDto.setCourseId(courseId);
        ExamDto createdExam = examService.createExam(examDto);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Exam created successfully",
                createdExam,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @PostMapping("/instructor/courses/{courseId}/generate-exam")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponseDto> generateExam(@PathVariable Long courseId,
                                                       @Valid @RequestBody AiGenerationRequestDto requestDto,
                                                       Principal principal) {
        // Verify that the course belongs to the instructor
        CourseDto course = courseService.getCourseById(courseId);
        User instructor = userService.getUserEntityByUsername(principal.getName());

        if (!course.getInstructorId().equals(instructor.getId()) && !instructor.getRole().name().equals("ADMIN")) {
            ApiResponseDto apiResponse = new ApiResponseDto(
                    false,
                    "You are not authorized to add exams to this course",
                    null,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
        }

        requestDto.setCourseId(courseId);
        requestDto.setExam(true);
        ExamDto generatedExam = aiGenerationService.generateExam(requestDto);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Exam generated successfully",
                generatedExam,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @PutMapping("/instructor/exams/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponseDto> updateExam(@PathVariable Long id,
                                                     @Valid @RequestBody ExamDto examDto,
                                                     Principal principal) {
        // Verify that the exam's course belongs to the instructor
        ExamDto existingExam = examService.getExamById(id);
        CourseDto course = courseService.getCourseById(existingExam.getCourseId());
        User instructor = userService.getUserEntityByUsername(principal.getName());

        if (!course.getInstructorId().equals(instructor.getId()) && !instructor.getRole().name().equals("ADMIN")) {
            ApiResponseDto apiResponse = new ApiResponseDto(
                    false,
                    "You are not authorized to update this exam",
                    null,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
        }

        examDto.setId(id);
        examDto.setCourseId(existingExam.getCourseId());
        ExamDto updatedExam = examService.updateExam(id, examDto);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Exam updated successfully",
                updatedExam,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @DeleteMapping("/instructor/exams/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponseDto> deleteExam(@PathVariable Long id, Principal principal) {
        // Verify that the exam's course belongs to the instructor
        ExamDto existingExam = examService.getExamById(id);
        CourseDto course = courseService.getCourseById(existingExam.getCourseId());
        User instructor = userService.getUserEntityByUsername(principal.getName());

        if (!course.getInstructorId().equals(instructor.getId()) && !instructor.getRole().name().equals("ADMIN")) {
            ApiResponseDto apiResponse = new ApiResponseDto(
                    false,
                    "You are not authorized to delete this exam",
                    null,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
        }

        examService.deleteExam(id);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Exam deleted successfully",
                null,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }
}

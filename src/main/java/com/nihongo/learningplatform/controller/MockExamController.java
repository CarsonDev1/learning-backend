package com.nihongo.learningplatform.controller;

import com.nihongo.learningplatform.dto.ApiResponseDto;
import com.nihongo.learningplatform.dto.MockExamAttemptDto;
import com.nihongo.learningplatform.dto.MockExamDto;
import com.nihongo.learningplatform.entity.Course;
import com.nihongo.learningplatform.entity.User;
import com.nihongo.learningplatform.service.CourseService;
import com.nihongo.learningplatform.service.EnrollmentService;
import com.nihongo.learningplatform.service.LearningHistoryService;
import com.nihongo.learningplatform.service.MockExamService;
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
import java.util.Map;

@RestController
@RequestMapping("/api")
public class MockExamController {

    private final MockExamService mockExamService;
    private final UserService userService;
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;
    private final LearningHistoryService learningHistoryService;

    @Autowired
    public MockExamController(MockExamService mockExamService,
                              UserService userService,
                              CourseService courseService,
                              EnrollmentService enrollmentService,
                              LearningHistoryService learningHistoryService) {
        this.mockExamService = mockExamService;
        this.userService = userService;
        this.courseService = courseService;
        this.enrollmentService = enrollmentService;
        this.learningHistoryService = learningHistoryService;
    }

    // Public endpoints

    @GetMapping("/public/mock-exams/levels")
    public ResponseEntity<ApiResponseDto> getPublicMockExamLevels() {
        List<String> levels = List.of("N5", "N4", "N3", "N2", "N1");

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Mock exam levels retrieved successfully",
                levels,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    // Student endpoints

    @GetMapping("/student/mock-exams")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<ApiResponseDto> getAvailableMockExams(Principal principal) {
        User user = userService.getUserEntityByUsername(principal.getName());

        // Get general mock exams
        List<MockExamDto> generalExams = mockExamService.getGeneralMockExams();

        // Get mock exams for courses the student is enrolled in
        List<Long> enrolledCourseIds = enrollmentService.getEnrollmentsByStudent(user.getId()).stream()
                .map(enrollment -> enrollment.getCourseId())
                .toList();

        List<MockExamDto> courseExams = enrolledCourseIds.stream()
                .flatMap(courseId -> mockExamService.getMockExamsByCourse(courseId).stream())
                .toList();

        // Combine the lists
        List<MockExamDto> allExams = new java.util.ArrayList<>(generalExams);
        allExams.addAll(courseExams);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Available mock exams retrieved successfully",
                allExams,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/student/mock-exams/level/{level}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<ApiResponseDto> getMockExamsByLevel(@PathVariable String level) {
        List<MockExamDto> exams = mockExamService.getMockExamsByLevel(level);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Mock exams for level " + level + " retrieved successfully",
                exams,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/student/mock-exams/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<ApiResponseDto> getMockExamById(@PathVariable Long id, Principal principal) {
        User user = userService.getUserEntityByUsername(principal.getName());
        MockExamDto mockExam = mockExamService.getMockExamById(id);

        // Check if the mock exam is a course-specific exam
        if (mockExam.getCourseId() != null) {
            // Check if the user is enrolled in the course
            boolean isEnrolled = enrollmentService.isStudentEnrolledInCourse(user.getId(), mockExam.getCourseId());
            boolean isInstructor = false;
            boolean isAdmin = user.getRole().name().equals("ADMIN");

            // Check if the user is the instructor for this course
            try {
                Course course = courseService.getCourseEntityById(mockExam.getCourseId());
                isInstructor = course.getInstructor().getId().equals(user.getId());
            } catch (Exception e) {
                // Ignore any exceptions when checking instructor
            }

            if (!isEnrolled && !isInstructor && !isAdmin) {
                ApiResponseDto apiResponse = new ApiResponseDto(
                        false,
                        "You do not have access to this mock exam",
                        null,
                        LocalDateTime.now()
                );

                return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
            }
        }

        // Remove the questions from the response to prevent cheating
        MockExamDto response = new MockExamDto();
        response.setId(mockExam.getId());
        response.setTitle(mockExam.getTitle());
        response.setDescription(mockExam.getDescription());
        response.setTimeLimit(mockExam.getTimeLimit());
        response.setPassingScore(mockExam.getPassingScore());
        response.setLevel(mockExam.getLevel());
        response.setCourseId(mockExam.getCourseId());
        response.setCourseName(mockExam.getCourseName());

        // Get the student's previous attempts and highest score
        List<MockExamAttemptDto> attempts = mockExamService.getMockExamAttemptsByUserAndMockExam(user.getId(), id);
        Integer highestScore = mockExamService.getHighestScoreByUserAndMockExam(user.getId(), id);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Mock exam retrieved successfully",
                Map.of(
                        "mockExam", response,
                        "attempts", attempts,
                        "highestScore", highestScore
                ),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PostMapping("/student/mock-exams/{id}/start")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<ApiResponseDto> startMockExam(@PathVariable Long id, Principal principal) {
        User user = userService.getUserEntityByUsername(principal.getName());
        MockExamDto mockExam = mockExamService.getMockExamById(id);

        // Check if the mock exam is a course-specific exam
        if (mockExam.getCourseId() != null) {
            // Check if the user is enrolled in the course
            boolean isEnrolled = enrollmentService.isStudentEnrolledInCourse(user.getId(), mockExam.getCourseId());
            boolean isInstructor = false;
            boolean isAdmin = user.getRole().name().equals("ADMIN");

            // Check if the user is the instructor for this course
            try {
                Course course = courseService.getCourseEntityById(mockExam.getCourseId());
                isInstructor = course.getInstructor().getId().equals(user.getId());
            } catch (Exception e) {
                // Ignore any exceptions when checking instructor
            }

            if (!isEnrolled && !isInstructor && !isAdmin) {
                ApiResponseDto apiResponse = new ApiResponseDto(
                        false,
                        "You do not have access to this mock exam",
                        null,
                        LocalDateTime.now()
                );

                return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
            }
        }

        MockExamAttemptDto attempt = mockExamService.startMockExamAttempt(id, user.getId());

        // Get the full mock exam with questions
        MockExamDto fullExam = mockExamService.getMockExamById(id);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Mock exam started successfully",
                Map.of(
                        "attempt", attempt,
                        "mockExam", fullExam
                ),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PostMapping("/student/mock-exams/attempts/{attemptId}/submit")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<ApiResponseDto> submitMockExam(@PathVariable Long attemptId,
                                                         @RequestBody Map<Long, List<Long>> answers,
                                                         Principal principal) {
        User user = userService.getUserEntityByUsername(principal.getName());

        // Check if the attempt belongs to the user
        MockExamAttemptDto attempt = mockExamService.getMockExamAttemptById(attemptId);
        if (!attempt.getUserId().equals(user.getId())) {
            ApiResponseDto apiResponse = new ApiResponseDto(
                    false,
                    "You do not have permission to submit this mock exam attempt",
                    null,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
        }

        MockExamAttemptDto submittedAttempt = mockExamService.submitMockExamAttempt(attemptId, answers);

        // Record the mock exam completion in the learning history
        learningHistoryService.recordMockExamCompleted(
                user.getId(),
                submittedAttempt.getMockExamId(),
                submittedAttempt.getScore(),
                submittedAttempt.getPassed()
        );

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Mock exam submitted successfully",
                submittedAttempt,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/student/mock-exams/attempts")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<ApiResponseDto> getUserMockExamAttempts(Principal principal) {
        User user = userService.getUserEntityByUsername(principal.getName());
        List<MockExamAttemptDto> attempts = mockExamService.getMockExamAttemptsByUser(user.getId());

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Your mock exam attempts retrieved successfully",
                attempts,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/student/mock-exams/attempts/{attemptId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<ApiResponseDto> getMockExamAttemptById(@PathVariable Long attemptId, Principal principal) {
        User user = userService.getUserEntityByUsername(principal.getName());

        // Check if the attempt belongs to the user
        MockExamAttemptDto attempt = mockExamService.getMockExamAttemptById(attemptId);
        if (!attempt.getUserId().equals(user.getId()) && !user.getRole().name().equals("ADMIN")) {
            ApiResponseDto apiResponse = new ApiResponseDto(
                    false,
                    "You do not have permission to view this mock exam attempt",
                    null,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
        }

        // Get the mock exam
        MockExamDto mockExam = mockExamService.getMockExamById(attempt.getMockExamId());

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Mock exam attempt retrieved successfully",
                Map.of(
                        "attempt", attempt,
                        "mockExam", mockExam
                ),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    // Instructor and Admin endpoints

    @PostMapping("/admin/mock-exams")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto> createMockExam(@Valid @RequestBody MockExamDto mockExamDto) {
        MockExamDto createdMockExam = mockExamService.createMockExam(mockExamDto);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Mock exam created successfully",
                createdMockExam,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @PostMapping("/instructor/courses/{courseId}/mock-exams")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponseDto> createCourseMockExam(@PathVariable Long courseId,
                                                               @Valid @RequestBody MockExamDto mockExamDto,
                                                               Principal principal) {
        User instructor = userService.getUserEntityByUsername(principal.getName());

        // Verify that the instructor owns the course
        if (!instructor.getRole().name().equals("ADMIN")) {
            try {
                Course course = courseService.getCourseEntityById(courseId);
                if (!course.getInstructor().getId().equals(instructor.getId())) {
                    ApiResponseDto apiResponse = new ApiResponseDto(
                            false,
                            "You are not authorized to create mock exams for this course",
                            null,
                            LocalDateTime.now()
                    );

                    return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
                }
            } catch (Exception e) {
                ApiResponseDto apiResponse = new ApiResponseDto(
                        false,
                        "Course not found",
                        null,
                        LocalDateTime.now()
                );

                return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
            }
        }

        mockExamDto.setCourseId(courseId);
        MockExamDto createdMockExam = mockExamService.createMockExam(mockExamDto);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Mock exam created successfully",
                createdMockExam,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @GetMapping("/admin/mock-exams")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto> getAllMockExams() {
        List<MockExamDto> mockExams = mockExamService.getAllMockExams();

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "All mock exams retrieved successfully",
                mockExams,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/instructor/mock-exams/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponseDto> getInstructorMockExamById(@PathVariable Long id, Principal principal) {
        User instructor = userService.getUserEntityByUsername(principal.getName());
        MockExamDto mockExam = mockExamService.getMockExamById(id);

        // Check if the mock exam is a course-specific exam and belongs to the instructor
        if (mockExam.getCourseId() != null && !instructor.getRole().name().equals("ADMIN")) {
            try {
                Course course = courseService.getCourseEntityById(mockExam.getCourseId());
                if (!course.getInstructor().getId().equals(instructor.getId())) {
                    ApiResponseDto apiResponse = new ApiResponseDto(
                            false,
                            "You are not authorized to view this mock exam",
                            null,
                            LocalDateTime.now()
                    );

                    return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
                }
            } catch (Exception e) {
                ApiResponseDto apiResponse = new ApiResponseDto(
                        false,
                        "Course not found",
                        null,
                        LocalDateTime.now()
                );

                return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
            }
        }

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Mock exam retrieved successfully",
                mockExam,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PutMapping("/admin/mock-exams/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto> updateMockExam(@PathVariable Long id,
                                                         @Valid @RequestBody MockExamDto mockExamDto) {
        mockExamDto.setId(id);
        MockExamDto updatedMockExam = mockExamService.updateMockExam(id, mockExamDto);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Mock exam updated successfully",
                updatedMockExam,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @DeleteMapping("/admin/mock-exams/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto> deleteMockExam(@PathVariable Long id) {
        mockExamService.deleteMockExam(id);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Mock exam deleted successfully",
                null,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }
}
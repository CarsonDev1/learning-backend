package com.nihongo.learningplatform.controller;

import com.nihongo.learningplatform.dto.ApiResponseDto;
import com.nihongo.learningplatform.dto.ComboEnrollmentDto;
import com.nihongo.learningplatform.dto.CourseComboDto;
import com.nihongo.learningplatform.entity.User;
import com.nihongo.learningplatform.service.CourseComboService;
import com.nihongo.learningplatform.service.LearningHistoryService;
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
public class CourseComboController {

    private final CourseComboService courseComboService;
    private final UserService userService;
    private final LearningHistoryService learningHistoryService;

    @Autowired
    public CourseComboController(CourseComboService courseComboService,
                                 UserService userService,
                                 LearningHistoryService learningHistoryService) {
        this.courseComboService = courseComboService;
        this.userService = userService;
        this.learningHistoryService = learningHistoryService;
    }

    // Public endpoints

    @GetMapping("/public/combos")
    public ResponseEntity<ApiResponseDto> getPublicCourseCombos() {
        List<CourseComboDto> combos = courseComboService.getActiveCourseCombos();

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Course combos retrieved successfully",
                combos,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/public/combos/{id}")
    public ResponseEntity<ApiResponseDto> getPublicCourseComboById(@PathVariable Long id) {
        CourseComboDto combo = courseComboService.getCourseComboById(id);

        // Check if course combo is active
        if (!combo.isActive()) {
            ApiResponseDto apiResponse = new ApiResponseDto(
                    false,
                    "Course combo not available",
                    null,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
        }

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Course combo retrieved successfully",
                combo,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/public/courses/{courseId}/combos")
    public ResponseEntity<ApiResponseDto> getPublicCourseCombosByCourse(@PathVariable Long courseId) {
        List<CourseComboDto> combos = courseComboService.getCourseCombosByCourseId(courseId);

        // Filter only active combos
        combos = combos.stream()
                .filter(CourseComboDto::isActive)
                .toList();

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Course combos for course retrieved successfully",
                combos,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    // Student endpoints

    @GetMapping("/student/combos")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<ApiResponseDto> getStudentCombos(Principal principal) {
        User user = userService.getUserEntityByUsername(principal.getName());
        List<ComboEnrollmentDto> enrollments = courseComboService.getComboEnrollmentsByUser(user.getId());

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Your combo enrollments retrieved successfully",
                enrollments,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PostMapping("/student/combos/{comboId}/enroll")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<ApiResponseDto> enrollInCombo(@PathVariable Long comboId,
                                                        @RequestParam(required = false) Long paymentId,
                                                        Principal principal) {
        User user = userService.getUserEntityByUsername(principal.getName());

        // Check if student is already enrolled
        if (courseComboService.isUserEnrolledInCombo(user.getId(), comboId)) {
            ApiResponseDto apiResponse = new ApiResponseDto(
                    false,
                    "You are already enrolled in this course combo",
                    null,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
        }

        ComboEnrollmentDto enrollment = courseComboService.enrollUserInCombo(comboId, user.getId(), paymentId);

        // Record in learning history
        learningHistoryService.recordCourseEnrollment(user.getId(), null); // Use combo enrollment details in JSON

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Enrolled in course combo successfully",
                enrollment,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    // Admin endpoints

    @PostMapping("/admin/combos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto> createCourseCombo(@Valid @RequestBody CourseComboDto courseComboDto) {
        CourseComboDto createdCombo = courseComboService.createCourseCombo(courseComboDto);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Course combo created successfully",
                createdCombo,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @PutMapping("/admin/combos/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto> updateCourseCombo(@PathVariable Long id,
                                                            @Valid @RequestBody CourseComboDto courseComboDto) {
        CourseComboDto updatedCombo = courseComboService.updateCourseCombo(id, courseComboDto);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Course combo updated successfully",
                updatedCombo,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PutMapping("/admin/combos/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto> activateCourseCombo(@PathVariable Long id) {
        CourseComboDto activatedCombo = courseComboService.activateCourseCombo(id);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Course combo activated successfully",
                activatedCombo,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PutMapping("/admin/combos/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto> deactivateCourseCombo(@PathVariable Long id) {
        CourseComboDto deactivatedCombo = courseComboService.deactivateCourseCombo(id);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Course combo deactivated successfully",
                deactivatedCombo,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @DeleteMapping("/admin/combos/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto> deleteCourseCombo(@PathVariable Long id) {
        courseComboService.deleteCourseCombo(id);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Course combo deleted successfully",
                null,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/admin/enrollments/combo")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto> getAllComboEnrollments() {
        List<ComboEnrollmentDto> enrollments = courseComboService.getAllComboEnrollments();

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "All combo enrollments retrieved successfully",
                enrollments,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }
}

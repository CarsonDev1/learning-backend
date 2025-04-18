package com.nihongo.learningplatform.controller;

import com.nihongo.learningplatform.dto.*;
import com.nihongo.learningplatform.entity.User;
import com.nihongo.learningplatform.service.*;
import jakarta.servlet.http.HttpServletRequest;
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
import java.util.HashMap;


@RestController
@RequestMapping("/api")
public class EnrollmentPaymentController {

    private final EnrollmentService enrollmentService;
    private final PaymentService paymentService;
    private final ReviewService reviewService;
    private final CourseService courseService;
    private final UserService userService;

    @Autowired
    public EnrollmentPaymentController(EnrollmentService enrollmentService,
                                       PaymentService paymentService,
                                       ReviewService reviewService,
                                       CourseService courseService,
                                       UserService userService) {
        this.enrollmentService = enrollmentService;
        this.paymentService = paymentService;
        this.reviewService = reviewService;
        this.courseService = courseService;
        this.userService = userService;
    }

    // Student enrollment endpoints

    @GetMapping("/student/enrollments")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<ApiResponseDto> getStudentEnrollments(Principal principal) {
        User user = userService.getUserEntityByUsername(principal.getName());
        List<EnrollmentDto> enrollments = enrollmentService.getEnrollmentsByStudent(user.getId());

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Enrollments retrieved successfully",
                enrollments,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/student/courses/{courseId}/enrollment")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<ApiResponseDto> getEnrollmentForCourse(@PathVariable Long courseId, Principal principal) {
        User user = userService.getUserEntityByUsername(principal.getName());

        // Check if student is enrolled
        if (!enrollmentService.isStudentEnrolledInCourse(user.getId(), courseId)) {
            ApiResponseDto apiResponse = new ApiResponseDto(
                    false,
                    "You are not enrolled in this course",
                    null,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
        }

        EnrollmentDto enrollment = enrollmentService.getEnrollmentByStudentAndCourse(user.getId(), courseId);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Enrollment retrieved successfully",
                enrollment,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PostMapping("/student/courses/{courseId}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<ApiResponseDto> completeCourse(@PathVariable Long courseId, Principal principal) {
        User user = userService.getUserEntityByUsername(principal.getName());

        // Check if student is enrolled
        if (!enrollmentService.isStudentEnrolledInCourse(user.getId(), courseId)) {
            ApiResponseDto apiResponse = new ApiResponseDto(
                    false,
                    "You are not enrolled in this course",
                    null,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
        }

        EnrollmentDto enrollment = enrollmentService.getEnrollmentByStudentAndCourse(user.getId(), courseId);
        EnrollmentDto completedEnrollment = enrollmentService.completeEnrollment(enrollment.getId());

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Course marked as completed",
                completedEnrollment,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    // Payment endpoints

    @PostMapping("/student/courses/{courseId}/payment")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<ApiResponseDto> createPaymentUrl(@PathVariable Long courseId,
                                                           @Valid @RequestBody VnPayRequestDto requestDto,
                                                           Principal principal) {
        User user = userService.getUserEntityByUsername(principal.getName());

        // Check if student is already enrolled
        if (enrollmentService.isStudentEnrolledInCourse(user.getId(), courseId)) {
            ApiResponseDto apiResponse = new ApiResponseDto(
                    false,
                    "You are already enrolled in this course",
                    null,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
        }

        requestDto.setCourseId(courseId);
        String paymentUrl = paymentService.createVnPayPaymentUrl(requestDto);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Payment URL created successfully",
                Map.of("paymentUrl", paymentUrl),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/payment/callback")
    public ResponseEntity<ApiResponseDto> paymentCallback(HttpServletRequest request, Principal principal) {
        // Extract query parameters
        Map<String, String> queryParams = new HashMap<>();
        Map<String, String[]> parameterMap = request.getParameterMap();

        for (String key : parameterMap.keySet()) {
            String[] values = parameterMap.get(key);
            if (values != null && values.length > 0) {
                queryParams.put(key, values[0]);
            }
        }

        Map<String, String> result = paymentService.processVnPayReturn(queryParams);

        boolean success = "success".equals(result.get("status"));

        ApiResponseDto apiResponse = new ApiResponseDto(
                success,
                result.get("message"),
                result,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, success ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
    }

    // Review endpoints

    @PostMapping("/student/courses/{courseId}/reviews")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<ApiResponseDto> createOrUpdateReview(@PathVariable Long courseId,
                                                               @Valid @RequestBody ReviewDto reviewDto,
                                                               Principal principal) {
        User user = userService.getUserEntityByUsername(principal.getName());

        // Check if student is enrolled
        if (!enrollmentService.isStudentEnrolledInCourse(user.getId(), courseId)) {
            ApiResponseDto apiResponse = new ApiResponseDto(
                    false,
                    "You must be enrolled in this course to leave a review",
                    null,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
        }

        reviewDto.setUserId(user.getId());
        reviewDto.setCourseId(courseId);
        ReviewDto createdReview = reviewService.createReview(reviewDto);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Review submitted successfully",
                createdReview,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/public/courses/{courseId}/reviews")
    public ResponseEntity<ApiResponseDto> getCourseReviews(@PathVariable Long courseId) {
        List<ReviewDto> reviews = reviewService.getReviewsByCourse(courseId);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Course reviews retrieved successfully",
                reviews,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/student/courses/{courseId}/reviews/my")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<ApiResponseDto> getUserReviewForCourse(@PathVariable Long courseId, Principal principal) {
        User user = userService.getUserEntityByUsername(principal.getName());

        try {
            ReviewDto review = reviewService.getReviewByUserAndCourse(user.getId(), courseId);

            ApiResponseDto apiResponse = new ApiResponseDto(
                    true,
                    "Your review retrieved successfully",
                    review,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponseDto apiResponse = new ApiResponseDto(
                    false,
                    "You haven't reviewed this course yet",
                    null,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
        }
    }

    // Admin endpoints

    @GetMapping("/admin/enrollments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto> getAllEnrollments() {
        List<EnrollmentDto> enrollments = enrollmentService.getAllEnrollments();

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "All enrollments retrieved successfully",
                enrollments,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/admin/enrollments/course/{courseId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto> getEnrollmentsByCourse(@PathVariable Long courseId) {
        List<EnrollmentDto> enrollments = enrollmentService.getEnrollmentsByCourse(courseId);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Enrollments for course retrieved successfully",
                enrollments,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/admin/payments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto> getAllPayments() {
        List<PaymentDto> payments = paymentService.getAllPayments();

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "All payments retrieved successfully",
                payments,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }
}


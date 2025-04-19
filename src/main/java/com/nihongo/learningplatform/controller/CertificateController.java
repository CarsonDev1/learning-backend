package com.nihongo.learningplatform.controller;

import com.nihongo.learningplatform.dto.ApiResponseDto;
import com.nihongo.learningplatform.dto.CertificateDto;
import com.nihongo.learningplatform.entity.Course;
import com.nihongo.learningplatform.entity.User;
import com.nihongo.learningplatform.service.CertificateService;
import com.nihongo.learningplatform.service.CourseService;
import com.nihongo.learningplatform.service.EnrollmentService;
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
public class CertificateController {

    private final CertificateService certificateService;
    private final UserService userService;
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;

    @Autowired
    public CertificateController(CertificateService certificateService,
                               UserService userService,
                               CourseService courseService,
                               EnrollmentService enrollmentService) {
        this.certificateService = certificateService;
        this.userService = userService;
        this.courseService = courseService;
        this.enrollmentService = enrollmentService;
    }

    // Public endpoint to verify certificate

    @GetMapping("/public/certificates/verify/{certificateNumber}")
    public ResponseEntity<ApiResponseDto> verifyCertificate(@PathVariable String certificateNumber) {
        try {
            CertificateDto certificate = certificateService.getCertificateByCertificateNumber(certificateNumber);

            ApiResponseDto apiResponse = new ApiResponseDto(
                    true,
                    "Certificate is valid",
                    certificate,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponseDto apiResponse = new ApiResponseDto(
                    false,
                    "Certificate is not valid",
                    null,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        }
    }

    // Student endpoints

    @GetMapping("/student/certificates")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<ApiResponseDto> getUserCertificates(Principal principal) {
        User user = userService.getUserEntityByUsername(principal.getName());
        List<CertificateDto> certificates = certificateService.getCertificatesByUser(user.getId());

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Your certificates retrieved successfully",
                certificates,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/student/certificates/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<ApiResponseDto> getCertificateById(@PathVariable Long id, Principal principal) {
        User user = userService.getUserEntityByUsername(principal.getName());
        CertificateDto certificate = certificateService.getCertificateById(id);

        // Check if the certificate belongs to the user or the user is an admin
        boolean isAdmin = user.getRole().name().equals("ADMIN");
        if (!isAdmin && !certificate.getUserId().equals(user.getId())) {
            ApiResponseDto apiResponse = new ApiResponseDto(
                    false,
                    "You do not have permission to view this certificate",
                    null,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
        }

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Certificate retrieved successfully",
                certificate,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/student/certificates/{id}/download")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<ApiResponseDto> downloadCertificate(@PathVariable Long id, Principal principal) {
        User user = userService.getUserEntityByUsername(principal.getName());
        CertificateDto certificate = certificateService.getCertificateById(id);

        // Check if the certificate belongs to the user or the user is an admin
        boolean isAdmin = user.getRole().name().equals("ADMIN");
        if (!isAdmin && !certificate.getUserId().equals(user.getId())) {
            ApiResponseDto apiResponse = new ApiResponseDto(
                    false,
                    "You do not have permission to download this certificate",
                    null,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
        }

        String pdfUrl = certificateService.generateCertificatePdf(id);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Certificate PDF generated successfully",
                pdfUrl,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/student/courses/{courseId}/certificate")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<ApiResponseDto> getCertificateForCourse(@PathVariable Long courseId, Principal principal) {
        User user = userService.getUserEntityByUsername(principal.getName());

        try {
            CertificateDto certificate = certificateService.getCertificateByUserAndCourse(user.getId(), courseId);

            ApiResponseDto apiResponse = new ApiResponseDto(
                    true,
                    "Certificate retrieved successfully",
                    certificate,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponseDto apiResponse = new ApiResponseDto(
                    false,
                    "No certificate found for this course",
                    null,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
        }
    }

    // Admin and Instructor endpoints

    @PostMapping("/instructor/courses/{courseId}/certificates")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponseDto> issueCertificate(@PathVariable Long courseId,
                                                        @Valid @RequestBody CertificateDto certificateDto,
                                                        Principal principal) {
        User instructor = userService.getUserEntityByUsername(principal.getName());
        boolean isAdmin = instructor.getRole().name().equals("ADMIN");

        // Verify that the instructor owns the course
        if (!isAdmin) {
            try {
                Course course = courseService.getCourseEntityById(courseId);
                if (!course.getInstructor().getId().equals(instructor.getId())) {
                    ApiResponseDto apiResponse = new ApiResponseDto(
                            false,
                            "You are not authorized to issue certificates for this course",
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

        // Verify that the student is enrolled and has completed the course
        boolean isEnrolled = enrollmentService.isStudentEnrolledInCourse(certificateDto.getUserId(), courseId);
        if (!isEnrolled) {
            ApiResponseDto apiResponse = new ApiResponseDto(
                    false,
                    "Student is not enrolled in this course",
                    null,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
        }

        certificateDto.setCourseId(courseId);
        certificateDto.setCertificateNumber(certificateService.generateCertificateNumber());
        certificateDto.setIssuedDate(LocalDateTime.now());

        CertificateDto createdCertificate = certificateService.createCertificate(certificateDto);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Certificate issued successfully",
                createdCertificate,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @GetMapping("/admin/certificates")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto> getAllCertificates() {
        List<CertificateDto> certificates = certificateService.getAllCertificates();

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "All certificates retrieved successfully",
                certificates,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/admin/courses/{courseId}/certificates")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto> getCertificatesByCourse(@PathVariable Long courseId) {
        List<CertificateDto> certificates = certificateService.getCertificatesByCourse(courseId);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Certificates for course retrieved successfully",
                certificates,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/admin/users/{userId}/certificates")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto> getCertificatesByUser(@PathVariable Long userId) {
        List<CertificateDto> certificates = certificateService.getCertificatesByUser(userId);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Certificates for user retrieved successfully",
                certificates,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PutMapping("/admin/certificates/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto> updateCertificate(@PathVariable Long id,
                                                         @Valid @RequestBody CertificateDto certificateDto) {
        certificateDto.setId(id);
        CertificateDto updatedCertificate = certificateService.updateCertificate(id, certificateDto);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Certificate updated successfully",
                updatedCertificate,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @DeleteMapping("/admin/certificates/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto> deleteCertificate(@PathVariable Long id) {
        certificateService.deleteCertificate(id);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Certificate deleted successfully",
                null,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }
}
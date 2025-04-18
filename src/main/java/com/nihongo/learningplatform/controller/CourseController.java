package com.nihongo.learningplatform.controller;

import com.nihongo.learningplatform.dto.ApiResponseDto;
import com.nihongo.learningplatform.dto.CourseDto;
import com.nihongo.learningplatform.entity.User;
import com.nihongo.learningplatform.service.CourseService;
import com.nihongo.learningplatform.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Course Controller", description = "API endpoints for course management")
public class CourseController {

    private final CourseService courseService;
    private final UserService userService;

    @Autowired
    public CourseController(CourseService courseService, UserService userService) {
        this.courseService = courseService;
        this.userService = userService;
    }

    // Public endpoints

    @GetMapping("/public/courses")
    public ResponseEntity<ApiResponseDto> getPublicCourses() {
        List<CourseDto> courses = courseService.getApprovedAndActiveCourses();

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Public courses retrieved successfully",
                courses,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/public/courses/{id}")
    public ResponseEntity<ApiResponseDto> getPublicCourseById(@PathVariable Long id) {
        CourseDto course = courseService.getCourseById(id);

        // Check if course is approved and active
        if (!course.isApproved() || !course.isActive()) {
            ApiResponseDto apiResponse = new ApiResponseDto(
                    false,
                    "Course not available",
                    null,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
        }

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Course retrieved successfully",
                course,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/public/courses/level/{level}")
    public ResponseEntity<ApiResponseDto> getPublicCoursesByLevel(@PathVariable String level) {
        List<CourseDto> courses = courseService.getCoursesByLevel(level);

        // Filter only approved and active courses
        courses = courses.stream()
                .filter(course -> course.isApproved() && course.isActive())
                .toList();

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Courses with level " + level + " retrieved successfully",
                courses,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/public/courses/search")
    public ResponseEntity<ApiResponseDto> searchPublicCourses(@RequestParam String keyword) {
        List<CourseDto> courses = courseService.searchCourses(keyword);

        // Filter only approved and active courses
        courses = courses.stream()
                .filter(course -> course.isApproved() && course.isActive())
                .toList();

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Search results for: " + keyword,
                courses,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    // Instructor endpoints

    @PostMapping("/instructor/courses")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponseDto> createCourse(@Valid @RequestBody CourseDto courseDto, Principal principal) {
        User instructor = userService.getUserEntityByUsername(principal.getName());
        CourseDto createdCourse = courseService.createCourse(courseDto, instructor);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Course created successfully",
                createdCourse,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @GetMapping("/instructor/courses")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponseDto> getInstructorCourses(Principal principal) {
        User instructor = userService.getUserEntityByUsername(principal.getName());
        List<CourseDto> courses = courseService.getCoursesByInstructor(instructor);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Instructor courses retrieved successfully",
                courses,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PutMapping("/instructor/courses/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponseDto> updateCourse(@PathVariable Long id, @Valid @RequestBody CourseDto courseDto, Principal principal) {
        // Verify that the course belongs to the instructor
        CourseDto existingCourse = courseService.getCourseById(id);
        User instructor = userService.getUserEntityByUsername(principal.getName());

        if (!existingCourse.getInstructorId().equals(instructor.getId()) && !instructor.getRole().equals("ADMIN")) {
            ApiResponseDto apiResponse = new ApiResponseDto(
                    false,
                    "You are not authorized to update this course",
                    null,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
        }

        CourseDto updatedCourse = courseService.updateCourse(id, courseDto);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Course updated successfully",
                updatedCourse,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    // Admin endpoints

    @GetMapping("/admin/courses")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto> getAllCourses() {
        List<CourseDto> courses = courseService.getAllCourses();

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "All courses retrieved successfully",
                courses,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/admin/courses/unapproved")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto> getUnapprovedCourses() {
        List<CourseDto> courses = courseService.getUnapprovedCourses();

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Unapproved courses retrieved successfully",
                courses,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PutMapping("/admin/courses/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto> approveCourse(@PathVariable Long id) {
        CourseDto approvedCourse = courseService.approveCourse(id);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Course approved successfully",
                approvedCourse,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PutMapping("/admin/courses/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto> rejectCourse(@PathVariable Long id) {
        CourseDto rejectedCourse = courseService.rejectCourse(id);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Course rejected successfully",
                rejectedCourse,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PutMapping("/admin/courses/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto> activateCourse(@PathVariable Long id) {
        CourseDto activatedCourse = courseService.activateCourse(id);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Course activated successfully",
                activatedCourse,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PutMapping("/admin/courses/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto> deactivateCourse(@PathVariable Long id) {
        CourseDto deactivatedCourse = courseService.deactivateCourse(id);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Course deactivated successfully",
                deactivatedCourse,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @DeleteMapping("/admin/courses/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Course deleted successfully",
                null,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }
}
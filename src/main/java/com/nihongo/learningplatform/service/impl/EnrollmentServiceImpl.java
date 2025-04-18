package com.nihongo.learningplatform.service.impl;

import com.nihongo.learningplatform.dto.EnrollmentDto;
import com.nihongo.learningplatform.entity.Course;
import com.nihongo.learningplatform.entity.Enrollment;
import com.nihongo.learningplatform.entity.User;
import com.nihongo.learningplatform.exception.ResourceNotFoundException;
import com.nihongo.learningplatform.repository.EnrollmentRepository;
import com.nihongo.learningplatform.service.CourseService;
import com.nihongo.learningplatform.service.EnrollmentService;
import com.nihongo.learningplatform.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final UserService userService;
    private final CourseService courseService;

    @Autowired
    public EnrollmentServiceImpl(EnrollmentRepository enrollmentRepository,
                                 UserService userService,
                                 CourseService courseService) {
        this.enrollmentRepository = enrollmentRepository;
        this.userService = userService;
        this.courseService = courseService;
    }

    @Override
    @Transactional
    public EnrollmentDto createEnrollment(EnrollmentDto enrollmentDto) {
        User student = userService.getUserEntityById(enrollmentDto.getStudentId());
        Course course = courseService.getCourseEntityById(enrollmentDto.getCourseId());

        // Check if enrollment already exists
        if (enrollmentRepository.existsByStudentAndCourse(student, course)) {
            throw new IllegalStateException("Student is already enrolled in this course");
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setEnrolledAt(LocalDateTime.now());
        enrollment.setCompleted(false);

        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
        return mapToDto(savedEnrollment);
    }

    @Override
    @Transactional
    public EnrollmentDto createEnrollmentAfterPayment(Long courseId, Long studentId) {
        User student = userService.getUserEntityById(studentId);
        Course course = courseService.getCourseEntityById(courseId);

        // Check if enrollment already exists
        if (enrollmentRepository.existsByStudentAndCourse(student, course)) {
            throw new IllegalStateException("Student is already enrolled in this course");
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setEnrolledAt(LocalDateTime.now());
        enrollment.setCompleted(false);

        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
        return mapToDto(savedEnrollment);
    }

    @Override
    public EnrollmentDto getEnrollmentById(Long id) {
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with id: " + id));
        return mapToDto(enrollment);
    }

    @Override
    public List<EnrollmentDto> getEnrollmentsByStudent(Long studentId) {
        User student = userService.getUserEntityById(studentId);
        List<Enrollment> enrollments = enrollmentRepository.findByStudent(student);
        return enrollments.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<EnrollmentDto> getEnrollmentsByCourse(Long courseId) {
        Course course = courseService.getCourseEntityById(courseId);
        List<Enrollment> enrollments = enrollmentRepository.findByCourse(course);
        return enrollments.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public EnrollmentDto getEnrollmentByStudentAndCourse(Long studentId, Long courseId) {
        User student = userService.getUserEntityById(studentId);
        Course course = courseService.getCourseEntityById(courseId);

        Enrollment enrollment = enrollmentRepository.findByStudentAndCourse(student, course)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found for student id: " +
                        studentId + " and course id: " + courseId));

        return mapToDto(enrollment);
    }

    @Override
    public boolean isStudentEnrolledInCourse(Long studentId, Long courseId) {
        User student = userService.getUserEntityById(studentId);
        Course course = courseService.getCourseEntityById(courseId);
        return enrollmentRepository.existsByStudentAndCourse(student, course);
    }

    @Override
    @Transactional
    public EnrollmentDto completeEnrollment(Long id) {
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with id: " + id));

        enrollment.setCompleted(true);
        enrollment.setCompletedAt(LocalDateTime.now());

        Enrollment updatedEnrollment = enrollmentRepository.save(enrollment);
        return mapToDto(updatedEnrollment);
    }

    @Override
    @Transactional
    public void deleteEnrollment(Long id) {
        if (!enrollmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Enrollment not found with id: " + id);
        }
        enrollmentRepository.deleteById(id);
    }

    @Override
    public Enrollment getEnrollmentEntityById(Long id) {
        return enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with id: " + id));
    }

    // Helper method to map Enrollment entity to EnrollmentDto
    private EnrollmentDto mapToDto(Enrollment enrollment) {
        EnrollmentDto enrollmentDto = new EnrollmentDto();
        enrollmentDto.setId(enrollment.getId());
        enrollmentDto.setStudentId(enrollment.getStudent().getId());
        enrollmentDto.setCourseId(enrollment.getCourse().getId());
        enrollmentDto.setEnrolledAt(enrollment.getEnrolledAt());
        enrollmentDto.setCompleted(enrollment.isCompleted());
        enrollmentDto.setCompletedAt(enrollment.getCompletedAt());
        return enrollmentDto;
    }

    @Override
    public List<EnrollmentDto> getAllEnrollments() {
        List<Enrollment> enrollments = enrollmentRepository.findAll();
        return enrollments.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
}
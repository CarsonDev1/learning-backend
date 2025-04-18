package com.nihongo.learningplatform.service;

import com.nihongo.learningplatform.dto.EnrollmentDto;
import com.nihongo.learningplatform.entity.Enrollment;
import com.nihongo.learningplatform.entity.User;

import java.util.List;

public interface EnrollmentService {
    EnrollmentDto createEnrollment(EnrollmentDto enrollmentDto);
    EnrollmentDto createEnrollmentAfterPayment(Long courseId, Long studentId);
    EnrollmentDto getEnrollmentById(Long id);
    List<EnrollmentDto> getEnrollmentsByStudent(Long studentId);
    List<EnrollmentDto> getEnrollmentsByCourse(Long courseId);
    EnrollmentDto getEnrollmentByStudentAndCourse(Long studentId, Long courseId);
    boolean isStudentEnrolledInCourse(Long studentId, Long courseId);
    EnrollmentDto completeEnrollment(Long id);
    void deleteEnrollment(Long id);
    Enrollment getEnrollmentEntityById(Long id);
    List<EnrollmentDto> getAllEnrollments();
}
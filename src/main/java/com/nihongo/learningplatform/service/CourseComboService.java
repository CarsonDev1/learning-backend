package com.nihongo.learningplatform.service;

import com.nihongo.learningplatform.dto.ComboEnrollmentDto;
import com.nihongo.learningplatform.dto.CourseComboDto;
import com.nihongo.learningplatform.entity.CourseCombo;

import java.util.List;

public interface CourseComboService {
    CourseComboDto createCourseCombo(CourseComboDto courseComboDto);
    CourseComboDto getCourseComboById(Long id);
    List<CourseComboDto> getAllCourseCombos();
    List<CourseComboDto> getActiveCourseCombos();
    List<CourseComboDto> getCourseCombosByCourseId(Long courseId);
    CourseComboDto updateCourseCombo(Long id, CourseComboDto courseComboDto);
    CourseComboDto activateCourseCombo(Long id);
    CourseComboDto deactivateCourseCombo(Long id);
    void deleteCourseCombo(Long id);
    CourseCombo getCourseComboEntityById(Long id);

    // Enrollment management
    ComboEnrollmentDto enrollUserInCombo(Long comboId, Long userId, Long paymentId);
    ComboEnrollmentDto getComboEnrollmentById(Long id);
    ComboEnrollmentDto getComboEnrollmentByUserAndCombo(Long userId, Long comboId);
    List<ComboEnrollmentDto> getComboEnrollmentsByUser(Long userId);
    List<ComboEnrollmentDto> getComboEnrollmentsByCombo(Long comboId);
    List<ComboEnrollmentDto> getAllComboEnrollments(); // Added missing method
    ComboEnrollmentDto completeComboEnrollment(Long id);
    void processExpiredCombos();
    boolean isUserEnrolledInCombo(Long userId, Long comboId);
}
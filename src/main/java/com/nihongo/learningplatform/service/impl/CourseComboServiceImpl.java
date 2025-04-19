package com.nihongo.learningplatform.service.impl;

import com.nihongo.learningplatform.dto.ComboEnrollmentDto;
import com.nihongo.learningplatform.dto.CourseComboDto;
import com.nihongo.learningplatform.entity.CourseCombo;
import com.nihongo.learningplatform.entity.ComboEnrollment;
import com.nihongo.learningplatform.entity.Payment;
import com.nihongo.learningplatform.entity.User;
import com.nihongo.learningplatform.mapper.CourseComboMapper;
import com.nihongo.learningplatform.mapper.ComboEnrollmentMapper;
import com.nihongo.learningplatform.repository.CourseComboRepository;
import com.nihongo.learningplatform.repository.ComboEnrollmentRepository;
import com.nihongo.learningplatform.repository.PaymentRepository;
import com.nihongo.learningplatform.repository.UserRepository;
import com.nihongo.learningplatform.service.CourseComboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CourseComboServiceImpl implements CourseComboService {

    @Autowired
    private CourseComboRepository courseComboRepository;

    @Autowired
    private ComboEnrollmentRepository comboEnrollmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private CourseComboMapper courseComboMapper;

    @Autowired
    private ComboEnrollmentMapper comboEnrollmentMapper;

    @Override
    public CourseComboDto createCourseCombo(CourseComboDto dto) {
        CourseCombo combo = courseComboMapper.toEntity(dto);
        combo.setActive(true);
        CourseCombo saved = courseComboRepository.save(combo);
        return courseComboMapper.toDto(saved);
    }

    @Override
    public CourseComboDto getCourseComboById(Long id) {
        CourseCombo combo = getCourseComboEntityById(id);
        return courseComboMapper.toDto(combo);
    }

    @Override
    public List<CourseComboDto> getAllCourseCombos() {
        return courseComboRepository.findAll().stream()
                .map(courseComboMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CourseComboDto> getActiveCourseCombos() {
        return courseComboRepository.findByActiveTrue().stream()
                .map(courseComboMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CourseComboDto> getCourseCombosByCourseId(Long courseId) {
        return courseComboRepository.findByCourses_Id(courseId).stream()
                .map(courseComboMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CourseComboDto updateCourseCombo(Long id, CourseComboDto dto) {
        CourseCombo existing = getCourseComboEntityById(id);
        courseComboMapper.updateEntityFromDto(dto, existing);
        return courseComboMapper.toDto(courseComboRepository.save(existing));
    }

    @Override
    public CourseComboDto activateCourseCombo(Long id) {
        CourseCombo combo = getCourseComboEntityById(id);
        combo.setActive(true);
        return courseComboMapper.toDto(courseComboRepository.save(combo));
    }

    @Override
    public CourseComboDto deactivateCourseCombo(Long id) {
        CourseCombo combo = getCourseComboEntityById(id);
        combo.setActive(false);
        return courseComboMapper.toDto(courseComboRepository.save(combo));
    }

    @Override
    public void deleteCourseCombo(Long id) {
        courseComboRepository.deleteById(id);
    }

    @Override
    public CourseCombo getCourseComboEntityById(Long id) {
        return courseComboRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course combo not found with id " + id));
    }

    @Override
    public ComboEnrollmentDto enrollUserInCombo(Long comboId, Long userId, Long paymentId) {
        CourseCombo combo = getCourseComboEntityById(comboId);

        // Fetch User and Payment entities from their repositories
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id " + userId));

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with id " + paymentId));

        ComboEnrollment enrollment = new ComboEnrollment();
        enrollment.setCombo(combo);
        enrollment.setStudent(user);        // Now passing User object instead of Long
        enrollment.setPayment(payment);     // Now passing Payment object instead of Long
        enrollment.setEnrollmentDate(LocalDateTime.now());
        enrollment.setCompleted(false);

        ComboEnrollment saved = comboEnrollmentRepository.save(enrollment);
        return comboEnrollmentMapper.toDto(saved);
    }

    @Override
    public ComboEnrollmentDto getComboEnrollmentById(Long id) {
        ComboEnrollment enrollment = comboEnrollmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Enrollment not found with id " + id));
        return comboEnrollmentMapper.toDto(enrollment);
    }

    @Override
    public ComboEnrollmentDto getComboEnrollmentByUserAndCombo(Long userId, Long comboId) {
        Optional<ComboEnrollment> optional = comboEnrollmentRepository.findByStudentIdAndComboId(userId, comboId);
        return optional.map(comboEnrollmentMapper::toDto)
                .orElse(null);
    }

    @Override
    public List<ComboEnrollmentDto> getComboEnrollmentsByUser(Long userId) {
        return comboEnrollmentRepository.findByStudentId(userId).stream()
                .map(comboEnrollmentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ComboEnrollmentDto> getComboEnrollmentsByCombo(Long comboId) {
        return comboEnrollmentRepository.findByComboId(comboId).stream()
                .map(comboEnrollmentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ComboEnrollmentDto> getAllComboEnrollments() {
        return comboEnrollmentRepository.findAll().stream()
                .map(comboEnrollmentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public ComboEnrollmentDto completeComboEnrollment(Long id) {
        ComboEnrollment enrollment = comboEnrollmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Enrollment not found with id " + id));
        enrollment.setCompleted(true);
        return comboEnrollmentMapper.toDto(comboEnrollmentRepository.save(enrollment));
    }

    @Override
    public void processExpiredCombos() {
        List<ComboEnrollment> expired = comboEnrollmentRepository.findExpiredEnrollments(LocalDateTime.now());
        expired.forEach(enrollment -> enrollment.setCompleted(true));
        comboEnrollmentRepository.saveAll(expired);
    }

    @Override
    public boolean isUserEnrolledInCombo(Long userId, Long comboId) {
        return comboEnrollmentRepository.existsByStudentIdAndComboId(userId, comboId);
    }
}
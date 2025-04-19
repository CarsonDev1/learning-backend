package com.nihongo.learningplatform.repository;

import com.nihongo.learningplatform.entity.ComboEnrollment;
import com.nihongo.learningplatform.entity.CourseCombo;
import com.nihongo.learningplatform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ComboEnrollmentRepository extends JpaRepository<ComboEnrollment, Long> {
    Optional<ComboEnrollment> findByStudentIdAndComboId(Long userId, Long comboId);
    List<ComboEnrollment> findByStudentId(Long userId);
    List<ComboEnrollment> findByComboId(Long comboId);
    boolean existsByStudentIdAndComboId(Long userId, Long comboId);
    @Query("SELECT e FROM ComboEnrollment e WHERE e.expirationDate < :now AND e.completed = false")
    List<ComboEnrollment> findExpiredEnrollments(@Param("now") LocalDateTime now);
}
package com.nihongo.learningplatform.repository;

import com.nihongo.learningplatform.entity.Course;
import com.nihongo.learningplatform.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    Optional<Voucher> findByCode(String code);

    List<Voucher> findByActive(boolean active);

    @Query("SELECT v FROM Voucher v WHERE v.active = true AND v.validFrom <= ?1 AND v.validTo >= ?1 AND v.usageCount < v.maxUsage")
    List<Voucher> findAllValidVouchers(LocalDateTime now);

    List<Voucher> findByCourse(Course course);

    List<Voucher> findByCourseIsNull();

    @Query("SELECT v FROM Voucher v WHERE (v.course = ?1 OR v.course IS NULL) AND v.active = true AND v.validFrom <= ?2 AND v.validTo >= ?2 AND v.usageCount < v.maxUsage")
    List<Voucher> findValidVouchersForCourse(Course course, LocalDateTime now);
}
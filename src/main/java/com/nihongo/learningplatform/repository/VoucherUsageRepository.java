package com.nihongo.learningplatform.repository;

import com.nihongo.learningplatform.entity.Course;
import com.nihongo.learningplatform.entity.User;
import com.nihongo.learningplatform.entity.Voucher;
import com.nihongo.learningplatform.entity.VoucherUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoucherUsageRepository extends JpaRepository<VoucherUsage, Long> {
    List<VoucherUsage> findByUser(User user);

    List<VoucherUsage> findByVoucher(Voucher voucher);

    List<VoucherUsage> findByUserAndVoucher(User user, Voucher voucher);

    List<VoucherUsage> findByCourse(Course course);

    boolean existsByUserAndVoucher(User user, Voucher voucher);
}
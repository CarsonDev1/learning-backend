package com.nihongo.learningplatform.repository;

import com.nihongo.learningplatform.entity.Enrollment;
import com.nihongo.learningplatform.entity.Payment;
import com.nihongo.learningplatform.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByStatus(PaymentStatus status);
    Optional<Payment> findByTransactionId(String transactionId);
    Optional<Payment> findByEnrollment(Enrollment enrollment);
}

package com.nihongo.learningplatform.repository;

import com.nihongo.learningplatform.entity.Invoice;
import com.nihongo.learningplatform.entity.InvoiceStatus;
import com.nihongo.learningplatform.entity.Payment;
import com.nihongo.learningplatform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findByUser(User user);
    List<Invoice> findByStatus(InvoiceStatus status);
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    Optional<Invoice> findByPayment(Payment payment);
}
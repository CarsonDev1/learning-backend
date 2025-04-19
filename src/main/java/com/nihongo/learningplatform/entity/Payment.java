package com.nihongo.learningplatform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String invoiceRef;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String paymentMethodDetails;
    private String bankCode;
    private String cardType;

    @OneToOne(mappedBy = "payment")
    private Invoice invoice;

    @OneToOne(mappedBy = "payment")
    private ComboEnrollment comboEnrollment;

    @Column(nullable = false)
    private String transactionId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private String paymentMethod = "VNPAY";

    private LocalDateTime paymentDate;

    @OneToOne
    @JoinColumn(name = "enrollment_id", nullable = false)
    private Enrollment enrollment;
}
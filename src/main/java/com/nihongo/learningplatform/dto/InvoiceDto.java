package com.nihongo.learningplatform.dto;

import com.nihongo.learningplatform.entity.InvoiceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceDto {
    private Long id;

    private String invoiceNumber;

    private LocalDateTime issuedDate;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    @NotNull(message = "Tax amount is required")
    @Positive(message = "Tax amount must be positive")
    private BigDecimal taxAmount;

    @NotNull(message = "Total amount is required")
    @Positive(message = "Total amount must be positive")
    private BigDecimal totalAmount;

    private String customerName;

    private String customerEmail;

    private String customerAddress;

    private String customerTaxId;

    @NotNull(message = "Status is required")
    private InvoiceStatus status;

    private String pdfUrl;

    @NotNull(message = "User ID is required")
    private Long userId;

    private String username;

    private Long paymentId;

    private LocalDateTime createdAt;
}
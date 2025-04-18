package com.nihongo.learningplatform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoucherDto {
    private Long id;

    @NotBlank(message = "Voucher code is required")
    private String code;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Discount amount is required")
    @PositiveOrZero(message = "Discount amount must be positive or zero")
    private BigDecimal discountAmount;

    @NotNull(message = "Minimum purchase amount is required")
    @PositiveOrZero(message = "Minimum purchase amount must be positive or zero")
    private BigDecimal minimumPurchaseAmount;

    @NotNull(message = "Valid from date is required")
    private LocalDateTime validFrom;

    @NotNull(message = "Valid to date is required")
    private LocalDateTime validTo;

    @NotNull(message = "Maximum usage is required")
    @Positive(message = "Maximum usage must be positive")
    private int maxUsage;

    private int usageCount;
    private boolean active;
    private Long courseId;
    private String courseName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean valid;
}

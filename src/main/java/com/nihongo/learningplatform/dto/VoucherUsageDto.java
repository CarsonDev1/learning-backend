package com.nihongo.learningplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoucherUsageDto {
    private Long id;
    private Long voucherId;
    private String voucherCode;
    private Long userId;
    private String username;
    private Long courseId;
    private String courseName;
    private Long paymentId;
    private LocalDateTime usedAt;
}
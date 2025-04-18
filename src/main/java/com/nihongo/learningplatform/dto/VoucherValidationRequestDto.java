package com.nihongo.learningplatform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoucherValidationRequestDto {
    @NotBlank(message = "Voucher code is required")
    private String code;

    @NotNull(message = "Course ID is required")
    private Long courseId;
}

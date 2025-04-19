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
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseComboDto {
    private Long id;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Price is required")
    @PositiveOrZero(message = "Price must not be negative")
    private BigDecimal price;

    private String thumbnailUrl;

    private boolean active;

    @Positive(message = "Duration must be positive")
    private int durationDays;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private List<Long> courseIds;

    private List<CourseDto> courses;

    // Calculated savings compared to buying courses individually
    private BigDecimal regularTotalPrice;
    private BigDecimal savingsAmount;
    private Integer savingsPercentage;
}
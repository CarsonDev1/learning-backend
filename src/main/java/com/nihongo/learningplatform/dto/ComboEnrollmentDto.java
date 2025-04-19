package com.nihongo.learningplatform.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComboEnrollmentDto {
    private Long id;

    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotNull(message = "Combo ID is required")
    private Long comboId;

    private String comboTitle;

    private LocalDateTime enrolledAt;

    private LocalDateTime expiresAt;

    private boolean completed;

    private LocalDateTime completedAt;

    private Long paymentId;

    private List<CourseDto> includedCourses;
}
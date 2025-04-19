package com.nihongo.learningplatform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CertificateDto {
    private Long id;

    private String certificateNumber;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    private LocalDateTime issuedDate;

    private String pdfUrl;

    private String imageUrl;

    @NotNull(message = "User ID is required")
    private Long userId;

    private String username;

    @NotNull(message = "Course ID is required")
    private Long courseId;

    private String courseName;

    private Integer finalScore;

    private LocalDateTime createdAt;
}
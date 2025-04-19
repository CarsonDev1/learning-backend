package com.nihongo.learningplatform.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MockExamAttemptDto {
    private Long id;

    @NotNull(message = "User ID is required")
    private Long userId;

    private String username;

    @NotNull(message = "Mock exam ID is required")
    private Long mockExamId;

    private String mockExamTitle;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer score;

    private Boolean passed;

    // Map of questionId to list of selected answerIds
    private Map<Long, List<Long>> answers;
}

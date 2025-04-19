package com.nihongo.learningplatform.dto;

import com.nihongo.learningplatform.entity.ActivityType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LearningHistoryDto {
    private Long id;
    private Long userId;
    private String username;
    private ActivityType activityType;
    private LocalDateTime timestamp;
    private String details;
    private Long courseId;
    private String courseName;
    private Long moduleId;
    private String moduleName;
    private Long lessonId;
    private String lessonName;
    private Long exerciseId;
    private String exerciseName;
    private Long examId;
    private String examName;
    private Long mockExamId;
    private String mockExamName;
}
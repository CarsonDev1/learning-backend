package com.nihongo.learningplatform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "mock_exam_attempts")
public class MockExamAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "mock_exam_id", nullable = false)
    private MockExam mockExam;

    @Column(nullable = false)
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer score;

    private Boolean passed;

    @Column(columnDefinition = "TEXT")
    private String answers; // JSON format to store user answers

    @PrePersist
    protected void onCreate() {
        this.startTime = LocalDateTime.now();
    }
}
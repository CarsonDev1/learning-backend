package com.nihongo.learningplatform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "mock_exams")
public class MockExam {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false)
    private int timeLimit; // in minutes

    @Column(nullable = false)
    private int passingScore;

    @Column(nullable = false)
    private boolean isAiGenerated = false;

    @Column(nullable = false)
    private String level; // N5, N4, N3, N2, N1

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course; // Can be null for general mock exams

    @OneToMany(mappedBy = "mockExam", cascade = CascadeType.ALL)
    private Set<Question> questions = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
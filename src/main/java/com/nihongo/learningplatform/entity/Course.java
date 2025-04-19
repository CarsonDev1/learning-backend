package com.nihongo.learningplatform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "courses")
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int durationDays;
    private int totalLessons;
    private int totalExercises;
    private int totalExams;
    private String level; // N5, N4, N3, N2, N1
    private LocalDateTime startDate; // Ngày bắt đầu khóa học
    private LocalDateTime endDate; // Ngày kết thúc khóa học (nếu có)
    private boolean requiresCertificate = false; // Khóa học có cấp chứng chỉ không
    private boolean hasMockExam = false; // Khóa học có bài thi thử không

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Module> modules = new HashSet<>();

    @OneToMany(mappedBy = "course")
    private Set<MockExam> mockExams = new HashSet<>();

    @OneToMany(mappedBy = "course")
    private Set<Certificate> certificates = new HashSet<>();

    @ManyToMany(mappedBy = "courses")
    private Set<CourseCombo> combos = new HashSet<>();


    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private String thumbnailUrl;

    private String thumbnailPublicId;

    @Column(nullable = false)
    private boolean approved = false;

    @Column(nullable = false)
    private boolean active = true;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "instructor_id", nullable = false)
    private User instructor;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    private Set<Lesson> lessons = new HashSet<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    private Set<Exercise> exercises = new HashSet<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    private Set<Exam> exams = new HashSet<>();

    @OneToMany(mappedBy = "course")
    private Set<Enrollment> enrollments = new HashSet<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    private Set<Review> reviews = new HashSet<>();

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
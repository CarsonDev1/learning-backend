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
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String profilePictureUrl;
    private String profilePicturePublicId;
    private String phoneNumber;
    private String address;
    private LocalDateTime lastLoginDate;
    private String resetPasswordToken;

    @OneToMany(mappedBy = "user")
    private Set<LearningHistory> learningHistory = new HashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<Certificate> certificates = new HashSet<>();

    @OneToMany(mappedBy = "student")
    private Set<ComboEnrollment> comboEnrollments = new HashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<MockExamAttempt> mockExamAttempts = new HashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<Invoice> invoices = new HashSet<>();

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    private String fullName;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole role;

    private boolean active = true;

    private boolean blocked = false;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String language = "vi"; // Default language: Vietnamese

    @OneToMany(mappedBy = "student")
    private Set<Enrollment> enrollments = new HashSet<>();

    @OneToMany(mappedBy = "instructor")
    private Set<Course> courses = new HashSet<>();

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


package com.nihongo.learningplatform.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Getter
@Setter
@Table(name = "combo_enrollments")
public class ComboEnrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime enrollmentDate;

    @Column(nullable = false)
    private LocalDateTime expirationDate;

    private boolean completed = false;

    private LocalDateTime completedAt;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne
    @JoinColumn(name = "combo_id", nullable = false)
    private CourseCombo combo;

    @OneToOne
    @JoinColumn(name = "payment_id")
    private Payment payment;

    @PrePersist
    protected void onCreate() {
        this.enrollmentDate = LocalDateTime.now();
        if (this.combo != null && this.combo.getDurationDays() > 0) {
            this.expirationDate = this.enrollmentDate.plusDays(this.combo.getDurationDays());
        }
    }
}
package com.nihongo.learningplatform.repository;

import com.nihongo.learningplatform.entity.Certificate;
import com.nihongo.learningplatform.entity.Course;
import com.nihongo.learningplatform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    List<Certificate> findByUser(User user);
    List<Certificate> findByCourse(Course course);
    Optional<Certificate> findByUserAndCourse(User user, Course course);
    Optional<Certificate> findByCertificateNumber(String certificateNumber);
}
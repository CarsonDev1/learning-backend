package com.nihongo.learningplatform.repository;

import com.nihongo.learningplatform.entity.PasswordResetToken;
import com.nihongo.learningplatform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    Optional<PasswordResetToken> findByUser(User user);

    @Modifying
    int deleteByExpiryDateBeforeOrUsed(LocalDateTime dateTime, boolean used);
}
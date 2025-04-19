package com.nihongo.learningplatform.service;

import com.nihongo.learningplatform.dto.PasswordResetDto;
import com.nihongo.learningplatform.dto.PasswordResetRequestDto;
import com.nihongo.learningplatform.entity.PasswordResetToken;
import com.nihongo.learningplatform.entity.User;

import java.util.Optional;

public interface PasswordResetService {
    void createPasswordResetTokenForUser(User user, String token);
    String validatePasswordResetToken(String token);
    Optional<User> getUserByPasswordResetToken(String token);
    void resetPassword(PasswordResetDto passwordResetDto);
    void requestPasswordReset(PasswordResetRequestDto requestDto);
    void deleteExpiredTokens();
}
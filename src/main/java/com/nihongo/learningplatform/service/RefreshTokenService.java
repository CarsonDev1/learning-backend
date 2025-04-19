package com.nihongo.learningplatform.service;

import com.nihongo.learningplatform.entity.RefreshToken;
import com.nihongo.learningplatform.entity.User;
import com.nihongo.learningplatform.exception.TokenRefreshException;

import java.util.Optional;

public interface RefreshTokenService {
    Optional<RefreshToken> findByToken(String token);
    RefreshToken createRefreshToken(Long userId);
    RefreshToken verifyExpiration(RefreshToken token) throws TokenRefreshException;
    int deleteByUserId(Long userId);
    void deleteExpiredTokens();
}
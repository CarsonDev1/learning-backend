package com.nihongo.learningplatform.service.impl;

import com.nihongo.learningplatform.entity.RefreshToken;
import com.nihongo.learningplatform.entity.User;
import com.nihongo.learningplatform.exception.TokenRefreshException;
import com.nihongo.learningplatform.repository.RefreshTokenRepository;
import com.nihongo.learningplatform.service.RefreshTokenService;
import com.nihongo.learningplatform.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

    @Value("${jwt.refreshExpiration}")
    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserService userService;

    @Autowired
    public RefreshTokenServiceImpl(RefreshTokenRepository refreshTokenRepository, UserService userService) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userService = userService;
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Override
    public RefreshToken createRefreshToken(Long userId) {
        // Delete any existing refresh tokens for this user
        refreshTokenRepository.deleteByUser(userService.getUserEntityById(userId));

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(userService.getUserEntityById(userId));
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setToken(UUID.randomUUID().toString());

        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException(token.getToken(), "Refresh token was expired. Please make a new login.");
        }

        return token;
    }

    @Override
    @Transactional
    public int deleteByUserId(Long userId) {
        User user = userService.getUserEntityById(userId);
        return refreshTokenRepository.deleteByUser(user);
    }

    @Override
    @Transactional
    public void deleteExpiredTokens() {
        refreshTokenRepository.deleteByExpiryDateBefore(Instant.now());
    }
}
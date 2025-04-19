package com.nihongo.learningplatform.config;

import com.nihongo.learningplatform.service.PasswordResetService;
import com.nihongo.learningplatform.service.RefreshTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class ScheduledTasks {

    private final RefreshTokenService refreshTokenService;
    private final PasswordResetService passwordResetService;

    @Autowired
    public ScheduledTasks(RefreshTokenService refreshTokenService,
                          PasswordResetService passwordResetService) {
        this.refreshTokenService = refreshTokenService;
        this.passwordResetService = passwordResetService;
    }

    @Scheduled(cron = "${app.scheduled.refresh-token-cleanup}")
    public void cleanupExpiredRefreshTokens() {
        refreshTokenService.deleteExpiredTokens();
    }

    @Scheduled(cron = "${app.scheduled.password-reset-token-cleanup}")
    public void cleanupExpiredPasswordResetTokens() {
        passwordResetService.deleteExpiredTokens();
    }
}
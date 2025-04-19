package com.nihongo.learningplatform.service;

import com.nihongo.learningplatform.dto.*;

public interface AuthService {
    JwtResponseDto login(LoginDto loginDto);
    JwtResponseDto register(UserRegistrationDto registrationDto);
    void changeLanguage(String username, String language);

    RefreshTokenResponseDto refreshToken(RefreshTokenRequestDto request);
    void requestPasswordReset(PasswordResetRequestDto request);
    void resetPassword(PasswordResetDto resetDto);
    void changePassword(String username, ChangePasswordDto changePasswordDto);
    void logout(String username);
}
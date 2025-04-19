package com.nihongo.learningplatform.controller;

import com.nihongo.learningplatform.dto.*;
import com.nihongo.learningplatform.service.AuthService;
import com.nihongo.learningplatform.service.LearningHistoryService;
import com.nihongo.learningplatform.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
public class AuthControllerExtended {

    private final AuthService authService;
    private final UserService userService;
    private final LearningHistoryService learningHistoryService;

    @Autowired
    public AuthControllerExtended(AuthService authService,
                                  UserService userService,
                                  LearningHistoryService learningHistoryService) {
        this.authService = authService;
        this.userService = userService;
        this.learningHistoryService = learningHistoryService;
    }

    @PostMapping("/refreshtoken")
    public ResponseEntity<ApiResponseDto> refreshToken(@Valid @RequestBody RefreshTokenRequestDto request) {
        RefreshTokenResponseDto response = authService.refreshToken(request);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Token refreshed successfully",
                response,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponseDto> forgotPassword(@Valid @RequestBody PasswordResetRequestDto request) {
        authService.requestPasswordReset(request);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Password reset email sent successfully",
                null,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponseDto> resetPassword(@Valid @RequestBody PasswordResetDto resetDto) {
        authService.resetPassword(resetDto);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Password reset successfully",
                null,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponseDto> changePassword(@Valid @RequestBody ChangePasswordDto changePasswordDto,
                                                         Principal principal) {
        authService.changePassword(principal.getName(), changePasswordDto);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Password changed successfully",
                null,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponseDto> logout(Principal principal) {
        // Get user ID for learning history
        Long userId = userService.getUserByUsername(principal.getName()).getId();

        // Process logout
        authService.logout(principal.getName());

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Logged out successfully",
                null,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }
}
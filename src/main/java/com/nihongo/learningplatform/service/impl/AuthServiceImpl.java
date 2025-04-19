package com.nihongo.learningplatform.service.impl;

import com.nihongo.learningplatform.dto.*;
import com.nihongo.learningplatform.entity.User;
import com.nihongo.learningplatform.exception.BadRequestException;
import com.nihongo.learningplatform.exception.TokenRefreshException;
import com.nihongo.learningplatform.security.JwtTokenProvider;
import com.nihongo.learningplatform.security.UserDetailsImpl;
import com.nihongo.learningplatform.service.AuthService;
import com.nihongo.learningplatform.service.PasswordResetService;
import com.nihongo.learningplatform.service.RefreshTokenService;
import com.nihongo.learningplatform.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final PasswordResetService passwordResetService;

    @Autowired
    public AuthServiceImpl(AuthenticationManager authenticationManager,
                           JwtTokenProvider tokenProvider,
                           UserService userService,
                           PasswordEncoder passwordEncoder,
                           RefreshTokenService refreshTokenService,
                           PasswordResetService passwordResetService) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
        this.passwordResetService = passwordResetService;
    }


    @Override
    public RefreshTokenResponseDto refreshToken(RefreshTokenRequestDto request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(refreshToken -> {
                    User user = refreshToken.getUser();
                    String newAccessToken = tokenProvider.generateTokenFromUsername(user.getUsername());
                    return new RefreshTokenResponseDto(newAccessToken, requestRefreshToken, "Bearer");
                })
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken, "Refresh token is not in database!"));
    }

    @Override
    public void requestPasswordReset(PasswordResetRequestDto request) {
        passwordResetService.requestPasswordReset(request);
    }

    @Override
    public void resetPassword(PasswordResetDto resetDto) {
        passwordResetService.resetPassword(resetDto);
    }

    @Override
    @Transactional
    public void changePassword(String username, ChangePasswordDto changePasswordDto) {
        User user = userService.getUserEntityByUsername(username);

        // Check if current password matches
        if (!passwordEncoder.matches(changePasswordDto.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        // Validate new password confirmation
        if (!changePasswordDto.getNewPassword().equals(changePasswordDto.getPasswordConfirm())) {
            throw new BadRequestException("Password confirmation doesn't match");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(changePasswordDto.getNewPassword()));
        userService.saveUser(user);

        // Invalidate all refresh tokens
        refreshTokenService.deleteByUserId(user.getId());
    }

    @Override
    @Transactional
    public void logout(String username) {
        User user = userService.getUserEntityByUsername(username);
        refreshTokenService.deleteByUserId(user.getId());
    }

    @Override
    public JwtResponseDto login(LoginDto loginDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateTokenFromUsername(String.valueOf(authentication));

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        return new JwtResponseDto(
                jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                userDetails.getRole(),
                userDetails.getLanguage()
        );
    }

    @Override
    @Transactional
    public JwtResponseDto register(UserRegistrationDto registrationDto) {
        // Validate password confirmation
        if (!registrationDto.getPassword().equals(registrationDto.getPasswordConfirm())) {
            throw new BadRequestException("Password confirmation doesn't match");
        }

        // Register user
        UserDto newUser = userService.registerUser(registrationDto);

        // Login the new user
        LoginDto loginDto = new LoginDto();
        loginDto.setUsername(registrationDto.getUsername());
        loginDto.setPassword(registrationDto.getPassword());

        return login(loginDto);
    }

    @Override
    @Transactional
    public void changeLanguage(String username, String language) {
        User user = userService.getUserEntityByUsername(username);
        user.setLanguage(language);
        userService.saveUser(user);
    }
}
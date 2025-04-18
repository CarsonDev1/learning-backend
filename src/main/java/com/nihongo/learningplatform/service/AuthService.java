package com.nihongo.learningplatform.service;

import com.nihongo.learningplatform.dto.JwtResponseDto;
import com.nihongo.learningplatform.dto.LoginDto;
import com.nihongo.learningplatform.dto.UserRegistrationDto;

public interface AuthService {
    JwtResponseDto login(LoginDto loginDto);
    JwtResponseDto register(UserRegistrationDto registrationDto);
    void changeLanguage(String username, String language);
}
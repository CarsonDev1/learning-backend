package com.nihongo.learningplatform.controller;

import com.nihongo.learningplatform.dto.ApiResponseDto;
import com.nihongo.learningplatform.dto.JwtResponseDto;
import com.nihongo.learningplatform.dto.LoginDto;
import com.nihongo.learningplatform.dto.UserRegistrationDto;
import com.nihongo.learningplatform.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponseDto> login(@Valid @RequestBody LoginDto loginDto) {
        JwtResponseDto response = authService.login(loginDto);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Login successful",
                response,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponseDto> register(@Valid @RequestBody UserRegistrationDto registrationDto) {
        JwtResponseDto response = authService.register(registrationDto);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Registration successful",
                response,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @PutMapping("/language/{language}")
    public ResponseEntity<ApiResponseDto> changeLanguage(@PathVariable String language, Principal principal) {
        authService.changeLanguage(principal.getName(), language);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Language changed successfully",
                null,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }
}
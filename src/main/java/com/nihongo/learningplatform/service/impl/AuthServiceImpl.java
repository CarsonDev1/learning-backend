package com.nihongo.learningplatform.service.impl;

import com.nihongo.learningplatform.dto.JwtResponseDto;
import com.nihongo.learningplatform.dto.LoginDto;
import com.nihongo.learningplatform.dto.UserDto;
import com.nihongo.learningplatform.dto.UserRegistrationDto;
import com.nihongo.learningplatform.entity.User;
import com.nihongo.learningplatform.exception.BadRequestException;
import com.nihongo.learningplatform.security.JwtTokenProvider;
import com.nihongo.learningplatform.security.UserDetailsImpl;
import com.nihongo.learningplatform.service.AuthService;
import com.nihongo.learningplatform.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;

    @Autowired
    public AuthServiceImpl(AuthenticationManager authenticationManager,
                           JwtTokenProvider tokenProvider,
                           UserService userService) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.userService = userService;
    }

    @Override
    public JwtResponseDto login(LoginDto loginDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

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
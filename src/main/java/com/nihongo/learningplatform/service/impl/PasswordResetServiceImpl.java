package com.nihongo.learningplatform.service.impl;

import com.nihongo.learningplatform.dto.PasswordResetDto;
import com.nihongo.learningplatform.dto.PasswordResetRequestDto;
import com.nihongo.learningplatform.entity.PasswordResetToken;
import com.nihongo.learningplatform.entity.User;
import com.nihongo.learningplatform.exception.BadRequestException;
import com.nihongo.learningplatform.exception.ResourceNotFoundException;
import com.nihongo.learningplatform.repository.PasswordResetTokenRepository;
import com.nihongo.learningplatform.repository.UserRepository;
import com.nihongo.learningplatform.service.PasswordResetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetServiceImpl implements PasswordResetService {

    @Value("${app.password-reset-token.expiration}")
    private Long tokenExpirationMs;

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    @Autowired
    public PasswordResetServiceImpl(PasswordResetTokenRepository passwordResetTokenRepository,
                                    UserRepository userRepository,
                                    PasswordEncoder passwordEncoder,
                                    JavaMailSender mailSender) {
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
    }

    @Override
    public void createPasswordResetTokenForUser(User user, String token) {
        // Delete any existing tokens for this user
        passwordResetTokenRepository.findByUser(user).ifPresent(passwordResetTokenRepository::delete);

        PasswordResetToken myToken = new PasswordResetToken();
        myToken.setToken(token);
        myToken.setUser(user);
        myToken.setExpiryDate(LocalDateTime.now().plus(tokenExpirationMs, ChronoUnit.MILLIS));
        myToken.setUsed(false);

        passwordResetTokenRepository.save(myToken);
    }

    @Override
    public String validatePasswordResetToken(String token) {
        Optional<PasswordResetToken> passwordResetToken = passwordResetTokenRepository.findByToken(token);

        if (passwordResetToken.isEmpty()) {
            return "invalidToken";
        }

        PasswordResetToken passToken = passwordResetToken.get();

        if (passToken.isUsed()) {
            return "usedToken";
        }

        if (passToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            return "expiredToken";
        }

        return "valid";
    }

    @Override
    public Optional<User> getUserByPasswordResetToken(String token) {
        return passwordResetTokenRepository.findByToken(token)
                .map(PasswordResetToken::getUser);
    }

    @Override
    @Transactional
    public void resetPassword(PasswordResetDto passwordResetDto) {
        String result = validatePasswordResetToken(passwordResetDto.getToken());

        if (!"valid".equals(result)) {
            throw new BadRequestException("Invalid or expired password reset token");
        }

        if (!passwordResetDto.getPassword().equals(passwordResetDto.getPasswordConfirm())) {
            throw new BadRequestException("Password confirmation doesn't match");
        }

        Optional<User> user = getUserByPasswordResetToken(passwordResetDto.getToken());

        if (user.isEmpty()) {
            throw new ResourceNotFoundException("User not found");
        }

        // Update password
        User resetUser = user.get();
        resetUser.setPassword(passwordEncoder.encode(passwordResetDto.getPassword()));
        userRepository.save(resetUser);

        // Mark token as used
        PasswordResetToken token = passwordResetTokenRepository.findByToken(passwordResetDto.getToken()).get();
        token.setUsed(true);
        passwordResetTokenRepository.save(token);
    }

    @Override
    public void requestPasswordReset(PasswordResetRequestDto requestDto) {
        Optional<User> user = userRepository.findByEmail(requestDto.getEmail());

        if (user.isEmpty()) {
            // Don't reveal that the email doesn't exist
            return;
        }

        String token = UUID.randomUUID().toString();
        createPasswordResetTokenForUser(user.get(), token);

        // Send email with reset link
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply@nihongolearning.com");
        message.setTo(requestDto.getEmail());
        message.setSubject("Password Reset Request");
        message.setText("To reset your password, click the link below:\n\n" +
                "http://localhost:8080/reset-password?token=" + token + "\n\n" +
                "This link will expire in 24 hours.");

        mailSender.send(message);
    }

    @Override
    @Transactional
    public void deleteExpiredTokens() {
        passwordResetTokenRepository.deleteByExpiryDateBeforeOrUsed(LocalDateTime.now(), true);
    }
}
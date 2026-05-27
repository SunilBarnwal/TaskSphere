package com.tasksphere.service;

import com.tasksphere.model.PasswordResetToken;
import com.tasksphere.repository.PasswordResetRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PasswordResetService {

    private final PasswordResetRepository repo;
    private final EmailService emailService;

    public PasswordResetService(PasswordResetRepository repo, EmailService emailService) {
        this.repo = repo;
        this.emailService = emailService;
    }

    // 🔹 Send reset link
    public void sendResetLink(String email) {

        String token = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(15);

        repo.save(email, token, expiry);

        String link = "http://localhost:9090/reset-password?token=" + token;

        emailService.send(email, "Reset Password", "Click: " + link);
    }

    // 🔹 Validate token
    public String validateToken(String token) {

        PasswordResetToken t = repo.findByToken(token);

        if (t == null || t.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expired");
        }

        return t.getEmail();
    }

    // 🔹 Delete single token
    public void deleteToken(String token) {
        repo.delete(token);
    }

    // 🔥 Auto delete expired tokens
    @Scheduled(fixedRate = 600000) // हर 10 मिनट
    public void deleteExpiredTokens() {
        repo.deleteExpired();
    }
}
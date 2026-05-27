package com.tasksphere.repository;

import com.tasksphere.model.PasswordResetToken;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public class PasswordResetRepository {

    private final JdbcTemplate jdbcTemplate;

    public PasswordResetRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(String email, String token, LocalDateTime expiry) {
        jdbcTemplate.update(
                "INSERT INTO password_reset_token (email, token, expiry_time) VALUES (?, ?, ?)",
                email, token, expiry
        );
    }

    public PasswordResetToken findByToken(String token) {
        return jdbcTemplate.query(
                "SELECT * FROM password_reset_token WHERE token = ?",
                (rs, rowNum) -> {
                    PasswordResetToken t = new PasswordResetToken();
                    t.setId(rs.getLong("id"));
                    t.setEmail(rs.getString("email"));
                    t.setToken(rs.getString("token"));
                    t.setExpiryTime(rs.getTimestamp("expiry_time").toLocalDateTime());
                    return t;
                },
                token
        ).stream().findFirst().orElse(null);
    }

    public void delete(String token) {
        jdbcTemplate.update(
                "DELETE FROM password_reset_token WHERE token = ?",
                token
        );
    }

    public void deleteExpired() {
        jdbcTemplate.update(
                "DELETE FROM password_reset_token WHERE expiry_time < NOW()"
        );
    }
}
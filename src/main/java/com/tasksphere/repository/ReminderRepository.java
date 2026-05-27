package com.tasksphere.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ReminderRepository {

    private final JdbcTemplate jdbcTemplate;

    public ReminderRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int getReminderDays() {
        Integer days = jdbcTemplate.queryForObject(
                "SELECT reminder_days FROM reminder_settings LIMIT 1",
                Integer.class
        );
        return days == null ? 2 : days;
    }

    public void updateReminderDays(int days) {
        jdbcTemplate.update(
                "UPDATE reminder_settings SET reminder_days = ? WHERE id = 1",
                days
        );
    }
}
package com.tasksphere.service;

import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class BootstrapService implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;
    private final PasswordService passwordService;

    public BootstrapService(JdbcTemplate jdbcTemplate, PasswordService passwordService) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordService = passwordService;
    }

    @Override
    public void run(org.springframework.boot.ApplicationArguments args) {

        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from super_admins",
                Integer.class
        );

        if (count == null || count == 0) {

            jdbcTemplate.update(
                    "insert into super_admins (name, email, password_hash, contact_number) values (?, ?, ?, ?)",
                    "TaskSphere Admin",
                    "admin@tasksphere.local",
                    passwordService.hash("Admin@123"),
                    "9000000000"
            );
        }
    }
}

package com.tasksphere.model;

public record Student(
        Long id,
        String name,
        String enrollmentNumber,
        String passwordHash,
        String program,
        boolean firstLoginRequired,
        String email,
        String contact,
        String status
) {}
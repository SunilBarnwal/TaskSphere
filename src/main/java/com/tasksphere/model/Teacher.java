package com.tasksphere.model;

public record Teacher(
        Long id,
        String name,
        String email,
        String passwordHash,
        String contactNumber,
        boolean firstLoginRequired
) {
}

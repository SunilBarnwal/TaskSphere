package com.tasksphere.model;

public record SuperAdmin(
        Long id,
        String name,
        String email,
        String passwordHash,
        String contactNumber
) {
}

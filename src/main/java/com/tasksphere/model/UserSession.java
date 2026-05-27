package com.tasksphere.model;

public record UserSession(
        Long id,
        String principal,
        String displayName,
        Role role,
        boolean firstLoginRequired
) {
}

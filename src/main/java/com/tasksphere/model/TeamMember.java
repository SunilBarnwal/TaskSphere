package com.tasksphere.model;

public record TeamMember(
        Long id,
        Long teamId,
        Long studentId,
        boolean leader
) {
}

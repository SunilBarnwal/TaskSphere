package com.tasksphere.model;

public record TeamMemberView(
        Long id,
        Long teamId,
        Long studentId,
        String studentName,
        String enrollmentNumber,
        String program,
        boolean leader,
        String email,
        String contact
) {}
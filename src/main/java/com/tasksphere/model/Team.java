package com.tasksphere.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record Team(
        Long teamId,
        String teamName,
        String taskName,
        Long teacherId,
        LocalDate deadline,
        TeamStatus status,
        String screenshotPath,
        boolean reminderSent,
        String teacherFeedback,
        String reviewStatus,
        LocalDateTime reviewedAt
) {
}

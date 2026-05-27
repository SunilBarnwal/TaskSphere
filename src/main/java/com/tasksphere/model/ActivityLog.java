package com.tasksphere.model;

import java.time.LocalDateTime;

public record ActivityLog(
        Long id,
        Long teamId,
        String actorRole,
        String actorDisplayName,
        String actionType,
        String description,
        LocalDateTime createdAt
) {
}

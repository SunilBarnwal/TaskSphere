package com.tasksphere.model;

public record DashboardSummary(
        long totalTeams,
        long pendingTasks,
        long inProgressTasks,
        long completedTasks
) {
}

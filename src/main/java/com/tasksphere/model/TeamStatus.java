package com.tasksphere.model;

public enum TeamStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED;

    public boolean canTransitionTo(TeamStatus nextStatus) {
        return switch (this) {
            case PENDING -> nextStatus == IN_PROGRESS;
            case IN_PROGRESS -> nextStatus == COMPLETED;
            case COMPLETED -> false;
        };
    }
}

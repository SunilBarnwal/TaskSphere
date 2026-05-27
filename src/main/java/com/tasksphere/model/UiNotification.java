package com.tasksphere.model;

public record UiNotification(
        String level,
        String title,
        String message
) {
}

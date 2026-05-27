package com.tasksphere.form;

import com.tasksphere.model.TeamStatus;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public class UpdateTeamStatusForm {

    @NotNull(message = "Select the next status.")
    private TeamStatus status;

    private MultipartFile screenshot;

    public TeamStatus getStatus() {
        return status;
    }

    public void setStatus(TeamStatus status) {
        this.status = status;
    }

    public MultipartFile getScreenshot() {
        return screenshot;
    }

    public void setScreenshot(MultipartFile screenshot) {
        this.screenshot = screenshot;
    }
}

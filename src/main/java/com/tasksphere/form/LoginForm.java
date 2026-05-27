package com.tasksphere.form;

import jakarta.validation.constraints.NotBlank;

public class LoginForm {

    @NotBlank(message = "Role is required.")
    private String role;

    @NotBlank(message = "Identifier is required.")
    private String identifier;

    @NotBlank(message = "Password is required.")
    private String password;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

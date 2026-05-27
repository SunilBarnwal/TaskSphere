package com.tasksphere.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CreateTeacherForm {

    @NotBlank(message = "Name is required.")
    @Size(max = 120, message = "Name must be at most 120 characters.")
    private String name;

    @NotBlank(message = "Email is required.")
    @Email(message = "Enter a valid email address.")
    @Size(max = 180, message = "Email must be at most 180 characters.")
    private String email;

    @NotBlank(message = "Contact number is required.")
    @Pattern(regexp = "^[0-9+\\- ]{8,20}$", message = "Enter a valid contact number.")
    private String contactNumber;

    @NotBlank(message = "Temporary password is required.")
    @Size(min = 8, message = "Temporary password must be at least 8 characters.")
    private String temporaryPassword;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getTemporaryPassword() {
        return temporaryPassword;
    }

    public void setTemporaryPassword(String temporaryPassword) {
        this.temporaryPassword = temporaryPassword;
    }
}

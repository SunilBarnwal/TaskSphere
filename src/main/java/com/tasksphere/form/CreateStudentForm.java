package com.tasksphere.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateStudentForm {

    @NotBlank(message = "Name is required.")
    @Size(max = 120, message = "Name must be at most 120 characters.")
    private String name;

    @NotBlank(message = "Enrollment number is required.")
    @Size(max = 40, message = "Enrollment number must be at most 40 characters.")
    private String enrollmentNumber;

    @NotBlank(message = "Program is required.")
    @Size(max = 160, message = "Program must be at most 160 characters.")
    private String program;

    @NotBlank(message = "Temporary password is required.")
    @Size(min = 8, message = "Temporary password must be at least 8 characters.")
    private String temporaryPassword;

    // 🔥 NEW FIELDS
    @Email(message = "Enter a valid email address.")
    private String email;

    @Size(min = 10, max = 15, message = "Enter a valid contact number.")
    private String contact;

    // ================= GETTERS & SETTERS =================

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEnrollmentNumber() {
        return enrollmentNumber;
    }

    public void setEnrollmentNumber(String enrollmentNumber) {
        this.enrollmentNumber = enrollmentNumber;
    }

    public String getProgram() {
        return program;
    }

    public void setProgram(String program) {
        this.program = program;
    }

    public String getTemporaryPassword() {
        return temporaryPassword;
    }

    public void setTemporaryPassword(String temporaryPassword) {
        this.temporaryPassword = temporaryPassword;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }
}
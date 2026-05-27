package com.tasksphere.form;

public class MemberDto {

    private Long studentId;
    private String name;
    private String email;
    private String contact;
    private String enrollment;

    private boolean leader;   // 🔥 ADD THIS

    // getters & setters

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getEnrollment() { return enrollment; }
    public void setEnrollment(String enrollment) { this.enrollment = enrollment; }

    public boolean isLeader() { return leader; }
    public void setLeader(boolean leader) { this.leader = leader; }
}
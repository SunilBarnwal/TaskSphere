package com.tasksphere.form;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public class CreateTeamForm {

    @NotBlank
    @Size(max = 180)
    private String taskName;

    @NotNull
    @Future
    private LocalDate deadline;

    private List<MemberDto> members;

    private Integer leaderIndex; // 🔥 important

    // getters & setters

    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }

    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }

    public List<MemberDto> getMembers() { return members; }
    public void setMembers(List<MemberDto> members) { this.members = members; }

    public Integer getLeaderIndex() { return leaderIndex; }
    public void setLeaderIndex(Integer leaderIndex) { this.leaderIndex = leaderIndex; }
}
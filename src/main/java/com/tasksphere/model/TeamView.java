package com.tasksphere.model;

import java.util.List;

public record TeamView(
        Team team,
        Teacher teacher,
        List<TeamMemberView> members
) {
    public TeamMemberView leader() {
        return members.stream()
                .filter(TeamMemberView::leader)
                .findFirst()
                .orElse(null);
    }

    public boolean isMember(Long studentId) {
        return members.stream().anyMatch(member -> member.studentId().equals(studentId));
    }

    public boolean isLeader(Long studentId) {
        return members.stream().anyMatch(member -> member.studentId().equals(studentId) && member.leader());
    }
}

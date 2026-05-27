package com.tasksphere.service;

import com.tasksphere.form.CreateTeamForm;
import com.tasksphere.form.UpdateTeamForm;
import com.tasksphere.form.UpdateTeamStatusForm;
import com.tasksphere.model.ActivityLog;
import com.tasksphere.model.DashboardSummary;
import com.tasksphere.model.Student;
import com.tasksphere.model.Team;
import com.tasksphere.model.TeamMemberView;
import com.tasksphere.model.TeamStatus;
import com.tasksphere.model.TeamView;
import com.tasksphere.model.Teacher;
import com.tasksphere.model.UiNotification;
import com.tasksphere.repository.ActivityLogRepository;
import com.tasksphere.repository.StudentRepository;
import com.tasksphere.repository.TeamMemberRepository;
import com.tasksphere.repository.TeamRepository;
import com.tasksphere.repository.TeacherRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.tasksphere.form.MemberDto;
import java.time.LocalDateTime;


@Service
public class TeamService {
    private final PasswordService passwordService;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final FileStorageService fileStorageService;
    private final ActivityLogRepository activityLogRepository;
    private final EmailService emailService;

    public TeamService(
            TeamRepository teamRepository,
            TeamMemberRepository teamMemberRepository,
            TeacherRepository teacherRepository,
            StudentRepository studentRepository,
            FileStorageService fileStorageService,
            ActivityLogRepository activityLogRepository,
            PasswordService passwordService,
            EmailService emailService
    ) {
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.teacherRepository = teacherRepository;
        this.studentRepository = studentRepository;
        this.fileStorageService = fileStorageService;
        this.activityLogRepository = activityLogRepository;
        this.passwordService = passwordService;
        this.emailService = emailService;
    }

    public List<TeamView> getTeamsForTeacher(Long teacherId) {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Teacher account was not found."));
        return teamRepository.findByTeacherId(teacherId).stream()
                .map(team -> new TeamView(team, teacher, teamMemberRepository.findViewsByTeamId(team.teamId())))
                .toList();
    }

    public TeamView getTeacherTeam(Long teacherId, Long teamId) {
        Team team = requireTeacherTeam(teacherId, teamId);
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Teacher account was not found."));
        return new TeamView(team, teacher, teamMemberRepository.findViewsByTeamId(teamId));
    }

    public List<TeamView> getTeamsForStudent(Long studentId) {
        return teamRepository.findByStudentId(studentId).stream()
                .map(team -> new TeamView(
                        team,
                        teacherRepository.findById(team.teacherId()).orElse(null),
                        teamMemberRepository.findViewsByTeamId(team.teamId())
                ))
                .toList();
    }

    public DashboardSummary getTeacherSummary(Long teacherId) {
        return summarize(teamRepository.findByTeacherId(teacherId));
    }

    public DashboardSummary getStudentSummary(Long studentId) {
        return summarize(teamRepository.findByStudentId(studentId));
    }

    public DashboardSummary getGlobalSummary() {
        return summarize(teamRepository.findAll());
    }

    public List<UiNotification> getTeacherNotifications(Long teacherId) {
        return buildNotifications(teamRepository.findByTeacherId(teacherId));
    }

    public List<UiNotification> getStudentNotifications(Long studentId) {
        return buildNotifications(teamRepository.findByStudentId(studentId));
    }

    public List<ActivityLog> getTeacherActivity(Long teacherId) {
        return activityLogRepository.findRecentByTeacherId(teacherId, 10);
    }

    public List<ActivityLog> getStudentActivity(Long studentId) {
        return activityLogRepository.findRecentByStudentId(studentId, 10);
    }

    @Transactional
    public void createTeam(Long teacherId, CreateTeamForm form) {

        validateDeadline(form.getDeadline());

        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Teacher account was not found."));

        // 🔥 NEW LOGIC
        List<MemberDto> members = form.getMembers();
        Integer leaderIndex = form.getLeaderIndex();

        if (members == null || members.isEmpty()) {
            throw new IllegalArgumentException("Add at least one team member.");
        }

        Long teamId = teamRepository.create(
                temporaryTeamName(),
                form.getTaskName().trim(),
                teacherId,
                form.getDeadline(),
                TeamStatus.PENDING
        );

        if (teamId == null) {
            throw new IllegalStateException("Failed to create the team.");
        }

        teamRepository.updateTeamName(teamId, generateTeamName(teamId));

        // 🔥 MEMBERS SAVE
        for (int i = 0; i < members.size(); i++) {

            MemberDto m = members.get(i);

            String enrollment = m.getEnrollment().trim().toUpperCase();
            String email = m.getEmail().trim().toLowerCase();

// duplicate email and enroll in DB
            if (teamMemberRepository.existsEmailOutsideTeam(email, teamId)) {

                throw new IllegalArgumentException(
                        "Email already exists: " + email
                );
            }

            if (teamMemberRepository.existsEnrollmentOutsideTeam(enrollment, teamId)) {

                throw new IllegalArgumentException(
                        "Enrollment already exists: " + enrollment
                );
            }

            Student student;

            if (m.getStudentId() != null) {

                studentRepository.updateStudent(
                        m.getStudentId(),
                        m.getName(),
                        m.getEmail(),
                        m.getContact()
                );

                student = studentRepository.findById(m.getStudentId())
                        .orElseThrow(() -> new IllegalArgumentException("Student not found"));

            } else {

                student = studentRepository.findByEnrollmentNumber(enrollment)
                        .orElse(null);

                if (student == null) {

                    String password = passwordService.hash(enrollment);

                    studentRepository.create(
                            m.getName(),
                            enrollment,
                            password,
                            "CSE",
                            m.getEmail(),
                            m.getContact()
                    );

                    student = studentRepository.findByEnrollmentNumber(enrollment)
                            .orElseThrow(() -> new IllegalStateException("Student creation failed"));
                }
            }

            boolean isLeader = (leaderIndex != null && i == leaderIndex);

            teamMemberRepository.create(teamId, student.id(), isLeader);
        }

        activityLogRepository.create(
                teamId,
                "TEACHER",
                teacher.name(),
                "TEAM_CREATED",
                "Created team " + generateTeamName(teamId) + " for task " + form.getTaskName().trim() + "."
        );
        sendTeamAssignedEmail(teamId,false);
    }

    @Transactional
    public void changeLeader(Long teacherId, Long teamId, Long newLeaderStudentId) {
        Team team = requireTeacherTeam(teacherId, teamId);
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Teacher account was not found."));
        TeamMemberView currentLeader = getLeader(teamId);
        if (currentLeader != null && currentLeader.studentId().equals(newLeaderStudentId)) {
            return;
        }

        TeamMemberView newLeader = teamMemberRepository.findViewsByTeamId(teamId).stream()
                .filter(member -> member.studentId().equals(newLeaderStudentId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("The selected student is not part of this team."));

        teamMemberRepository.clearLeader(teamId);
        teamMemberRepository.markLeader(teamId, newLeaderStudentId);
        activityLogRepository.create(
                teamId,
                "TEACHER",
                teacher.name(),
                "LEADER_CHANGED",
                "Changed leader for " + team.teamName() + " to " + newLeader.studentName() + "."
        );
    }

    @Transactional
    public void removeMember(Long teacherId, Long teamId, Long studentId) {
        requireTeacherTeam(teacherId, teamId);
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Teacher account was not found."));
        TeamMemberView leader = getLeader(teamId);
        if (leader != null && leader.studentId().equals(studentId)) {
            throw new IllegalArgumentException("Change the team leader before removing the current leader.");
        }
        if (teamMemberRepository.countByTeamId(teamId) <= 1) {
            throw new IllegalArgumentException("A team must keep at least one member.");
        }
        TeamMemberView member = teamMemberRepository.findViewsByTeamId(teamId).stream()
                .filter(entry -> entry.studentId().equals(studentId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Team member was not found."));
        teamMemberRepository.deleteByTeamIdAndStudentId(teamId, studentId);
        activityLogRepository.create(
                teamId,
                "TEACHER",
                teacher.name(),
                "MEMBER_REMOVED",
                "Removed member " + member.studentName() + " (" + member.enrollmentNumber() + ")."
        );
    }

    @Transactional
    public void updateStatus(Long studentId, Long teamId, UpdateTeamStatusForm form) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Team was not found."));
        TeamMemberView leader = getLeader(teamId);
        if (leader == null || !leader.studentId().equals(studentId)) {
            throw new IllegalArgumentException("Only the team leader can update the task status.");
        }

        TeamStatus nextStatus = form.getStatus();
        if (nextStatus == null) {
            throw new IllegalArgumentException("Select the next status.");
        }
        if (!team.status().canTransitionTo(nextStatus)) {
            throw new IllegalArgumentException("Status can only move forward one step in the workflow.");
        }

        String screenshotPath = team.screenshotPath();
        if (nextStatus == TeamStatus.COMPLETED) {

            if (form.getScreenshot() == null || form.getScreenshot().isEmpty()) {
                throw new IllegalArgumentException("\"Completion proof file is required to mark task as COMPLETED.\"");
            }

            screenshotPath = fileStorageService.storeCompletionScreenshot(
                    form.getScreenshot(),
                    teamId,
                    studentId
            );
        }

        teamRepository.updateStatus(teamId, nextStatus, screenshotPath);
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student account was not found."));
        activityLogRepository.create(
                teamId,
                "STUDENT",
                student.name(),
                "STATUS_UPDATED",
                "Moved " + team.teamName() + " from " + team.status().name() + " to " + nextStatus.name() + "."
        );
    }

    public UpdateTeamForm buildUpdateForm(TeamView teamView) {
        UpdateTeamForm form = new UpdateTeamForm();
        form.setTaskName(teamView.team().taskName());
        form.setDeadline(teamView.team().deadline());
        return form;
    }

    private Team requireTeacherTeam(Long teacherId, Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Team was not found."));
        if (!team.teacherId().equals(teacherId)) {
            throw new IllegalArgumentException("This team does not belong to the current teacher.");
        }
        return team;
    }

    private void validateDeadline(LocalDate deadline) {
        if (deadline == null || !deadline.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Deadline must be a future date.");
        }
    }

    private TeamMemberView getLeader(Long teamId) {
        return teamMemberRepository.findViewsByTeamId(teamId).stream()
                .filter(TeamMemberView::leader)
                .findFirst()
                .orElse(null);
    }

    private String generateTeamName(Long teamId) {
        return "TEAM-%04d".formatted(teamId);
    }

    private String temporaryTeamName() {
        return "TMP-" + UUID.randomUUID();
    }

    private DashboardSummary summarize(List<Team> teams) {
        long pending = teams.stream().filter(team -> team.status() == TeamStatus.PENDING).count();
        long inProgress = teams.stream().filter(team -> team.status() == TeamStatus.IN_PROGRESS).count();
        long completed = teams.stream().filter(team -> team.status() == TeamStatus.COMPLETED).count();
        return new DashboardSummary(teams.size(), pending, inProgress, completed);
    }

    private List<UiNotification> buildNotifications(List<Team> teams) {
        LocalDate today = LocalDate.now();
        List<UiNotification> notifications = new ArrayList<>();

        teams.stream()
                .filter(team -> team.deadline() != null && !team.deadline().isBefore(today) && !team.deadline().isAfter(today.plusDays(3)))
                .forEach(team -> notifications.add(new UiNotification(
                        "warning",
                        "Deadline approaching",
                        team.teamName() + " is due on " + team.deadline() + "."
                )));

        teams.stream()
                .filter(team -> team.status() == TeamStatus.COMPLETED)
                .limit(3)
                .forEach(team -> notifications.add(new UiNotification(
                        "success",
                        "Task completed",
                        team.teamName() + " has been marked completed."
                )));

        return notifications.stream().limit(8).toList();
    }

    @Transactional
    public void updateTeamFull(Long teacherId, Long teamId, UpdateTeamForm form) {

        validateDeadline(form.getDeadline());

        // 🔥 update task
        teamRepository.updateTaskAndDeadline(
                teamId,
                form.getTaskName(),
                form.getDeadline()
        );

        List<MemberDto> members = form.getMembers();
        Integer leaderIndex = form.getLeaderIndex();

        if (members == null || members.isEmpty()) {
            throw new IllegalArgumentException("Add at least one member");
        }

        // 🔥 पहले पुराना data delete
        teamMemberRepository.deleteByTeamId(teamId);

// 🔥 फिर नया insert
        for (int i = 0; i < members.size(); i++) {

            MemberDto m = members.get(i);

            String enrollment = m.getEnrollment().trim().toUpperCase();
            String email = m.getEmail().trim().toLowerCase();

// duplicate email in DB
            if (teamMemberRepository.existsEmailOutsideTeam(email, teamId)) {

                throw new IllegalArgumentException(
                        "Email already exists: " + email
                );
            }

            if (teamMemberRepository.existsEnrollmentOutsideTeam(enrollment, teamId)) {

                throw new IllegalArgumentException(
                        "Enrollment already exists: " + enrollment
                );
            }

            if (enrollment.isEmpty()) continue;

            Student student;

            if (m.getStudentId() != null) {

                // 🔥 existing student update
                studentRepository.updateStudent(
                        m.getStudentId(),
                        m.getName(),
                        m.getEmail(),
                        m.getContact()
                );

                student = studentRepository.findById(m.getStudentId())
                        .orElseThrow(() -> new IllegalArgumentException("Student not found"));
            } else {
                student = studentRepository.findByEnrollmentNumber(enrollment).orElse(null);

                if (student == null) {
                    studentRepository.create(
                            m.getName(),
                            enrollment,
                            passwordService.hash(enrollment),
                            "CSE",
                            m.getEmail(),
                            m.getContact()
                    );

                    student = studentRepository.findByEnrollmentNumber(enrollment).orElse(null);
                }
            } // existing logic same

            boolean isLeader = (leaderIndex != null && i == leaderIndex);

            // 🔥 simple insert
            teamMemberRepository.create(teamId, student.id(), isLeader);
        }

// 🔥 mail
        sendTeamAssignedEmail(teamId,true);
    }

    @Transactional
    public void deleteTeam(Long teacherId, Long teamId) {

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found"));

        if (!team.teacherId().equals(teacherId)) {
            throw new IllegalArgumentException("Unauthorized");
        }

        // delete activity logs
        activityLogRepository.deleteByTeamId(teamId);

        // delete members
        teamMemberRepository.deleteByTeamId(teamId);

        // delete team
        teamRepository.deleteById(teamId);
    }

    private void sendTeamAssignedEmail(Long teamId, boolean isUpdate) {

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found"));

        List<TeamMemberView> members = teamMemberRepository.findViewsByTeamId(teamId);

        if (members.isEmpty()) return;

        // 🔥 leader find
        TeamMemberView leader = members.stream()
                .filter(TeamMemberView::leader)
                .findFirst()
                .orElse(null);

        String leaderName = leader != null ? leader.studentName() : "Not assigned";

        // 🔥 member list बनाओ
        StringBuilder memberList = new StringBuilder();
        for (TeamMemberView m : members) {
            memberList.append("- ")
                    .append(m.studentName())
                    .append(" (")
                    .append(m.enrollmentNumber())
                    .append(")\n");
        }

        // 🔥 subject
        String subject = isUpdate
                ? "Team Updated - TaskSphere"
                : "Team Assigned - TaskSphere";

        // 🔥 message
        String message =
                "Hello,\n\n" +
                        (isUpdate
                                ? "Your team details have been updated.\n\n"
                                : "You have been assigned to a new team.\n\n") +
                        "Team: " + team.teamName() + "\n" +
                        "Task: " + team.taskName() + "\n" +
                        "Deadline: " + team.deadline() + "\n\n" +
                        "Leader: " + leaderName + "\n\n" +
                        "Members:\n" + memberList +
                        "\nRegards,\nTaskSphere";

        //  send to all
        for (TeamMemberView m : members) {

            if (m.email() != null && !m.email().isEmpty()) {

                emailService.send(
                        m.email(),
                        subject,
                        message
                );
            }
        }
    }

    @Transactional
    public void reviewSubmission(
            Long teacherId,
            Long teamId,
            String action,
            String feedback
    ) {

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Team not found"));

        if (!team.teacherId().equals(teacherId)) {
            throw new IllegalArgumentException("Unauthorized");
        }

        String reviewStatus;

        TeamStatus nextStatus;

        if ("approve".equalsIgnoreCase(action)) {

            reviewStatus = "APPROVED";

            nextStatus = TeamStatus.COMPLETED;

        } else {

            reviewStatus = "REUPLOAD_REQUIRED";

            nextStatus = TeamStatus.IN_PROGRESS;
        }

        teamRepository.updateReview(
                teamId,
                feedback,
                reviewStatus,
                LocalDateTime.now(),
                nextStatus.name()
        );

        activityLogRepository.create(
                teamId,
                "TEACHER",
                "Teacher",
                "TEAM_REVIEWED",
                "Teacher reviewed uploaded submission."
        );

        notifyStudents(teamId, reviewStatus, feedback);
    }

    private void notifyStudents(
            Long teamId,
            String reviewStatus,
            String feedback
    ) {

        List<TeamMemberView> members =
                teamMemberRepository.findViewsByTeamId(teamId);

        for (TeamMemberView member : members) {

            if (member.email() == null ||
                    member.email().isBlank()) {
                continue;
            }

            String subject;

            String body;

            if ("APPROVED".equals(reviewStatus)) {

                subject = "Task Approved - TaskSphere";

                body =
                        "Hello " + member.studentName() + ",\n\n"
                                + "Your uploaded submission has been approved successfully.\n\n"
                                + "Regards,\nTaskSphere";

            } else {

                subject = "Re-upload Required - TaskSphere";

                body =
                        "Hello " + member.studentName() + ",\n\n"
                                + "Teacher Feedback:\n"
                                + feedback
                                + "\n\nPlease re-upload updated completion evidence.\n\n"
                                + "Regards,\nTaskSphere";
            }

            emailService.send(
                    member.email(),
                    subject,
                    body
            );
        }
    }

    public void makeLeader(Long teacherId,
                           Long teamId,
                           Long studentId) {

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found"));

        if (!team.teacherId().equals(teacherId)) {
            throw new IllegalArgumentException("Unauthorized");
        }

        teamMemberRepository.clearLeader(teamId);

        teamMemberRepository.markLeader(teamId, studentId);
    }
    public boolean emailExistsOutsideTeam(
            String email,
            Long teamId
    ) {

        return teamMemberRepository
                .existsEmailOutsideTeam(
                        email,
                        teamId
                );
    }

    public boolean enrollmentExistsOutsideTeam(
            String enrollment,
            Long teamId
    ) {

        return teamMemberRepository
                .existsEnrollmentOutsideTeam(
                        enrollment,
                        teamId
                );
    }

}

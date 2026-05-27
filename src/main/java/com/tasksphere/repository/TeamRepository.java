package com.tasksphere.repository;

import com.tasksphere.model.Team;
import com.tasksphere.model.TeamStatus;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class TeamRepository {

    private static final RowMapper<Team> ROW_MAPPER = new RowMapper<>() {
        @Override
        public Team mapRow(ResultSet rs, int rowNum) throws SQLException {
            Date deadline = rs.getDate("deadline");
            Timestamp reviewedAt = rs.getTimestamp("reviewed_at");

            return new Team(
                    rs.getLong("team_id"),
                    rs.getString("team_name"),
                    rs.getString("task_name"),
                    rs.getLong("teacher_id"),
                    deadline == null ? null : deadline.toLocalDate(),
                    TeamStatus.valueOf(rs.getString("status")),
                    rs.getString("screenshot_path"),
                    rs.getBoolean("reminder_sent"),
                    rs.getString("teacher_feedback"),
                    rs.getString("review_status"),
                    reviewedAt == null ? null : reviewedAt.toLocalDateTime()
            );
        }
    };

    private final JdbcTemplate jdbcTemplate;

    public TeamRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long create(String teamName, String taskName, Long teacherId, LocalDate deadline, TeamStatus status) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "insert into teams (team_name, task_name, teacher_id, deadline, status) values (?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, teamName);
            ps.setString(2, taskName);
            ps.setLong(3, teacherId);
            ps.setDate(4, Date.valueOf(deadline));
            ps.setString(5, status.name());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        return key == null ? null : key.longValue();
    }

    public void updateTeamName(Long teamId, String teamName) {
        jdbcTemplate.update("update teams set team_name = ? where team_id = ?", teamName, teamId);
    }

    public Optional<Team> findById(Long teamId) {
        return jdbcTemplate.query(
                "select team_id, team_name, task_name, teacher_id, deadline, status, screenshot_path, reminder_sent, teacher_feedback,review_status,reviewed_at from teams where team_id = ?",
                ROW_MAPPER,
                teamId
        ).stream().findFirst();
    }

    public List<Team> findByTeacherId(Long teacherId) {
        return jdbcTemplate.query(
                "select team_id, team_name, task_name, teacher_id, deadline, status, screenshot_path, reminder_sent, teacher_feedback,review_status,reviewed_at from teams where teacher_id = ? order by deadline asc, team_id asc",
                ROW_MAPPER,
                teacherId
        );
    }

    public List<Team> findByStudentId(Long studentId) {
        return jdbcTemplate.query(
                """
                select t.team_id, t.team_name, t.task_name, t.teacher_id, t.deadline, t.status, t.screenshot_path, t.reminder_sent, t.teacher_feedback, t.review_status, t.reviewed_at
                from teams t
                join team_members tm on tm.team_id = t.team_id
                where tm.student_id = ?
                order by t.deadline asc, t.team_id asc
                """,
                ROW_MAPPER,
                studentId
        );
    }

    public List<Team> findAll() {
        return jdbcTemplate.query(
                "select team_id, team_name, task_name, teacher_id, deadline, status, screenshot_path, reminder_sent, teacher_feedback,review_status,reviewed_at from teams order by deadline asc, team_id asc",
                ROW_MAPPER
        );
    }

    public void updateTaskAndDeadline(Long teamId, String taskName, LocalDate deadline) {
        jdbcTemplate.update(
                "update teams set task_name = ?, deadline = ? where team_id = ?",
                taskName,
                Date.valueOf(deadline),
                teamId
        );
    }

    public void updateStatus(Long teamId, TeamStatus status, String screenshotPath) {
        jdbcTemplate.update(
                "update teams set status = ?, screenshot_path = ? where team_id = ?",
                status.name(),
                screenshotPath,
                teamId
        );
    }
    public void deleteById(Long teamId) {
        jdbcTemplate.update("delete from teams where team_id = ?", teamId);
    }

    public List<String> getStudentEmailsByTeamId(Long teamId) {
        return jdbcTemplate.queryForList(
                """
                select s.email 
                from students s
                join team_members tm on tm.student_id = s.id
                where tm.team_id = ?
                """,
                String.class,
                teamId
        );
    }

    public void markReminderSent(Long teamId) {
        jdbcTemplate.update(
                "UPDATE teams SET reminder_sent = true WHERE team_id = ?",
                teamId
        );
    }
    public void updateReview(
            Long teamId,
            String feedback,
            String reviewStatus,
            LocalDateTime reviewedAt,
            String taskStatus
    ) {

        jdbcTemplate.update(
                """
                update teams
                set teacher_feedback = ?,
                    review_status = ?,
                    reviewed_at = ?,
                    status = ?
                where team_id = ?
                """,
                feedback,
                reviewStatus,
                Timestamp.valueOf(reviewedAt),
                taskStatus,
                teamId
        );
    }
}

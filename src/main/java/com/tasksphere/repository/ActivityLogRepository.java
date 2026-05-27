package com.tasksphere.repository;

import com.tasksphere.model.ActivityLog;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class ActivityLogRepository {

    private static final RowMapper<ActivityLog> ROW_MAPPER = new RowMapper<>() {
        @Override
        public ActivityLog mapRow(ResultSet rs, int rowNum) throws SQLException {
            Timestamp createdAt = rs.getTimestamp("created_at");
            return new ActivityLog(
                    rs.getLong("id"),
                    rs.getLong("team_id"),
                    rs.getString("actor_role"),
                    rs.getString("actor_display_name"),
                    rs.getString("action_type"),
                    rs.getString("description"),
                    createdAt == null ? null : createdAt.toLocalDateTime()
            );
        }
    };

    private final JdbcTemplate jdbcTemplate;

    public ActivityLogRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void create(Long teamId, String actorRole, String actorDisplayName, String actionType, String description) {
        jdbcTemplate.update(
                "insert into activity_logs (team_id, actor_role, actor_display_name, action_type, description, created_at) values (?, ?, ?, ?, ?, ?)",
                teamId,
                actorRole,
                actorDisplayName,
                actionType,
                description,
                Timestamp.valueOf(LocalDateTime.now())
        );
    }

    public List<ActivityLog> findRecentByTeacherId(Long teacherId, int limit) {
        return jdbcTemplate.query(
                """
                select al.id, al.team_id, al.actor_role, al.actor_display_name, al.action_type, al.description, al.created_at
                from activity_logs al
                join teams t on t.team_id = al.team_id
                where t.teacher_id = ?
                order by al.created_at desc, al.id desc
                limit ?
                """,
                ROW_MAPPER,
                teacherId,
                limit
        );
    }

    public List<ActivityLog> findRecentByStudentId(Long studentId, int limit) {
        return jdbcTemplate.query(
                """
                select distinct al.id, al.team_id, al.actor_role, al.actor_display_name, al.action_type, al.description, al.created_at
                from activity_logs al
                join team_members tm on tm.team_id = al.team_id
                where tm.student_id = ?
                order by al.created_at desc, al.id desc
                limit ?
                """,
                ROW_MAPPER,
                studentId,
                limit
        );
    }
    public void deleteByTeamId(Long teamId) {

        jdbcTemplate.update(
                "delete from activity_logs where team_id = ?",
                teamId
        );
    }
}

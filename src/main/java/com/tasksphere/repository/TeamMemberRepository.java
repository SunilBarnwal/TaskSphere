package com.tasksphere.repository;

import com.tasksphere.model.TeamMember;
import com.tasksphere.model.TeamMemberView;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class TeamMemberRepository {

    private static final RowMapper<TeamMember> ROW_MAPPER = new RowMapper<>() {
        @Override
        public TeamMember mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new TeamMember(
                    rs.getLong("id"),
                    rs.getLong("team_id"),
                    rs.getLong("student_id"),
                    rs.getBoolean("is_leader")
            );
        }
    };

    private static final RowMapper<TeamMemberView> VIEW_ROW_MAPPER = new RowMapper<>() {
        @Override
        public TeamMemberView mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new TeamMemberView(
                    rs.getLong("id"),
                    rs.getLong("team_id"),
                    rs.getLong("student_id"),
                    rs.getString("student_name"),
                    rs.getString("enrollment_number"),
                    rs.getString("program"),
                    rs.getBoolean("is_leader"),
                    rs.getString("email"),
                    rs.getString("contact")
            );
        }
    };

    private final JdbcTemplate jdbcTemplate;

    public TeamMemberRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void create(Long teamId, Long studentId, boolean isLeader) {
        jdbcTemplate.update(
                "insert into team_members (team_id, student_id, is_leader) values (?, ?, ?)",
                teamId,
                studentId,
                isLeader
        );
    }

    public List<TeamMemberView> findViewsByTeamId(Long teamId) {
        return jdbcTemplate.query(
                """
                select tm.id, tm.team_id, tm.student_id, tm.is_leader,
                       s.name as student_name, s.enrollment_number, s.program, s.email, s.contact
                from team_members tm
                join students s on s.id = tm.student_id
                where tm.team_id = ?
                order by tm.is_leader desc, s.name asc
                """,
                VIEW_ROW_MAPPER,
                teamId
        );
    }

    public Optional<TeamMember> findByTeamIdAndStudentId(Long teamId, Long studentId) {
        return jdbcTemplate.query(
                "select id, team_id, student_id, is_leader from team_members where team_id = ? and student_id = ?",
                ROW_MAPPER,
                teamId,
                studentId
        ).stream().findFirst();
    }

    public void clearLeader(Long teamId) {
        jdbcTemplate.update("update team_members set is_leader = false where team_id = ?", teamId);
    }

    public void markLeader(Long teamId, Long studentId) {
        jdbcTemplate.update(
                "update team_members set is_leader = true where team_id = ? and student_id = ?",
                teamId,
                studentId
        );
    }

    public void deleteByTeamIdAndStudentId(Long teamId, Long studentId) {
        jdbcTemplate.update("delete from team_members where team_id = ? and student_id = ?", teamId, studentId);
    }

    public void deleteByTeamId(Long teamId) {
        jdbcTemplate.update("delete from team_members where team_id = ?", teamId);
    }

    public long countByTeamId(Long teamId) {
        Long count = jdbcTemplate.queryForObject(
                "select count(*) from team_members where team_id = ?",
                Long.class,
                teamId
        );
        return count == null ? 0 : count;
    }


    public boolean existsByStudentId(Long studentId) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from team_members where student_id = ?",
                Integer.class,
                studentId
        );
        return count != null && count > 0;
    }

    public boolean existsEmailOutsideTeam(
            String email,
            Long teamId
    ) {

        Integer count = jdbcTemplate.queryForObject(
                """
                select count(*)
                from team_members tm
                join students s on s.id = tm.student_id
                where lower(s.email) = lower(?)
                and (? is null or tm.team_id <> ?)
                """,
                Integer.class,
                email,
                teamId,
                teamId
        );

        return count != null && count > 0;
    }

    public boolean existsEnrollmentOutsideTeam(
            String enrollment,
            Long teamId
    ) {

        Integer count = jdbcTemplate.queryForObject(
                """
                select count(*)
                from team_members tm
                join students s on s.id = tm.student_id
                where upper(s.enrollment_number) = upper(?)
                and (? is null or tm.team_id <> ?)
                """,
                Integer.class,
                enrollment,
                teamId,
                teamId
        );

        return count != null && count > 0;
    }

}

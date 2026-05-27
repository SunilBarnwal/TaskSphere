package com.tasksphere.repository;

import com.tasksphere.model.Teacher;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class TeacherRepository {

    private static final RowMapper<Teacher> ROW_MAPPER = new RowMapper<>() {
        @Override
        public Teacher mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Teacher(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("password_hash"),
                    rs.getString("contact_number"),
                    rs.getBoolean("first_login_required")
            );
        }
    };

    public void updateTeacher(Long id, String name, String email, String contact) {
        jdbcTemplate.update(
                "update teachers set name=?, email=?, contact_number=? where id=?",
                name, email, contact, id
        );
    }

    private final JdbcTemplate jdbcTemplate;

    public TeacherRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Teacher> findAll() {
        return jdbcTemplate.query(
                "select id, name, email, password_hash, contact_number, first_login_required from teachers order by name asc",
                ROW_MAPPER
        );
    }

    public Optional<Teacher> findById(Long id) {
        return jdbcTemplate.query(
                "select id, name, email, password_hash, contact_number, first_login_required from teachers where id = ?",
                ROW_MAPPER,
                id
        ).stream().findFirst();
    }

    public Optional<Teacher> findByEmail(String email) {
        return jdbcTemplate.query(
                "select id, name, email, password_hash, contact_number, first_login_required from teachers where lower(email) = lower(?)",
                ROW_MAPPER,
                email
        ).stream().findFirst();
    }

    public void create(String name, String email, String passwordHash, String contactNumber) {
        jdbcTemplate.update(
                "insert into teachers (name, email, password_hash, contact_number, first_login_required) values (?, ?, ?, ?, true)",
                name,
                email,
                passwordHash,
                contactNumber
        );
    }

    public void updatePassword(Long id, String passwordHash, boolean firstLoginRequired) {
        jdbcTemplate.update(
                "update teachers set password_hash = ?, first_login_required = ? where id = ?",
                passwordHash,
                firstLoginRequired,
                id
        );
    }

    public long countAll() {
        Long count = jdbcTemplate.queryForObject("select count(*) from teachers", Long.class);
        return count == null ? 0 : count;
    }

    public boolean existsByEmail(String email) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from teachers where email = ?",
                Integer.class,
                email
        );
        return count != null && count > 0;
    }

    public void updatePassword(String email, String password) {
        jdbcTemplate.update(
                "update teachers set password_hash = ?, first_login_required = false where email = ?",
                password,
                email
        );
    }

    public void markFirstLoginCompleted(Long id) {

        jdbcTemplate.update(
                "update teachers set first_login_required = false where id = ?",
                id
        );
    }
}

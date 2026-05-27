package com.tasksphere.repository;

import com.tasksphere.model.SuperAdmin;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class SuperAdminRepository {

    private static final RowMapper<SuperAdmin> ROW_MAPPER = new RowMapper<>() {
        @Override
        public SuperAdmin mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new SuperAdmin(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("password_hash"),
                    rs.getString("contact_number")
            );
        }
    };

    private final JdbcTemplate jdbcTemplate;

    public SuperAdminRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<SuperAdmin> findByEmail(String email) {
        return jdbcTemplate.query(
                "select id, name, email, password_hash, contact_number from super_admins where lower(email) = lower(?)",
                ROW_MAPPER,
                email
        ).stream().findFirst();
    }

    public long countAll() {
        Long count = jdbcTemplate.queryForObject("select count(*) from super_admins", Long.class);
        return count == null ? 0 : count;
    }

    public boolean existsByEmail(String email) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from super_admins where email = ?",
                Integer.class,
                email
        );
        return count != null && count > 0;
    }

    public void updatePassword(String email, String password) {
        jdbcTemplate.update(
                "update super_admins set password_hash = ? where email = ?",
                password,
                email
        );
    }
    public Optional<SuperAdmin> findById(Long id) {
        return jdbcTemplate.query(
                "select * from super_admins where id = ?",
                ROW_MAPPER,
                id
        ).stream().findFirst();
    }

    public void updateAdmin(Long id, String email, String passwordHash) {
        jdbcTemplate.update(
                "update super_admins set email = ?, password_hash = ? where id = ?",
                email, passwordHash, id
        );
    }

    public void updateEmail(Long id, String email) {
        jdbcTemplate.update(
                "update super_admins set email = ? where id = ?",
                email, id
        );
    }
}

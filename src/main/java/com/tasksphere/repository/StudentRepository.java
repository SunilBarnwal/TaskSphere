package com.tasksphere.repository;

import com.tasksphere.model.Student;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class StudentRepository {

    private static final RowMapper<Student> ROW_MAPPER = new RowMapper<>() {
        @Override
        public Student mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Student(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getString("enrollment_number"),
                    rs.getString("password_hash"),
                    rs.getString("program"),
                    rs.getBoolean("first_login_required"),
                    rs.getString("email"),
                    rs.getString("contact"),
                    null
            );
        }
    };

    private final JdbcTemplate jdbcTemplate;

    public StudentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Student> findAll() {
        return jdbcTemplate.query(
                "select id, name, enrollment_number, password_hash, program, first_login_required, email, contact from students order by name asc",
                ROW_MAPPER
        );
    }

    public Optional<Student> findById(Long id) {
        return jdbcTemplate.query(
                "select id, name, enrollment_number, password_hash, program, first_login_required, email, contact from students where id = ?",
                ROW_MAPPER,
                id
        ).stream().findFirst();
    }

    public Optional<Student> findByEnrollmentNumber(String enrollmentNumber) {
        return jdbcTemplate.query(
                "select id, name, enrollment_number, password_hash, program, first_login_required, email, contact from students where upper(enrollment_number) = upper(?)",
                ROW_MAPPER,
                enrollmentNumber
        ).stream().findFirst();
    }

    // 🔥 UPDATED create
    public void create(String name, String enrollmentNumber, String passwordHash, String program, String email, String contact) {
        jdbcTemplate.update(
                "insert into students (name, enrollment_number, password_hash, program, email, contact, first_login_required) values (?, ?, ?, ?, ?, ?, true)",
                name,
                enrollmentNumber,
                passwordHash,
                program,
                email,
                contact
        );
    }

    public void updatePassword(Long id, String passwordHash, boolean firstLoginRequired) {
        jdbcTemplate.update(
                "update students set password_hash = ?, first_login_required = ? where id = ?",
                passwordHash,
                firstLoginRequired,
                id
        );
    }

    // 🔥 NEW METHOD (important)
    public void updateStudent(Long id, String name, String email, String contact) {
        jdbcTemplate.update(
                "update students set name = ?, email = ?, contact = ? where id = ?",
                name,
                email,
                contact,
                id
        );
    }

    public long countAll() {
        Long count = jdbcTemplate.queryForObject("select count(*) from students", Long.class);
        return count == null ? 0 : count;
    }

    public boolean existsByEmail(String email) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from students where email = ?",
                Integer.class,
                email
        );
        return count != null && count > 0;
    }

    public void updatePasswordByEmail(String email, String password) {
        jdbcTemplate.update(
                "update students set password_hash = ?, first_login_required = false where email = ?",
                password,
                email
        );
    }

    public List<Student> findStudentsWithTeamStatus() {
        return jdbcTemplate.query(
                """
                SELECT s.id, s.name, s.enrollment_number, s.password_hash,
                       s.program, s.first_login_required,
                       s.email, s.contact,
                       t.status
                FROM students s
                JOIN team_members tm ON tm.student_id = s.id
                JOIN teams t ON t.team_id = tm.team_id
                """,
                (rs, rowNum) -> new Student(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("enrollment_number"),
                        rs.getString("password_hash"),
                        rs.getString("program"),
                        rs.getBoolean("first_login_required"),
                        rs.getString("email"),
                        rs.getString("contact"),
                        rs.getString("status")
                )
        );
    }

    public Optional<Student> findByEmail(String email) {

        return jdbcTemplate.query(
                "select id, name, enrollment_number, password_hash, program, first_login_required, email, contact from students where lower(email)=lower(?)",
                ROW_MAPPER,
                email
        ).stream().findFirst();
    }

}
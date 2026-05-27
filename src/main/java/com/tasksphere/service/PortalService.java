package com.tasksphere.service;

import com.tasksphere.form.ChangePasswordForm;
import com.tasksphere.form.CreateStudentForm;
import com.tasksphere.form.CreateTeacherForm;
import com.tasksphere.form.EditTeacherForm;
import com.tasksphere.model.Role;
import com.tasksphere.model.Student;
import com.tasksphere.model.Teacher;
import com.tasksphere.model.UserSession;
import com.tasksphere.repository.StudentRepository;
import com.tasksphere.repository.SuperAdminRepository;
import com.tasksphere.repository.TeacherRepository;
import java.util.List;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import com.tasksphere.model.SuperAdmin;

@Service
public class PortalService {

    private final SuperAdminRepository superAdminRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final PasswordService passwordService;

    public PortalService(
            SuperAdminRepository superAdminRepository,
            TeacherRepository teacherRepository,
            StudentRepository studentRepository,
            PasswordService passwordService
    ) {
        this.superAdminRepository = superAdminRepository;
        this.teacherRepository = teacherRepository;
        this.studentRepository = studentRepository;
        this.passwordService = passwordService;
    }

    public long totalSuperAdmins() {
        return superAdminRepository.countAll();
    }

    public long totalTeachers() {
        return teacherRepository.countAll();
    }

    public long totalStudents() {
        return studentRepository.countAll();
    }

    public List<Teacher> getTeachers() {
        return teacherRepository.findAll();
    }

    public List<Student> getStudents() {
        return studentRepository.findStudentsWithTeamStatus();
    }

    public void createTeacher(CreateTeacherForm form) {
        try {
            teacherRepository.create(
                    form.getName().trim(),
                    form.getEmail().trim().toLowerCase(),
                    passwordService.hash(form.getTemporaryPassword()),
                    form.getContactNumber().trim()
            );
        } catch (DuplicateKeyException ex) {
            throw new IllegalArgumentException("A teacher with that email already exists.");
        }
    }

    public void createStudent(CreateStudentForm form) {
        try {
            studentRepository.create(
                    form.getName().trim(),
                    form.getEnrollmentNumber().trim().toUpperCase(),
                    passwordService.hash(form.getEnrollmentNumber().trim().toUpperCase()),
                    form.getProgram().trim(),
                    form.getEmail(),     // 🔥 NEW
                    form.getContact()    // 🔥 NEW
            );
        } catch (DuplicateKeyException ex) {
            throw new IllegalArgumentException("A student with that enrollment number already exists.");
        }
    }

    public void updateTeacher(EditTeacherForm form) {
        teacherRepository.updateTeacher(
                form.getId(),
                form.getName(),
                form.getEmail(),
                form.getContact()
        );
    }

    public void changePassword(UserSession session, ChangePasswordForm form) {
        if (!form.getNewPassword().equals(form.getConfirmPassword())) {
            throw new IllegalArgumentException("New password and confirmation do not match.");
        }

        switch (session.role()) {
            case SUPER_ADMIN -> throw new IllegalArgumentException("Super admin password changes are not available from this screen.");
            case TEACHER -> {
                Teacher teacher = teacherRepository.findById(session.id())
                        .orElseThrow(() -> new IllegalArgumentException("Teacher account was not found."));
                if (!passwordService.matches(form.getCurrentPassword(), teacher.passwordHash())) {
                    throw new IllegalArgumentException("Current password is incorrect.");
                }
                teacherRepository.updatePassword(teacher.id(), passwordService.hash(form.getNewPassword()), false);
            }
            case STUDENT -> {
                Student student = studentRepository.findById(session.id())
                        .orElseThrow(() -> new IllegalArgumentException("Student account was not found."));
                if (!passwordService.matches(form.getCurrentPassword(), student.passwordHash())) {
                    throw new IllegalArgumentException("Current password is incorrect.");
                }
                studentRepository.updatePassword(student.id(), passwordService.hash(form.getNewPassword()), false);
            }
        }
    }

    public UserSession refreshSession(UserSession session) {
        return switch (session.role()) {
            case SUPER_ADMIN -> session;
            case TEACHER -> teacherRepository.findById(session.id())
                    .map(teacher -> new UserSession(teacher.id(), teacher.email(), teacher.name(), Role.TEACHER, teacher.firstLoginRequired()))
                    .orElseThrow(() -> new IllegalArgumentException("Teacher account was not found."));
            case STUDENT -> studentRepository.findById(session.id())
                    .map(student -> new UserSession(student.id(), student.enrollmentNumber(), student.name(), Role.STUDENT, student.firstLoginRequired()))
                    .orElseThrow(() -> new IllegalArgumentException("Student account was not found."));
        };
    }

    public void updateAdminSecure(Long id,
                                  String email,
                                  String currentPassword,
                                  String newPassword,
                                  String confirmPassword) {

        SuperAdmin admin = superAdminRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found"));

        if (!passwordService.matches(currentPassword, admin.passwordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        if (newPassword != null && !newPassword.isBlank()) {

            if (!newPassword.equals(confirmPassword)) {
                throw new IllegalArgumentException("Passwords do not match");
            }

            String newHash = passwordService.hash(newPassword);

            superAdminRepository.updateAdmin(id, email, newHash);

        } else {
            superAdminRepository.updateEmail(id, email);
        }
    }
}

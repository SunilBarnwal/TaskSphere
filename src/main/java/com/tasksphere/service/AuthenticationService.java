package com.tasksphere.service;

import com.tasksphere.model.Role;
import com.tasksphere.model.UserSession;
import com.tasksphere.repository.StudentRepository;
import com.tasksphere.repository.SuperAdminRepository;
import com.tasksphere.repository.TeacherRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final SuperAdminRepository superAdminRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final PasswordService passwordService;

    public AuthenticationService(
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

    public Optional<UserSession> authenticate(Role role, String identifier, String password) {
        return switch (role) {
            case SUPER_ADMIN -> superAdminRepository.findByEmail(identifier)
                    .filter(admin -> passwordService.matches(password, admin.passwordHash()))
                    .map(admin -> new UserSession(admin.id(), admin.email(), admin.name(), Role.SUPER_ADMIN, false));
            case TEACHER -> teacherRepository.findByEmail(identifier)
                    .filter(teacher -> passwordService.matches(password, teacher.passwordHash()))
                    .map(teacher -> {

                        if (teacher.firstLoginRequired()) {

                            teacherRepository.markFirstLoginCompleted(
                                    teacher.id()
                            );
                        }

                        return new UserSession(
                                teacher.id(),
                                teacher.email(),
                                teacher.name(),
                                Role.TEACHER,
                                false
                        );
                    });
            case STUDENT -> {

                var studentOptional = identifier.contains("@")
                        ? studentRepository.findByEmail(
                        identifier.trim().toLowerCase()
                )
                        : studentRepository.findByEnrollmentNumber(
                        identifier.trim().toUpperCase()
                );

                yield studentOptional
                        .filter(student ->
                                passwordService.matches(
                                        password.trim(),
                                        student.passwordHash()
                                )
                        )
                        .map(student -> new UserSession(
                                student.id(),
                                student.enrollmentNumber(),
                                student.name(),
                                Role.STUDENT,
                                student.firstLoginRequired()
                        ));
            }
        };
    }

}

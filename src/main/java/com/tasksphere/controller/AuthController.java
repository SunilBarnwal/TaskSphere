package com.tasksphere.controller;

import com.tasksphere.controller.BaseController;
import com.tasksphere.repository.StudentRepository;
import com.tasksphere.repository.SuperAdminRepository;
import com.tasksphere.repository.TeacherRepository;
import com.tasksphere.service.AuthenticationService;
import com.tasksphere.service.EmailService;
import com.tasksphere.service.PasswordResetService;
import com.tasksphere.service.PasswordService;
import com.tasksphere.form.LoginForm;
import com.tasksphere.model.Role;
import com.tasksphere.model.UserSession;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import java.util.Locale;

import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.springframework.stereotype.Controller;

@Controller
public class AuthController extends BaseController {

    private final AuthenticationService authenticationService;
    private final PasswordResetService resetService;
    private final PasswordService passwordService;
    private final SuperAdminRepository superAdminRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final EmailService emailService; //test


    // constructor
    public AuthController(
            AuthenticationService authenticationService,
            PasswordResetService resetService,
            PasswordService passwordService,
            SuperAdminRepository superAdminRepository,
            TeacherRepository teacherRepository,
            StudentRepository studentRepository,
            EmailService emailService   //test
    ) {
        this.authenticationService = authenticationService;
        this.resetService = resetService;
        this.passwordService = passwordService;
        this.superAdminRepository = superAdminRepository;
        this.teacherRepository = teacherRepository;
        this.studentRepository = studentRepository;
        this.emailService = emailService; //test
    }

    // ================= LOGIN =================

    @PostMapping("/login")
    public String login(
            @Valid @ModelAttribute("loginForm") LoginForm loginForm,
            BindingResult bindingResult,
            HttpSession httpSession,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("roles", Role.values());
            return "login";
        }

        Role role;
        try {
            role = Role.valueOf(loginForm.getRole().trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            bindingResult.rejectValue("role", "invalid.role", "Select a valid role.");
            model.addAttribute("roles", Role.values());
            return "login";
        }

        UserSession userSession = authenticationService.authenticate(
                role,
                loginForm.getIdentifier().trim(),
                loginForm.getPassword()
        ).orElse(null);

        if (userSession == null) {
            model.addAttribute("roles", Role.values());
            model.addAttribute("errorMessage", "Invalid credentials.");
            return "login";
        }

        httpSession.setAttribute(SESSION_KEY, userSession);
        return "redirect:/";
    }

    // ================= LOGOUT =================

    @PostMapping("/logout")
    public String logout(HttpSession httpSession, RedirectAttributes redirectAttributes) {
        httpSession.invalidate();
        redirectAttributes.addFlashAttribute("successMessage", "Logged out successfully.");
        return "redirect:/login";
    }

    // ================= FORGOT PASSWORD =================

    @GetMapping("/forgot-password")
    public String forgotPage() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String sendLink(@RequestParam String email) {
        resetService.sendResetLink(email);
        return "redirect:/login?success=Check your email";
    }

    // ================= RESET PASSWORD =================

    @GetMapping("/reset-password")
    public String resetPage(@RequestParam String token, Model model) {
        model.addAttribute("token", token);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String updatePassword(@RequestParam String token,
                                 @RequestParam String password) {

        String email = resetService.validateToken(token);
        String hash = passwordService.hash(password);

        if (superAdminRepository.existsByEmail(email)) {
            superAdminRepository.updatePassword(email, hash);
        } else if (teacherRepository.existsByEmail(email)) {
            teacherRepository.updatePassword(email, hash);
        } else if (studentRepository.existsByEmail(email)) {
            studentRepository.updatePasswordByEmail(email, hash);
        }

        resetService.deleteToken(token);

        return "redirect:/login?success=Password updated";
    }

}
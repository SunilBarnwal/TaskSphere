package com.tasksphere.controller;

import com.tasksphere.form.LoginForm;
import com.tasksphere.model.Role;
import com.tasksphere.model.UserSession;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController extends BaseController {

    @GetMapping("/")
    public String home(HttpSession httpSession) {
        UserSession userSession = currentUser(httpSession);
        if (userSession == null) {
            return "redirect:/login";
        }
        return switch (userSession.role()) {
            case SUPER_ADMIN -> "redirect:/admin/dashboard";
            case TEACHER -> "redirect:/teacher/dashboard";
            case STUDENT -> "redirect:/student/dashboard";
        };
    }

    @GetMapping("/login")
    public String login(HttpSession httpSession, Model model) {
        if (currentUser(httpSession) != null) {
            return "redirect:/";
        }
        if (!model.containsAttribute("loginForm")) {
            LoginForm form = new LoginForm();
            form.setRole(Role.STUDENT.name());
            model.addAttribute("loginForm", form);
        }
        model.addAttribute("roles", Role.values());
        return "login";
    }
}

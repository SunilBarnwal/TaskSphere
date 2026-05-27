package com.tasksphere.controller;

import com.tasksphere.form.CreateStudentForm;
import com.tasksphere.form.CreateTeacherForm;
import com.tasksphere.form.EditTeacherForm;
import com.tasksphere.model.DashboardSummary;
import com.tasksphere.model.Role;
import com.tasksphere.model.UserSession;
import com.tasksphere.service.PortalService;
import com.tasksphere.service.TeamService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AdminController extends BaseController {

    private final PortalService portalService;
    private final TeamService teamService;

    public AdminController(PortalService portalService, TeamService teamService) {
        this.portalService = portalService;
        this.teamService = teamService;
    }

    @GetMapping("/admin/dashboard")
    public String dashboard(HttpSession httpSession, Model model, RedirectAttributes redirectAttributes) {
        if (!hasRole(httpSession, Role.SUPER_ADMIN)) {
            return redirectWithError(redirectAttributes, "Please sign in as a super admin.", "/login");
        }

        UserSession currentUser = currentUser(httpSession);
        DashboardSummary teamSummary = teamService.getGlobalSummary();
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("totalSuperAdmins", portalService.totalSuperAdmins());
        model.addAttribute("totalTeachers", portalService.totalTeachers());
        model.addAttribute("totalStudents", portalService.totalStudents());
        model.addAttribute("teamSummary", teamSummary);
        model.addAttribute("teachers", portalService.getTeachers());
        model.addAttribute("students", portalService.getStudents());
        if (!model.containsAttribute("createTeacherForm")) {
            model.addAttribute("createTeacherForm", new CreateTeacherForm());
        }
        if (!model.containsAttribute("createStudentForm")) {
            model.addAttribute("createStudentForm", new CreateStudentForm());
        }
        return "admin-dashboard";
    }

    @PostMapping("/admin/teachers")
    public String createTeacher(
            @Valid @ModelAttribute("createTeacherForm") CreateTeacherForm createTeacherForm,
            BindingResult bindingResult,
            HttpSession httpSession,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (!hasRole(httpSession, Role.SUPER_ADMIN)) {
            return redirectWithError(redirectAttributes, "Please sign in as a super admin.", "/login");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("createStudentForm", new CreateStudentForm());
            return dashboard(httpSession, model, redirectAttributes);
        }

        try {
            portalService.createTeacher(createTeacherForm);
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("createStudentForm", new CreateStudentForm());
            return dashboard(httpSession, model, redirectAttributes);
        }

        return redirectWithSuccess(redirectAttributes, "Teacher account created successfully.", "/admin/dashboard");
    }

    @PostMapping("/admin/teacher/update")
    public String updateTeacher(
            @ModelAttribute EditTeacherForm form,
            RedirectAttributes redirectAttributes
    ) {
        portalService.updateTeacher(form);
        redirectAttributes.addFlashAttribute("successMessage", "Teacher updated successfully");
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/admin/students")
    public String createStudent(
            @Valid @ModelAttribute("createStudentForm") CreateStudentForm createStudentForm,
            BindingResult bindingResult,
            HttpSession httpSession,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (!hasRole(httpSession, Role.SUPER_ADMIN)) {
            return redirectWithError(redirectAttributes, "Please sign in as a super admin.", "/login");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("createTeacherForm", new CreateTeacherForm());
            return dashboard(httpSession, model, redirectAttributes);
        }

        try {
            portalService.createStudent(createStudentForm);
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("createTeacherForm", new CreateTeacherForm());
            return dashboard(httpSession, model, redirectAttributes);
        }

        return redirectWithSuccess(redirectAttributes, "Student account created successfully.", "/admin/dashboard");
    }
    @PostMapping("/admin/update-profile")
    public String updateAdminProfile(
            @RequestParam String email,
            @RequestParam String currentPassword,
            @RequestParam(required = false) String newPassword,
            @RequestParam(required = false) String confirmPassword,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        if (!hasRole(session, Role.SUPER_ADMIN)) {
            return "redirect:/login";
        }

        UserSession user = currentUser(session);

        try {
            portalService.updateAdminSecure(
                    user.id(),
                    email,
                    currentPassword,
                    newPassword,
                    confirmPassword
            );
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/admin/dashboard";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully");
        return "redirect:/admin/dashboard";
    }
}

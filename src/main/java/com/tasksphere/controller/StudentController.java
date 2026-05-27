package com.tasksphere.controller;

import com.tasksphere.form.ChangePasswordForm;
import com.tasksphere.form.UpdateTeamStatusForm;
import com.tasksphere.model.DashboardSummary;
import com.tasksphere.model.Role;
import com.tasksphere.model.TeamStatus;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class StudentController extends BaseController {

    private final PortalService portalService;
    private final TeamService teamService;

    public StudentController(PortalService portalService, TeamService teamService) {
        this.portalService = portalService;
        this.teamService = teamService;
    }

    @GetMapping("/student/dashboard")
    public String dashboard(HttpSession httpSession, Model model, RedirectAttributes redirectAttributes) {
        if (!hasRole(httpSession, Role.STUDENT)) {
            return redirectWithError(redirectAttributes, "Please sign in as a student.", "/login");
        }

        UserSession currentUser = portalService.refreshSession(currentUser(httpSession));
        httpSession.setAttribute(SESSION_KEY, currentUser);
        DashboardSummary summary = teamService.getStudentSummary(currentUser.id());
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("teams", teamService.getTeamsForStudent(currentUser.id()));
        model.addAttribute("summary", summary);
        model.addAttribute("notifications", teamService.getStudentNotifications(currentUser.id()));
        model.addAttribute("activityLogs", teamService.getStudentActivity(currentUser.id()));
        model.addAttribute("teamStatuses", TeamStatus.values());
        if (!model.containsAttribute("changePasswordForm")) {
            model.addAttribute("changePasswordForm", new ChangePasswordForm());
        }
        if (!model.containsAttribute("updateTeamStatusForm")) {
            model.addAttribute("updateTeamStatusForm", new UpdateTeamStatusForm());
        }
        return "student-dashboard";
    }

    @PostMapping("/student/change-password")
    public String changePassword(
            @Valid @ModelAttribute("changePasswordForm") ChangePasswordForm changePasswordForm,
            BindingResult bindingResult,
            HttpSession httpSession,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (!hasRole(httpSession, Role.STUDENT)) {
            return redirectWithError(redirectAttributes, "Please sign in as a student.", "/login");
        }

        if (bindingResult.hasErrors()) {
            return dashboard(httpSession, model, redirectAttributes);
        }

        try {
            UserSession updatedSession = portalService.refreshSession(currentUser(httpSession));
            portalService.changePassword(updatedSession, changePasswordForm);
            httpSession.setAttribute(SESSION_KEY, portalService.refreshSession(updatedSession));
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return dashboard(httpSession, model, redirectAttributes);
        }

        return redirectWithSuccess(redirectAttributes, "Password updated successfully.", "/student/dashboard");
    }

    @PostMapping("/student/teams/{teamId}/status")
    public String updateStatus(
            @PathVariable Long teamId,
            @Valid @ModelAttribute("updateTeamStatusForm") UpdateTeamStatusForm updateTeamStatusForm,
            BindingResult bindingResult,
            HttpSession httpSession,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (!hasRole(httpSession, Role.STUDENT)) {
            return redirectWithError(redirectAttributes, "Please sign in as a student.", "/login");
        }

        if (bindingResult.hasErrors()) {
            return dashboard(httpSession, model, redirectAttributes);
        }

        try {
            UserSession currentUser = portalService.refreshSession(currentUser(httpSession));

            TeamStatus newStatus = updateTeamStatusForm.getStatus();

            // 🔥 Screenshot check
            if (newStatus == TeamStatus.COMPLETED &&
                    (updateTeamStatusForm.getScreenshot() == null ||
                            updateTeamStatusForm.getScreenshot().isEmpty())) {

                model.addAttribute("errorMessage", "Completion proof file is required to mark task as COMPLETED.");
                return dashboard(httpSession, model, redirectAttributes);
            }

            teamService.updateStatus(currentUser.id(), teamId, updateTeamStatusForm);

        } catch (IllegalArgumentException | IllegalStateException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return dashboard(httpSession, model, redirectAttributes);
        }

        return redirectWithSuccess(redirectAttributes, "Task status updated successfully.", "/student/dashboard");
    }
}

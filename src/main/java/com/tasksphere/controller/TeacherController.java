package com.tasksphere.controller;

import com.tasksphere.form.ChangePasswordForm;
import com.tasksphere.form.CreateTeamForm;
import com.tasksphere.form.UpdateTeamForm;
import com.tasksphere.model.DashboardSummary;
import com.tasksphere.model.Role;
import com.tasksphere.model.TeamView;
import com.tasksphere.model.UserSession;
import com.tasksphere.repository.ReminderRepository;
import com.tasksphere.repository.StudentRepository;
import com.tasksphere.service.PortalService;
import com.tasksphere.service.TeamService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.ArrayList;
import java.util.List;
import com.tasksphere.form.MemberDto;
import com.tasksphere.model.TeamMemberView;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class TeacherController extends BaseController {

    private final PortalService portalService;
    private final TeamService teamService;
    private final ReminderRepository reminderRepository;
    private final StudentRepository studentRepository;

    public TeacherController(PortalService portalService,
                             TeamService teamService,
                             ReminderRepository reminderRepository,StudentRepository studentRepository) {
        this.portalService = portalService;
        this.teamService = teamService;
        this.reminderRepository = reminderRepository;
        this.studentRepository = studentRepository;

    }

    @GetMapping("/teacher/dashboard")
    public String dashboard(HttpSession httpSession, Model model, RedirectAttributes redirectAttributes) {
        if (!hasRole(httpSession, Role.TEACHER)) {
            return redirectWithError(redirectAttributes, "Please sign in as a teacher.", "/login");
        }

        UserSession currentUser = portalService.refreshSession(currentUser(httpSession));
        httpSession.setAttribute(SESSION_KEY, currentUser);
        DashboardSummary summary = teamService.getTeacherSummary(currentUser.id());

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("teams", teamService.getTeamsForTeacher(currentUser.id()));
        model.addAttribute("summary", summary);
        model.addAttribute("notifications", teamService.getTeacherNotifications(currentUser.id()));
        model.addAttribute("activityLogs", teamService.getTeacherActivity(currentUser.id()));
        model.addAttribute("reminderDays", reminderRepository.getReminderDays());

        if (!model.containsAttribute("changePasswordForm")) {
            model.addAttribute("changePasswordForm", new ChangePasswordForm());
        }
        if (!model.containsAttribute("createTeamForm")) {
            model.addAttribute("createTeamForm", new CreateTeamForm());
        }
        return "teacher-dashboard";
    }

    @GetMapping("/teacher/teams/{teamId}")
    public String teamDetails(@PathVariable Long teamId, HttpSession httpSession, Model model, RedirectAttributes redirectAttributes) {
        if (!hasRole(httpSession, Role.TEACHER)) {
            return redirectWithError(redirectAttributes, "Please sign in as a teacher.", "/login");
        }

        UserSession currentUser = portalService.refreshSession(currentUser(httpSession));
        try {
            TeamView team = teamService.getTeacherTeam(currentUser.id(), teamId);
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("team", team);

            if (!model.containsAttribute("updateTeamForm")) {
                UpdateTeamForm form = new UpdateTeamForm();
                form.setTaskName(team.team().taskName());
                form.setDeadline(team.team().deadline());
                List<MemberDto> members = new ArrayList<>();
                for (TeamMemberView m : team.members()) {
                    MemberDto dto = new MemberDto();
                    dto.setStudentId(m.studentId());
                    dto.setName(m.studentName());
                    dto.setEnrollment(m.enrollmentNumber());
                    dto.setContact(m.contact());
                    dto.setEmail(m.email());
                    dto.setLeader(m.leader());
                    members.add(dto);
                }
                form.setMembers(members);
                model.addAttribute("updateTeamForm", form);
            }
            return "teacher-team-detail";
        } catch (IllegalArgumentException ex) {
            return redirectWithError(redirectAttributes, ex.getMessage(), "/teacher/dashboard");
        }
    }

    @PostMapping("/teacher/teams")
    public String createTeam(@Valid @ModelAttribute("createTeamForm") CreateTeamForm createTeamForm, BindingResult bindingResult, HttpSession httpSession, Model model, RedirectAttributes redirectAttributes) {
        if (!hasRole(httpSession, Role.TEACHER)) {
            return redirectWithError(redirectAttributes, "Please sign in as a teacher.", "/login");
        }
        if (bindingResult.hasErrors()) {
            return dashboard(httpSession, model, redirectAttributes);
        }
        try {
            UserSession currentUser = currentUser(httpSession);
            teamService.createTeam(currentUser.id(), createTeamForm);
            return redirectWithSuccess(redirectAttributes, "Team created successfully.", "/teacher/dashboard");
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return dashboard(httpSession, model, redirectAttributes);
        }
    }

    @PostMapping("/teacher/teams/{teamId}/edit")
    public String updateTeam(@PathVariable Long teamId, @Valid @ModelAttribute("updateTeamForm") UpdateTeamForm updateTeamForm, BindingResult bindingResult, HttpSession httpSession, Model model, RedirectAttributes redirectAttributes) {
        if (!hasRole(httpSession, Role.TEACHER)) {
            return redirectWithError(redirectAttributes, "Please sign in as a teacher.", "/login");
        }
        if (bindingResult.hasErrors()) {
            return teamDetails(teamId, httpSession, model, redirectAttributes);
        }
        try {
            UserSession currentUser = currentUser(httpSession);
            teamService.updateTeamFull(currentUser.id(), teamId, updateTeamForm);
            return redirectWithSuccess(redirectAttributes, "Team updated successfully.", "/teacher/teams/" + teamId);
        }
        catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/teacher/teams/" + teamId;
        }
    }

    @PostMapping("/teacher/teams/delete/{teamId}")
    public String deleteTeam(@PathVariable Long teamId, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!hasRole(session, Role.TEACHER)) {
            return "redirect:/login";
        }
        UserSession currentUser = currentUser(session);
        teamService.deleteTeam(currentUser.id(), teamId);
        redirectAttributes.addFlashAttribute("successMessage", "Team deleted successfully");
        return "redirect:/teacher/dashboard";
    }

    @PostMapping("/teacher/teams/{teamId}/leader/{studentId}")
    public String makeLeader(@PathVariable Long teamId,
                             @PathVariable Long studentId,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {

        if (!hasRole(session, Role.TEACHER)) {
            return "redirect:/login";
        }

        UserSession currentUser = currentUser(session);

        teamService.makeLeader(
                currentUser.id(),
                teamId,
                studentId
        );

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Leader updated successfully."
        );

        return "redirect:/teacher/teams/" + teamId;
    }

    @PostMapping("/teacher/reminder")
    public String saveReminder(
            @RequestParam int reminderDays,
            RedirectAttributes redirectAttributes
    ) {
        reminderRepository.updateReminderDays(reminderDays);
        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Reminder settings updated successfully."
        );

        return "redirect:/teacher/dashboard";
    }

    @PostMapping("/teacher/change-password")
    public String changePassword(@Valid @ModelAttribute("changePasswordForm") ChangePasswordForm form, BindingResult result, HttpSession session, Model model, RedirectAttributes ra) {
        if (!hasRole(session, Role.TEACHER)) return "redirect:/login";
        if (result.hasErrors()) return dashboard(session, model, ra);
        try {
            portalService.changePassword(currentUser(session), form);
            return redirectWithSuccess(ra, "Password updated.", "/teacher/dashboard");
        } catch (Exception ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/teacher/dashboard";
        }
    }
    @GetMapping("/teacher/check-email")
    @ResponseBody
    public String checkEmail(
            @RequestParam String email,
            @RequestParam(required = false) Long teamId
    ) {

        boolean exists =
                teamService.emailExistsOutsideTeam(
                        email,
                        teamId
                );

        return exists ? "EXISTS" : "OK";
    }

    @GetMapping("/teacher/check-enrollment")
    @ResponseBody
    public String checkEnrollment(
            @RequestParam String enrollment,
            @RequestParam(required = false) Long teamId
    ) {

        boolean exists =
                teamService.enrollmentExistsOutsideTeam(
                        enrollment,
                        teamId
                );

        return exists ? "EXISTS" : "OK";
    }

    @PostMapping("/teacher/team/review/{teamId}")
    public String reviewTeam(
            @PathVariable Long teamId,
            @RequestParam String action,
            @RequestParam(required = false) String feedback,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {

        if (!hasRole(session, Role.TEACHER)) {
            return "redirect:/login";
        }

        UserSession currentUser = currentUser(session);

        teamService.reviewSubmission(
                currentUser.id(),
                teamId,
                action,
                feedback
        );

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Review submitted successfully."
        );

        return "redirect:/teacher/teams/" + teamId;
    }
}
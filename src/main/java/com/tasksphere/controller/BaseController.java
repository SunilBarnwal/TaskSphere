package com.tasksphere.controller;

import com.tasksphere.model.Role;
import com.tasksphere.model.UserSession;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

public abstract class BaseController {

    protected static final String SESSION_KEY = "TASKSPHERE_USER";

    protected UserSession currentUser(HttpSession httpSession) {
        Object sessionUser = httpSession.getAttribute(SESSION_KEY);
        return sessionUser instanceof UserSession userSession ? userSession : null;
    }

    protected boolean hasRole(HttpSession httpSession, Role role) {
        UserSession userSession = currentUser(httpSession);
        return userSession != null && userSession.role() == role;
    }

    protected String redirectWithError(RedirectAttributes redirectAttributes, String message, String location) {
        redirectAttributes.addFlashAttribute("errorMessage", message);
        return "redirect:" + location;
    }

    protected String redirectWithSuccess(RedirectAttributes redirectAttributes, String message, String location) {
        redirectAttributes.addFlashAttribute("successMessage", message);
        return "redirect:" + location;
    }
}

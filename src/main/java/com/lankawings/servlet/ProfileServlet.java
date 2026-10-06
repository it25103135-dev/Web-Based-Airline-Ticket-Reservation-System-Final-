package com.lankawings.servlet;

import com.lankawings.dao.NotificationDAO;
import com.lankawings.dao.UserDAO;
import com.lankawings.util.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.logging.Level;

/** Profile: read, update details, change password, close (deactivate) account. */
@WebServlet("/profile")
public class ProfileServlet extends HttpServlet {
    private static final RateLimiter PASSWORD_CHECKS = new RateLimiter(5, 15 * 60_000L, 15 * 60_000L);
    private final UserDAO users = new UserDAO();
    private final NotificationDAO notes = new NotificationDAO();

    @Override
    protected void doGet(HttpServletRequest r, HttpServletResponse s) throws ServletException, IOException {
        try { r.setAttribute("profile", users.findById(Req.uid(r))); } catch (Exception e) { Req.error(r, e); }
        r.getRequestDispatcher("/WEB-INF/views/profile.jsp").forward(r, s);
    }

    @Override
    protected void doPost(HttpServletRequest r, HttpServletResponse s) throws ServletException, IOException {
        int uid = Req.uid(r);
        String action = Req.param(r, "action");
        try {
            switch (action) {
                case "profile" -> {
                    String name = Validator.fullName(Req.param(r, "fullName"), "Full name");
                    String email = Validator.email(Req.param(r, "email"));
                    String phone = Validator.phone(Req.param(r, "phone"));
                    users.updateProfile(uid, name, email, phone);
                    r.getSession().setAttribute("fullName", name);
                    Req.flash(r, "Profile updated.");
                }
                case "password" -> {
                    requireCurrentPassword(r, uid);
                    String current = Req.param(r, "currentPassword");
                    String next = Validator.password(Req.param(r, "newPassword"), (String) r.getSession().getAttribute("username"));
                    if (!next.equals(Req.param(r, "confirmPassword"))) throw new ValidationException("New password and confirmation do not match.");
                    if (next.equals(current)) throw new ValidationException("Your new password must be different from the current one.");
                    users.updatePassword(uid, next);
                    r.changeSessionId();
                    quietNotify(uid, "Password changed", "Your password was changed. If this wasn't you, contact Lanka Wings support immediately.");
                    Req.flash(r, "Password updated.");
                }
                case "deactivate" -> {
                    if (Req.isAdmin(r)) throw new ValidationException("Administrator accounts must be deactivated by another administrator.");
                    requireCurrentPassword(r, uid);
                    if (!"on".equals(r.getParameter("confirmClose"))) throw new ValidationException("Tick the box to confirm you want to close your account.");
                    users.deactivate(uid);
                    r.getSession().invalidate();
                    Req.flash(r, "Your account has been closed. We're sorry to see you go.");
                    s.sendRedirect(r.getContextPath() + "/login");
                    return;
                }
                default -> throw new ValidationException("Unknown action.");
            }
            s.sendRedirect(r.getContextPath() + "/profile");
        } catch (Exception e) {
            Req.error(r, e);
            doGet(r, s);
        }
    }

    private void requireCurrentPassword(HttpServletRequest r, int uid) throws Exception {
        String key = "pw|" + uid;
        if (PASSWORD_CHECKS.isBlocked(key)) throw new ValidationException(PASSWORD_CHECKS.waitMessage(key));
        if (!users.verifyPassword(uid, Req.param(r, "currentPassword"))) {
            PASSWORD_CHECKS.record(key);
            throw new ValidationException("Your current password is incorrect.");
        }
        PASSWORD_CHECKS.reset(key);
    }

    private void quietNotify(int uid, String title, String msg) {
        try { notes.add(uid, title, msg, "SECURITY"); } catch (Exception e) { Req.log().log(Level.WARNING, "notification failed", e); }
    }
}

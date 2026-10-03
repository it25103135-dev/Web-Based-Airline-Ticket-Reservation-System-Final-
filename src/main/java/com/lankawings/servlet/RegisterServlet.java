package com.lankawings.servlet;

import com.lankawings.dao.NotificationDAO;
import com.lankawings.dao.UserDAO;
import com.lankawings.util.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.logging.Level;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    private static final RateLimiter SIGNUPS = new RateLimiter(10, 60 * 60_000L, 60 * 60_000L);   // max 10 sign-ups per IP per hour
    private final UserDAO users = new UserDAO();
    private final NotificationDAO notes = new NotificationDAO();

    @Override
    protected void doGet(HttpServletRequest r, HttpServletResponse s) throws ServletException, IOException {
        if (r.getParameter("expired") != null && r.getAttribute("error") == null)
            r.setAttribute("error", "Your page expired for security reasons. Please submit the form again.");
        r.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(r, s);
    }

    @Override
    protected void doPost(HttpServletRequest r, HttpServletResponse s) throws ServletException, IOException {
        String ip = Req.ip(r);
        try {
            if (!Req.param(r, "website").isBlank()) { s.sendRedirect(r.getContextPath() + "/login"); return; }   // honeypot: real people never fill this hidden field
            if (SIGNUPS.isBlocked(ip)) throw new ValidationException(SIGNUPS.waitMessage(ip));

            String fullName = Validator.fullName(Req.param(r, "fullName"), "Full name");
            String username = Validator.username(Req.param(r, "username"));
            String email = Validator.email(Req.param(r, "email"));
            String phone = Validator.phone(Req.param(r, "phone"));
            String password = Validator.password(Req.param(r, "password"), username);
            if (!password.equals(Req.param(r, "confirmPassword"))) throw new ValidationException("Passwords do not match.");
            if (!"on".equals(r.getParameter("terms"))) throw new ValidationException("Please accept the terms and privacy notice to continue.");

            int id = users.register(fullName, username, email, phone, password);
            SIGNUPS.record(ip);
            try {
                notes.add(id, "Welcome to Lanka Wings", "Your passenger account is ready. Search flights and start your next journey.", "WELCOME");
            } catch (Exception e) { Req.log().log(Level.WARNING, "Welcome notification failed", e); }

            Req.flash(r, "Account created successfully. You can now sign in.");
            s.sendRedirect(r.getContextPath() + "/login");
        } catch (Exception e) {
            Req.error(r, e);
            r.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(r, s);
        }
    }
}

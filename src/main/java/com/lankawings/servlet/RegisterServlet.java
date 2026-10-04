package com.lankawings.servlet;

import com.lankawings.dao.NotificationDAO;
import com.lankawings.dao.UserDAO;
import com.lankawings.util.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    private static final RateLimiter SIGNUPS = new RateLimiter(10, 60 * 60_000L, 60 * 60_000L);
    private final UserDAO users = new UserDAO();
    private final NotificationDAO notifications = new NotificationDAO();

    @Override
    protected void doGet(HttpServletRequest r, HttpServletResponse s) throws ServletException, IOException {
        if (r.getParameter("expired") != null && r.getAttribute("error") == null) {
            r.setAttribute("error", "Your page expired for security reasons. Please submit the form again.");
        }
        r.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(r, s);
    }

    @Override
    protected void doPost(HttpServletRequest r, HttpServletResponse s) throws ServletException, IOException {
        String ip = Req.ip(r);
        try {
            if (!Req.param(r, "website").isBlank()) {
                s.sendRedirect(r.getContextPath() + "/login");
                return;
            }
            if (SIGNUPS.isBlocked(ip)) throw new ValidationException(SIGNUPS.waitMessage(ip));

            String fullName = Validator.fullName(Req.param(r, "fullName"), "Full name");
            String username = Validator.username(Req.param(r, "username"));
            String email = Validator.email(Req.param(r, "email"));
            String phone = Validator.phone(Req.param(r, "phone"));
            String password = Validator.password(Req.param(r, "password"), username);

            if (!password.equals(Req.param(r, "confirmPassword"))) {
                throw new ValidationException("Passwords do not match.");
            }
            if (!"on".equals(r.getParameter("terms"))) {
                throw new ValidationException("Please accept the terms and privacy notice to continue.");
            }

            int userId = users.register(fullName, username, email, phone, password);
            notifications.add(
                    userId,
                    "Welcome to Lanka Wings",
                    "Your passenger account has been created successfully. You can manage your profile and notifications after signing in.",
                    "WELCOME"
            );

            SIGNUPS.record(ip);
            Req.flash(r, "Account created successfully. You can now sign in.");
            s.sendRedirect(r.getContextPath() + "/login");
        } catch (Exception e) {
            Req.error(r, e);
            r.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(r, s);
        }
    }
}

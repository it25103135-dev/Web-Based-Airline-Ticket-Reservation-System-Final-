package com.lankawings.servlet;

import com.lankawings.dao.NotificationDAO;
import com.lankawings.dao.UserDAO;
import com.lankawings.util.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

/** Admin user management: list, activate / deactivate, change role. Admins cannot change their own account here. */
@WebServlet("/admin/users")
public class AdminUserServlet extends HttpServlet {
    private final UserDAO users = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest r, HttpServletResponse s) throws ServletException, IOException {
        try { r.setAttribute("users", users.listAll()); } catch (Exception e) { Req.error(r, e); }
        r.getRequestDispatcher("/WEB-INF/views/admin-users.jsp").forward(r, s);
    }

    @Override
    protected void doPost(HttpServletRequest r, HttpServletResponse s) throws ServletException, IOException {
        try {
            int id = Req.id(r, "id", "user");
            if (id == Req.uid(r)) throw new ValidationException("You cannot change your own role or status. Ask another administrator.");
            switch (Req.param(r, "action")) {
                case "status" -> {
                    String status = Validator.oneOf(Req.param(r, "value"), "status", "ACTIVE", "INACTIVE");
                    users.setStatus(id, status);
                    Req.flash(r, "User " + ("ACTIVE".equals(status) ? "activated." : "deactivated. They are signed out on their next click."));
                }
                case "role" -> {
                    String role = Validator.oneOf(Req.param(r, "value"), "role", "PASSENGER", "FINANCE", "ADMIN");
                    users.setRole(id, role);
                    try { new NotificationDAO().add(id, "Account role updated", "Your account role is now " + role + ".", "SECURITY"); } catch (Exception ignored) { }
                    Req.flash(r, "Role changed to " + role + ".");
                }
                default -> throw new ValidationException("Unknown action.");
            }
            s.sendRedirect(r.getContextPath() + "/admin/users");
        } catch (Exception e) {
            Req.error(r, e);
            doGet(r, s);
        }
    }
}

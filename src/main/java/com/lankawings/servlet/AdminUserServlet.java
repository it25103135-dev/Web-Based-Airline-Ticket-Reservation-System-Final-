package com.lankawings.servlet;

import com.lankawings.dao.NotificationDAO;
import com.lankawings.dao.UserAuditDAO;
import com.lankawings.dao.UserDAO;
import com.lankawings.util.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/admin/users")
public class AdminUserServlet extends HttpServlet {
    private final UserDAO users = new UserDAO();
    private final UserAuditDAO audit = new UserAuditDAO();
    private final NotificationDAO notifications = new NotificationDAO();

    @Override
    protected void doGet(HttpServletRequest r, HttpServletResponse s) throws ServletException, IOException {
        try {
            r.setAttribute("users", users.listAll());
            r.setAttribute("audit", audit.recent(40));
            r.setAttribute("roles", UserDAO.ROLES);
        } catch (Exception e) {
            Req.error(r, e);
        }
        r.getRequestDispatcher("/WEB-INF/views/admin-users.jsp").forward(r, s);
    }

    @Override
    protected void doPost(HttpServletRequest r, HttpServletResponse s) throws ServletException, IOException {
        try {
            int actor = Req.uid(r);
            String action = Req.param(r, "action");

            switch (action) {
                case "create" -> {
                    String name = Validator.fullName(Req.param(r, "fullName"), "Full name");
                    String username = Validator.username(Req.param(r, "username"));
                    String email = Validator.email(Req.param(r, "email"));
                    String phone = Validator.phone(Req.param(r, "phone"));
                    String role = Validator.oneOf(Req.param(r, "role"), "role", UserDAO.ROLES);
                    String status = Validator.oneOf(Req.param(r, "status"), "status", "ACTIVE", "INACTIVE");
                    String password = Validator.password(Req.param(r, "password"), username);

                    int id = users.createByAdmin(name, username, email, phone, password, role, status);
                    audit.add(actor, id, "ACCOUNT_CREATE",
                            "Created account with role " + role + " and status " + status);
                    notifications.add(
                            id,
                            "Account created",
                            "An administrator created your Lanka Wings account. Role: " + role +
                                    ". Account status: " + status + ".",
                            "SECURITY"
                    );
                    Req.flash(r, "User account created.");
                }

                case "update" -> {
                    int id = Req.id(r, "id", "user");
                    if (id == actor) {
                        throw new ValidationException(
                                "Edit your own details from Profile. Your own role/status cannot be changed here.");
                    }

                    String name = Validator.fullName(Req.param(r, "fullName"), "Full name");
                    String email = Validator.email(Req.param(r, "email"));
                    String phone = Validator.phone(Req.param(r, "phone"));
                    String role = Validator.oneOf(Req.param(r, "role"), "role", UserDAO.ROLES);
                    String status = Validator.oneOf(Req.param(r, "status"), "status", "ACTIVE", "INACTIVE");

                    users.updateByAdmin(id, name, email, phone, role, status);
                    audit.add(actor, id, "ACCOUNT_UPDATE",
                            "Updated profile, role=" + role + ", status=" + status);
                    notifications.add(
                            id,
                            "Account access updated",
                            "An administrator updated your account. Current role: " + role +
                                    ". Current status: " + status + ".",
                            "SECURITY"
                    );
                    Req.flash(r, "User account updated.");
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

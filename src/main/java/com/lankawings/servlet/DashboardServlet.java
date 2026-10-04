package com.lankawings.servlet;

import com.lankawings.dao.NotificationDAO;
import com.lankawings.dao.UserDAO;
import com.lankawings.util.Req;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {
    private final UserDAO users = new UserDAO();
    private final NotificationDAO notifications = new NotificationDAO();

    @Override
    protected void doGet(HttpServletRequest r, HttpServletResponse s) throws ServletException, IOException {
        try {
            int uid = Req.uid(r);
            r.setAttribute("profile", users.findById(uid));
            r.setAttribute("unreadCount", notifications.unread(uid));
            if (Req.isAdmin(r)) {
                r.setAttribute("userCount", users.count());
                r.setAttribute("activeCount", users.countActive());
                r.setAttribute("adminCount", users.countAdmins());
            }
        } catch (Exception e) {
            Req.error(r, e);
        }
        r.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(r, s);
    }
}

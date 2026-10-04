package com.lankawings.servlet;

import com.lankawings.dao.NotificationDAO;
import com.lankawings.util.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

/**
 * Notification Management:
 * - list notifications
 * - live unread count for the header bell
 * - mark one/all as read
 * - delete one / clear read
 * - ADMIN: send announcement to everyone or one active username
 */
@WebServlet("/notifications")
public class NotificationServlet extends HttpServlet {
    private final NotificationDAO dao = new NotificationDAO();

    @Override
    protected void doGet(HttpServletRequest r, HttpServletResponse s) throws ServletException, IOException {
        int uid = Req.uid(r);

        if ("count".equals(r.getParameter("format"))) {
            int unread = 0;
            try {
                unread = dao.unread(uid);
            } catch (Exception e) {
                Req.log().warning("Unread notification count failed: " + e.getMessage());
            }
            s.setContentType("application/json;charset=UTF-8");
            s.getWriter().write("{\"unread\":" + unread + "}");
            return;
        }

        try {
            r.setAttribute("notifications", dao.list(uid));
            r.setAttribute("unreadCount", dao.unread(uid));
        } catch (Exception e) {
            Req.error(r, e);
        }
        r.getRequestDispatcher("/WEB-INF/views/notifications.jsp").forward(r, s);
    }

    @Override
    protected void doPost(HttpServletRequest r, HttpServletResponse s) throws ServletException, IOException {
        int uid = Req.uid(r);
        try {
            switch (Req.param(r, "action")) {
                case "read" -> dao.markRead(Req.id(r, "id", "notification"), uid);
                case "readAll" -> {
                    dao.markAllRead(uid);
                    Req.flash(r, "All notifications marked as read.");
                }
                case "delete" -> dao.delete(Req.id(r, "id", "notification"), uid);
                case "deleteRead" -> {
                    dao.deleteRead(uid);
                    Req.flash(r, "Read notifications cleared.");
                }
                case "send" -> {
                    if (!Req.isAdmin(r)) {
                        throw new ValidationException("Only administrators can send announcements.");
                    }
                    String title = Validator.text(Req.param(r, "title"), "Title", 3, 150);
                    String message = Validator.text(Req.param(r, "message"), "Message", 5, 800);
                    String target = Validator.oneOf(Req.param(r, "target"), "target", "ALL", "USER");

                    if ("ALL".equals(target)) {
                        int sent = dao.broadcast(title, message, "ANNOUNCEMENT");
                        Req.flash(r, "Announcement sent to " + sent + " active users.");
                    } else {
                        String username = Validator.username(Req.param(r, "username"));
                        dao.sendToUsername(username, title, message, "ANNOUNCEMENT");
                        Req.flash(r, "Notification sent to " + username + ".");
                    }
                }
                default -> throw new ValidationException("Unknown action.");
            }
            s.sendRedirect(r.getContextPath() + "/notifications");
        } catch (Exception e) {
            Req.error(r, e);
            doGet(r, s);
        }
    }
}

package com.lankawings.servlet;

import com.lankawings.dao.NotificationDAO;
import com.lankawings.util.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

/**
 * Notifications: list, mark one / all as read, delete one / all read.
 * Admins can also send announcements to everyone or to one user.
 * GET ?format=count returns {"unread":N} for the live bell badge.
 */
@WebServlet("/notifications")
public class NotificationServlet extends HttpServlet {
    private final NotificationDAO dao = new NotificationDAO();

    @Override
    protected void doGet(HttpServletRequest r, HttpServletResponse s) throws ServletException, IOException {
        int uid = Req.uid(r);
        if ("count".equals(r.getParameter("format"))) {
            int n = 0;
            try { n = dao.unread(uid); } catch (Exception e) { Req.log().warning("unread count failed: " + e.getMessage()); }
            s.setContentType("application/json;charset=UTF-8");
            s.getWriter().write("{\"unread\":" + n + "}");
            return;
        }
        try { r.setAttribute("notifications", dao.list(uid)); } catch (Exception e) { Req.error(r, e); }
        r.getRequestDispatcher("/WEB-INF/views/notifications.jsp").forward(r, s);
    }

    @Override
    protected void doPost(HttpServletRequest r, HttpServletResponse s) throws ServletException, IOException {
        int uid = Req.uid(r);
        try {
            switch (Req.param(r, "action")) {
                case "read" -> dao.markRead(Req.id(r, "id", "notification"), uid);
                case "readAll" -> { dao.markAllRead(uid); Req.flash(r, "All notifications marked as read."); }
                case "delete" -> dao.delete(Req.id(r, "id", "notification"), uid);
                case "deleteRead" -> { dao.deleteRead(uid); Req.flash(r, "Read notifications cleared."); }
                case "send" -> {
                    if (!Req.isAdmin(r)) throw new ValidationException("Only administrators can send announcements.");
                    String title = Validator.text(Req.param(r, "title"), "Title", 3, 150);
                    String message = Validator.text(Req.param(r, "message"), "Message", 5, 800);
                    String target = Validator.oneOf(Req.param(r, "target"), "target", "ALL", "USER");
                    if ("ALL".equals(target)) {
                        int n = dao.broadcast(title, message, "ANNOUNCEMENT");
                        Req.flash(r, "Announcement sent to " + n + " users.");
                    } else {
                        dao.sendToUsername(Validator.username(Req.param(r, "username")), title, message, "ANNOUNCEMENT");
                        Req.flash(r, "Notification sent.");
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

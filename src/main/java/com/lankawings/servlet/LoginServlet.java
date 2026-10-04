package com.lankawings.servlet;

import com.lankawings.dao.UserDAO;
import com.lankawings.model.User;
import com.lankawings.util.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private static final long MIN = 60_000L;
    private static final RateLimiter PER_ACCOUNT = new RateLimiter(5, 15 * MIN, 15 * MIN);
    private static final RateLimiter PER_IP = new RateLimiter(20, 15 * MIN, 15 * MIN);
    private final UserDAO dao = new UserDAO();

    @Override protected void doGet(HttpServletRequest r, HttpServletResponse s) throws ServletException, IOException {
        HttpSession session = r.getSession(false);
        if (session != null && session.getAttribute("userId") != null) { s.sendRedirect(r.getContextPath() + "/dashboard"); return; }
        if (r.getParameter("expired") != null && r.getAttribute("error") == null) r.setAttribute("error", "Your page expired for security reasons. Please sign in again.");
        r.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(r, s);
    }

    @Override protected void doPost(HttpServletRequest r, HttpServletResponse s) throws ServletException, IOException {
        String login = Req.param(r, "username").strip(), password = Req.param(r, "password"), ip = Req.ip(r), key = ip + "|" + login.toLowerCase();
        try {
            if (login.isEmpty() || password.isEmpty()) throw new ValidationException("Enter your username and password.");
            if (login.length() > 120 || password.length() > 128) throw new ValidationException("Invalid username or password.");
            if (PER_ACCOUNT.isBlocked(key)) throw new ValidationException(PER_ACCOUNT.waitMessage(key));
            if (PER_IP.isBlocked(ip)) throw new ValidationException(PER_IP.waitMessage(ip));
            User u = dao.authenticate(login, password);
            if (u == null) { PER_ACCOUNT.record(key); PER_IP.record(ip); throw new ValidationException("Invalid username or password."); }
            PER_ACCOUNT.reset(key);
            HttpSession old = r.getSession(false); if (old != null) old.invalidate();
            HttpSession session = r.getSession(true); session.setMaxInactiveInterval(30 * 60);
            session.setAttribute("userId", u.getUserId()); session.setAttribute("username", u.getUsername()); session.setAttribute("fullName", u.getFullName()); session.setAttribute("role", u.getRole());
            s.sendRedirect(r.getContextPath() + "/dashboard");
        } catch (Exception e) { Req.error(r, e); r.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(r, s); }
    }
}

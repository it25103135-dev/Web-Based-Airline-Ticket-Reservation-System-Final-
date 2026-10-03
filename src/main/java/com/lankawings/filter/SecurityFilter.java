package com.lankawings.filter;

import com.lankawings.dao.UserDAO;
import com.lankawings.util.Csrf;
import com.lankawings.util.Req;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Set;
import java.util.logging.Level;

/** Shared security gate required by Feedback Management. */
@WebFilter(urlPatterns = "/*")
public class SecurityFilter implements Filter {
    private static final Set<String> PUBLIC = Set.of("/", "/index.jsp", "/login", "/logout");
    private static final String CSP = "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; " +
            "font-src https://fonts.gstatic.com; img-src 'self' data:; connect-src 'self'; object-src 'none'; base-uri 'self'; " +
            "form-action 'self'; frame-ancestors 'none'";
    private final UserDAO users = new UserDAO();

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest r = (HttpServletRequest) req;
        HttpServletResponse s = (HttpServletResponse) res;
        r.setCharacterEncoding("UTF-8");
        String path = r.getRequestURI().substring(r.getContextPath().length());

        s.setHeader("X-Content-Type-Options", "nosniff");
        s.setHeader("X-Frame-Options", "DENY");
        s.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        s.setHeader("Permissions-Policy", "camera=(), microphone=(), geolocation=(), payment=()");
        s.setHeader("Content-Security-Policy", CSP);
        if (r.isSecure()) s.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");

        if (path.startsWith("/assets/")) { chain.doFilter(req, res); return; }

        s.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        s.setHeader("Pragma", "no-cache");
        s.setDateHeader("Expires", 0);

        String method = r.getMethod();
        boolean readOnly = "GET".equals(method) || "HEAD".equals(method) || "OPTIONS".equals(method);
        if (!readOnly && !Csrf.valid(r)) { csrfFailed(r, s, path); return; }

        if (!PUBLIC.contains(path)) {
            HttpSession session = r.getSession(false);
            Integer uid = session == null ? null : (Integer) session.getAttribute("userId");
            if (uid == null) {
                if ("GET".equals(method)) {
                    String q = r.getQueryString();
                    r.getSession(true).setAttribute("returnTo", path + (q == null ? "" : "?" + q));
                }
                s.sendRedirect(r.getContextPath() + "/login");
                return;
            }
            try {
                UserDAO.SessionInfo info = users.sessionInfo(uid);
                if (info == null || !"ACTIVE".equals(info.status)) {
                    session.invalidate();
                    r.getSession(true).setAttribute("flashError", "Your account is no longer active.");
                    s.sendRedirect(r.getContextPath() + "/login");
                    return;
                }
                session.setAttribute("role", info.role);
            } catch (SQLException e) {
                Req.log().log(Level.WARNING, "Session check failed", e);
            }
        }
        chain.doFilter(req, res);
    }

    private void csrfFailed(HttpServletRequest r, HttpServletResponse s, String path) throws IOException {
        HttpSession session = r.getSession(false);
        boolean loggedIn = session != null && session.getAttribute("userId") != null;
        if ("/login".equals(path)) { s.sendRedirect(r.getContextPath() + "/login?expired=1"); return; }
        if (!loggedIn) { s.sendRedirect(r.getContextPath() + "/login?expired=1"); return; }
        s.sendError(403, "Security token missing or expired. Go back, reload the page and try again.");
    }
}

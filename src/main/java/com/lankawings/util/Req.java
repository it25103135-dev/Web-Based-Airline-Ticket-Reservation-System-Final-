package com.lankawings.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Request/session helpers for the Flight Management module. */
public final class Req {
    private Req() {}
    private static final Logger LOG = Logger.getLogger("com.lankawings.flightmanagement");

    public static boolean isFlightManager(HttpServletRequest r) {
        HttpSession s = r.getSession(false);
        if (s == null) return false;
        Object role = s.getAttribute("role");
        return "RESERVATION_MANAGER".equals(role) || "ADMIN".equals(role);
    }

    public static String param(HttpServletRequest r, String name) {
        String v = r.getParameter(name);
        return v == null ? "" : v;
    }

    public static int id(HttpServletRequest r, String name, String label) { return Validator.id(r.getParameter(name), label); }
    public static String ip(HttpServletRequest r) { return r.getRemoteAddr(); }
    public static void flash(HttpServletRequest r, String message) { r.getSession().setAttribute("flash", message); }
    public static void flashError(HttpServletRequest r, String message) { r.getSession().setAttribute("flashError", message); }

    public static void error(HttpServletRequest r, Exception e) {
        if (e instanceof ValidationException) r.setAttribute("error", e.getMessage());
        else {
            LOG.log(Level.SEVERE, "Unexpected error on " + r.getMethod() + " " + r.getRequestURI(), e);
            r.setAttribute("error", "Something went wrong on our side. Please try again in a moment.");
        }
    }

    public static Logger log() { return LOG; }
}

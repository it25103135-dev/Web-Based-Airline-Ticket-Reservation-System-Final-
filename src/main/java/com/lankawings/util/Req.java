package com.lankawings.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Small request/session helpers shared by all servlets. */
public final class Req {
    private Req() {}
    private static final Logger LOG = Logger.getLogger("com.lankawings");

    public static boolean isAdmin(HttpServletRequest r) {
        HttpSession s = r.getSession(false);
        return s != null && "ADMIN".equals(s.getAttribute("role"));
    }

    public static int uid(HttpServletRequest r) {
        return (Integer) r.getSession().getAttribute("userId");
    }

    public static String param(HttpServletRequest r, String name) {
        String v = r.getParameter(name);
        return v == null ? "" : v;
    }

    public static int id(HttpServletRequest r, String name, String label) {
        return Validator.id(r.getParameter(name), label);
    }

    public static String ip(HttpServletRequest r) { return r.getRemoteAddr(); }

    /** One-shot message shown as a toast on the next page (survives a redirect). */
    public static void flash(HttpServletRequest r, String message) { r.getSession().setAttribute("flash", message); }
    public static void flashError(HttpServletRequest r, String message) { r.getSession().setAttribute("flashError", message); }

    /**
     * Put a safe message in request attribute "error".
     * ValidationException -> its own friendly message.  Anything else -> logged, generic text (no SQL details leak).
     */
    public static void error(HttpServletRequest r, Exception e) {
        if (e instanceof ValidationException) {
            r.setAttribute("error", e.getMessage());
        } else {
            LOG.log(Level.SEVERE, "Unexpected error on " + r.getMethod() + " " + r.getRequestURI(), e);
            r.setAttribute("error", "Something went wrong on our side. Please try again in a moment.");
        }
    }

    public static Logger log() { return LOG; }
}

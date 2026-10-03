package com.lankawings.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/** Per-session anti-CSRF token. Every POST form carries it as the hidden field "_csrf". */
public final class Csrf {
    private Csrf() {}
    public static final String FIELD = "_csrf";
    private static final String ATTR = "csrfToken";
    private static final SecureRandom RNG = new SecureRandom();

    public static String token(HttpSession session) {
        Object t = session.getAttribute(ATTR);
        if (t == null) {
            byte[] b = new byte[32];
            RNG.nextBytes(b);
            t = Base64.getUrlEncoder().withoutPadding().encodeToString(b);
            session.setAttribute(ATTR, t);
        }
        return (String) t;
    }

    public static boolean valid(HttpServletRequest r) {
        HttpSession s = r.getSession(false);
        if (s == null) return false;
        Object expected = s.getAttribute(ATTR);
        String given = r.getParameter(FIELD);
        if (given == null) given = r.getHeader("X-CSRF-Token");
        if (expected == null || given == null) return false;
        return MessageDigest.isEqual(((String) expected).getBytes(StandardCharsets.UTF_8), given.getBytes(StandardCharsets.UTF_8));
    }
}

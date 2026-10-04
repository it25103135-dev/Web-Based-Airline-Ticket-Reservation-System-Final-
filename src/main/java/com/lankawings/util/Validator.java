package com.lankawings.util;

import java.util.Set;
import java.util.regex.Pattern;

/** Validation rules used only by the User Management function. */
public final class Validator {
    private Validator() {}

    private static final Pattern NAME = Pattern.compile("^[\\p{L}\\p{M}][\\p{L}\\p{M} .'\\-]+$");
    private static final Pattern USERNAME = Pattern.compile("^[A-Za-z][A-Za-z0-9._-]{3,29}$");
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9-]+(?:\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$");
    private static final Pattern LK_MOBILE = Pattern.compile("^07[0-8]\\d{7}$");
    private static final Set<String> WEAK_PASSWORDS = Set.of("password", "password123", "12345678", "qwerty123", "admin123");

    public static String text(String v, String label, int min, int max) {
        if (v == null) v = "";
        v = v.strip().replaceAll("\\s{2,}", " ");
        if (v.length() < min) throw new ValidationException(label + " is required.");
        if (v.length() > max) throw new ValidationException(label + " is too long (maximum " + max + " characters).");
        return v;
    }

    public static String fullName(String v, String label) {
        v = text(v, label, 2, 80);
        if (!NAME.matcher(v).matches()) throw new ValidationException(label + " may contain only letters, spaces and . ' -");
        return v;
    }

    public static String username(String v) {
        v = text(v, "Username", 4, 30);
        if (!USERNAME.matcher(v).matches()) throw new ValidationException("Username must start with a letter and use only letters, numbers, dot, dash or underscore (4-30 characters).");
        return v;
    }

    public static String email(String v) {
        v = text(v, "Email", 1, 120).toLowerCase();
        if (!EMAIL.matcher(v).matches()) throw new ValidationException("Enter a valid email address, e.g. name@example.com.");
        return v;
    }

    public static String phone(String raw) {
        if (raw == null || raw.isBlank()) throw new ValidationException("Mobile number is required.");
        String s = raw.replaceAll("[\\s\\-()]", "");
        if (s.startsWith("+94")) s = "0" + s.substring(3);
        else if (s.startsWith("0094")) s = "0" + s.substring(4);
        else if (s.startsWith("94") && s.length() == 11) s = "0" + s.substring(2);
        if (!s.matches("\\d+")) throw new ValidationException("Mobile number can contain digits only.");
        if (!LK_MOBILE.matcher(s).matches()) throw new ValidationException("Enter a valid 10-digit Sri Lankan mobile number starting with 070 to 078.");
        return s;
    }

    public static String phoneForDisplay(String stored) {
        if (stored == null) return "";
        try { return phone(stored); } catch (ValidationException e) { return stored; }
    }

    public static String password(String pw, String username) {
        if (pw == null || pw.isEmpty()) throw new ValidationException("Password is required.");
        if (pw.length() < 8) throw new ValidationException("Password must be at least 8 characters.");
        if (pw.length() > 64) throw new ValidationException("Password must be at most 64 characters.");
        if (pw.chars().anyMatch(Character::isWhitespace)) throw new ValidationException("Password must not contain spaces.");
        if (pw.chars().noneMatch(Character::isLowerCase)) throw new ValidationException("Password needs at least one lowercase letter.");
        if (pw.chars().noneMatch(Character::isUpperCase)) throw new ValidationException("Password needs at least one uppercase letter.");
        if (pw.chars().noneMatch(Character::isDigit)) throw new ValidationException("Password needs at least one number.");
        if (pw.chars().allMatch(Character::isLetterOrDigit)) throw new ValidationException("Password needs at least one special character (e.g. @ # $ % !).");
        if (WEAK_PASSWORDS.contains(pw.toLowerCase())) throw new ValidationException("That password is too common. Choose something harder to guess.");
        if (username != null && username.length() >= 4 && pw.toLowerCase().contains(username.toLowerCase())) throw new ValidationException("Password must not contain your username.");
        return pw;
    }

    public static int id(String raw, String label) {
        try {
            int v = Integer.parseInt(raw);
            if (v <= 0) throw new NumberFormatException();
            return v;
        } catch (Exception e) { throw new ValidationException("Invalid " + label + "."); }
    }

    public static String oneOf(String v, String label, String... allowed) {
        for (String a : allowed) if (a.equals(v)) return v;
        throw new ValidationException("Invalid " + label + ".");
    }
}

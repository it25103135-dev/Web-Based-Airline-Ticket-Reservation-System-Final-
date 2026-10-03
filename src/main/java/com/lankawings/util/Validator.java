package com.lankawings.util;

/** Validation rules required only by the isolated Feedback Management function. */
public final class Validator {
    private Validator() {}

    public static int id(String raw, String label) {
        try {
            int v = Integer.parseInt(raw == null ? "" : raw.trim());
            if (v <= 0) throw new NumberFormatException();
            return v;
        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid " + label + " id.");
        }
    }

    public static int intRange(String raw, String label, int min, int max) {
        final int v;
        try { v = Integer.parseInt(raw == null ? "" : raw.trim()); }
        catch (NumberFormatException e) { throw new ValidationException(label + " must be a number between " + min + " and " + max + "."); }
        if (v < min || v > max) throw new ValidationException(label + " must be between " + min + " and " + max + ".");
        return v;
    }

    public static String text(String raw, String label, int min, int max) {
        String v = raw == null ? "" : raw.trim();
        if (v.length() < min) throw new ValidationException(label + " must be at least " + min + " characters.");
        if (v.length() > max) throw new ValidationException(label + " must be no more than " + max + " characters.");
        return v;
    }
}

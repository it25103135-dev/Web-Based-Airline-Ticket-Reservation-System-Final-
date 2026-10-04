package com.lankawings.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Collections;
import java.util.regex.Pattern;

/** Server-side validation rules required by Flight Management only. */
public final class Validator {
    private Validator() {}

    public static final Set<String> FLIGHT_STATUSES = Collections.unmodifiableSet(new LinkedHashSet<>(List.of("SCHEDULED", "BOARDING", "DELAYED", "CANCELLED", "COMPLETED")));
    private static final Pattern FLIGHT_NO = Pattern.compile("^[A-Z]{2}\\d{2,4}$");
    private static final Pattern PLACE = Pattern.compile("^[\\p{L}\\p{M} ().,'-]+$");
    private static final Pattern AIRCRAFT = Pattern.compile("^[A-Za-z0-9 .\\-/]+$");

    public static int id(String raw, String label) {
        try {
            int id = Integer.parseInt(raw == null ? "" : raw.trim());
            if (id <= 0) throw new NumberFormatException();
            return id;
        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid " + label + " id.");
        }
    }

    public static String text(String raw, String label, int min, int max) {
        String v = raw == null ? "" : raw.trim();
        if (v.length() < min) throw new ValidationException(label + " must be at least " + min + " characters.");
        if (v.length() > max) throw new ValidationException(label + " must be at most " + max + " characters.");
        return v;
    }

    public static String flightNo(String raw) {
        String v = text(raw, "Flight number", 4, 6).toUpperCase();
        if (!FLIGHT_NO.matcher(v).matches()) throw new ValidationException("Flight number must be 2 letters followed by 2-4 digits, e.g. LW101.");
        return v;
    }

    public static String place(String raw, String label) {
        String v = text(raw, label, 2, 80);
        if (!PLACE.matcher(v).matches()) throw new ValidationException(label + " contains invalid characters.");
        return v;
    }

    public static String aircraft(String raw) {
        String v = text(raw, "Aircraft", 2, 80);
        if (!AIRCRAFT.matcher(v).matches()) throw new ValidationException("Aircraft may contain only letters, numbers, spaces and . - /");
        return v;
    }

    public static LocalDateTime dateTime(String raw, String label) {
        try { return LocalDateTime.parse(text(raw, label, 1, 40)); }
        catch (DateTimeParseException e) { throw new ValidationException("Choose a valid " + label.toLowerCase() + " date and time."); }
    }

    public static LocalDate date(String raw, String label) {
        try { return LocalDate.parse(text(raw, label, 1, 20)); }
        catch (DateTimeParseException e) { throw new ValidationException("Choose a valid " + label.toLowerCase() + "."); }
    }

    public static BigDecimal money(String raw, String label, double min, double max) {
        try {
            BigDecimal v = new BigDecimal(text(raw, label, 1, 30));
            if (v.compareTo(BigDecimal.valueOf(min)) < 0 || v.compareTo(BigDecimal.valueOf(max)) > 0)
                throw new ValidationException(label + " must be between " + min + " and " + max + ".");
            if (v.scale() > 2) throw new ValidationException(label + " may have at most 2 decimal places.");
            return v;
        } catch (NumberFormatException e) { throw new ValidationException("Enter a valid " + label.toLowerCase() + "."); }
    }

    public static int intRange(String raw, String label, int min, int max) {
        try {
            int v = Integer.parseInt(text(raw, label, 1, 10));
            if (v < min || v > max) throw new ValidationException(label + " must be between " + min + " and " + max + ".");
            return v;
        } catch (NumberFormatException e) { throw new ValidationException(label + " must be a whole number."); }
    }

    public static String oneOf(String raw, String label, Set<String> allowed) {
        String v = text(raw, label, 1, 40).toUpperCase();
        if (!allowed.contains(v)) throw new ValidationException("Invalid " + label + ".");
        return v;
    }
}

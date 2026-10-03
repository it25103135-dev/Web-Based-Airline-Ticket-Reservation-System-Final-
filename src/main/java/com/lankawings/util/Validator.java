package com.lankawings.util;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/** Server-side rules used only by Ticket Reservation. */
public final class Validator {
    private Validator() {}
    private static final Pattern NAME = Pattern.compile("^[\\p{L}][\\p{L}\\p{M} .'\\-]{1,79}$");
    private static final Pattern PASSPORT = Pattern.compile("^[A-Z0-9]{6,15}$");
    private static final Pattern SEAT = Pattern.compile("^([1-9]\\d?)([A-F])$");
    public static final int SEATS_PER_ROW = 6;

    public static String text(String v, String label, int min, int max) {
        if (v == null) v = "";
        v = v.strip().replaceAll("[ \\t]+", " ");
        if (v.chars().anyMatch(c -> Character.isISOControl(c) && c != '\n' && c != '\r')) throw new ValidationException(label + " contains invalid characters.");
        if (v.isEmpty() && min > 0) throw new ValidationException(label + " is required.");
        if (v.length() < min) throw new ValidationException(label + " must be at least " + min + " characters.");
        if (v.length() > max) throw new ValidationException(label + " must be at most " + max + " characters.");
        return v;
    }

    public static int id(String v, String label) {
        try { int n = Integer.parseInt(v == null ? "" : v.trim()); if (n <= 0) throw new NumberFormatException(); return n; }
        catch (NumberFormatException e) { throw new ValidationException("Invalid " + label + "."); }
    }

    public static LocalDate date(String v, String label) {
        try { return LocalDate.parse(v.trim()); }
        catch (DateTimeParseException | NullPointerException e) { throw new ValidationException(label + " is not a valid date."); }
    }

    public static String fullName(String v, String label) {
        v = text(v, label, 2, 80);
        if (!NAME.matcher(v).matches()) throw new ValidationException(label + " may contain only letters, spaces and . ' - (no numbers or symbols).");
        return v;
    }

    public static String passport(String v) {
        v = text(v, "Passport / ID number", 6, 15).toUpperCase();
        if (!PASSPORT.matcher(v).matches()) throw new ValidationException("Passport / ID number must be 6-15 letters or digits with no spaces or symbols, e.g. N1234567.");
        return v;
    }

    public static String seat(String v) {
        v = text(v, "Seat", 2, 3).toUpperCase();
        if (!SEAT.matcher(v).matches()) throw new ValidationException("Seat must look like 12A (row number + letter A-F).");
        return v;
    }

    public static int seatRows(int totalSeats) { return (totalSeats + SEATS_PER_ROW - 1) / SEATS_PER_ROW; }

    public static void seatInLayout(String seat, int totalSeats) {
        var m = SEAT.matcher(seat);
        if (!m.matches()) throw new ValidationException("Invalid seat.");
        int row = Integer.parseInt(m.group(1));
        int col = m.group(2).charAt(0) - 'A';
        int index = (row - 1) * SEATS_PER_ROW + col + 1;
        if (row > seatRows(totalSeats) || index > totalSeats) throw new ValidationException("Seat " + seat + " does not exist on this aircraft.");
    }
}

package com.lankawings.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Central server-side validation. The browser (validate.js) mirrors these rules for instant
 * feedback, but the browser can be bypassed, so every rule is enforced here as well.
 * Every method returns the cleaned value or throws ValidationException with a friendly message.
 */
public final class Validator {
    private Validator() {}

    private static final Pattern NAME      = Pattern.compile("^[\\p{L}][\\p{L}\\p{M} .'\\-]{1,79}$");
    private static final Pattern USERNAME  = Pattern.compile("^[A-Za-z][A-Za-z0-9._-]{3,29}$");
    private static final Pattern EMAIL     = Pattern.compile("^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9](?:[A-Za-z0-9\\-]*[A-Za-z0-9])?(?:\\.[A-Za-z0-9](?:[A-Za-z0-9\\-]*[A-Za-z0-9])?)*\\.[A-Za-z]{2,}$");
    private static final Pattern LK_MOBILE = Pattern.compile("^07[0-8]\\d{7}$");
    private static final Pattern PASSPORT  = Pattern.compile("^[A-Z0-9]{6,15}$");
    private static final Pattern FLIGHT_NO = Pattern.compile("^[A-Z]{2}\\d{2,4}$");
    private static final Pattern SEAT      = Pattern.compile("^([1-9]\\d?)([A-F])$");
    private static final Pattern PLACE     = Pattern.compile("^[\\p{L}][\\p{L} ().,'\\-]{1,79}$");
    private static final Pattern AIRCRAFT  = Pattern.compile("^[A-Za-z0-9][A-Za-z0-9 .\\-/]{1,79}$");
    private static final Pattern EXPIRY    = Pattern.compile("^(0[1-9]|1[0-2])\\s*/\\s*(\\d{2})$");
    private static final Pattern CVV       = Pattern.compile("^\\d{3}$");

    private static final Set<String> WEAK_PASSWORDS = Set.of(
            "password", "password1", "password123", "12345678", "123456789", "1234567890",
            "qwerty123", "qwertyuiop", "iloveyou", "admin123", "letmein123", "welcome123",
            "lankawings", "lankawings123", "passenger123");

    public static final String[] FLIGHT_STATUSES = {"SCHEDULED", "BOARDING", "DELAYED", "CANCELLED", "COMPLETED"};
    public static final int SEATS_PER_ROW = 6;

    // ------------------------------------------------------------------ generic helpers

    /** Trim, collapse inner whitespace, reject control characters, enforce length. */
    public static String text(String v, String label, int min, int max) {
        if (v == null) v = "";
        v = v.strip().replaceAll("[ \\t]+", " ");
        if (v.chars().anyMatch(c -> Character.isISOControl(c) && c != '\n' && c != '\r'))
            throw new ValidationException(label + " contains invalid characters.");
        if (v.isEmpty() && min > 0) throw new ValidationException(label + " is required.");
        if (v.length() < min) throw new ValidationException(label + " must be at least " + min + " characters (you entered " + v.length() + ").");
        if (v.length() > max) throw new ValidationException(label + " must be at most " + max + " characters (you entered " + v.length() + ").");
        return v;
    }

    public static int id(String v, String label) {
        try {
            int n = Integer.parseInt(v == null ? "" : v.trim());
            if (n <= 0) throw new NumberFormatException();
            return n;
        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid " + label + ".");
        }
    }

    public static int intRange(String v, String label, int min, int max) {
        int n;
        try { n = Integer.parseInt(v == null ? "" : v.trim()); }
        catch (NumberFormatException e) { throw new ValidationException(label + " must be a whole number."); }
        if (n < min || n > max) throw new ValidationException(label + " must be between " + min + " and " + max + ".");
        return n;
    }

    public static String oneOf(String v, String label, String... allowed) {
        String s = v == null ? "" : v.strip();
        for (String a : allowed) if (a.equals(s)) return a;
        throw new ValidationException("Invalid " + label + ".");
    }

    public static BigDecimal money(String v, String label, double min, double max) {
        BigDecimal n;
        try { n = new BigDecimal(v == null ? "" : v.trim()); }
        catch (NumberFormatException e) { throw new ValidationException(label + " must be a valid amount."); }
        if (n.scale() > 2) throw new ValidationException(label + " can have at most 2 decimal places.");
        if (n.compareTo(BigDecimal.valueOf(min)) < 0 || n.compareTo(BigDecimal.valueOf(max)) > 0)
            throw new ValidationException(label + " must be between " + String.format("%,.2f", min) + " and " + String.format("%,.2f", max) + ".");
        return n.setScale(2);
    }

    public static LocalDateTime dateTime(String v, String label) {
        try { return LocalDateTime.parse(v == null ? "" : v.trim()); }
        catch (DateTimeParseException e) { throw new ValidationException(label + " is not a valid date and time."); }
    }

    public static LocalDate date(String v, String label) {
        try { return LocalDate.parse(v.trim()); }
        catch (Exception e) { throw new ValidationException(label + " is not a valid date."); }
    }

    // ------------------------------------------------------------------ people / account

    public static String fullName(String v, String label) {
        v = text(v, label, 2, 80);
        if (!NAME.matcher(v).matches())
            throw new ValidationException(label + " may contain only letters, spaces and . ' - (no numbers or symbols).");
        return v;
    }

    public static String username(String v) {
        v = text(v, "Username", 4, 30);
        if (!USERNAME.matcher(v).matches())
            throw new ValidationException("Username must start with a letter and use only letters, numbers, dot, dash or underscore (4-30 characters).");
        return v;
    }

    public static String email(String v) {
        v = text(v, "Email", 1, 120).toLowerCase();
        if (!EMAIL.matcher(v).matches()) throw new ValidationException("Enter a valid email address, e.g. name@example.com.");
        return v;
    }

    /** Sri Lankan mobile numbers: exactly 10 digits, 07[0-8]XXXXXXX. Also accepts +94 / 0094 / 94 prefixes. */
    public static String phone(String raw) {
        if (raw == null || raw.isBlank()) throw new ValidationException("Mobile number is required.");
        String s = raw.replaceAll("[\\s\\-()]", "");
        if (s.startsWith("+94")) s = "0" + s.substring(3);
        else if (s.startsWith("0094")) s = "0" + s.substring(4);
        else if (s.startsWith("94") && s.length() == 11) s = "0" + s.substring(2);
        if (!s.matches("\\d+")) throw new ValidationException("Mobile number can contain digits only.");
        if (s.length() < 10) throw new ValidationException("Mobile number is too short. It must be exactly 10 digits (e.g. 0771234567). You entered " + s.length() + ".");
        if (s.length() > 10) throw new ValidationException("Mobile number is too long. It must be exactly 10 digits (e.g. 0771234567). You entered " + s.length() + ".");
        if (!LK_MOBILE.matcher(s).matches()) throw new ValidationException("Enter a valid Sri Lankan mobile number starting with 070 to 078.");
        return s;
    }

    /** Lenient version used only to pre-fill forms from older stored values (never throws). */
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
        if (username != null && username.length() >= 4 && pw.toLowerCase().contains(username.toLowerCase()))
            throw new ValidationException("Password must not contain your username.");
        return pw;
    }

    // ------------------------------------------------------------------ flights / booking

    public static String flightNo(String v) {
        v = text(v, "Flight number", 4, 6).toUpperCase();
        if (!FLIGHT_NO.matcher(v).matches()) throw new ValidationException("Flight number must be 2 letters followed by 2-4 digits, e.g. LW101.");
        return v;
    }

    public static String place(String v, String label) {
        v = text(v, label, 2, 80);
        if (!PLACE.matcher(v).matches()) throw new ValidationException(label + " may contain only letters, spaces and ( ) . , ' -  e.g. Colombo (CMB).");
        return v;
    }

    public static String aircraft(String v) {
        v = text(v, "Aircraft", 2, 80);
        if (!AIRCRAFT.matcher(v).matches()) throw new ValidationException("Aircraft may contain only letters, numbers, spaces and . - /");
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

    /** Make sure the seat physically exists on an aircraft with this many seats (6 per row, A-F). */
    public static void seatInLayout(String seat, int totalSeats) {
        var m = SEAT.matcher(seat);
        if (!m.matches()) throw new ValidationException("Invalid seat.");
        int row = Integer.parseInt(m.group(1));
        int col = m.group(2).charAt(0) - 'A';
        int index = (row - 1) * SEATS_PER_ROW + col + 1;
        if (row > seatRows(totalSeats) || index > totalSeats)
            throw new ValidationException("Seat " + seat + " does not exist on this aircraft.");
    }

    // ------------------------------------------------------------------ payment

    public static String cardHolder(String v) {
        v = text(v, "Cardholder name", 2, 60);
        if (!NAME.matcher(v).matches()) throw new ValidationException("Cardholder name may contain only letters, spaces and . ' -");
        return v;
    }

    public static String cardBrand(String digits) {
        if (digits.startsWith("4")) return "VISA";
        if (digits.length() >= 2) {
            int two = Integer.parseInt(digits.substring(0, 2));
            if (two >= 51 && two <= 55) return "MASTERCARD";
        }
        if (digits.length() >= 4) {
            int four = Integer.parseInt(digits.substring(0, 4));
            if (four >= 2221 && four <= 2720) return "MASTERCARD";
        }
        return "UNKNOWN";
    }

    public static boolean luhn(String digits) {
        int sum = 0;
        boolean dbl = false;
        for (int i = digits.length() - 1; i >= 0; i--) {
            int n = digits.charAt(i) - '0';
            if (dbl) { n *= 2; if (n > 9) n -= 9; }
            sum += n;
            dbl = !dbl;
        }
        return sum % 10 == 0;
    }

    /** Returns digits only. method is Visa / Mastercard / Debit Card. */
    public static String cardNumber(String raw, String method) {
        if (raw == null || raw.isBlank()) throw new ValidationException("Card number is required.");
        String d = raw.replaceAll("[\\s\\-]", "");
        if (!d.matches("\\d+")) throw new ValidationException("Card number can contain digits only.");
        if (d.length() < 16) throw new ValidationException("Card number is too short. It must be exactly 16 digits (you entered " + d.length() + ").");
        if (d.length() > 16) throw new ValidationException("Card number is too long. It must be exactly 16 digits (you entered " + d.length() + ").");
        if (!luhn(d)) throw new ValidationException("This card number is not valid. Please check it for typing mistakes.");
        String brand = cardBrand(d);
        if ("Visa".equals(method) && !"VISA".equals(brand)) throw new ValidationException("This is not a Visa card number. Choose the correct payment method.");
        if ("Mastercard".equals(method) && !"MASTERCARD".equals(brand)) throw new ValidationException("This is not a Mastercard number. Choose the correct payment method.");
        if ("UNKNOWN".equals(brand)) throw new ValidationException("Only Visa and Mastercard cards are accepted.");
        return d;
    }

    public static YearMonth expiry(String raw) {
        var m = EXPIRY.matcher(raw == null ? "" : raw.trim());
        if (!m.matches()) throw new ValidationException("Enter the expiry date as MM/YY, e.g. 08/28.");
        YearMonth ym = YearMonth.of(2000 + Integer.parseInt(m.group(2)), Integer.parseInt(m.group(1)));
        YearMonth now = YearMonth.now();
        if (ym.isBefore(now)) throw new ValidationException("This card has expired.");
        if (ym.isAfter(now.plusYears(12))) throw new ValidationException("Expiry date is too far in the future.");
        return ym;
    }

    public static String cvv(String v) {
        if (v == null || !CVV.matcher(v.trim()).matches()) throw new ValidationException("CVV must be exactly 3 digits.");
        return v.trim();
    }
}

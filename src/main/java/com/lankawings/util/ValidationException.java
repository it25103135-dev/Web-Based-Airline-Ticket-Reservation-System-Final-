package com.lankawings.util;

/**
 * A problem the user can fix (bad input or a business rule such as "seat already taken").
 * The message is always safe to show on screen. Anything that is NOT a ValidationException
 * is treated as an internal error: it is logged on the server and the user sees a generic message.
 */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) { super(message); }
}

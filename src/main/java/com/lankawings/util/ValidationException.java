package com.lankawings.util;

/** A validation or business-rule problem safe to display to the user. */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) { super(message); }
}

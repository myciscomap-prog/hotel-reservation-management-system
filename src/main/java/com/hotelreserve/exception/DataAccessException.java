package com.hotelreserve.exception;

/**
 * Unchecked wrapper around low-level persistence failures (SQLException, missing rows that
 * should never be missing, etc). Kept unchecked because a database failure is not something
 * calling code can meaningfully recover from mid-operation — it is reported to the user via
 * the global uncaught-exception handler in {@code App}.
 */
public class DataAccessException extends RuntimeException {

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataAccessException(String message) {
        super(message);
    }
}

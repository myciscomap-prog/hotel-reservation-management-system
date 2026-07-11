package com.hotelreserve.exception;

/**
 * Thrown when a domain object (Room, Guest, Reservation, Payment, ...) is constructed
 * or mutated with data that fails business validation rules. Checked, because callers
 * (services and form controllers) are expected to catch it and show the user a message
 * rather than let it crash the application.
 */
public class ValidationException extends Exception {

    public ValidationException(String message) {
        super(message);
    }
}

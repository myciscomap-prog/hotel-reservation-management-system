package com.hotelreserve.exception;

/**
 * Thrown when a booking is attempted for a room that already has an overlapping,
 * non-cancelled/non-checked-out reservation for the requested date range.
 */
public class RoomUnavailableException extends Exception {

    private final Integer conflictingReservationId;

    public RoomUnavailableException(String message) {
        super(message);
        this.conflictingReservationId = null;
    }

    public RoomUnavailableException(String message, int conflictingReservationId) {
        super(message);
        this.conflictingReservationId = conflictingReservationId;
    }

    /** Null when the conflict was detected but no specific reservation id was available. */
    public Integer getConflictingReservationId() {
        return conflictingReservationId;
    }
}

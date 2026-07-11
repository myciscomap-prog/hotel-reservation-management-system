package com.hotelreserve.model;

import com.hotelreserve.exception.ValidationException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class Reservation {

    private final int id;
    private Guest guest;
    private Room room;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private int numberOfGuests;
    private ReservationStatus status;
    private final LocalDateTime createdAt;

    public Reservation(int id, Guest guest, Room room, LocalDate checkInDate, LocalDate checkOutDate,
                        int numberOfGuests, ReservationStatus status, LocalDateTime createdAt)
            throws ValidationException {
        validate(guest, room, checkInDate, checkOutDate, numberOfGuests);
        this.id = id;
        this.guest = guest;
        this.room = room;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.numberOfGuests = numberOfGuests;
        this.status = status == null ? ReservationStatus.PENDING : status;
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
    }

    private static void validate(Guest guest, Room room, LocalDate checkInDate, LocalDate checkOutDate,
                                  int numberOfGuests) throws ValidationException {
        if (guest == null) {
            throw new ValidationException("A guest is required");
        }
        if (room == null) {
            throw new ValidationException("A room is required");
        }
        if (checkInDate == null || checkOutDate == null) {
            throw new ValidationException("Check-in and check-out dates are required");
        }
        if (!checkOutDate.isAfter(checkInDate)) {
            throw new ValidationException("Check-out date must be after the check-in date");
        }
        if (numberOfGuests < 1 || numberOfGuests > room.getMaxOccupancy()) {
            throw new ValidationException(
                    "Number of guests must be between 1 and " + room.getMaxOccupancy() + " for room " + room.getRoomNumber());
        }
    }

    /** Number of nights stayed, computed from the check-in/check-out dates. */
    public long nights() {
        return ChronoUnit.DAYS.between(checkInDate, checkOutDate);
    }

    public int getId() {
        return id;
    }

    public Guest getGuest() {
        return guest;
    }

    public void setGuest(Guest guest) throws ValidationException {
        if (guest == null) {
            throw new ValidationException("A guest is required");
        }
        this.guest = guest;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) throws ValidationException {
        if (room == null) {
            throw new ValidationException("A room is required");
        }
        this.room = room;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) throws ValidationException {
        if (checkInDate == null || checkOutDate == null || !checkOutDate.isAfter(checkInDate)) {
            throw new ValidationException("Check-out date must be after the check-in date");
        }
        this.checkInDate = checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) throws ValidationException {
        if (checkOutDate == null || !checkOutDate.isAfter(checkInDate)) {
            throw new ValidationException("Check-out date must be after the check-in date");
        }
        this.checkOutDate = checkOutDate;
    }

    public int getNumberOfGuests() {
        return numberOfGuests;
    }

    public void setNumberOfGuests(int numberOfGuests) throws ValidationException {
        if (numberOfGuests < 1 || numberOfGuests > room.getMaxOccupancy()) {
            throw new ValidationException(
                    "Number of guests must be between 1 and " + room.getMaxOccupancy() + " for room " + room.getRoomNumber());
        }
        this.numberOfGuests = numberOfGuests;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) throws ValidationException {
        if (status == null) {
            throw new ValidationException("Reservation status is required");
        }
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "Reservation #" + id + " - " + guest.getFullName() + " - " + room.getRoomNumber()
                + " (" + checkInDate + " to " + checkOutDate + ") [" + status + "]";
    }
}

package com.hotelreserve.model;

import com.hotelreserve.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReservationTest {

    private Guest guest;
    private Room room;

    @BeforeEach
    void setUp() throws ValidationException {
        guest = new Guest(1, "Ama Owusu", "ama@example.com", "0244000000", "GHA-000000000-0");
        room = new StandardRoom(1, "101", 1, RoomStatus.AVAILABLE, 2, 350.0);
    }

    @Test
    void checkOutBeforeCheckInThrows() {
        LocalDate checkIn = LocalDate.of(2026, 8, 10);
        LocalDate checkOut = LocalDate.of(2026, 8, 5);
        assertThrows(ValidationException.class,
                () -> new Reservation(0, guest, room, checkIn, checkOut, 1, ReservationStatus.PENDING, LocalDateTime.now()));
    }

    @Test
    void checkOutEqualToCheckInThrows() {
        LocalDate date = LocalDate.of(2026, 8, 10);
        assertThrows(ValidationException.class,
                () -> new Reservation(0, guest, room, date, date, 1, ReservationStatus.PENDING, LocalDateTime.now()));
    }

    @Test
    void numberOfGuestsExceedingCapacityThrows() {
        LocalDate checkIn = LocalDate.of(2026, 8, 10);
        LocalDate checkOut = LocalDate.of(2026, 8, 12);
        assertThrows(ValidationException.class,
                () -> new Reservation(0, guest, room, checkIn, checkOut, 5, ReservationStatus.PENDING, LocalDateTime.now()));
    }

    @Test
    void zeroGuestsThrows() {
        LocalDate checkIn = LocalDate.of(2026, 8, 10);
        LocalDate checkOut = LocalDate.of(2026, 8, 12);
        assertThrows(ValidationException.class,
                () -> new Reservation(0, guest, room, checkIn, checkOut, 0, ReservationStatus.PENDING, LocalDateTime.now()));
    }

    @Test
    void validReservationComputesCorrectNights() throws ValidationException {
        LocalDate checkIn = LocalDate.of(2026, 8, 10);
        LocalDate checkOut = LocalDate.of(2026, 8, 15);
        Reservation reservation = assertDoesNotThrow(() ->
                new Reservation(0, guest, room, checkIn, checkOut, 2, ReservationStatus.PENDING, LocalDateTime.now()));
        assertEquals(5, reservation.nights());
    }
}

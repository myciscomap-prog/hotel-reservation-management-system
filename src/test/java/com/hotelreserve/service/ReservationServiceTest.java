package com.hotelreserve.service;

import com.hotelreserve.dao.InMemoryGuestRepository;
import com.hotelreserve.dao.InMemoryReservationRepository;
import com.hotelreserve.dao.InMemoryRoomRepository;
import com.hotelreserve.exception.RoomUnavailableException;
import com.hotelreserve.exception.ValidationException;
import com.hotelreserve.model.Guest;
import com.hotelreserve.model.Reservation;
import com.hotelreserve.model.Room;
import com.hotelreserve.model.RoomStatus;
import com.hotelreserve.model.StandardRoom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReservationServiceTest {

    private InMemoryRoomRepository roomRepository;
    private InMemoryGuestRepository guestRepository;
    private InMemoryReservationRepository reservationRepository;
    private ReservationService reservationService;
    private Guest guest;
    private Room room;

    @BeforeEach
    void setUp() throws ValidationException {
        roomRepository = new InMemoryRoomRepository();
        guestRepository = new InMemoryGuestRepository();
        reservationRepository = new InMemoryReservationRepository();
        reservationService = new ReservationService(reservationRepository, roomRepository);

        room = roomRepository.save(new StandardRoom(0, "101", 1, RoomStatus.AVAILABLE, 2, 350.0));
        guest = guestRepository.save(new Guest(0, "Ama Owusu", "ama@example.com", "0244000000", "GHA-000000000-0"));
    }

    @Test
    void bookingSucceedsAndMarksRoomReserved() throws Exception {
        Reservation reservation = reservationService.bookReservation(
                guest, room, LocalDate.of(2026, 8, 10), LocalDate.of(2026, 8, 12), 2);

        assertTrue(reservation.getId() > 0);
        assertEquals(RoomStatus.RESERVED, roomRepository.findById(room.getId()).orElseThrow().getStatus());
    }

    @Test
    void bookingOverlappingDatesThrowsRoomUnavailable() throws Exception {
        reservationService.bookReservation(guest, room, LocalDate.of(2026, 8, 10), LocalDate.of(2026, 8, 15), 2);

        assertThrows(RoomUnavailableException.class, () -> reservationService.bookReservation(
                guest, room, LocalDate.of(2026, 8, 12), LocalDate.of(2026, 8, 14), 2));
    }

    @Test
    void bookingNonOverlappingDatesAfterFirstStaySucceeds() throws Exception {
        reservationService.bookReservation(guest, room, LocalDate.of(2026, 8, 10), LocalDate.of(2026, 8, 12), 2);

        assertDoesNotThrow(() -> reservationService.bookReservation(
                guest, room, LocalDate.of(2026, 8, 12), LocalDate.of(2026, 8, 14), 2));
    }

    @Test
    void checkInThenCheckOutTransitionsSucceed() throws Exception {
        Reservation reservation = reservationService.bookReservation(
                guest, room, LocalDate.of(2026, 8, 10), LocalDate.of(2026, 8, 12), 2);

        reservationService.checkIn(reservation.getId());
        assertEquals(RoomStatus.OCCUPIED, roomRepository.findById(room.getId()).orElseThrow().getStatus());

        reservationService.checkOut(reservation.getId());
        assertEquals(RoomStatus.AVAILABLE, roomRepository.findById(room.getId()).orElseThrow().getStatus());
    }

    @Test
    void cancelReservationFreesTheRoom() throws Exception {
        Reservation reservation = reservationService.bookReservation(
                guest, room, LocalDate.of(2026, 8, 10), LocalDate.of(2026, 8, 12), 2);

        reservationService.cancelReservation(reservation.getId());
        assertEquals(RoomStatus.AVAILABLE, roomRepository.findById(room.getId()).orElseThrow().getStatus());
    }

    @Test
    void cannotCheckOutAReservationThatHasNotCheckedIn() throws Exception {
        Reservation reservation = reservationService.bookReservation(
                guest, room, LocalDate.of(2026, 8, 10), LocalDate.of(2026, 8, 12), 2);

        assertThrows(ValidationException.class, () -> reservationService.checkOut(reservation.getId()));
    }

    @Test
    void cannotCancelAnAlreadyCancelledReservation() throws Exception {
        Reservation reservation = reservationService.bookReservation(
                guest, room, LocalDate.of(2026, 8, 10), LocalDate.of(2026, 8, 12), 2);
        reservationService.cancelReservation(reservation.getId());

        assertThrows(ValidationException.class, () -> reservationService.cancelReservation(reservation.getId()));
    }

    @Test
    void cannotCheckInACancelledReservation() throws Exception {
        Reservation reservation = reservationService.bookReservation(
                guest, room, LocalDate.of(2026, 8, 10), LocalDate.of(2026, 8, 12), 2);
        reservationService.cancelReservation(reservation.getId());

        assertThrows(ValidationException.class, () -> reservationService.checkIn(reservation.getId()));
    }
}

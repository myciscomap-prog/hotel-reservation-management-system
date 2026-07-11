package com.hotelreserve.service;

import com.hotelreserve.dao.ReservationRepository;
import com.hotelreserve.dao.RoomRepository;
import com.hotelreserve.exception.DataAccessException;
import com.hotelreserve.exception.RoomUnavailableException;
import com.hotelreserve.exception.ValidationException;
import com.hotelreserve.model.Guest;
import com.hotelreserve.model.Reservation;
import com.hotelreserve.model.ReservationStatus;
import com.hotelreserve.model.Room;
import com.hotelreserve.model.RoomStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Owns the reservation lifecycle: booking, cancellation, and check-in/check-out state
 * transitions, keeping the linked room's status in sync at every step.
 */
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;

    public ReservationService(ReservationRepository reservationRepository, RoomRepository roomRepository) {
        this.reservationRepository = reservationRepository;
        this.roomRepository = roomRepository;
    }

    /** True when the room has no active (non-cancelled/non-checked-out) reservation overlapping the range. */
    public boolean checkAvailability(Room room, LocalDate checkIn, LocalDate checkOut) {
        return reservationRepository.findOverlapping(room.getId(), checkIn, checkOut).isEmpty();
    }

    public Reservation bookReservation(Guest guest, Room room, LocalDate checkIn, LocalDate checkOut,
                                        int numberOfGuests) throws ValidationException, RoomUnavailableException {
        Reservation candidate = new Reservation(0, guest, room, checkIn, checkOut, numberOfGuests,
                ReservationStatus.PENDING, java.time.LocalDateTime.now());

        List<Reservation> conflicts = reservationRepository.findOverlapping(room.getId(), checkIn, checkOut);
        if (!conflicts.isEmpty()) {
            Reservation conflict = conflicts.get(0);
            throw new RoomUnavailableException(
                    "Room " + room.getRoomNumber() + " is already booked for an overlapping period ("
                            + conflict.getCheckInDate() + " to " + conflict.getCheckOutDate() + ")",
                    conflict.getId());
        }

        Reservation saved = reservationRepository.save(candidate);
        room.setStatus(RoomStatus.RESERVED);
        roomRepository.update(room);
        return saved;
    }

    public Reservation cancelReservation(int reservationId) throws ValidationException {
        Reservation reservation = findOrThrow(reservationId);
        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new ValidationException("Reservation #" + reservationId + " is already cancelled");
        }
        if (reservation.getStatus() == ReservationStatus.CHECKED_OUT) {
            throw new ValidationException("Cannot cancel a reservation that has already been checked out");
        }
        reservation.setStatus(ReservationStatus.CANCELLED);
        Reservation saved = reservationRepository.save(reservation);
        reservation.getRoom().setStatus(RoomStatus.AVAILABLE);
        roomRepository.update(reservation.getRoom());
        return saved;
    }

    public Reservation checkIn(int reservationId) throws ValidationException {
        Reservation reservation = findOrThrow(reservationId);
        if (reservation.getStatus() != ReservationStatus.PENDING && reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new ValidationException(
                    "Cannot check in a reservation with status " + reservation.getStatus());
        }
        reservation.setStatus(ReservationStatus.CHECKED_IN);
        Reservation saved = reservationRepository.save(reservation);
        reservation.getRoom().setStatus(RoomStatus.OCCUPIED);
        roomRepository.update(reservation.getRoom());
        return saved;
    }

    public Reservation checkOut(int reservationId) throws ValidationException {
        Reservation reservation = findOrThrow(reservationId);
        if (reservation.getStatus() != ReservationStatus.CHECKED_IN) {
            throw new ValidationException(
                    "Cannot check out a reservation with status " + reservation.getStatus() + " (must be CHECKED_IN)");
        }
        reservation.setStatus(ReservationStatus.CHECKED_OUT);
        Reservation saved = reservationRepository.save(reservation);
        reservation.getRoom().setStatus(RoomStatus.AVAILABLE);
        roomRepository.update(reservation.getRoom());
        return saved;
    }

    /** Occupancy snapshot for the dashboard: how many rooms currently sit in each status. */
    public Map<RoomStatus, Long> occupancyByStatus() {
        return roomRepository.findAll().stream()
                .collect(Collectors.groupingBy(Room::getStatus, Collectors.counting()));
    }

    public List<Reservation> todaysCheckIns() {
        return reservationRepository.findAll().stream()
                .filter(r -> r.getCheckInDate().isEqual(LocalDate.now()))
                .toList();
    }

    public List<Reservation> todaysCheckOuts() {
        return reservationRepository.findAll().stream()
                .filter(r -> r.getCheckOutDate().isEqual(LocalDate.now()))
                .toList();
    }

    private Reservation findOrThrow(int reservationId) throws ValidationException {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ValidationException("No reservation found with id " + reservationId));
    }
}

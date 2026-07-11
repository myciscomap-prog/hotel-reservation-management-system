package com.hotelreserve.dao;

import com.hotelreserve.exception.DataAccessException;
import com.hotelreserve.exception.ValidationException;
import com.hotelreserve.model.Reservation;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InMemoryReservationRepository implements ReservationRepository {

    private final List<Reservation> store = new ArrayList<>();
    private int nextId = 1;

    @Override
    public List<Reservation> findAll() {
        return List.copyOf(store);
    }

    @Override
    public Optional<Reservation> findById(int id) {
        return store.stream().filter(r -> r.getId() == id).findFirst();
    }

    @Override
    public List<Reservation> findOverlapping(int roomId, LocalDate checkIn, LocalDate checkOut) {
        return store.stream()
                .filter(r -> r.getRoom().getId() == roomId)
                .filter(r -> r.getStatus() != com.hotelreserve.model.ReservationStatus.CANCELLED
                        && r.getStatus() != com.hotelreserve.model.ReservationStatus.CHECKED_OUT)
                .filter(r -> r.getCheckInDate().isBefore(checkOut) && r.getCheckOutDate().isAfter(checkIn))
                .toList();
    }

    @Override
    public Reservation save(Reservation reservation) {
        if (reservation.getId() == 0) {
            Reservation persisted = withId(reservation, nextId++);
            store.add(persisted);
            return persisted;
        }
        update(reservation);
        return reservation;
    }

    @Override
    public void update(Reservation reservation) {
        store.removeIf(r -> r.getId() == reservation.getId());
        store.add(reservation);
    }

    @Override
    public void delete(int id) {
        store.removeIf(r -> r.getId() == id);
    }

    private Reservation withId(Reservation reservation, int id) {
        try {
            return new Reservation(id, reservation.getGuest(), reservation.getRoom(), reservation.getCheckInDate(),
                    reservation.getCheckOutDate(), reservation.getNumberOfGuests(), reservation.getStatus(),
                    reservation.getCreatedAt());
        } catch (ValidationException e) {
            throw new DataAccessException("Failed to assign id to in-memory reservation", e);
        }
    }
}

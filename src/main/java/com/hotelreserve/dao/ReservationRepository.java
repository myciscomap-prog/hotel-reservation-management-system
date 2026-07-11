package com.hotelreserve.dao;

import com.hotelreserve.model.Reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository {
    List<Reservation> findAll();
    Optional<Reservation> findById(int id);
    List<Reservation> findOverlapping(int roomId, LocalDate checkIn, LocalDate checkOut);
    Reservation save(Reservation reservation);
    void update(Reservation reservation);
    void delete(int id);
}

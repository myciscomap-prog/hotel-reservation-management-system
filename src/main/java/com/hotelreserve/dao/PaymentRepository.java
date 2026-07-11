package com.hotelreserve.dao;

import com.hotelreserve.model.Payment;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository {
    List<Payment> findAll();
    Optional<Payment> findById(int id);
    List<Payment> findByReservationId(int reservationId);
    Payment save(Payment payment);
    void update(Payment payment);
}

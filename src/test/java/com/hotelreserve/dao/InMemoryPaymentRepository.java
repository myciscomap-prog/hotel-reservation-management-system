package com.hotelreserve.dao;

import com.hotelreserve.exception.DataAccessException;
import com.hotelreserve.exception.ValidationException;
import com.hotelreserve.model.Payment;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InMemoryPaymentRepository implements PaymentRepository {

    private final List<Payment> store = new ArrayList<>();
    private int nextId = 1;

    @Override
    public List<Payment> findAll() {
        return List.copyOf(store);
    }

    @Override
    public Optional<Payment> findById(int id) {
        return store.stream().filter(p -> p.getId() == id).findFirst();
    }

    @Override
    public List<Payment> findByReservationId(int reservationId) {
        return store.stream().filter(p -> p.getReservation().getId() == reservationId).toList();
    }

    @Override
    public Payment save(Payment payment) {
        if (payment.getId() == 0) {
            Payment persisted = withId(payment, nextId++);
            store.add(persisted);
            return persisted;
        }
        update(payment);
        return payment;
    }

    @Override
    public void update(Payment payment) {
        store.removeIf(p -> p.getId() == payment.getId());
        store.add(payment);
    }

    private Payment withId(Payment payment, int id) {
        try {
            return new Payment(id, payment.getReservation(), payment.getAmount(), payment.getMethod(),
                    payment.getStatus(), payment.getPaymentDate());
        } catch (ValidationException e) {
            throw new DataAccessException("Failed to assign id to in-memory payment", e);
        }
    }
}

package com.hotelreserve.model;

import com.hotelreserve.exception.ValidationException;

import java.time.LocalDateTime;

public class Payment {

    private final int id;
    private Reservation reservation;
    private double amount;
    private PaymentMethod method;
    private PaymentStatus status;
    private final LocalDateTime paymentDate;

    public Payment(int id, Reservation reservation, double amount, PaymentMethod method, PaymentStatus status,
                   LocalDateTime paymentDate) throws ValidationException {
        validate(reservation, amount, method);
        this.id = id;
        this.reservation = reservation;
        this.amount = amount;
        this.method = method;
        this.status = status == null ? PaymentStatus.PENDING : status;
        this.paymentDate = paymentDate == null ? LocalDateTime.now() : paymentDate;
    }

    private static void validate(Reservation reservation, double amount, PaymentMethod method)
            throws ValidationException {
        if (reservation == null) {
            throw new ValidationException("A reservation is required");
        }
        if (amount <= 0) {
            throw new ValidationException("Payment amount must be greater than zero");
        }
        if (method == null) {
            throw new ValidationException("A payment method is required");
        }
    }

    public int getId() {
        return id;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public void setReservation(Reservation reservation) throws ValidationException {
        if (reservation == null) {
            throw new ValidationException("A reservation is required");
        }
        this.reservation = reservation;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) throws ValidationException {
        if (amount <= 0) {
            throw new ValidationException("Payment amount must be greater than zero");
        }
        this.amount = amount;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public void setMethod(PaymentMethod method) throws ValidationException {
        if (method == null) {
            throw new ValidationException("A payment method is required");
        }
        this.method = method;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) throws ValidationException {
        if (status == null) {
            throw new ValidationException("Payment status is required");
        }
        this.status = status;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }
}

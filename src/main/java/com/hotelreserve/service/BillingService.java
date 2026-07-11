package com.hotelreserve.service;

import com.hotelreserve.dao.PaymentRepository;
import com.hotelreserve.exception.ValidationException;
import com.hotelreserve.model.Payment;
import com.hotelreserve.model.PaymentMethod;
import com.hotelreserve.model.PaymentStatus;
import com.hotelreserve.model.Reservation;

import java.util.List;

/**
 * Generates itemized invoices from a {@link PricingEngine} quote and records payments
 * against a reservation, tracking how much of the total has been settled.
 */
public class BillingService {

    private final PaymentRepository paymentRepository;
    private final PricingEngine pricingEngine;

    public BillingService(PaymentRepository paymentRepository) {
        this(paymentRepository, new PricingEngine());
    }

    public BillingService(PaymentRepository paymentRepository, PricingEngine pricingEngine) {
        this.paymentRepository = paymentRepository;
        this.pricingEngine = pricingEngine;
    }

    public record Invoice(Reservation reservation, PricingEngine.PriceBreakdown breakdown, double amountPaid,
                           double balanceDue) {
    }

    public Invoice generateInvoice(Reservation reservation) throws ValidationException {
        PricingEngine.PriceBreakdown breakdown = pricingEngine.computeQuote(
                reservation.getRoom(), reservation.getCheckInDate(), reservation.getCheckOutDate());

        double amountPaid = paymentRepository.findByReservationId(reservation.getId()).stream()
                .filter(p -> p.getStatus() == PaymentStatus.PAID)
                .mapToDouble(Payment::getAmount)
                .sum();

        double balanceDue = Math.max(0.0, breakdown.total() - amountPaid);
        return new Invoice(reservation, breakdown, amountPaid, balanceDue);
    }

    public Payment recordPayment(Reservation reservation, double amount, PaymentMethod method)
            throws ValidationException {
        Invoice invoice = generateInvoice(reservation);
        double totalPaidAfter = invoice.amountPaid() + amount;
        PaymentStatus status = totalPaidAfter >= invoice.breakdown().total() ? PaymentStatus.PAID : PaymentStatus.PENDING;

        Payment payment = new Payment(0, reservation, amount, method, status, java.time.LocalDateTime.now());
        return paymentRepository.save(payment);
    }

    public List<Payment> paymentsFor(Reservation reservation) {
        return paymentRepository.findByReservationId(reservation.getId());
    }
}

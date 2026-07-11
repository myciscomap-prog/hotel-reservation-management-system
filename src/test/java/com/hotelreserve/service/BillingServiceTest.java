package com.hotelreserve.service;

import com.hotelreserve.dao.InMemoryPaymentRepository;
import com.hotelreserve.exception.ValidationException;
import com.hotelreserve.model.Guest;
import com.hotelreserve.model.PaymentMethod;
import com.hotelreserve.model.PaymentStatus;
import com.hotelreserve.model.Reservation;
import com.hotelreserve.model.ReservationStatus;
import com.hotelreserve.model.Room;
import com.hotelreserve.model.RoomStatus;
import com.hotelreserve.model.StandardRoom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BillingServiceTest {

    private InMemoryPaymentRepository paymentRepository;
    private BillingService billingService;
    private Reservation reservation;

    @BeforeEach
    void setUp() throws ValidationException {
        paymentRepository = new InMemoryPaymentRepository();
        billingService = new BillingService(paymentRepository);

        Guest guest = new Guest(1, "Ama Owusu", "ama@example.com", "0244000000", "GHA-000000000-0");
        Room room = new StandardRoom(1, "101", 1, RoomStatus.AVAILABLE, 2, 100.0);
        // Mon Jan 5 to Wed Jan 7, 2026 — 2 weekday nights, total GHS 200, no discount/surcharge.
        reservation = new Reservation(1, guest, room, LocalDate.of(2026, 1, 5), LocalDate.of(2026, 1, 7),
                2, ReservationStatus.CONFIRMED, LocalDateTime.now());
    }

    @Test
    void generateInvoiceComputesExpectedTotal() throws ValidationException {
        BillingService.Invoice invoice = billingService.generateInvoice(reservation);
        assertEquals(200.0, invoice.breakdown().total(), 0.0001);
        assertEquals(0.0, invoice.amountPaid(), 0.0001);
        assertEquals(200.0, invoice.balanceDue(), 0.0001);
    }

    @Test
    void partialPaymentLeavesStatusPending() throws ValidationException {
        billingService.recordPayment(reservation, 100.0, PaymentMethod.CASH);
        BillingService.Invoice invoice = billingService.generateInvoice(reservation);
        assertEquals(0.0, invoice.amountPaid(), 0.0001); // PENDING payment doesn't count as paid yet
        assertEquals(200.0, invoice.balanceDue(), 0.0001);
    }

    @Test
    void fullPaymentMarksStatusPaidAndClearsBalance() throws ValidationException {
        var payment = billingService.recordPayment(reservation, 200.0, PaymentMethod.MOBILE_MONEY);
        assertEquals(PaymentStatus.PAID, payment.getStatus());

        BillingService.Invoice invoice = billingService.generateInvoice(reservation);
        assertEquals(200.0, invoice.amountPaid(), 0.0001);
        assertEquals(0.0, invoice.balanceDue(), 0.0001);
        assertTrue(invoice.balanceDue() <= 0.0001);
    }
}

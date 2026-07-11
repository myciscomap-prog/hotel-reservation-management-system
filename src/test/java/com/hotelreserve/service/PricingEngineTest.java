package com.hotelreserve.service;

import com.hotelreserve.exception.ValidationException;
import com.hotelreserve.model.Room;
import com.hotelreserve.model.RoomStatus;
import com.hotelreserve.model.StandardRoom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * All dates below are anchored to January 2026: Jan 1, 2026 is a Thursday, so
 * Jan 2 (Fri) and Jan 3 (Sat) are the only weekend nights in the first week —
 * this makes the weekend-surcharge math easy to hand-verify.
 */
class PricingEngineTest {

    private PricingEngine pricingEngine;
    private Room room;

    @BeforeEach
    void setUp() throws ValidationException {
        pricingEngine = new PricingEngine();
        room = new StandardRoom(1, "101", 1, RoomStatus.AVAILABLE, 2, 100.0);
    }

    @Test
    void twoNightStayNoWeekendHasNoDiscountOrSurcharge() throws ValidationException {
        // Mon 5th to Wed 7th Jan 2026 — 2 weekday nights, no discount tier, no weekend surcharge.
        PricingEngine.PriceBreakdown quote = pricingEngine.computeQuote(room,
                LocalDate.of(2026, 1, 5), LocalDate.of(2026, 1, 7));
        assertEquals(2, quote.nights());
        assertEquals(200.0, quote.subtotal(), 0.0001);
        assertEquals(0.0, quote.discount(), 0.0001);
        assertEquals(0.0, quote.surcharge(), 0.0001);
        assertEquals(200.0, quote.total(), 0.0001);
    }

    @Test
    void threeToSixNightStayGetsFivePercentDiscount() throws ValidationException {
        // Mon 5th to Thu 8th Jan 2026 — 3 weekday nights, medium-stay discount tier.
        PricingEngine.PriceBreakdown quote = pricingEngine.computeQuote(room,
                LocalDate.of(2026, 1, 5), LocalDate.of(2026, 1, 8));
        assertEquals(3, quote.nights());
        assertEquals(300.0, quote.subtotal(), 0.0001);
        assertEquals(15.0, quote.discount(), 0.0001);
        assertEquals(0.0, quote.surcharge(), 0.0001);
        assertEquals(285.0, quote.total(), 0.0001);
    }

    @Test
    void sevenPlusNightStayGetsTenPercentDiscountPlusWeekendSurcharge() throws ValidationException {
        // Thu Jan 1 to Thu Jan 8, 2026 — 7 nights, spans Fri 2nd and Sat 3rd (2 weekend nights).
        PricingEngine.PriceBreakdown quote = pricingEngine.computeQuote(room,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 8));
        assertEquals(7, quote.nights());
        assertEquals(700.0, quote.subtotal(), 0.0001);
        assertEquals(70.0, quote.discount(), 0.0001);
        assertEquals(30.0, quote.surcharge(), 0.0001);
        assertEquals(660.0, quote.total(), 0.0001);
    }

    @Test
    void weekendOnlyStayAppliesSurchargeWithoutDiscount() throws ValidationException {
        // Fri Jan 2 to Sun Jan 4, 2026 — 2 nights, both Fri and Sat, no discount tier.
        PricingEngine.PriceBreakdown quote = pricingEngine.computeQuote(room,
                LocalDate.of(2026, 1, 2), LocalDate.of(2026, 1, 4));
        assertEquals(2, quote.nights());
        assertEquals(200.0, quote.subtotal(), 0.0001);
        assertEquals(0.0, quote.discount(), 0.0001);
        assertEquals(30.0, quote.surcharge(), 0.0001);
        assertEquals(230.0, quote.total(), 0.0001);
    }

    @Test
    void checkOutNotAfterCheckInThrows() {
        LocalDate date = LocalDate.of(2026, 1, 5);
        assertThrows(ValidationException.class, () -> pricingEngine.computeQuote(room, date, date));
    }
}

package com.hotelreserve.service;

import com.hotelreserve.exception.ValidationException;
import com.hotelreserve.model.Room;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Computes a per-stay price quote for a room. Two adjustments stack on top of the base
 * nightly rate: a length-of-stay discount (rewards longer bookings) and a weekend
 * surcharge (Friday/Saturday nights cost more, reflecting real demand patterns).
 */
public class PricingEngine {

    private static final double WEEKEND_SURCHARGE_RATE = 0.15;
    private static final double MEDIUM_STAY_DISCOUNT_RATE = 0.05;
    private static final double LONG_STAY_DISCOUNT_RATE = 0.10;
    private static final int MEDIUM_STAY_NIGHTS = 3;
    private static final int LONG_STAY_NIGHTS = 7;

    public record PriceBreakdown(double subtotal, double discount, double surcharge, double total, long nights) {
    }

    public PriceBreakdown computeQuote(Room room, LocalDate checkIn, LocalDate checkOut) throws ValidationException {
        if (room == null) {
            throw new ValidationException("A room is required to compute a price quote");
        }
        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            throw new ValidationException("Check-out date must be after the check-in date");
        }

        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        double nightlyRate = room.nightlyRate();
        double subtotal = nightlyRate * nights;

        double discount;
        if (nights >= LONG_STAY_NIGHTS) {
            discount = subtotal * LONG_STAY_DISCOUNT_RATE;
        } else if (nights >= MEDIUM_STAY_NIGHTS) {
            discount = subtotal * MEDIUM_STAY_DISCOUNT_RATE;
        } else {
            discount = 0.0;
        }

        double surcharge = 0.0;
        LocalDate date = checkIn;
        while (date.isBefore(checkOut)) {
            DayOfWeek day = date.getDayOfWeek();
            if (day == DayOfWeek.FRIDAY || day == DayOfWeek.SATURDAY) {
                surcharge += nightlyRate * WEEKEND_SURCHARGE_RATE;
            }
            date = date.plusDays(1);
        }

        double total = subtotal - discount + surcharge;
        return new PriceBreakdown(subtotal, discount, surcharge, total, nights);
    }
}

package com.hotelreserve.model;

import com.hotelreserve.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GuestTest {

    @Test
    void validGuestConstructsFine() {
        Guest guest = assertDoesNotThrow(() ->
                new Guest(1, "Ama Owusu", "ama@example.com", "0244000000", "GHA-000000000-0"));
        assertEquals("Ama Owusu", guest.getFullName());
    }

    @Test
    void badEmailThrows() {
        assertThrows(ValidationException.class,
                () -> new Guest(0, "Kwame Boateng", "not-an-email", "0201234567", "GHA-111111111-1"));
    }

    @Test
    void blankFullNameThrows() {
        assertThrows(ValidationException.class,
                () -> new Guest(0, "  ", "kwame@example.com", "0201234567", "GHA-111111111-1"));
    }

    @Test
    void blankPhoneThrows() {
        assertThrows(ValidationException.class,
                () -> new Guest(0, "Kwame Boateng", "kwame@example.com", "", "GHA-111111111-1"));
    }

    @Test
    void blankIdNumberThrows() {
        assertThrows(ValidationException.class,
                () -> new Guest(0, "Kwame Boateng", "kwame@example.com", "0201234567", " "));
    }
}

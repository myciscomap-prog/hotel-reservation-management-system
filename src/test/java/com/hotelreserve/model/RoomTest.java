package com.hotelreserve.model;

import com.hotelreserve.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoomTest {

    @Test
    void blankRoomNumberThrows() {
        assertThrows(ValidationException.class,
                () -> new StandardRoom(0, "  ", 1, RoomStatus.AVAILABLE, 2, 100.0));
    }

    @Test
    void nonPositiveBaseRateThrows() {
        assertThrows(ValidationException.class,
                () -> new StandardRoom(0, "101", 1, RoomStatus.AVAILABLE, 2, 0.0));
        assertThrows(ValidationException.class,
                () -> new StandardRoom(0, "101", 1, RoomStatus.AVAILABLE, 2, -50.0));
    }

    @Test
    void nonPositiveMaxOccupancyThrows() {
        assertThrows(ValidationException.class,
                () -> new StandardRoom(0, "101", 1, RoomStatus.AVAILABLE, 0, 100.0));
    }

    @Test
    void validRoomConstructsSuccessfully() {
        assertDoesNotThrow(() -> new StandardRoom(1, "101", 1, RoomStatus.AVAILABLE, 2, 350.0));
    }

    @Test
    void standardRoomPolymorphism() throws ValidationException {
        Room room = new StandardRoom(1, "101", 1, RoomStatus.AVAILABLE, 2, 350.0);
        assertEquals(350.0, room.nightlyRate(), 0.0001);
        assertEquals("STANDARD", room.roomType());
        assertTrue(room.amenities().contains("Wi-Fi"));
        assertTrue(room.amenities().size() == 3);
    }

    @Test
    void deluxeRoomPolymorphism() throws ValidationException {
        Room room = new DeluxeRoom(1, "201", 2, RoomStatus.AVAILABLE, 3, 600.0);
        assertEquals(600.0, room.nightlyRate(), 0.0001);
        assertEquals("DELUXE", room.roomType());
        assertTrue(room.amenities().contains("Mini-Bar"));
        assertTrue(room.amenities().size() > 3);
    }

    @Test
    void suiteRoomPolymorphism() throws ValidationException {
        Room room = new SuiteRoom(1, "301", 3, RoomStatus.AVAILABLE, 4, 1200.0);
        assertEquals(1200.0, room.nightlyRate(), 0.0001);
        assertEquals("SUITE", room.roomType());
        assertTrue(room.amenities().contains("Jacuzzi"));
        assertTrue(room.amenities().size() > 6);
    }

    @Test
    void setBaseRateValidates() throws ValidationException {
        Room room = new StandardRoom(1, "101", 1, RoomStatus.AVAILABLE, 2, 350.0);
        assertThrows(ValidationException.class, () -> room.setBaseRate(-1.0));
        assertDoesNotThrow(() -> room.setBaseRate(400.0));
        assertEquals(400.0, room.getBaseRate(), 0.0001);
    }
}

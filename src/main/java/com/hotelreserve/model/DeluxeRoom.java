package com.hotelreserve.model;

import com.hotelreserve.exception.ValidationException;

import java.util.List;

public class DeluxeRoom extends Room {

    public DeluxeRoom(int id, String roomNumber, int floor, RoomStatus status, int maxOccupancy, double baseRate)
            throws ValidationException {
        super(id, roomNumber, floor, status, maxOccupancy, baseRate);
    }

    @Override
    public double nightlyRate() {
        return getBaseRate();
    }

    @Override
    public List<String> amenities() {
        return List.of("Wi-Fi", "TV", "Air Conditioning", "Mini-Bar", "Balcony", "Room Service");
    }

    @Override
    public String roomType() {
        return "DELUXE";
    }
}

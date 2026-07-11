package com.hotelreserve.model;

import com.hotelreserve.exception.ValidationException;

import java.util.List;

/**
 * A physical hotel room. Concrete subtypes (StandardRoom, DeluxeRoom, SuiteRoom) determine
 * the nightly rate policy and the amenities included — see {@link #nightlyRate()} and
 * {@link #amenities()}.
 */
public abstract class Room {

    private final int id;
    private String roomNumber;
    private int floor;
    private RoomStatus status;
    private int maxOccupancy;
    private double baseRate;

    protected Room(int id, String roomNumber, int floor, RoomStatus status, int maxOccupancy, double baseRate)
            throws ValidationException {
        validate(roomNumber, maxOccupancy, baseRate);
        this.id = id;
        this.roomNumber = roomNumber.trim();
        this.floor = floor;
        this.status = status == null ? RoomStatus.AVAILABLE : status;
        this.maxOccupancy = maxOccupancy;
        this.baseRate = baseRate;
    }

    private static void validate(String roomNumber, int maxOccupancy, double baseRate) throws ValidationException {
        if (roomNumber == null || roomNumber.isBlank()) {
            throw new ValidationException("Room number is required");
        }
        if (maxOccupancy <= 0) {
            throw new ValidationException("Max occupancy must be positive");
        }
        if (baseRate <= 0) {
            throw new ValidationException("Base rate must be greater than zero");
        }
    }

    /** Per-night rate for this room type before length-of-stay/weekend adjustments. */
    public abstract double nightlyRate();

    /** Amenities included with this room type. */
    public abstract List<String> amenities();

    /** Discriminator string persisted in the database ("STANDARD", "DELUXE", "SUITE"). */
    public abstract String roomType();

    public int getId() {
        return id;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) throws ValidationException {
        if (roomNumber == null || roomNumber.isBlank()) {
            throw new ValidationException("Room number is required");
        }
        this.roomNumber = roomNumber.trim();
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public RoomStatus getStatus() {
        return status;
    }

    public void setStatus(RoomStatus status) {
        this.status = status == null ? RoomStatus.AVAILABLE : status;
    }

    public int getMaxOccupancy() {
        return maxOccupancy;
    }

    public void setMaxOccupancy(int maxOccupancy) throws ValidationException {
        if (maxOccupancy <= 0) {
            throw new ValidationException("Max occupancy must be positive");
        }
        this.maxOccupancy = maxOccupancy;
    }

    public double getBaseRate() {
        return baseRate;
    }

    public void setBaseRate(double baseRate) throws ValidationException {
        if (baseRate <= 0) {
            throw new ValidationException("Base rate must be greater than zero");
        }
        this.baseRate = baseRate;
    }

    @Override
    public String toString() {
        return roomNumber + " (" + roomType() + ", Floor " + floor + ", " + status + ")";
    }
}

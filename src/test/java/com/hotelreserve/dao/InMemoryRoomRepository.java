package com.hotelreserve.dao;

import com.hotelreserve.exception.DataAccessException;
import com.hotelreserve.exception.ValidationException;
import com.hotelreserve.model.DeluxeRoom;
import com.hotelreserve.model.Room;
import com.hotelreserve.model.StandardRoom;
import com.hotelreserve.model.SuiteRoom;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InMemoryRoomRepository implements RoomRepository {

    private final List<Room> store = new ArrayList<>();
    private int nextId = 1;

    @Override
    public List<Room> findAll() {
        return List.copyOf(store);
    }

    @Override
    public Optional<Room> findById(int id) {
        return store.stream().filter(r -> r.getId() == id).findFirst();
    }

    @Override
    public Room save(Room room) {
        if (room.getId() == 0) {
            Room persisted = withId(room, nextId++);
            store.add(persisted);
            return persisted;
        }
        update(room);
        return room;
    }

    @Override
    public void update(Room room) {
        store.removeIf(r -> r.getId() == room.getId());
        store.add(room);
    }

    @Override
    public void delete(int id) {
        store.removeIf(r -> r.getId() == id);
    }

    private Room withId(Room room, int id) {
        try {
            return switch (room.roomType()) {
                case "DELUXE" -> new DeluxeRoom(id, room.getRoomNumber(), room.getFloor(), room.getStatus(),
                        room.getMaxOccupancy(), room.getBaseRate());
                case "SUITE" -> new SuiteRoom(id, room.getRoomNumber(), room.getFloor(), room.getStatus(),
                        room.getMaxOccupancy(), room.getBaseRate());
                default -> new StandardRoom(id, room.getRoomNumber(), room.getFloor(), room.getStatus(),
                        room.getMaxOccupancy(), room.getBaseRate());
            };
        } catch (ValidationException e) {
            throw new DataAccessException("Failed to assign id to in-memory room", e);
        }
    }
}

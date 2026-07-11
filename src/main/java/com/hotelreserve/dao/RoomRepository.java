package com.hotelreserve.dao;

import com.hotelreserve.model.Room;

import java.util.List;
import java.util.Optional;

public interface RoomRepository {
    List<Room> findAll();
    Optional<Room> findById(int id);
    Room save(Room room);
    void update(Room room);
    void delete(int id);
}

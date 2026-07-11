package com.hotelreserve.dao;

import com.hotelreserve.model.Guest;

import java.util.List;
import java.util.Optional;

public interface GuestRepository {
    List<Guest> findAll();
    Optional<Guest> findById(int id);
    Guest save(Guest guest);
    void update(Guest guest);
    void delete(int id);
}

package com.hotelreserve.dao;

import com.hotelreserve.exception.DataAccessException;
import com.hotelreserve.exception.ValidationException;
import com.hotelreserve.model.Guest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InMemoryGuestRepository implements GuestRepository {

    private final List<Guest> store = new ArrayList<>();
    private int nextId = 1;

    @Override
    public List<Guest> findAll() {
        return List.copyOf(store);
    }

    @Override
    public Optional<Guest> findById(int id) {
        return store.stream().filter(g -> g.getId() == id).findFirst();
    }

    @Override
    public Guest save(Guest guest) {
        if (guest.getId() == 0) {
            Guest persisted = withId(guest, nextId++);
            store.add(persisted);
            return persisted;
        }
        update(guest);
        return guest;
    }

    @Override
    public void update(Guest guest) {
        store.removeIf(g -> g.getId() == guest.getId());
        store.add(guest);
    }

    @Override
    public void delete(int id) {
        store.removeIf(g -> g.getId() == id);
    }

    private Guest withId(Guest guest, int id) {
        try {
            return new Guest(id, guest.getFullName(), guest.getEmail(), guest.getPhone(), guest.getIdNumber());
        } catch (ValidationException e) {
            throw new DataAccessException("Failed to assign id to in-memory guest", e);
        }
    }
}

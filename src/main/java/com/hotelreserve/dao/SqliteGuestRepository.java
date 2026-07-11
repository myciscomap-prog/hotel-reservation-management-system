package com.hotelreserve.dao;

import com.hotelreserve.exception.DataAccessException;
import com.hotelreserve.exception.ValidationException;
import com.hotelreserve.model.Guest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteGuestRepository implements GuestRepository {

    @Override
    public Guest save(Guest guest) {
        String sql = guest.getId() == 0
                ? "INSERT INTO guests (full_name, email, phone, id_number) VALUES (?, ?, ?, ?)"
                : "UPDATE guests SET full_name = ?, email = ?, phone = ?, id_number = ? WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, guest.getFullName());
            ps.setString(2, guest.getEmail());
            ps.setString(3, guest.getPhone());
            ps.setString(4, guest.getIdNumber());
            if (guest.getId() != 0) {
                ps.setInt(5, guest.getId());
            }
            ps.executeUpdate();

            if (guest.getId() == 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        return new Guest(keys.getInt(1), guest.getFullName(), guest.getEmail(), guest.getPhone(),
                                guest.getIdNumber());
                    }
                }
            }
            return guest;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to save guest", e);
        } catch (ValidationException e) {
            throw new DataAccessException("Guest returned from database was invalid", e);
        }
    }

    @Override
    public void update(Guest guest) {
        save(guest);
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM guests WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete guest", e);
        }
    }

    @Override
    public List<Guest> findAll() {
        return query("SELECT id, full_name, email, phone, id_number FROM guests ORDER BY full_name", ps -> {});
    }

    @Override
    public Optional<Guest> findById(int id) {
        return query("SELECT id, full_name, email, phone, id_number FROM guests WHERE id = ?",
                ps -> ps.setInt(1, id)).stream().findFirst();
    }

    public List<Guest> searchByName(String namePart) {
        return query("SELECT id, full_name, email, phone, id_number FROM guests WHERE full_name LIKE ? ORDER BY full_name",
                ps -> ps.setString(1, "%" + namePart + "%"));
    }

    private interface StatementBinder {
        void bind(PreparedStatement ps) throws SQLException;
    }

    private List<Guest> query(String sql, StatementBinder binder) {
        List<Guest> results = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            binder.bind(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
            return results;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to load guests", e);
        }
    }

    private Guest mapRow(ResultSet rs) throws SQLException {
        try {
            return new Guest(rs.getInt("id"), rs.getString("full_name"), rs.getString("email"),
                    rs.getString("phone"), rs.getString("id_number"));
        } catch (ValidationException e) {
            throw new DataAccessException("Guest row in database was invalid", e);
        }
    }
}

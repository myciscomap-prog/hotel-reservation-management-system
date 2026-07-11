package com.hotelreserve.dao;

import com.hotelreserve.exception.DataAccessException;
import com.hotelreserve.exception.ValidationException;
import com.hotelreserve.model.DeluxeRoom;
import com.hotelreserve.model.Room;
import com.hotelreserve.model.RoomStatus;
import com.hotelreserve.model.StandardRoom;
import com.hotelreserve.model.SuiteRoom;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteRoomRepository implements RoomRepository {

    @Override
    public Room save(Room room) {
        String sql = room.getId() == 0
                ? "INSERT INTO rooms (room_number, room_type, floor, max_occupancy, base_rate, status) VALUES (?, ?, ?, ?, ?, ?)"
                : "UPDATE rooms SET room_number = ?, room_type = ?, floor = ?, max_occupancy = ?, base_rate = ?, status = ? WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, room.getRoomNumber());
            ps.setString(2, room.roomType());
            ps.setInt(3, room.getFloor());
            ps.setInt(4, room.getMaxOccupancy());
            ps.setDouble(5, room.getBaseRate());
            ps.setString(6, room.getStatus().name());
            if (room.getId() != 0) {
                ps.setInt(7, room.getId());
            }
            ps.executeUpdate();

            if (room.getId() == 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        return build(keys.getInt(1), room.roomType(), room.getRoomNumber(), room.getFloor(),
                                room.getStatus(), room.getMaxOccupancy(), room.getBaseRate());
                    }
                }
            }
            return room;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to save room", e);
        } catch (ValidationException e) {
            throw new DataAccessException("Room returned from database was invalid", e);
        }
    }

    @Override
    public void update(Room room) {
        save(room);
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM rooms WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete room", e);
        }
    }

    @Override
    public List<Room> findAll() {
        return query("SELECT id, room_number, room_type, floor, max_occupancy, base_rate, status " +
                "FROM rooms ORDER BY room_number", ps -> {});
    }

    @Override
    public Optional<Room> findById(int id) {
        List<Room> found = query("SELECT id, room_number, room_type, floor, max_occupancy, base_rate, status " +
                "FROM rooms WHERE id = ?", ps -> ps.setInt(1, id));
        return found.stream().findFirst();
    }

    public List<Room> findByStatus(RoomStatus status) {
        return query("SELECT id, room_number, room_type, floor, max_occupancy, base_rate, status " +
                "FROM rooms WHERE status = ? ORDER BY room_number", ps -> ps.setString(1, status.name()));
    }

    private interface StatementBinder {
        void bind(PreparedStatement ps) throws SQLException;
    }

    private List<Room> query(String sql, StatementBinder binder) {
        List<Room> results = new ArrayList<>();
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
            throw new DataAccessException("Failed to load rooms", e);
        }
    }

    private Room mapRow(ResultSet rs) throws SQLException {
        try {
            return build(
                    rs.getInt("id"),
                    rs.getString("room_type"),
                    rs.getString("room_number"),
                    rs.getInt("floor"),
                    RoomStatus.valueOf(rs.getString("status")),
                    rs.getInt("max_occupancy"),
                    rs.getDouble("base_rate")
            );
        } catch (ValidationException e) {
            throw new DataAccessException("Room row in database was invalid", e);
        }
    }

    /** The one acceptable place for a switch-on-string: reconstructing the correct Room subtype from the stored discriminator. */
    private Room build(int id, String roomType, String roomNumber, int floor, RoomStatus status,
                        int maxOccupancy, double baseRate) throws ValidationException {
        return switch (roomType) {
            case "DELUXE" -> new DeluxeRoom(id, roomNumber, floor, status, maxOccupancy, baseRate);
            case "SUITE" -> new SuiteRoom(id, roomNumber, floor, status, maxOccupancy, baseRate);
            default -> new StandardRoom(id, roomNumber, floor, status, maxOccupancy, baseRate);
        };
    }
}

package com.hotelreserve.dao;

import com.hotelreserve.exception.DataAccessException;
import com.hotelreserve.exception.ValidationException;
import com.hotelreserve.model.Guest;
import com.hotelreserve.model.Reservation;
import com.hotelreserve.model.ReservationStatus;
import com.hotelreserve.model.Room;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteReservationRepository implements ReservationRepository {

    private final GuestRepository guestRepository;
    private final RoomRepository roomRepository;

    public SqliteReservationRepository(GuestRepository guestRepository, RoomRepository roomRepository) {
        this.guestRepository = guestRepository;
        this.roomRepository = roomRepository;
    }

    @Override
    public Reservation save(Reservation reservation) {
        String sql = reservation.getId() == 0
                ? "INSERT INTO reservations (guest_id, room_id, check_in_date, check_out_date, number_of_guests, status, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)"
                : "UPDATE reservations SET guest_id = ?, room_id = ?, check_in_date = ?, check_out_date = ?, number_of_guests = ?, status = ?, created_at = ? WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, reservation.getGuest().getId());
            ps.setInt(2, reservation.getRoom().getId());
            ps.setString(3, reservation.getCheckInDate().toString());
            ps.setString(4, reservation.getCheckOutDate().toString());
            ps.setInt(5, reservation.getNumberOfGuests());
            ps.setString(6, reservation.getStatus().name());
            ps.setString(7, reservation.getCreatedAt().toString());
            if (reservation.getId() != 0) {
                ps.setInt(8, reservation.getId());
            }
            ps.executeUpdate();

            if (reservation.getId() == 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        return new Reservation(keys.getInt(1), reservation.getGuest(), reservation.getRoom(),
                                reservation.getCheckInDate(), reservation.getCheckOutDate(),
                                reservation.getNumberOfGuests(), reservation.getStatus(), reservation.getCreatedAt());
                    }
                }
            }
            return reservation;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to save reservation", e);
        } catch (ValidationException e) {
            throw new DataAccessException("Reservation returned from database was invalid", e);
        }
    }

    @Override
    public void update(Reservation reservation) {
        save(reservation);
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM reservations WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete reservation", e);
        }
    }

    @Override
    public List<Reservation> findAll() {
        return query("SELECT id, guest_id, room_id, check_in_date, check_out_date, number_of_guests, status, created_at " +
                "FROM reservations ORDER BY check_in_date DESC, id DESC", ps -> {});
    }

    @Override
    public Optional<Reservation> findById(int id) {
        return query("SELECT id, guest_id, room_id, check_in_date, check_out_date, number_of_guests, status, created_at " +
                "FROM reservations WHERE id = ?", ps -> ps.setInt(1, id)).stream().findFirst();
    }

    /**
     * Reservations for a room that overlap the given date range and are still "active"
     * (not CANCELLED or CHECKED_OUT) — used by availability checks before booking.
     */
    @Override
    public List<Reservation> findOverlapping(int roomId, LocalDate checkIn, LocalDate checkOut) {
        return query("SELECT id, guest_id, room_id, check_in_date, check_out_date, number_of_guests, status, created_at " +
                "FROM reservations WHERE room_id = ? AND status NOT IN ('CANCELLED', 'CHECKED_OUT') " +
                "AND check_in_date < ? AND check_out_date > ?",
                ps -> {
                    ps.setInt(1, roomId);
                    ps.setString(2, checkOut.toString());
                    ps.setString(3, checkIn.toString());
                });
    }

    public List<Reservation> findByCheckInDate(LocalDate date) {
        return query("SELECT id, guest_id, room_id, check_in_date, check_out_date, number_of_guests, status, created_at " +
                "FROM reservations WHERE check_in_date = ?", ps -> ps.setString(1, date.toString()));
    }

    public List<Reservation> findByCheckOutDate(LocalDate date) {
        return query("SELECT id, guest_id, room_id, check_in_date, check_out_date, number_of_guests, status, created_at " +
                "FROM reservations WHERE check_out_date = ?", ps -> ps.setString(1, date.toString()));
    }

    private interface StatementBinder {
        void bind(PreparedStatement ps) throws SQLException;
    }

    private List<Reservation> query(String sql, StatementBinder binder) {
        List<Reservation> results = new ArrayList<>();
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
            throw new DataAccessException("Failed to load reservations", e);
        }
    }

    private Reservation mapRow(ResultSet rs) throws SQLException {
        int guestId = rs.getInt("guest_id");
        int roomId = rs.getInt("room_id");
        Guest guest = guestRepository.findById(guestId)
                .orElseThrow(() -> new DataAccessException("Reservation references missing guest id " + guestId, null));
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new DataAccessException("Reservation references missing room id " + roomId, null));

        try {
            return new Reservation(
                    rs.getInt("id"),
                    guest,
                    room,
                    LocalDate.parse(rs.getString("check_in_date")),
                    LocalDate.parse(rs.getString("check_out_date")),
                    rs.getInt("number_of_guests"),
                    ReservationStatus.valueOf(rs.getString("status")),
                    LocalDateTime.parse(rs.getString("created_at"))
            );
        } catch (ValidationException e) {
            throw new DataAccessException("Reservation row in database was invalid", e);
        }
    }
}

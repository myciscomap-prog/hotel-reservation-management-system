package com.hotelreserve.dao;

import com.hotelreserve.exception.DataAccessException;
import com.hotelreserve.exception.ValidationException;
import com.hotelreserve.model.Payment;
import com.hotelreserve.model.PaymentMethod;
import com.hotelreserve.model.PaymentStatus;
import com.hotelreserve.model.Reservation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqlitePaymentRepository implements PaymentRepository {

    private final ReservationRepository reservationRepository;

    public SqlitePaymentRepository(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Override
    public Payment save(Payment payment) {
        String sql = payment.getId() == 0
                ? "INSERT INTO payments (reservation_id, amount, method, status, payment_date) VALUES (?, ?, ?, ?, ?)"
                : "UPDATE payments SET reservation_id = ?, amount = ?, method = ?, status = ?, payment_date = ? WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, payment.getReservation().getId());
            ps.setDouble(2, payment.getAmount());
            ps.setString(3, payment.getMethod().name());
            ps.setString(4, payment.getStatus().name());
            ps.setString(5, payment.getPaymentDate().toString());
            if (payment.getId() != 0) {
                ps.setInt(6, payment.getId());
            }
            ps.executeUpdate();

            if (payment.getId() == 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        return new Payment(keys.getInt(1), payment.getReservation(), payment.getAmount(),
                                payment.getMethod(), payment.getStatus(), payment.getPaymentDate());
                    }
                }
            }
            return payment;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to save payment", e);
        } catch (ValidationException e) {
            throw new DataAccessException("Payment returned from database was invalid", e);
        }
    }

    @Override
    public void update(Payment payment) {
        save(payment);
    }

    @Override
    public List<Payment> findAll() {
        return query("SELECT id, reservation_id, amount, method, status, payment_date FROM payments " +
                "ORDER BY payment_date DESC, id DESC", ps -> {});
    }

    @Override
    public Optional<Payment> findById(int id) {
        return query("SELECT id, reservation_id, amount, method, status, payment_date FROM payments WHERE id = ?",
                ps -> ps.setInt(1, id)).stream().findFirst();
    }

    @Override
    public List<Payment> findByReservationId(int reservationId) {
        return query("SELECT id, reservation_id, amount, method, status, payment_date FROM payments " +
                "WHERE reservation_id = ? ORDER BY payment_date DESC, id DESC", ps -> ps.setInt(1, reservationId));
    }

    private interface StatementBinder {
        void bind(PreparedStatement ps) throws SQLException;
    }

    private List<Payment> query(String sql, StatementBinder binder) {
        List<Payment> results = new ArrayList<>();
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
            throw new DataAccessException("Failed to load payments", e);
        }
    }

    private Payment mapRow(ResultSet rs) throws SQLException {
        int reservationId = rs.getInt("reservation_id");
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new DataAccessException("Payment references missing reservation id " + reservationId, null));

        try {
            return new Payment(
                    rs.getInt("id"),
                    reservation,
                    rs.getDouble("amount"),
                    PaymentMethod.valueOf(rs.getString("method")),
                    PaymentStatus.valueOf(rs.getString("status")),
                    LocalDateTime.parse(rs.getString("payment_date"))
            );
        } catch (ValidationException e) {
            throw new DataAccessException("Payment row in database was invalid", e);
        }
    }
}

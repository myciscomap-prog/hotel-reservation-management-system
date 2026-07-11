package com.hotelreserve.dao;

import com.hotelreserve.exception.DataAccessException;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseManager {

    private static final String DB_FILE = "hotelreserve.db";
    private static final String URL = "jdbc:sqlite:" + Path.of(DB_FILE).toAbsolutePath();

    private DatabaseManager() {
    }

    public static Connection getConnection() {
        try {
            Connection connection = DriverManager.getConnection(URL);
            try (Statement pragma = connection.createStatement()) {
                pragma.execute("PRAGMA foreign_keys = ON");
            }
            return connection;
        } catch (SQLException e) {
            throw new DataAccessException("Could not connect to the database", e);
        }
    }

    public static void initSchema() {
        String rooms = """
                CREATE TABLE IF NOT EXISTS rooms (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    room_number TEXT NOT NULL UNIQUE,
                    room_type TEXT NOT NULL CHECK (room_type IN ('STANDARD', 'DELUXE', 'SUITE')),
                    floor INTEGER NOT NULL,
                    max_occupancy INTEGER NOT NULL,
                    base_rate REAL NOT NULL CHECK (base_rate > 0),
                    status TEXT NOT NULL DEFAULT 'AVAILABLE'
                )""";

        String guests = """
                CREATE TABLE IF NOT EXISTS guests (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    full_name TEXT NOT NULL,
                    email TEXT NOT NULL,
                    phone TEXT NOT NULL,
                    id_number TEXT NOT NULL
                )""";

        String reservations = """
                CREATE TABLE IF NOT EXISTS reservations (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    guest_id INTEGER NOT NULL,
                    room_id INTEGER NOT NULL,
                    check_in_date TEXT NOT NULL,
                    check_out_date TEXT NOT NULL,
                    number_of_guests INTEGER NOT NULL,
                    status TEXT NOT NULL DEFAULT 'PENDING',
                    created_at TEXT NOT NULL,
                    FOREIGN KEY (guest_id) REFERENCES guests(id),
                    FOREIGN KEY (room_id) REFERENCES rooms(id)
                )""";

        String payments = """
                CREATE TABLE IF NOT EXISTS payments (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    reservation_id INTEGER NOT NULL,
                    amount REAL NOT NULL CHECK (amount > 0),
                    method TEXT NOT NULL,
                    status TEXT NOT NULL DEFAULT 'PENDING',
                    payment_date TEXT NOT NULL,
                    FOREIGN KEY (reservation_id) REFERENCES reservations(id)
                )""";

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(rooms);
            stmt.execute(guests);
            stmt.execute(reservations);
            stmt.execute(payments);
            seedRooms(conn);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to initialize database schema", e);
        }
    }

    private static void seedRooms(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement();
             var rs = stmt.executeQuery("SELECT COUNT(*) FROM rooms")) {
            rs.next();
            if (rs.getInt(1) > 0) {
                return;
            }
        }

        Object[][] defaults = {
                {"101", "STANDARD", 1, 2, 350.0},
                {"102", "STANDARD", 1, 2, 350.0},
                {"103", "STANDARD", 1, 2, 350.0},
                {"201", "DELUXE", 2, 3, 600.0},
                {"202", "DELUXE", 2, 3, 600.0},
                {"203", "DELUXE", 2, 3, 650.0},
                {"301", "SUITE", 3, 4, 1200.0},
                {"302", "SUITE", 3, 4, 1350.0},
        };

        String insertSql = """
                INSERT INTO rooms (room_number, room_type, floor, max_occupancy, base_rate, status)
                VALUES (?, ?, ?, ?, ?, 'AVAILABLE')""";
        try (var insert = conn.prepareStatement(insertSql)) {
            for (Object[] row : defaults) {
                insert.setString(1, (String) row[0]);
                insert.setString(2, (String) row[1]);
                insert.setInt(3, (Integer) row[2]);
                insert.setInt(4, (Integer) row[3]);
                insert.setDouble(5, (Double) row[4]);
                insert.executeUpdate();
            }
        }
    }
}

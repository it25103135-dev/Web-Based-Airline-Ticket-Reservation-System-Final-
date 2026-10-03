package com.lankawings.dao;

import com.lankawings.config.DBConnection;
import com.lankawings.model.Booking;
import com.lankawings.util.Validator;
import com.lankawings.util.ValidationException;

import java.sql.*;
import java.util.*;

/** Bookings = the reservation records. Create / Read / Update / Cancel(soft delete, with automatic refund). */
public class BookingDAO {
    private static final String BASE =
            "SELECT b.*,u.Username,f.FlightNo,f.Origin,f.Destination,f.DepartureTime,f.Fare,f.Status AS FlightStatus," +
            "CASE WHEN f.DepartureTime<=SYSDATETIME() THEN 1 ELSE 0 END AS Departed " +
            "FROM Bookings b JOIN Flights f ON b.FlightID=f.FlightID JOIN Users u ON b.UserID=u.UserID";

    private static final String LOCK =
            "SELECT b.UserID,b.BookingStatus,b.PaymentStatus,b.PNR,b.FlightID,f.TotalSeats," +
            "CASE WHEN f.DepartureTime<=SYSDATETIME() THEN 1 ELSE 0 END AS Departed " +
            "FROM Bookings b WITH (UPDLOCK,HOLDLOCK) JOIN Flights f ON b.FlightID=f.FlightID WHERE b.BookingID=?";

    // ------------------------------------------------------------------ CREATE
    public String create(int userId, int flightId, String passenger, String passport, String seat) throws SQLException {
        String pnr = "LW" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                int total;
                try (PreparedStatement lock = c.prepareStatement(
                        "SELECT TotalSeats,Status,CASE WHEN DepartureTime>SYSDATETIME() THEN 1 ELSE 0 END AS Future," +
                        "(SELECT COUNT(*) FROM Bookings WHERE FlightID=? AND BookingStatus<>'CANCELLED') AS Booked " +
                        "FROM Flights WITH (UPDLOCK,HOLDLOCK) WHERE FlightID=?")) {
                    lock.setInt(1, flightId); lock.setInt(2, flightId);
                    try (ResultSet r = lock.executeQuery()) {
                        if (!r.next()) throw new ValidationException("Flight not found.");
                        String st = r.getString("Status");
                        total = r.getInt("TotalSeats");
                        if (r.getInt("Future") == 0) throw new ValidationException("This flight has already departed.");
                        if (!"SCHEDULED".equals(st) && !"DELAYED".equals(st))
                            throw new ValidationException("This flight is not open for booking (status: " + st + ").");
                        if (r.getInt("Booked") >= total) throw new ValidationException("No seats are available on this flight.");
                    }
                }
                Validator.seatInLayout(seat, total);
                if (passportUsed(c, flightId, passport, 0))
                    throw new ValidationException("A passenger with this passport / ID already has an active booking on this flight.");
                try (PreparedStatement s = c.prepareStatement("INSERT INTO Bookings(PNR,UserID,FlightID,PassengerName,PassportNo,SeatNumber) VALUES(?,?,?,?,?,?)")) {
                    s.setString(1, pnr); s.setInt(2, userId); s.setInt(3, flightId); s.setString(4, passenger); s.setString(5, passport); s.setString(6, seat);
                    s.executeUpdate();
                }
                c.commit();
                return pnr;
            } catch (SQLException e) {
                c.rollback();
                if (DBConnection.isDuplicate(e)) throw new ValidationException("Sorry, seat " + seat + " was just taken. Please pick another seat.");
                throw e;
            } catch (RuntimeException e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    private boolean passportUsed(Connection c, int flightId, String passport, int excludeBookingId) throws SQLException {
        try (PreparedStatement p = c.prepareStatement("SELECT 1 FROM Bookings WHERE FlightID=? AND PassportNo=? AND BookingStatus<>'CANCELLED' AND BookingID<>?")) {
            p.setInt(1, flightId); p.setString(2, passport); p.setInt(3, excludeBookingId);
            try (ResultSet r = p.executeQuery()) { return r.next(); }
        }
    }

    // ------------------------------------------------------------------ READ
    public Set<String> takenSeats(int flightId) throws SQLException {
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement("SELECT SeatNumber FROM Bookings WHERE FlightID=? AND BookingStatus<>'CANCELLED'")) {
            p.setInt(1, flightId);
            try (ResultSet r = p.executeQuery()) {
                Set<String> s = new HashSet<>();
                while (r.next()) s.add(r.getString(1).toUpperCase());
                return s;
            }
        }
    }

    public List<Booking> listForUser(int userId) throws SQLException { return list(BASE + " WHERE b.UserID=? ORDER BY b.BookedAt DESC", userId); }
    public List<Booking> all() throws SQLException { return list(BASE + " ORDER BY b.BookedAt DESC"); }
    public Booking find(int id) throws SQLException { List<Booking> l = list(BASE + " WHERE b.BookingID=?", id); return l.isEmpty() ? null : l.get(0); }
    public Booking findByPnr(String pnr) throws SQLException { List<Booking> l = list(BASE + " WHERE b.PNR=?", pnr); return l.isEmpty() ? null : l.get(0); }

    private List<Booking> list(String q, Object... args) throws SQLException {
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement(q)) {
            for (int i = 0; i < args.length; i++) p.setObject(i + 1, args[i]);
            try (ResultSet r = p.executeQuery()) {
                List<Booking> l = new ArrayList<>();
                while (r.next()) l.add(map(r));
                return l;
            }
        }
    }

    public int countForUser(int userId) throws SQLException { return count("SELECT COUNT(*) FROM Bookings WHERE UserID=?", userId); }
    public int countAll() throws SQLException { return count("SELECT COUNT(*) FROM Bookings"); }
    private int count(String q, Object... args) throws SQLException {
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement(q)) {
            for (int i = 0; i < args.length; i++) p.setObject(i + 1, args[i]);
            try (ResultSet r = p.executeQuery()) { r.next(); return r.getInt(1); }
        }
    }

    // ------------------------------------------------------------------ UPDATE
    public Booking updateDetails(int id, int userId, boolean admin, String passenger, String passport, String seat) throws SQLException {
        return updateDetails(id, userId, admin, passenger, passport, seat, false);
    }

    public Booking updateDetails(int id, int userId, boolean admin, String passenger, String passport, String seat, boolean ticketRequired) throws SQLException {
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                int flightId, total;
                try (PreparedStatement p = c.prepareStatement(LOCK)) {
                    p.setInt(1, id);
                    try (ResultSet r = p.executeQuery()) {
                        if (!r.next() || (!admin && r.getInt("UserID") != userId)) throw new ValidationException("Booking not found.");
                        if ("CANCELLED".equals(r.getString("BookingStatus"))) throw new ValidationException("A cancelled booking cannot be edited.");
                        if (r.getInt("Departed") == 1) throw new ValidationException("This flight has already departed, so the booking can no longer be edited.");
                        flightId = r.getInt("FlightID"); total = r.getInt("TotalSeats");
                    }
                }
                if (ticketRequired) {
                    try (PreparedStatement p = c.prepareStatement("SELECT TicketID FROM Tickets WHERE BookingID=? AND TicketStatus='ISSUED'")) {
                        p.setInt(1, id);
                        try (ResultSet r = p.executeQuery()) {
                            if (!r.next()) throw new ValidationException("An issued ticket is required to edit ticket details.");
                        }
                    }
                }
                Validator.seatInLayout(seat, total);
                if (passportUsed(c, flightId, passport, id))
                    throw new ValidationException("Another active booking on this flight already uses this passport / ID.");
                try (PreparedStatement p = c.prepareStatement("UPDATE Bookings SET PassengerName=?,PassportNo=?,SeatNumber=? WHERE BookingID=?")) {
                    p.setString(1, passenger); p.setString(2, passport); p.setString(3, seat); p.setInt(4, id);
                    p.executeUpdate();
                }
                try (PreparedStatement p = c.prepareStatement("UPDATE Tickets SET LastUpdatedAt=SYSDATETIME() WHERE BookingID=?")) {
                    p.setInt(1, id); p.executeUpdate();
                }
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                if (DBConnection.isDuplicate(e)) throw new ValidationException("Seat " + seat + " is already booked. Please choose another seat.");
                throw e;
            } catch (RuntimeException e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        }
        return find(id);
    }

    // ------------------------------------------------------------------ DELETE (cancel + refund)
    /** Cancels the booking, cancels its ticket and refunds the payment if it was paid. Returns the updated booking. */
    public Booking cancel(int id, int userId, boolean admin) throws SQLException {
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                boolean paid;
                try (PreparedStatement p = c.prepareStatement(LOCK)) {
                    p.setInt(1, id);
                    try (ResultSet r = p.executeQuery()) {
                        if (!r.next() || (!admin && r.getInt("UserID") != userId)) throw new ValidationException("Booking not found.");
                        if ("CANCELLED".equals(r.getString("BookingStatus"))) throw new ValidationException("This booking is already cancelled.");
                        if (!admin && r.getInt("Departed") == 1) throw new ValidationException("This flight has already departed and cannot be cancelled.");
                        paid = "PAID".equals(r.getString("PaymentStatus"));
                    }
                }
                try (PreparedStatement p = c.prepareStatement("UPDATE Bookings SET BookingStatus='CANCELLED',PaymentStatus=CASE WHEN PaymentStatus='PAID' THEN 'REFUNDED' ELSE PaymentStatus END WHERE BookingID=?")) {
                    p.setInt(1, id); p.executeUpdate();
                }
                if (paid) {
                    try (PreparedStatement p = c.prepareStatement("UPDATE Payments SET PaymentStatus='REFUNDED' WHERE BookingID=? AND PaymentStatus='SUCCESS'")) {
                        p.setInt(1, id); p.executeUpdate();
                    }
                }
                try (PreparedStatement p = c.prepareStatement("UPDATE Tickets SET TicketStatus='CANCELLED',LastUpdatedAt=SYSDATETIME() WHERE BookingID=?")) {
                    p.setInt(1, id); p.executeUpdate();
                }
                c.commit();
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        }
        return find(id);
    }

    private Booking map(ResultSet r) throws SQLException {
        Booking b = new Booking();
        b.setBookingId(r.getInt("BookingID")); b.setPnr(r.getString("PNR")); b.setUserId(r.getInt("UserID")); b.setFlightId(r.getInt("FlightID"));
        b.setPassengerName(r.getString("PassengerName")); b.setPassportNo(r.getString("PassportNo")); b.setSeatNumber(r.getString("SeatNumber"));
        b.setBookingStatus(r.getString("BookingStatus")); b.setPaymentStatus(r.getString("PaymentStatus")); b.setBookedAt(r.getTimestamp("BookedAt"));
        b.setFlightNo(r.getString("FlightNo")); b.setOrigin(r.getString("Origin")); b.setDestination(r.getString("Destination"));
        b.setDepartureTime(r.getTimestamp("DepartureTime")); b.setFare(r.getBigDecimal("Fare")); b.setUsername(r.getString("Username"));
        b.setFlightStatus(r.getString("FlightStatus")); b.setDeparted(r.getInt("Departed") == 1);
        return b;
    }
}

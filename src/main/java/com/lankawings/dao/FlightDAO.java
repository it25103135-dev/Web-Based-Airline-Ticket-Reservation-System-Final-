package com.lankawings.dao;

import com.lankawings.config.DBConnection;
import com.lankawings.model.Flight;
import com.lankawings.util.ValidationException;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Flight schedule CRUD, search, capacity protection and aircraft schedule-conflict checking. */
public class FlightDAO {
    private static final String SELECT = "SELECT f.*, " +
            "(SELECT COUNT(*) FROM Bookings b WHERE b.FlightID=f.FlightID AND b.BookingStatus<>'CANCELLED') AS BookedSeats " +
            "FROM Flights f";

    public List<Flight> search(String origin, String destination, java.sql.Date date, boolean upcomingOnly) throws SQLException {
        StringBuilder q = new StringBuilder(SELECT + " WHERE 1=1");
        List<Object> args = new ArrayList<>();
        if (origin != null && !origin.isBlank()) { q.append(" AND f.Origin LIKE ?"); args.add("%" + origin.trim() + "%"); }
        if (destination != null && !destination.isBlank()) { q.append(" AND f.Destination LIKE ?"); args.add("%" + destination.trim() + "%"); }
        if (date != null) { q.append(" AND CAST(f.DepartureTime AS DATE)=?"); args.add(date); }
        if (upcomingOnly) q.append(" AND f.DepartureTime>SYSDATETIME()");
        q.append(" ORDER BY f.DepartureTime");
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement(q.toString())) {
            for (int i = 0; i < args.size(); i++) p.setObject(i + 1, args.get(i));
            try (ResultSet r = p.executeQuery()) {
                List<Flight> list = new ArrayList<>();
                while (r.next()) list.add(map(r));
                return list;
            }
        }
    }

    public List<Flight> all() throws SQLException { return search(null, null, null, false); }

    public List<Flight> next(int limit) throws SQLException {
        String sql = "SELECT TOP (?) f.*, (SELECT COUNT(*) FROM Bookings b WHERE b.FlightID=f.FlightID AND b.BookingStatus<>'CANCELLED') AS BookedSeats " +
                "FROM Flights f WHERE f.DepartureTime>SYSDATETIME() ORDER BY f.DepartureTime";
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, limit);
            try (ResultSet r = p.executeQuery()) {
                List<Flight> list = new ArrayList<>(); while (r.next()) list.add(map(r)); return list;
            }
        }
    }

    public int countUpcoming() throws SQLException { return count("DepartureTime>SYSDATETIME() AND Status<>'CANCELLED'"); }
    public int countStatus(String status) throws SQLException {
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement("SELECT COUNT(*) FROM Flights WHERE Status=?")) {
            p.setString(1, status); try (ResultSet r = p.executeQuery()) { r.next(); return r.getInt(1); }
        }
    }
    private int count(String where) throws SQLException {
        try (Connection c = DBConnection.getConnection(); Statement s = c.createStatement(); ResultSet r = s.executeQuery("SELECT COUNT(*) FROM Flights WHERE " + where)) {
            r.next(); return r.getInt(1);
        }
    }

    public Flight find(int id) throws SQLException {
        try (Connection c = DBConnection.getConnection()) { return find(c, id); }
    }

    public void create(String no, String origin, String dest, Timestamp dep, Timestamp arr, BigDecimal fare, int seats, String status, String aircraft) throws SQLException {
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            c.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            try {
                assertNoScheduleConflict(c, 0, aircraft, dep, arr);
                try (PreparedStatement p = c.prepareStatement("INSERT INTO Flights(FlightNo,Origin,Destination,DepartureTime,ArrivalTime,Fare,TotalSeats,Status,Aircraft) VALUES(?,?,?,?,?,?,?,?,?)")) {
                    set(p, no, origin, dest, dep, arr, fare, seats, status, aircraft); p.executeUpdate();
                }
                c.commit();
            } catch (Exception e) {
                c.rollback();
                if (e instanceof SQLException se && DBConnection.isDuplicate(se)) throw new ValidationException("Flight number " + no + " already exists.");
                if (e instanceof SQLException se) throw se;
                if (e instanceof RuntimeException re) throw re;
                throw new SQLException(e);
            } finally { c.setAutoCommit(true); }
        }
    }

    public void update(int id, String no, String origin, String dest, Timestamp dep, Timestamp arr, BigDecimal fare, int seats, String status, String aircraft) throws SQLException {
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            c.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            try {
                Flight existing = find(c, id);
                if (existing == null) throw new ValidationException("Flight not found.");
                if (seats < existing.getBookedSeats()) throw new ValidationException("Total seats cannot be lower than the " + existing.getBookedSeats() + " seats already booked.");
                if (!"CANCELLED".equals(status)) assertNoScheduleConflict(c, id, aircraft, dep, arr);
                try (PreparedStatement p = c.prepareStatement("UPDATE Flights SET FlightNo=?,Origin=?,Destination=?,DepartureTime=?,ArrivalTime=?,Fare=?,TotalSeats=?,Status=?,Aircraft=? WHERE FlightID=?")) {
                    set(p, no, origin, dest, dep, arr, fare, seats, status, aircraft); p.setInt(10, id); p.executeUpdate();
                }
                c.commit();
            } catch (Exception e) {
                c.rollback();
                if (e instanceof SQLException se && DBConnection.isDuplicate(se)) throw new ValidationException("Flight number " + no + " already exists.");
                if (e instanceof SQLException se) throw se;
                if (e instanceof RuntimeException re) throw re;
                throw new SQLException(e);
            } finally { c.setAutoCommit(true); }
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement(
                "DELETE FROM Flights WHERE FlightID=? AND NOT EXISTS(SELECT 1 FROM Bookings WHERE FlightID=?)")) {
            p.setInt(1, id); p.setInt(2, id);
            if (p.executeUpdate() == 0) throw new ValidationException("This flight has bookings (or does not exist) and cannot be deleted. Set its status to CANCELLED instead.");
        }
    }

    /** Overlap rule: existing departure < proposed arrival AND existing arrival > proposed departure. */
    public static boolean overlaps(Timestamp existingDeparture, Timestamp existingArrival, Timestamp proposedDeparture, Timestamp proposedArrival) {
        return existingDeparture.before(proposedArrival) && existingArrival.after(proposedDeparture);
    }

    private void assertNoScheduleConflict(Connection c, int excludeId, String aircraft, Timestamp dep, Timestamp arr) throws SQLException {
        String sql = "SELECT TOP 1 FlightNo,DepartureTime,ArrivalTime FROM Flights WITH (UPDLOCK,HOLDLOCK) " +
                "WHERE Aircraft=? AND FlightID<>? AND Status<>'CANCELLED' AND DepartureTime < ? AND ArrivalTime > ? ORDER BY DepartureTime";
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, aircraft); p.setInt(2, excludeId); p.setTimestamp(3, arr); p.setTimestamp(4, dep);
            try (ResultSet r = p.executeQuery()) {
                if (r.next()) throw new ValidationException("Scheduling conflict: " + aircraft + " is already assigned to flight " + r.getString("FlightNo") +
                        " from " + r.getTimestamp("DepartureTime") + " to " + r.getTimestamp("ArrivalTime") + ".");
            }
        }
    }

    private Flight find(Connection c, int id) throws SQLException {
        try (PreparedStatement p = c.prepareStatement(SELECT + " WHERE f.FlightID=?")) {
            p.setInt(1, id); try (ResultSet r = p.executeQuery()) { return r.next() ? map(r) : null; }
        }
    }

    private void set(PreparedStatement p, String no, String origin, String dest, Timestamp dep, Timestamp arr, BigDecimal fare, int seats, String status, String aircraft) throws SQLException {
        p.setString(1, no); p.setString(2, origin); p.setString(3, dest); p.setTimestamp(4, dep); p.setTimestamp(5, arr);
        p.setBigDecimal(6, fare); p.setInt(7, seats); p.setString(8, status); p.setString(9, aircraft);
    }

    private Flight map(ResultSet r) throws SQLException {
        Flight f = new Flight();
        f.setFlightId(r.getInt("FlightID")); f.setFlightNo(r.getString("FlightNo")); f.setOrigin(r.getString("Origin"));
        f.setDestination(r.getString("Destination")); f.setDepartureTime(r.getTimestamp("DepartureTime")); f.setArrivalTime(r.getTimestamp("ArrivalTime"));
        f.setFare(r.getBigDecimal("Fare")); f.setTotalSeats(r.getInt("TotalSeats")); f.setBookedSeats(r.getInt("BookedSeats"));
        f.setStatus(r.getString("Status")); f.setAircraft(r.getString("Aircraft"));
        return f;
    }
}

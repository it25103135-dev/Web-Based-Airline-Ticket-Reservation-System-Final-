package com.lankawings.dao;

import com.lankawings.config.DBConnection;
import com.lankawings.model.Flight;
import com.lankawings.util.ValidationException;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FlightDAO {
    private static final String SELECT = "SELECT f.*, (SELECT COUNT(*) FROM Bookings b WHERE b.FlightID=f.FlightID AND b.BookingStatus<>'CANCELLED') AS BookedSeats FROM Flights f";

    /** Passenger search. upcomingOnly hides flights that already departed. */
    public List<Flight> search(String origin, String destination, java.sql.Date date, boolean upcomingOnly) throws SQLException {
        StringBuilder q = new StringBuilder(SELECT + " WHERE 1=1");
        List<Object> a = new ArrayList<>();
        if (origin != null && !origin.isBlank()) { q.append(" AND f.Origin LIKE ?"); a.add("%" + origin.trim() + "%"); }
        if (destination != null && !destination.isBlank()) { q.append(" AND f.Destination LIKE ?"); a.add("%" + destination.trim() + "%"); }
        if (date != null) { q.append(" AND CAST(f.DepartureTime AS DATE)=?"); a.add(date); }
        if (upcomingOnly) q.append(" AND f.DepartureTime>SYSDATETIME()");
        q.append(" ORDER BY f.DepartureTime");
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement(q.toString())) {
            for (int i = 0; i < a.size(); i++) p.setObject(i + 1, a.get(i));
            try (ResultSet r = p.executeQuery()) {
                List<Flight> l = new ArrayList<>();
                while (r.next()) l.add(map(r));
                return l;
            }
        }
    }

    public List<Flight> all() throws SQLException { return search(null, null, null, false); }

    public int countUpcoming() throws SQLException {
        try (Connection c = DBConnection.getConnection(); Statement s = c.createStatement();
             ResultSet r = s.executeQuery("SELECT COUNT(*) FROM Flights WHERE DepartureTime>SYSDATETIME() AND Status<>'CANCELLED'")) {
            r.next(); return r.getInt(1);
        }
    }

    public Flight find(int id) throws SQLException {
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement(SELECT + " WHERE f.FlightID=?")) {
            p.setInt(1, id);
            try (ResultSet r = p.executeQuery()) { return r.next() ? map(r) : null; }
        }
    }

    public void create(String no, String origin, String dest, Timestamp dep, Timestamp arr, BigDecimal fare, int seats, String status, String aircraft) throws SQLException {
        String q = "INSERT INTO Flights(FlightNo,Origin,Destination,DepartureTime,ArrivalTime,Fare,TotalSeats,Status,Aircraft) VALUES(?,?,?,?,?,?,?,?,?)";
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement(q)) {
            set(p, no, origin, dest, dep, arr, fare, seats, status, aircraft);
            p.executeUpdate();
        } catch (SQLException e) {
            if (DBConnection.isDuplicate(e)) throw new ValidationException("Flight number " + no + " already exists.");
            throw e;
        }
    }

    public void update(int id, String no, String origin, String dest, Timestamp dep, Timestamp arr, BigDecimal fare, int seats, String status, String aircraft) throws SQLException {
        Flight existing = find(id);
        if (existing == null) throw new ValidationException("Flight not found.");
        if (seats < existing.getBookedSeats())
            throw new ValidationException("Total seats cannot be lower than the " + existing.getBookedSeats() + " seats already booked.");
        String q = "UPDATE Flights SET FlightNo=?,Origin=?,Destination=?,DepartureTime=?,ArrivalTime=?,Fare=?,TotalSeats=?,Status=?,Aircraft=? WHERE FlightID=?";
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement(q)) {
            set(p, no, origin, dest, dep, arr, fare, seats, status, aircraft);
            p.setInt(10, id);
            p.executeUpdate();
        } catch (SQLException e) {
            if (DBConnection.isDuplicate(e)) throw new ValidationException("Flight number " + no + " already exists.");
            throw e;
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement("DELETE FROM Flights WHERE FlightID=? AND NOT EXISTS(SELECT 1 FROM Bookings WHERE FlightID=?)")) {
            p.setInt(1, id); p.setInt(2, id);
            if (p.executeUpdate() == 0) throw new ValidationException("This flight has bookings (or does not exist) and cannot be deleted. Set its status to CANCELLED instead.");
        }
    }

    private void set(PreparedStatement p, String no, String o, String d, Timestamp dep, Timestamp arr, BigDecimal fare, int seats, String status, String air) throws SQLException {
        p.setString(1, no); p.setString(2, o); p.setString(3, d); p.setTimestamp(4, dep); p.setTimestamp(5, arr);
        p.setBigDecimal(6, fare); p.setInt(7, seats); p.setString(8, status); p.setString(9, air);
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

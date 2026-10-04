package com.lankawings.dao;

import com.lankawings.config.DBConnection;
import com.lankawings.model.Booking;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Read-only booking dependency for.
 * No reservation create/edit/cancel functionality is included in this split.
 */
public class BookingDAO {
    private static final String BASE =
        "SELECT b.BookingID,b.UserID,b.FlightID,b.PNR,b.PassengerName,b.SeatNumber,b.BookingStatus,b.PaymentStatus," +
        "f.FlightNo,f.Origin,f.Destination,f.DepartureTime,f.Fare,f.Status AS FlightStatus," +
        "CASE WHEN f.DepartureTime<=SYSDATETIME() THEN 1 ELSE 0 END AS Departed " +
        "FROM Bookings b JOIN Flights f ON b.FlightID=f.FlightID";

    public Booking find(int id) throws SQLException {
        try (Connection c=DBConnection.getConnection(); PreparedStatement p=c.prepareStatement(BASE+" WHERE b.BookingID=?")) {
            p.setInt(1,id);
            try(ResultSet r=p.executeQuery()){ return r.next()?map(r):null; }
        }
    }

    /** Existing unpaid bookings are shown only so the passenger can start the payment use case. */
    public List<Booking> listPayableForUser(int userId) throws SQLException {
        String q=BASE+" WHERE b.UserID=? AND b.PaymentStatus='UNPAID' AND b.BookingStatus<>'CANCELLED' " +
                "AND f.Status<>'CANCELLED' AND f.DepartureTime>SYSDATETIME() ORDER BY f.DepartureTime";
        try(Connection c=DBConnection.getConnection(); PreparedStatement p=c.prepareStatement(q)){
            p.setInt(1,userId);
            try(ResultSet r=p.executeQuery()){
                List<Booking> out=new ArrayList<>(); while(r.next()) out.add(map(r)); return out;
            }
        }
    }

    private Booking map(ResultSet r) throws SQLException {
        Booking b=new Booking();
        b.setBookingId(r.getInt("BookingID")); b.setUserId(r.getInt("UserID")); b.setFlightId(r.getInt("FlightID"));
        b.setPnr(r.getString("PNR")); b.setPassengerName(r.getString("PassengerName")); b.setSeatNumber(r.getString("SeatNumber"));
        b.setBookingStatus(r.getString("BookingStatus")); b.setPaymentStatus(r.getString("PaymentStatus"));
        b.setFlightNo(r.getString("FlightNo")); b.setOrigin(r.getString("Origin")); b.setDestination(r.getString("Destination"));
        b.setDepartureTime(r.getTimestamp("DepartureTime")); b.setFare(r.getBigDecimal("Fare")); b.setFlightStatus(r.getString("FlightStatus"));
        b.setDeparted(r.getInt("Departed")==1); return b;
    }
}

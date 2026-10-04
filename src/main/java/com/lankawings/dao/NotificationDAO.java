package com.lankawings.dao;

import com.lankawings.config.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Flight-change notification integration only. There is intentionally no general notification UI
 * in this isolated function.
 */
public class NotificationDAO {
    public int notifyFlightPassengers(int flightId, String title, String message) throws SQLException {
        String sql = "INSERT INTO Notifications(UserID,Title,Message,Type) " +
                "SELECT DISTINCT b.UserID,?,?, 'FLIGHT_UPDATE' FROM Bookings b " +
                "WHERE b.FlightID=? AND b.BookingStatus<>'CANCELLED'";
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, title);
            p.setString(2, message);
            p.setInt(3, flightId);
            return p.executeUpdate();
        }
    }
}

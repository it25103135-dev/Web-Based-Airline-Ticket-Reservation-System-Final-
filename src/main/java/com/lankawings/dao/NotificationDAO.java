package com.lankawings.dao;

import com.lankawings.config.DBConnection;
import com.lankawings.model.Notification;
import com.lankawings.util.ValidationException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Notifications: create (system events + admin announcements), read, mark read, delete. */
public class NotificationDAO {

    public void add(int userId, String title, String message, String type) throws SQLException {
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement("INSERT INTO Notifications(UserID,Title,Message,Type) VALUES(?,?,?,?)")) {
            p.setInt(1, userId); p.setString(2, title); p.setString(3, message); p.setString(4, type);
            p.executeUpdate();
        }
    }

    /** Announcement to every ACTIVE user. Returns how many people received it. */
    public int broadcast(String title, String message, String type) throws SQLException {
        return bulk("INSERT INTO Notifications(UserID,Title,Message,Type) SELECT UserID,?,?,? FROM Users WHERE Status='ACTIVE'", title, message, type);
    }

    public void notifyAdmins(String title, String message, String type) throws SQLException {
        bulk("INSERT INTO Notifications(UserID,Title,Message,Type) SELECT UserID,?,?,? FROM Users WHERE Role='ADMIN' AND Status='ACTIVE'", title, message, type);
    }

    public void sendToUsername(String username, String title, String message, String type) throws SQLException {
        int n = bulk("INSERT INTO Notifications(UserID,Title,Message,Type) SELECT UserID,?,?,? FROM Users WHERE Username=? AND Status='ACTIVE'", title, message, type, username);
        if (n == 0) throw new ValidationException("No active user named \"" + username + "\".");
    }

    /** Everyone holding an active booking on the flight (used when an admin delays / cancels a flight). */
    public int notifyFlightPassengers(int flightId, String title, String message) throws SQLException {
        return bulk("INSERT INTO Notifications(UserID,Title,Message,Type) SELECT DISTINCT UserID,?,?,'FLIGHT' FROM Bookings WHERE FlightID=? AND BookingStatus<>'CANCELLED'", title, message, flightId);
    }

    private int bulk(String sql, Object... args) throws SQLException {
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            for (int i = 0; i < args.length; i++) p.setObject(i + 1, args[i]);
            return p.executeUpdate();
        }
    }

    public List<Notification> list(int userId) throws SQLException {
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement("SELECT TOP 50 * FROM Notifications WHERE UserID=? ORDER BY CreatedAt DESC,NotificationID DESC")) {
            p.setInt(1, userId);
            try (ResultSet r = p.executeQuery()) {
                List<Notification> l = new ArrayList<>();
                while (r.next()) {
                    Notification n = new Notification();
                    n.setNotificationId(r.getInt("NotificationID")); n.setTitle(r.getString("Title")); n.setMessage(r.getString("Message"));
                    n.setType(r.getString("Type")); n.setRead(r.getBoolean("IsRead")); n.setCreatedAt(r.getTimestamp("CreatedAt"));
                    l.add(n);
                }
                return l;
            }
        }
    }

    public int unread(int userId) throws SQLException {
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement("SELECT COUNT(*) FROM Notifications WHERE UserID=? AND IsRead=0")) {
            p.setInt(1, userId);
            try (ResultSet r = p.executeQuery()) { r.next(); return r.getInt(1); }
        }
    }

    public void markRead(int id, int userId) throws SQLException { bulk("UPDATE Notifications SET IsRead=1 WHERE NotificationID=? AND UserID=?", id, userId); }
    public void markAllRead(int userId) throws SQLException { bulk("UPDATE Notifications SET IsRead=1 WHERE UserID=?", userId); }
    public void delete(int id, int userId) throws SQLException { bulk("DELETE FROM Notifications WHERE NotificationID=? AND UserID=?", id, userId); }
    public void deleteRead(int userId) throws SQLException { bulk("DELETE FROM Notifications WHERE UserID=? AND IsRead=1", userId); }
}

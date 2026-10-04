package com.lankawings.dao;

import com.lankawings.config.DBConnection;
import com.lankawings.model.Notification;
import com.lankawings.util.ValidationException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Notification Management data access.
 * Scope is intentionally limited to Users + Notifications:
 * system/user-account messages, admin announcements, unread state and deletion.
 */
public class NotificationDAO {

    public void add(int userId, String title, String message, String type) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(
                     "INSERT INTO Notifications(UserID,Title,Message,Type) VALUES(?,?,?,?)")) {
            p.setInt(1, userId);
            p.setString(2, title);
            p.setString(3, message);
            p.setString(4, type);
            p.executeUpdate();
        }
    }

    /** Sends an announcement to every ACTIVE account. */
    public int broadcast(String title, String message, String type) throws SQLException {
        return bulk(
                "INSERT INTO Notifications(UserID,Title,Message,Type) " +
                "SELECT UserID,?,?,? FROM Users WHERE Status='ACTIVE'",
                title, message, type
        );
    }

    /** Sends a notification to one active account by username. */
    public void sendToUsername(String username, String title, String message, String type) throws SQLException {
        int n = bulk(
                "INSERT INTO Notifications(UserID,Title,Message,Type) " +
                "SELECT UserID,?,?,? FROM Users WHERE Username=? AND Status='ACTIVE'",
                title, message, type, username
        );
        if (n == 0) throw new ValidationException("No active user named \"" + username + "\".");
    }

    public List<Notification> list(int userId) throws SQLException {
        String sql = "SELECT TOP 50 NotificationID,Title,Message,Type,IsRead,CreatedAt " +
                     "FROM Notifications WHERE UserID=? ORDER BY CreatedAt DESC,NotificationID DESC";
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, userId);
            try (ResultSet r = p.executeQuery()) {
                List<Notification> list = new ArrayList<>();
                while (r.next()) {
                    Notification n = new Notification();
                    n.setNotificationId(r.getInt("NotificationID"));
                    n.setTitle(r.getString("Title"));
                    n.setMessage(r.getString("Message"));
                    n.setType(r.getString("Type"));
                    n.setRead(r.getBoolean("IsRead"));
                    n.setCreatedAt(r.getTimestamp("CreatedAt"));
                    list.add(n);
                }
                return list;
            }
        }
    }

    public int unread(int userId) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(
                     "SELECT COUNT(*) FROM Notifications WHERE UserID=? AND IsRead=0")) {
            p.setInt(1, userId);
            try (ResultSet r = p.executeQuery()) {
                r.next();
                return r.getInt(1);
            }
        }
    }

    public void markRead(int id, int userId) throws SQLException {
        bulk("UPDATE Notifications SET IsRead=1 WHERE NotificationID=? AND UserID=?", id, userId);
    }

    public void markAllRead(int userId) throws SQLException {
        bulk("UPDATE Notifications SET IsRead=1 WHERE UserID=?", userId);
    }

    public void delete(int id, int userId) throws SQLException {
        bulk("DELETE FROM Notifications WHERE NotificationID=? AND UserID=?", id, userId);
    }

    public void deleteRead(int userId) throws SQLException {
        bulk("DELETE FROM Notifications WHERE UserID=? AND IsRead=1", userId);
    }

    private int bulk(String sql, Object... args) throws SQLException {
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            for (int i = 0; i < args.length; i++) p.setObject(i + 1, args[i]);
            return p.executeUpdate();
        }
    }
}

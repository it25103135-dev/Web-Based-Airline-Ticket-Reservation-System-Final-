package com.lankawings.dao;

import com.lankawings.config.DBConnection;
import com.lankawings.model.User;
import com.lankawings.util.PasswordUtil;
import com.lankawings.util.Req;
import com.lankawings.util.ValidationException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class UserDAO {
    private static final String COLS = "UserID,FullName,Username,Email,Phone,Role,Status,CreatedAt";

    /** Light record the security filter reads on every request. */
    public static final class SessionInfo {
        public final String role, status; public final int unread;
        SessionInfo(String role, String status, int unread) { this.role = role; this.status = status; this.unread = unread; }
    }

    /** Login by username OR email. Returns null when credentials are wrong. */
    public User authenticate(String login, String password) throws SQLException {
        String sql = "SELECT TOP 1 " + COLS + ",PasswordHash FROM Users WHERE Username=? OR Email=? ORDER BY CASE WHEN Username=? THEN 0 ELSE 1 END";
        User u; String stored;
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, login); p.setString(2, login); p.setString(3, login);
            try (ResultSet r = p.executeQuery()) {
                if (!r.next()) { PasswordUtil.dummyVerify(password); return null; }
                u = map(r); stored = r.getString("PasswordHash");
            }
        }
        if (!PasswordUtil.verify(password, stored)) return null;
        if (!"ACTIVE".equals(u.getStatus())) throw new ValidationException("This account has been deactivated. Please contact Lanka Wings support.");
        if (PasswordUtil.needsUpgrade(stored)) {           // silently move old SHA-256 hashes to PBKDF2
            try { setHash(u.getUserId(), PasswordUtil.hash(password)); }
            catch (SQLException e) { Req.log().log(Level.WARNING, "Could not upgrade password hash (run sql/UPGRADE_SECURITY.sql?)", e); }
        }
        return u;
    }

    public SessionInfo sessionInfo(int userId) throws SQLException {
        String sql = "SELECT Role,Status,(SELECT COUNT(*) FROM Notifications n WHERE n.UserID=u.UserID AND n.IsRead=0) AS Unread FROM Users u WHERE UserID=?";
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, userId);
            try (ResultSet r = p.executeQuery()) { return r.next() ? new SessionInfo(r.getString(1), r.getString(2), r.getInt(3)) : null; }
        }
    }

    public boolean usernameExists(String username) throws SQLException { return exists("SELECT 1 FROM Users WHERE Username=?", username); }
    public boolean emailExists(String email, int excludeUserId) throws SQLException {
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement("SELECT 1 FROM Users WHERE Email=? AND UserID<>?")) {
            p.setString(1, email); p.setInt(2, excludeUserId);
            try (ResultSet r = p.executeQuery()) { return r.next(); }
        }
    }
    private boolean exists(String sql, String v) throws SQLException {
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, v);
            try (ResultSet r = p.executeQuery()) { return r.next(); }
        }
    }

    /** Creates a PASSENGER account and returns the new user id. */
    public int register(String fullName, String username, String email, String phone, String password) throws SQLException {
        if (usernameExists(username)) throw new ValidationException("That username is already taken. Please choose another.");
        if (emailExists(email, 0)) throw new ValidationException("An account with this email already exists. Try signing in.");
        String sql = "INSERT INTO Users(FullName,Username,Email,Phone,PasswordHash,Role) VALUES(?,?,?,?,?,'PASSENGER')";
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            p.setString(1, fullName); p.setString(2, username); p.setString(3, email); p.setString(4, phone); p.setString(5, PasswordUtil.hash(password));
            p.executeUpdate();
            try (ResultSet k = p.getGeneratedKeys()) { k.next(); return k.getInt(1); }
        } catch (SQLException e) {
            if (DBConnection.isDuplicate(e)) throw new ValidationException("That username or email is already registered.");
            throw e;
        }
    }

    public User findById(int id) throws SQLException {
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement("SELECT " + COLS + " FROM Users WHERE UserID=?")) {
            p.setInt(1, id);
            try (ResultSet r = p.executeQuery()) { return r.next() ? map(r) : null; }
        }
    }

    public List<User> listAll() throws SQLException {
        try (Connection c = DBConnection.getConnection(); Statement s = c.createStatement();
             ResultSet r = s.executeQuery("SELECT " + COLS + " FROM Users ORDER BY CreatedAt DESC")) {
            List<User> l = new ArrayList<>();
            while (r.next()) l.add(map(r));
            return l;
        }
    }

    public int count() throws SQLException {
        try (Connection c = DBConnection.getConnection(); Statement s = c.createStatement(); ResultSet r = s.executeQuery("SELECT COUNT(*) FROM Users")) {
            r.next(); return r.getInt(1);
        }
    }

    public void updateProfile(int id, String fullName, String email, String phone) throws SQLException {
        if (emailExists(email, id)) throw new ValidationException("Another account already uses this email address.");
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement("UPDATE Users SET FullName=?,Email=?,Phone=? WHERE UserID=?")) {
            p.setString(1, fullName); p.setString(2, email); p.setString(3, phone); p.setInt(4, id);
            p.executeUpdate();
        } catch (SQLException e) {
            if (DBConnection.isDuplicate(e)) throw new ValidationException("Another account already uses this email address.");
            throw e;
        }
    }

    public boolean verifyPassword(int id, String password) throws SQLException {
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement("SELECT PasswordHash FROM Users WHERE UserID=?")) {
            p.setInt(1, id);
            try (ResultSet r = p.executeQuery()) { return r.next() && PasswordUtil.verify(password, r.getString(1)); }
        }
    }

    public void updatePassword(int id, String password) throws SQLException { setHash(id, PasswordUtil.hash(password)); }

    private void setHash(int id, String hash) throws SQLException {
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement("UPDATE Users SET PasswordHash=? WHERE UserID=?")) {
            p.setString(1, hash); p.setInt(2, id); p.executeUpdate();
        }
    }

    /** Self-service "delete account" = deactivate (keeps booking history intact). */
    public void deactivate(int id) throws SQLException {
        try (Connection c = DBConnection.getConnection()) {
            try (PreparedStatement p = c.prepareStatement(
                    "SELECT COUNT(*) FROM Bookings b JOIN Flights f ON b.FlightID=f.FlightID WHERE b.UserID=? AND b.BookingStatus<>'CANCELLED' AND f.DepartureTime>SYSDATETIME()")) {
                p.setInt(1, id);
                try (ResultSet r = p.executeQuery()) {
                    r.next();
                    if (r.getInt(1) > 0) throw new ValidationException("You still have upcoming bookings. Cancel them before closing your account.");
                }
            }
            try (PreparedStatement p = c.prepareStatement("UPDATE Users SET Status='INACTIVE' WHERE UserID=?")) { p.setInt(1, id); p.executeUpdate(); }
        }
    }

    // ---- admin
    public void setStatus(int id, String status) throws SQLException { setColumn("Status", id, status); }
    public void setRole(int id, String role) throws SQLException { setColumn("Role", id, role); }
    private void setColumn(String col, int id, String value) throws SQLException {
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement("UPDATE Users SET " + col + "=? WHERE UserID=?")) {
            p.setString(1, value); p.setInt(2, id);
            if (p.executeUpdate() == 0) throw new ValidationException("User not found.");
        }
    }

    private User map(ResultSet r) throws SQLException {
        User u = new User();
        u.setUserId(r.getInt("UserID")); u.setFullName(r.getString("FullName")); u.setUsername(r.getString("Username"));
        u.setEmail(r.getString("Email")); u.setPhone(r.getString("Phone")); u.setRole(r.getString("Role"));
        u.setStatus(r.getString("Status")); u.setCreatedAt(r.getTimestamp("CreatedAt"));
        return u;
    }
}

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
    public static final String[] ROLES = {"PASSENGER","TRAVEL_AGENT","CHECKIN_STAFF","RESERVATION_MANAGER","CUSTOMER_SUPPORT","FINANCE","ADMIN"};

    public static final class SessionInfo {
        public final String role, status;
        SessionInfo(String role, String status) { this.role = role; this.status = status; }
    }

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
        if (!"ACTIVE".equals(u.getStatus())) throw new ValidationException("This account has been deactivated. Please contact the system administrator.");
        if (PasswordUtil.needsUpgrade(stored)) {
            try { setHash(u.getUserId(), PasswordUtil.hash(password)); }
            catch (SQLException e) { Req.log().log(Level.WARNING, "Could not upgrade password hash", e); }
        }
        return u;
    }

    public SessionInfo sessionInfo(int userId) throws SQLException {
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement("SELECT Role,Status FROM Users WHERE UserID=?")) {
            p.setInt(1, userId);
            try (ResultSet r = p.executeQuery()) { return r.next() ? new SessionInfo(r.getString(1), r.getString(2)) : null; }
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

    public int register(String fullName, String username, String email, String phone, String password) throws SQLException {
        return create(fullName, username, email, phone, password, "PASSENGER", "ACTIVE");
    }

    public int createByAdmin(String fullName, String username, String email, String phone, String password, String role, String status) throws SQLException {
        return create(fullName, username, email, phone, password, role, status);
    }

    private int create(String fullName, String username, String email, String phone, String password, String role, String status) throws SQLException {
        if (usernameExists(username)) throw new ValidationException("That username is already taken.");
        if (emailExists(email, 0)) throw new ValidationException("An account with this email already exists.");
        String sql = "INSERT INTO Users(FullName,Username,Email,Phone,PasswordHash,Role,Status) VALUES(?,?,?,?,?,?,?)";
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            p.setString(1, fullName); p.setString(2, username); p.setString(3, email); p.setString(4, phone);
            p.setString(5, PasswordUtil.hash(password)); p.setString(6, role); p.setString(7, status);
            p.executeUpdate();
            try (ResultSet k = p.getGeneratedKeys()) { if (!k.next()) throw new SQLException("No generated user id"); return k.getInt(1); }
        } catch (SQLException e) {
            if (DBConnection.isDuplicate(e)) throw new ValidationException("That username or email is already registered.");
            throw e;
        }
    }

    public User findById(int id) throws SQLException {
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement("SELECT " + COLS + " FROM Users WHERE UserID=?")) {
            p.setInt(1, id); try (ResultSet r = p.executeQuery()) { return r.next() ? map(r) : null; }
        }
    }

    public List<User> listAll() throws SQLException {
        List<User> l = new ArrayList<>();
        try (Connection c = DBConnection.getConnection(); Statement s = c.createStatement(); ResultSet r = s.executeQuery("SELECT " + COLS + " FROM Users ORDER BY CreatedAt DESC")) {
            while (r.next()) l.add(map(r));
        }
        return l;
    }

    public int count() throws SQLException { return scalar("SELECT COUNT(*) FROM Users"); }
    public int countActive() throws SQLException { return scalar("SELECT COUNT(*) FROM Users WHERE Status='ACTIVE'"); }
    public int countAdmins() throws SQLException { return scalar("SELECT COUNT(*) FROM Users WHERE Role='ADMIN' AND Status='ACTIVE'"); }
    private int scalar(String sql) throws SQLException {
        try (Connection c = DBConnection.getConnection(); Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) { r.next(); return r.getInt(1); }
    }

    public void updateProfile(int id, String fullName, String email, String phone) throws SQLException {
        if (emailExists(email, id)) throw new ValidationException("Another account already uses this email address.");
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement("UPDATE Users SET FullName=?,Email=?,Phone=? WHERE UserID=?")) {
            p.setString(1, fullName); p.setString(2, email); p.setString(3, phone); p.setInt(4, id);
            if (p.executeUpdate() == 0) throw new ValidationException("User not found.");
        } catch (SQLException e) {
            if (DBConnection.isDuplicate(e)) throw new ValidationException("Another account already uses this email address.");
            throw e;
        }
    }

    public void updateByAdmin(int id, String fullName, String email, String phone, String role, String status) throws SQLException {
        if (emailExists(email, id)) throw new ValidationException("Another account already uses this email address.");
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement("UPDATE Users SET FullName=?,Email=?,Phone=?,Role=?,Status=? WHERE UserID=?")) {
            p.setString(1, fullName); p.setString(2, email); p.setString(3, phone); p.setString(4, role); p.setString(5, status); p.setInt(6, id);
            if (p.executeUpdate() == 0) throw new ValidationException("User not found.");
        } catch (SQLException e) {
            if (DBConnection.isDuplicate(e)) throw new ValidationException("Another account already uses this email address.");
            throw e;
        }
    }

    public boolean verifyPassword(int id, String password) throws SQLException {
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement("SELECT PasswordHash FROM Users WHERE UserID=?")) {
            p.setInt(1, id); try (ResultSet r = p.executeQuery()) { return r.next() && PasswordUtil.verify(password, r.getString(1)); }
        }
    }

    public void updatePassword(int id, String password) throws SQLException { setHash(id, PasswordUtil.hash(password)); }
    private void setHash(int id, String hash) throws SQLException {
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement("UPDATE Users SET PasswordHash=? WHERE UserID=?")) {
            p.setString(1, hash); p.setInt(2, id); if (p.executeUpdate() == 0) throw new ValidationException("User not found.");
        }
    }

    public void deactivate(int id) throws SQLException {
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement("UPDATE Users SET Status='INACTIVE' WHERE UserID=?")) {
            p.setInt(1, id); if (p.executeUpdate() == 0) throw new ValidationException("User not found.");
        }
    }

    private User map(ResultSet r) throws SQLException {
        User u = new User();
        u.setUserId(r.getInt("UserID")); u.setFullName(r.getString("FullName")); u.setUsername(r.getString("Username"));
        u.setEmail(r.getString("Email")); u.setPhone(r.getString("Phone")); u.setRole(r.getString("Role"));
        u.setStatus(r.getString("Status")); u.setCreatedAt(r.getTimestamp("CreatedAt")); return u;
    }
}

package com.lankawings.dao;

import com.lankawings.config.DBConnection;
import com.lankawings.model.User;
import com.lankawings.util.PasswordUtil;
import com.lankawings.util.Req;
import com.lankawings.util.ValidationException;

import java.sql.*;
import java.util.logging.Level;

/** Minimum identity dependency required by the isolated Feedback Management function. */
public class UserDAO {
    private static final String COLS = "UserID,FullName,Username,Email,Phone,Role,Status,CreatedAt";

    public static final class SessionInfo {
        public final String role, status;
        SessionInfo(String role, String status) { this.role = role; this.status = status; }
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
        if (PasswordUtil.needsUpgrade(stored)) {
            try { setHash(u.getUserId(), PasswordUtil.hash(password)); }
            catch (SQLException e) { Req.log().log(Level.WARNING, "Could not upgrade password hash", e); }
        }
        return u;
    }

    /** Re-check account role/status on every protected request. */
    public SessionInfo sessionInfo(int userId) throws SQLException {
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement("SELECT Role,Status FROM Users WHERE UserID=?")) {
            p.setInt(1, userId);
            try (ResultSet r = p.executeQuery()) { return r.next() ? new SessionInfo(r.getString(1), r.getString(2)) : null; }
        }
    }

    private void setHash(int id, String hash) throws SQLException {
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement("UPDATE Users SET PasswordHash=? WHERE UserID=?")) {
            p.setString(1, hash); p.setInt(2, id); p.executeUpdate();
        }
    }

    private static User map(ResultSet r) throws SQLException {
        User u = new User();
        u.setUserId(r.getInt("UserID"));
        u.setFullName(r.getString("FullName"));
        u.setUsername(r.getString("Username"));
        u.setEmail(r.getString("Email"));
        u.setPhone(r.getString("Phone"));
        u.setRole(r.getString("Role"));
        u.setStatus(r.getString("Status"));
        u.setCreatedAt(r.getTimestamp("CreatedAt"));
        return u;
    }
}

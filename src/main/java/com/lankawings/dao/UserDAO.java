package com.lankawings.dao;

import com.lankawings.config.DBConnection;
import com.lankawings.model.User;
import com.lankawings.util.PasswordUtil;
import com.lankawings.util.ValidationException;

import java.sql.*;

/** Minimal account DAO: authentication and current role/status only. */
public class UserDAO {
    public static final class SessionInfo {
        public final String role;
        public final String status;
        public SessionInfo(String role, String status) { this.role = role; this.status = status; }
    }

    public User authenticate(String login, String password) throws SQLException {
        String sql = "SELECT TOP 1 UserID,FullName,Username,Email,Role,Status,PasswordHash FROM Users " +
                "WHERE Username=? OR Email=? ORDER BY CASE WHEN Username=? THEN 0 ELSE 1 END";
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, login); p.setString(2, login); p.setString(3, login);
            try (ResultSet r = p.executeQuery()) {
                if (!r.next()) { PasswordUtil.dummyVerify(password); return null; }
                String hash = r.getString("PasswordHash");
                if (!PasswordUtil.verify(password, hash)) return null;
                User u = map(r);
                if (!"ACTIVE".equals(u.getStatus())) throw new ValidationException("This account has been deactivated.");
                return u;
            }
        }
    }

    public SessionInfo sessionInfo(int userId) throws SQLException {
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement("SELECT Role,Status FROM Users WHERE UserID=?")) {
            p.setInt(1, userId);
            try (ResultSet r = p.executeQuery()) { return r.next() ? new SessionInfo(r.getString(1), r.getString(2)) : null; }
        }
    }

    private User map(ResultSet r) throws SQLException {
        User u = new User();
        u.setUserId(r.getInt("UserID"));
        u.setFullName(r.getString("FullName"));
        u.setUsername(r.getString("Username"));
        u.setEmail(r.getString("Email"));
        u.setRole(r.getString("Role"));
        u.setStatus(r.getString("Status"));
        return u;
    }
}

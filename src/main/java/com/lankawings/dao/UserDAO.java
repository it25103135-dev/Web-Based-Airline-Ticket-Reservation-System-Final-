package com.lankawings.dao;

import com.lankawings.config.DBConnection;
import com.lankawings.model.User;
import com.lankawings.util.PasswordUtil;
import com.lankawings.util.ValidationException;
import java.sql.*;

/** Minimum account dependency: login and current session status only. */
public class UserDAO {
    public static final class SessionInfo {
        public final String role, status;
        public SessionInfo(String role, String status) { this.role = role; this.status = status; }
    }

    public User authenticate(String login, String password) throws SQLException {
        String q = "SELECT TOP 1 UserID,FullName,Username,Email,Phone,Role,Status,CreatedAt,PasswordHash FROM Users WHERE Username=? OR Email=? ORDER BY CASE WHEN Username=? THEN 0 ELSE 1 END";
        try (Connection c=DBConnection.getConnection(); PreparedStatement p=c.prepareStatement(q)) {
            p.setString(1,login); p.setString(2,login); p.setString(3,login);
            try (ResultSet r=p.executeQuery()) {
                if (!r.next()) { PasswordUtil.dummyVerify(password); return null; }
                if (!PasswordUtil.verify(password,r.getString("PasswordHash"))) return null;
                User u=map(r);
                if (!"ACTIVE".equals(u.getStatus())) throw new ValidationException("This account has been deactivated.");
                return u;
            }
        }
    }

    public SessionInfo sessionInfo(int userId) throws SQLException {
        try (Connection c=DBConnection.getConnection(); PreparedStatement p=c.prepareStatement("SELECT Role,Status FROM Users WHERE UserID=?")) {
            p.setInt(1,userId); try(ResultSet r=p.executeQuery()){ return r.next()?new SessionInfo(r.getString(1),r.getString(2)):null; }
        }
    }

    private User map(ResultSet r) throws SQLException {
        User u=new User();
        u.setUserId(r.getInt("UserID")); u.setFullName(r.getString("FullName")); u.setUsername(r.getString("Username"));
        u.setEmail(r.getString("Email")); u.setPhone(r.getString("Phone")); u.setRole(r.getString("Role")); u.setStatus(r.getString("Status")); u.setCreatedAt(r.getTimestamp("CreatedAt"));
        return u;
    }
}

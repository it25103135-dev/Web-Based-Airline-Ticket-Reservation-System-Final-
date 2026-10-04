package com.lankawings.dao;

import com.lankawings.config.DBConnection;
import com.lankawings.model.UserAudit;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserAuditDAO {
    public void add(int actorUserId, int targetUserId, String action, String details) throws SQLException {
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement(
                "INSERT INTO UserAudit(ActorUserID,TargetUserID,Action,Details) VALUES(?,?,?,?)")) {
            p.setInt(1, actorUserId); p.setInt(2, targetUserId); p.setString(3, action); p.setString(4, details); p.executeUpdate();
        }
    }

    public List<UserAudit> recent(int limit) throws SQLException {
        String sql = "SELECT TOP (?) a.AuditID,a.ActorUserID,a.TargetUserID,aa.Username ActorUsername,tu.Username TargetUsername,a.Action,a.Details,a.CreatedAt " +
                "FROM UserAudit a JOIN Users aa ON aa.UserID=a.ActorUserID JOIN Users tu ON tu.UserID=a.TargetUserID ORDER BY a.CreatedAt DESC";
        List<UserAudit> out = new ArrayList<>();
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, Math.max(1, Math.min(limit, 200)));
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    UserAudit a = new UserAudit(); a.setAuditId(r.getInt("AuditID")); a.setActorUserId(r.getInt("ActorUserID")); a.setTargetUserId(r.getInt("TargetUserID"));
                    a.setActorUsername(r.getString("ActorUsername")); a.setTargetUsername(r.getString("TargetUsername")); a.setAction(r.getString("Action")); a.setDetails(r.getString("Details")); a.setCreatedAt(r.getTimestamp("CreatedAt")); out.add(a);
                }
            }
        }
        return out;
    }
}

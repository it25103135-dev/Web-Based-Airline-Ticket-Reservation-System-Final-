package com.lankawings.dao;

import com.lankawings.config.DBConnection;
import com.lankawings.model.Feedback;
import com.lankawings.util.ValidationException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Feedback & reviews: passengers create / edit / delete their own (edit only until answered); admins respond, close, delete. */
public class FeedbackDAO {

    public static final class Stats {
        public double average; public int total; public final int[] perStar = new int[6]; // index 1..5
    }

    public void add(int uid, int rating, String subject, String message) throws SQLException {
        try (Connection c = DBConnection.getConnection()) {
            try (PreparedStatement p = c.prepareStatement("SELECT COUNT(*) FROM Feedback WHERE UserID=? AND CreatedAt>DATEADD(HOUR,-24,SYSDATETIME())")) {
                p.setInt(1, uid);
                try (ResultSet r = p.executeQuery()) {
                    r.next();
                    if (r.getInt(1) >= 5) throw new ValidationException("You have reached the limit of 5 reviews per day. Please try again tomorrow.");
                }
            }
            try (PreparedStatement p = c.prepareStatement("INSERT INTO Feedback(UserID,Rating,Subject,Message) VALUES(?,?,?,?)")) {
                p.setInt(1, uid); p.setInt(2, rating); p.setString(3, subject); p.setString(4, message);
                p.executeUpdate();
            }
        }
    }

    public void update(int id, int uid, int rating, String subject, String message) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement("UPDATE Feedback SET Rating=?,Subject=?,Message=? WHERE FeedbackID=? AND UserID=? AND Status='OPEN'")) {
            p.setInt(1, rating); p.setString(2, subject); p.setString(3, message); p.setInt(4, id); p.setInt(5, uid);
            if (p.executeUpdate() == 0) throw new ValidationException("Only your own feedback that has not been answered yet can be edited.");
        }
    }

    public void delete(int id, int uid, boolean admin) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement("DELETE FROM Feedback WHERE FeedbackID=?" + (admin ? "" : " AND UserID=?"))) {
            p.setInt(1, id);
            if (!admin) p.setInt(2, uid);
            if (p.executeUpdate() == 0) throw new ValidationException("Feedback not found.");
        }
    }

    /** Returns the user id of the author so they can be notified. */
    public int respond(int id, String response) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement("UPDATE Feedback SET AdminResponse=?,Status='RESPONDED' WHERE FeedbackID=?")) {
            p.setString(1, response); p.setInt(2, id);
            if (p.executeUpdate() == 0) throw new ValidationException("Feedback not found.");
        }
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement("SELECT UserID FROM Feedback WHERE FeedbackID=?")) {
            p.setInt(1, id);
            try (ResultSet r = p.executeQuery()) { return r.next() ? r.getInt(1) : 0; }
        }
    }

    public void close(int id) throws SQLException {
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement("UPDATE Feedback SET Status='CLOSED' WHERE FeedbackID=?")) {
            p.setInt(1, id);
            if (p.executeUpdate() == 0) throw new ValidationException("Feedback not found.");
        }
    }

    public List<Feedback> list(Integer uid) throws SQLException {
        String q = "SELECT f.*,u.Username FROM Feedback f JOIN Users u ON f.UserID=u.UserID" + (uid == null ? "" : " WHERE f.UserID=?") + " ORDER BY f.CreatedAt DESC";
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement(q)) {
            if (uid != null) p.setInt(1, uid);
            try (ResultSet r = p.executeQuery()) {
                List<Feedback> l = new ArrayList<>();
                while (r.next()) {
                    Feedback f = new Feedback();
                    f.setFeedbackId(r.getInt("FeedbackID")); f.setUserId(r.getInt("UserID")); f.setUsername(r.getString("Username"));
                    f.setRating(r.getInt("Rating")); f.setSubject(r.getString("Subject")); f.setMessage(r.getString("Message"));
                    f.setAdminResponse(r.getString("AdminResponse")); f.setStatus(r.getString("Status")); f.setCreatedAt(r.getTimestamp("CreatedAt"));
                    l.add(f);
                }
                return l;
            }
        }
    }

    /** Overall rating summary across all passengers. */
    public Stats stats() throws SQLException {
        Stats s = new Stats();
        try (Connection c = DBConnection.getConnection(); Statement st = c.createStatement();
             ResultSet r = st.executeQuery("SELECT Rating,COUNT(*) FROM Feedback GROUP BY Rating")) {
            int sum = 0;
            while (r.next()) {
                int star = r.getInt(1), n = r.getInt(2);
                if (star >= 1 && star <= 5) { s.perStar[star] = n; s.total += n; sum += star * n; }
            }
            s.average = s.total == 0 ? 0 : (double) sum / s.total;
        }
        return s;
    }
}

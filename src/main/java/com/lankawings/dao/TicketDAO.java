package com.lankawings.dao;

import com.lankawings.config.DBConnection;
import com.lankawings.model.Ticket;
import java.sql.*;
import java.util.*;

public class TicketDAO {
    public Ticket findByBooking(int bookingId) throws SQLException {
        String q="SELECT * FROM Tickets WHERE BookingID=?";
        try(Connection c=DBConnection.getConnection(); PreparedStatement p=c.prepareStatement(q)){
            p.setInt(1,bookingId);
            try(ResultSet r=p.executeQuery()){return r.next()?map(r):null;}
        }
    }

    /** Mutations lock the booking first, matching payment/cancellation lock ordering. */
    public void create(int bookingId, int userId, boolean admin) throws SQLException {
        mutate(bookingId, userId, admin, true);
    }

    public void delete(int bookingId, int userId, boolean admin) throws SQLException {
        if (!admin) throw new com.lankawings.util.ValidationException("Only administrators can delete tickets.");
        mutate(bookingId, userId, admin, false);
    }

    private void mutate(int bookingId, int userId, boolean admin, boolean create) throws SQLException {
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                String pnr;
                try (PreparedStatement p = c.prepareStatement(
                        "SELECT b.UserID,b.PNR,b.BookingStatus,b.PaymentStatus,f.Status," +
                        "CASE WHEN f.DepartureTime>SYSDATETIME() THEN 1 ELSE 0 END AS Future " +
                        "FROM Bookings b WITH (UPDLOCK,HOLDLOCK) JOIN Flights f ON b.FlightID=f.FlightID WHERE b.BookingID=?")) {
                    p.setInt(1, bookingId);
                    try (ResultSet r = p.executeQuery()) {
                        if (!r.next() || (!admin && r.getInt("UserID") != userId))
                            throw new com.lankawings.util.ValidationException("Booking not found.");
                        pnr = r.getString("PNR");
                        if (create && (!"PAID".equals(r.getString("PaymentStatus")) ||
                                !"CONFIRMED".equals(r.getString("BookingStatus")) ||
                                "CANCELLED".equals(r.getString("Status")) || r.getInt("Future") == 0))
                            throw new com.lankawings.util.ValidationException("Tickets require a paid, confirmed booking on a flight that has not departed or been cancelled.");
                    }
                }
                String sql = create
                        ? "INSERT INTO Tickets(TicketNumber,BookingID,TicketStatus) VALUES(?,?,'ISSUED')"
                        : "DELETE FROM Tickets WHERE BookingID=?";
                try (PreparedStatement p = c.prepareStatement(sql)) {
                    if (create) { p.setString(1, "TKT-" + pnr); p.setInt(2, bookingId); }
                    else p.setInt(1, bookingId);
                    if (p.executeUpdate() == 0) throw new com.lankawings.util.ValidationException("Ticket no longer exists. Reload the page.");
                }
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                if (DBConnection.isDuplicate(e)) throw new com.lankawings.util.ValidationException("This booking already has a ticket.");
                throw e;
            } catch (RuntimeException e) { c.rollback(); throw e; }
            finally { c.setAutoCommit(true); }
        }
    }

    public List<Ticket> listForUser(int userId) throws SQLException {
        return list("SELECT t.* FROM Tickets t JOIN Bookings b ON t.BookingID=b.BookingID WHERE b.UserID=? ORDER BY t.IssuedAt DESC",userId);
    }

    public List<Ticket> all() throws SQLException {
        return list("SELECT t.* FROM Tickets t ORDER BY t.IssuedAt DESC");
    }

    private List<Ticket> list(String q,Object...args)throws SQLException{
        try(Connection c=DBConnection.getConnection(); PreparedStatement p=c.prepareStatement(q)){
            for(int i=0;i<args.length;i++)p.setObject(i+1,args[i]);
            try(ResultSet r=p.executeQuery()){
                List<Ticket> out=new ArrayList<>();
                while(r.next())out.add(map(r));
                return out;
            }
        }
    }

    public void cancelForBooking(int bookingId)throws SQLException{
        try(Connection c=DBConnection.getConnection(); PreparedStatement p=c.prepareStatement(
                "UPDATE Tickets SET TicketStatus='CANCELLED',LastUpdatedAt=SYSDATETIME() WHERE BookingID=?")){
            p.setInt(1,bookingId);
            p.executeUpdate();
        }
    }

    private Ticket map(ResultSet r)throws SQLException{
        Ticket t=new Ticket();
        t.setTicketId(r.getInt("TicketID"));
        t.setTicketNumber(r.getString("TicketNumber"));
        t.setBookingId(r.getInt("BookingID"));
        t.setTicketStatus(r.getString("TicketStatus"));
        t.setIssuedAt(r.getTimestamp("IssuedAt"));
        t.setLastUpdatedAt(r.getTimestamp("LastUpdatedAt"));
        return t;
    }
}

package com.lankawings.dao;

import com.lankawings.config.DBConnection;
import com.lankawings.model.Payment;
import com.lankawings.util.ValidationException;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * PAYMENT MANAGEMENT.
 * Handles payment validation result persistence, transaction status/history and receipt reference.
 * Full card number, CVV and expiry are never persisted; only the last four digits are stored.
 */
public class PaymentDAO {
    public void delete(int paymentId, boolean admin) throws SQLException {
        if (!admin) throw new ValidationException("Only administrators can delete payments.");
        if (paymentId <= 0) throw new ValidationException("Invalid payment ID.");
        try(Connection c=DBConnection.getConnection(); PreparedStatement p=c.prepareStatement("DELETE FROM Payments WHERE PaymentID=?")){
            p.setInt(1,paymentId);
            if(p.executeUpdate()==0) throw new ValidationException("Payment no longer exists. Reload the page.");
        }
    }

    public Payment pay(int bookingId, int userId, String method, String last4) throws SQLException {
        String ref="TXN-"+UUID.randomUUID().toString().replace("-","").substring(0,12).toUpperCase();
        try(Connection c=DBConnection.getConnection()){
            c.setAutoCommit(false);
            try{
                String pnr; BigDecimal fare;
                try(PreparedStatement check=c.prepareStatement(
                        "SELECT b.UserID,b.PaymentStatus,b.BookingStatus,b.PNR,f.Fare,f.Status AS FlightStatus,"+
                        "CASE WHEN f.DepartureTime<=SYSDATETIME() THEN 1 ELSE 0 END AS Departed "+
                        "FROM Bookings b WITH (UPDLOCK,HOLDLOCK) JOIN Flights f ON b.FlightID=f.FlightID WHERE b.BookingID=?")){
                    check.setInt(1,bookingId);
                    try(ResultSet r=check.executeQuery()){
                        if(!r.next() || r.getInt("UserID")!=userId) throw new ValidationException("Booking not found.");
                        if("CANCELLED".equals(r.getString("BookingStatus"))) throw new ValidationException("Cancelled bookings cannot be paid.");
                        if("PAID".equals(r.getString("PaymentStatus"))) throw new ValidationException("This booking is already paid.");
                        if(r.getInt("Departed")==1) throw new ValidationException("This flight has already departed.");
                        if("CANCELLED".equals(r.getString("FlightStatus"))) throw new ValidationException("This flight has been cancelled.");
                        pnr=r.getString("PNR"); fare=r.getBigDecimal("Fare");
                    }
                }
                try(PreparedStatement p=c.prepareStatement("INSERT INTO Payments(BookingID,Amount,Method,CardLast4,TransactionRef) VALUES(?,?,?,?,?)")){
                    p.setInt(1,bookingId); p.setBigDecimal(2,fare); p.setString(3,method); p.setString(4,last4); p.setString(5,ref); p.executeUpdate();
                }
                // Integration boundary: Payment Management updates the reservation's payment/confirmation state,
                // but Ticket Management itself is intentionally not included in this member split.
                try(PreparedStatement p=c.prepareStatement("UPDATE Bookings SET PaymentStatus='PAID',BookingStatus='CONFIRMED' WHERE BookingID=?")){
                    p.setInt(1,bookingId); p.executeUpdate();
                }
                c.commit();
                Payment pay=new Payment(); pay.setBookingId(bookingId); pay.setPnr(pnr); pay.setAmount(fare); pay.setTransactionRef(ref); pay.setMethod(method); pay.setCardLast4(last4); pay.setStatus("SUCCESS"); return pay;
            }catch(SQLException|RuntimeException e){ c.rollback(); throw e; }
            finally{ c.setAutoCommit(true); }
        }
    }

    /** Keeps an audit row for a declined attempt; it does not alter the booking. */
    public void recordFailure(int bookingId, BigDecimal amount, String method, String last4) throws SQLException {
        String ref="TXN-F"+UUID.randomUUID().toString().replace("-","").substring(0,11).toUpperCase();
        try(Connection c=DBConnection.getConnection(); PreparedStatement p=c.prepareStatement(
                "INSERT INTO Payments(BookingID,Amount,Method,CardLast4,TransactionRef,PaymentStatus) VALUES(?,?,?,?,?,'FAILED')")){
            p.setInt(1,bookingId); p.setBigDecimal(2,amount); p.setString(3,method); p.setString(4,last4); p.setString(5,ref); p.executeUpdate();
        }
    }

    public List<Payment> history(int userId, boolean admin) throws SQLException {
        String q="SELECT p.*,b.PNR,u.Username FROM Payments p JOIN Bookings b ON p.BookingID=b.BookingID JOIN Users u ON b.UserID=u.UserID"+
                (admin?"":" WHERE b.UserID=?")+" ORDER BY p.PaidAt DESC";
        try(Connection c=DBConnection.getConnection(); PreparedStatement s=c.prepareStatement(q)){
            if(!admin) s.setInt(1,userId);
            try(ResultSet r=s.executeQuery()){
                List<Payment> list=new ArrayList<>();
                while(r.next()){
                    Payment p=new Payment(); p.setPaymentId(r.getInt("PaymentID")); p.setBookingId(r.getInt("BookingID")); p.setPnr(r.getString("PNR")); p.setUsername(r.getString("Username"));
                    p.setAmount(r.getBigDecimal("Amount")); p.setMethod(r.getString("Method")); p.setCardLast4(r.getString("CardLast4")); p.setTransactionRef(r.getString("TransactionRef"));
                    p.setStatus(r.getString("PaymentStatus")); p.setPaidAt(r.getTimestamp("PaidAt")); list.add(p);
                }
                return list;
            }
        }
    }

    public BigDecimal revenue() throws SQLException {
        try(Connection c=DBConnection.getConnection(); Statement s=c.createStatement(); ResultSet r=s.executeQuery("SELECT COALESCE(SUM(Amount),0) FROM Payments WHERE PaymentStatus='SUCCESS'")){
            r.next(); return r.getBigDecimal(1);
        }
    }
}

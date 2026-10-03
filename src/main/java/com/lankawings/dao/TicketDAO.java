package com.lankawings.dao;

import com.lankawings.config.DBConnection;
import com.lankawings.model.Ticket;
import com.lankawings.util.ValidationException;
import java.sql.*;
import java.util.*;

/** Ticket issuing and administration for the isolated Ticket Reservation function. */
public class TicketDAO {
    public Ticket findByBooking(int bookingId)throws SQLException{
        try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement("SELECT * FROM Tickets WHERE BookingID=?")){
            p.setInt(1,bookingId);try(ResultSet r=p.executeQuery()){return r.next()?map(r):null;}
        }
    }

    /**
     * Passenger demo action requested for this module: one click marks the reservation PAID + CONFIRMED
     * and issues the e-ticket. This project uses only the simple one-click PAID status for the ticket demo.
     */
    public Ticket markPaidAndIssue(int bookingId,int userId)throws SQLException{
        try(Connection c=DBConnection.getConnection()){
            c.setAutoCommit(false);
            try{
                String pnr;
                try(PreparedStatement p=c.prepareStatement(
                        "SELECT b.PNR,b.UserID,b.BookingStatus,f.Status AS FlightStatus,"+
                        "CASE WHEN f.DepartureTime>SYSDATETIME() THEN 1 ELSE 0 END AS Future "+
                        "FROM Bookings b WITH (UPDLOCK,HOLDLOCK) JOIN Flights f ON b.FlightID=f.FlightID WHERE b.BookingID=?")){
                    p.setInt(1,bookingId);try(ResultSet r=p.executeQuery()){
                        if(!r.next()||r.getInt("UserID")!=userId)throw new ValidationException("Reservation not found.");
                        if("CANCELLED".equals(r.getString("BookingStatus")))throw new ValidationException("A cancelled reservation cannot be marked paid.");
                        if("CANCELLED".equals(r.getString("FlightStatus"))||r.getInt("Future")==0)throw new ValidationException("A ticket cannot be issued for this flight.");
                        pnr=r.getString("PNR");
                    }
                }
                try(PreparedStatement p=c.prepareStatement("UPDATE Bookings SET BookingStatus='CONFIRMED',PaymentStatus='PAID' WHERE BookingID=?")){
                    p.setInt(1,bookingId);p.executeUpdate();
                }
                Ticket existing=findByBookingInConnection(c,bookingId);
                if(existing==null){
                    String no="LWT-"+pnr+"-"+UUID.randomUUID().toString().replace("-","").substring(0,6).toUpperCase();
                    try(PreparedStatement p=c.prepareStatement("INSERT INTO Tickets(TicketNumber,BookingID,TicketStatus) VALUES(?,?,'ISSUED')")){
                        p.setString(1,no);p.setInt(2,bookingId);p.executeUpdate();
                    }
                }else if(!"ISSUED".equals(existing.getTicketStatus())){
                    try(PreparedStatement p=c.prepareStatement("UPDATE Tickets SET TicketStatus='ISSUED',LastUpdatedAt=SYSDATETIME() WHERE BookingID=?")){
                        p.setInt(1,bookingId);p.executeUpdate();
                    }
                }
                Ticket result=findByBookingInConnection(c,bookingId);c.commit();return result;
            }catch(SQLException|RuntimeException e){c.rollback();throw e;}
            finally{c.setAutoCommit(true);}
        }
    }

    public int countForUser(int userId)throws SQLException{
        String q="SELECT COUNT(*) FROM Tickets t JOIN Bookings b ON t.BookingID=b.BookingID WHERE b.UserID=? AND t.TicketStatus='ISSUED'";
        try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(q)){p.setInt(1,userId);try(ResultSet r=p.executeQuery()){r.next();return r.getInt(1);}}
    }

    public int countAll()throws SQLException{
        try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement("SELECT COUNT(*) FROM Tickets");ResultSet r=p.executeQuery()){r.next();return r.getInt(1);}
    }

    public Map<Integer,Ticket> mapForUser(int userId)throws SQLException{
        String q="SELECT t.* FROM Tickets t JOIN Bookings b ON t.BookingID=b.BookingID WHERE b.UserID=?";
        try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(q)){p.setInt(1,userId);try(ResultSet r=p.executeQuery()){Map<Integer,Ticket>m=new HashMap<>();while(r.next()){Ticket t=map(r);m.put(t.getBookingId(),t);}return m;}}
    }

    public List<Ticket> all()throws SQLException{return list("SELECT * FROM Tickets ORDER BY IssuedAt DESC");}

    /** Admin-only action from the admin servlet. Deletes the ticket document only; reservation is kept. */
    public void deleteForAdmin(int bookingId)throws SQLException{
        try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement("DELETE FROM Tickets WHERE BookingID=?")){
            p.setInt(1,bookingId);if(p.executeUpdate()==0)throw new ValidationException("Ticket no longer exists. Reload the page.");
        }
    }

    private List<Ticket> list(String q,Object...args)throws SQLException{
        try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(q)){
            for(int i=0;i<args.length;i++)p.setObject(i+1,args[i]);
            try(ResultSet r=p.executeQuery()){List<Ticket>out=new ArrayList<>();while(r.next())out.add(map(r));return out;}
        }
    }

    private Ticket findByBookingInConnection(Connection c,int bookingId)throws SQLException{
        try(PreparedStatement p=c.prepareStatement("SELECT * FROM Tickets WHERE BookingID=?")){p.setInt(1,bookingId);try(ResultSet r=p.executeQuery()){return r.next()?map(r):null;}}
    }

    private Ticket map(ResultSet r)throws SQLException{
        Ticket t=new Ticket();t.setTicketId(r.getInt("TicketID"));t.setTicketNumber(r.getString("TicketNumber"));t.setBookingId(r.getInt("BookingID"));t.setTicketStatus(r.getString("TicketStatus"));t.setIssuedAt(r.getTimestamp("IssuedAt"));t.setLastUpdatedAt(r.getTimestamp("LastUpdatedAt"));return t;
    }
}

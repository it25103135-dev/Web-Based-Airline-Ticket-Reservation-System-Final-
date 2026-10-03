package com.lankawings.dao;

import com.lankawings.config.DBConnection;
import com.lankawings.model.Booking;
import com.lankawings.util.Validator;
import com.lankawings.util.ValidationException;
import java.sql.*;
import java.util.*;

/** Reservation records used by Ticket Reservation. */
public class BookingDAO {
    private static final String BASE =
            "SELECT b.*,u.Username,f.FlightNo,f.Origin,f.Destination,f.DepartureTime,f.Fare,f.Status AS FlightStatus,"+
            "CASE WHEN f.DepartureTime<=SYSDATETIME() THEN 1 ELSE 0 END AS Departed "+
            "FROM Bookings b JOIN Flights f ON b.FlightID=f.FlightID JOIN Users u ON b.UserID=u.UserID";

    private static final String LOCK =
            "SELECT b.UserID,b.BookingStatus,b.PaymentStatus,b.PNR,b.FlightID,f.TotalSeats,"+
            "CASE WHEN f.DepartureTime<=SYSDATETIME() THEN 1 ELSE 0 END AS Departed "+
            "FROM Bookings b WITH (UPDLOCK,HOLDLOCK) JOIN Flights f ON b.FlightID=f.FlightID WHERE b.BookingID=?";

    public String create(int userId,int flightId,String passenger,String passport,String seat)throws SQLException{
        String pnr="LW"+UUID.randomUUID().toString().replace("-","").substring(0,8).toUpperCase();
        try(Connection c=DBConnection.getConnection()){
            c.setAutoCommit(false);
            try{
                int total;
                try(PreparedStatement lock=c.prepareStatement(
                        "SELECT TotalSeats,Status,CASE WHEN DepartureTime>SYSDATETIME() THEN 1 ELSE 0 END AS Future,"+
                        "(SELECT COUNT(*) FROM Bookings WHERE FlightID=? AND BookingStatus<>'CANCELLED') AS Booked "+
                        "FROM Flights WITH (UPDLOCK,HOLDLOCK) WHERE FlightID=?")){
                    lock.setInt(1,flightId);lock.setInt(2,flightId);
                    try(ResultSet r=lock.executeQuery()){
                        if(!r.next())throw new ValidationException("Flight not found.");
                        String st=r.getString("Status"); total=r.getInt("TotalSeats");
                        if(r.getInt("Future")==0)throw new ValidationException("This flight has already departed.");
                        if(!"SCHEDULED".equals(st)&&!"DELAYED".equals(st))throw new ValidationException("This flight is not open for booking (status: "+st+").");
                        if(r.getInt("Booked")>=total)throw new ValidationException("No seats are available on this flight.");
                    }
                }
                Validator.seatInLayout(seat,total);
                if(passportUsed(c,flightId,passport,0))throw new ValidationException("A passenger with this passport / ID already has an active booking on this flight.");
                try(PreparedStatement p=c.prepareStatement("INSERT INTO Bookings(PNR,UserID,FlightID,PassengerName,PassportNo,SeatNumber) VALUES(?,?,?,?,?,?)")){
                    p.setString(1,pnr);p.setInt(2,userId);p.setInt(3,flightId);p.setString(4,passenger);p.setString(5,passport);p.setString(6,seat);p.executeUpdate();
                }
                c.commit();return pnr;
            }catch(SQLException e){c.rollback();if(DBConnection.isDuplicate(e))throw new ValidationException("Sorry, seat "+seat+" was just taken. Please pick another seat.");throw e;}
            catch(RuntimeException e){c.rollback();throw e;}
            finally{c.setAutoCommit(true);}
        }
    }

    private boolean passportUsed(Connection c,int flightId,String passport,int excludeBookingId)throws SQLException{
        String q=excludeBookingId>0
                ?"SELECT 1 FROM Bookings WHERE FlightID=? AND PassportNo=? AND BookingStatus<>'CANCELLED' AND BookingID<>?"
                :"SELECT 1 FROM Bookings WHERE FlightID=? AND PassportNo=? AND BookingStatus<>'CANCELLED'";
        try(PreparedStatement p=c.prepareStatement(q)){
            p.setInt(1,flightId);p.setString(2,passport);if(excludeBookingId>0)p.setInt(3,excludeBookingId);
            try(ResultSet r=p.executeQuery()){return r.next();}
        }
    }

    public Set<String> takenSeats(int flightId)throws SQLException{
        try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement("SELECT SeatNumber FROM Bookings WHERE FlightID=? AND BookingStatus<>'CANCELLED'")){
            p.setInt(1,flightId);try(ResultSet r=p.executeQuery()){Set<String>s=new HashSet<>();while(r.next())s.add(r.getString(1).toUpperCase());return s;}
        }
    }

    public List<Booking> listForUser(int userId)throws SQLException{return list(BASE+" WHERE b.UserID=? ORDER BY b.BookedAt DESC",userId);}
    public List<Booking> all()throws SQLException{return list(BASE+" ORDER BY b.BookedAt DESC");}
    public Booking find(int id)throws SQLException{List<Booking>l=list(BASE+" WHERE b.BookingID=?",id);return l.isEmpty()?null:l.get(0);}
    public Booking findByPnr(String pnr)throws SQLException{List<Booking>l=list(BASE+" WHERE b.PNR=?",pnr);return l.isEmpty()?null:l.get(0);}

    public int countForUser(int userId)throws SQLException{return count("SELECT COUNT(*) FROM Bookings WHERE UserID=?",userId);}
    public int countPendingPaymentForUser(int userId)throws SQLException{return count("SELECT COUNT(*) FROM Bookings WHERE UserID=? AND BookingStatus='PENDING' AND PaymentStatus='UNPAID'",userId);}
    public int countConfirmedForUser(int userId)throws SQLException{return count("SELECT COUNT(*) FROM Bookings WHERE UserID=? AND BookingStatus='CONFIRMED' AND PaymentStatus='PAID'",userId);}
    public int countAll()throws SQLException{return count("SELECT COUNT(*) FROM Bookings");}

    /** Admin ticket editor: update the passenger details printed on an existing ticket. */
    public Booking updateTicketDetails(int id,String passenger,String passport,String seat)throws SQLException{
        try(Connection c=DBConnection.getConnection()){
            c.setAutoCommit(false);
            try{
                int flightId,total;
                try(PreparedStatement p=c.prepareStatement(LOCK)){
                    p.setInt(1,id);try(ResultSet r=p.executeQuery()){
                        if(!r.next())throw new ValidationException("Reservation not found.");
                        if("CANCELLED".equals(r.getString("BookingStatus")))throw new ValidationException("A cancelled reservation cannot be edited.");
                        if(r.getInt("Departed")==1)throw new ValidationException("This flight has already departed, so the ticket cannot be edited.");
                        flightId=r.getInt("FlightID");total=r.getInt("TotalSeats");
                    }
                }
                try(PreparedStatement p=c.prepareStatement("SELECT TicketID FROM Tickets WHERE BookingID=?")){
                    p.setInt(1,id);try(ResultSet r=p.executeQuery()){if(!r.next())throw new ValidationException("Ticket not found.");}
                }
                Validator.seatInLayout(seat,total);
                if(passportUsed(c,flightId,passport,id))throw new ValidationException("Another active reservation on this flight already uses this passport / ID.");
                try(PreparedStatement p=c.prepareStatement("UPDATE Bookings SET PassengerName=?,PassportNo=?,SeatNumber=? WHERE BookingID=?")){
                    p.setString(1,passenger);p.setString(2,passport);p.setString(3,seat);p.setInt(4,id);p.executeUpdate();
                }
                try(PreparedStatement p=c.prepareStatement("UPDATE Tickets SET LastUpdatedAt=SYSDATETIME() WHERE BookingID=?")){p.setInt(1,id);p.executeUpdate();}
                c.commit();
            }catch(SQLException e){c.rollback();if(DBConnection.isDuplicate(e))throw new ValidationException("Seat "+seat+" is already booked. Please choose another seat.");throw e;}
            catch(RuntimeException e){c.rollback();throw e;}
            finally{c.setAutoCommit(true);}
        }
        return find(id);
    }

    private int count(String q,Object...a)throws SQLException{try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(q)){for(int i=0;i<a.length;i++)p.setObject(i+1,a[i]);try(ResultSet r=p.executeQuery()){r.next();return r.getInt(1);}}}
    private List<Booking> list(String q,Object...a)throws SQLException{try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(q)){for(int i=0;i<a.length;i++)p.setObject(i+1,a[i]);try(ResultSet r=p.executeQuery()){List<Booking>l=new ArrayList<>();while(r.next())l.add(map(r));return l;}}}

    private Booking map(ResultSet r)throws SQLException{
        Booking b=new Booking(); b.setBookingId(r.getInt("BookingID"));b.setPnr(r.getString("PNR"));b.setUserId(r.getInt("UserID"));b.setFlightId(r.getInt("FlightID"));
        b.setPassengerName(r.getString("PassengerName"));b.setPassportNo(r.getString("PassportNo"));b.setSeatNumber(r.getString("SeatNumber"));
        b.setBookingStatus(r.getString("BookingStatus"));b.setPaymentStatus(r.getString("PaymentStatus"));b.setBookedAt(r.getTimestamp("BookedAt"));
        b.setFlightNo(r.getString("FlightNo"));b.setOrigin(r.getString("Origin"));b.setDestination(r.getString("Destination"));b.setDepartureTime(r.getTimestamp("DepartureTime"));
        b.setFare(r.getBigDecimal("Fare"));b.setUsername(r.getString("Username"));b.setFlightStatus(r.getString("FlightStatus"));b.setDeparted(r.getInt("Departed")==1);return b;
    }
}

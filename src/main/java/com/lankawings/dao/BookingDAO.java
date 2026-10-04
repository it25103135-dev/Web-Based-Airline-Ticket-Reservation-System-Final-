package com.lankawings.dao;

import com.lankawings.config.DBConnection;
import com.lankawings.model.Booking;
import com.lankawings.util.ValidationException;
import com.lankawings.util.Validator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.util.*;

/** Booking Management: agent-created client bookings plus view/edit/cancel operations. */
public class BookingDAO {
    private static final String BASE =
            "SELECT b.*,c.Username AS ClientUsername,c.FullName AS ClientFullName,a.Username AS AgentUsername,a.FullName AS AgentFullName,"+
            "f.FlightNo,f.Origin,f.Destination,f.DepartureTime,f.Fare,f.Status AS FlightStatus,"+
            "CASE WHEN f.DepartureTime<=SYSDATETIME() THEN 1 ELSE 0 END AS Departed "+
            "FROM Bookings b JOIN Users c ON b.UserID=c.UserID JOIN Users a ON b.AgentUserID=a.UserID JOIN Flights f ON b.FlightID=f.FlightID";

    public String createForClient(int agentUserId,int clientUserId,int flightId,String passenger,String passport,String seat) throws SQLException {
        String pnr="LW"+UUID.randomUUID().toString().replace("-","").substring(0,8).toUpperCase();
        try(Connection c=DBConnection.getConnection()){
            c.setAutoCommit(false);
            try{
                validateAgentAndClient(c,agentUserId,clientUserId);
                int total; BigDecimal fare;
                try(PreparedStatement p=c.prepareStatement(
                        "SELECT TotalSeats,Fare,Status,CASE WHEN DepartureTime>SYSDATETIME() THEN 1 ELSE 0 END AS Future,"+
                        "(SELECT COUNT(*) FROM Bookings WHERE FlightID=? AND BookingStatus<>'CANCELLED') AS Booked "+
                        "FROM Flights WITH (UPDLOCK,HOLDLOCK) WHERE FlightID=?")){
                    p.setInt(1,flightId);p.setInt(2,flightId);
                    try(ResultSet r=p.executeQuery()){
                        if(!r.next()) throw new ValidationException("Flight not found.");
                        total=r.getInt("TotalSeats");fare=r.getBigDecimal("Fare");
                        if(r.getInt("Future")==0) throw new ValidationException("This flight has already departed.");
                        if(!Set.of("SCHEDULED","DELAYED").contains(r.getString("Status"))) throw new ValidationException("This flight is not open for booking.");
                        if(r.getInt("Booked")>=total) throw new ValidationException("No seats are available on this flight.");
                    }
                }
                Validator.seatInLayout(seat,total);
                if(passportUsed(c,flightId,passport,0)) throw new ValidationException("A passenger with this passport / ID already has an active booking on this flight.");
                BigDecimal commission=fare.multiply(new BigDecimal("0.05")).setScale(2,RoundingMode.HALF_UP);
                try(PreparedStatement p=c.prepareStatement("INSERT INTO Bookings(PNR,UserID,AgentUserID,FlightID,PassengerName,PassportNo,SeatNumber,CommissionAmount) VALUES(?,?,?,?,?,?,?,?)")){
                    p.setString(1,pnr);p.setInt(2,clientUserId);p.setInt(3,agentUserId);p.setInt(4,flightId);p.setString(5,passenger);p.setString(6,passport);p.setString(7,seat);p.setBigDecimal(8,commission);p.executeUpdate();
                }
                audit(c,pnr,agentUserId,"CREATE","Travel agent created booking for client account ID "+clientUserId+".");
                c.commit();return pnr;
            }catch(SQLException e){c.rollback();if(DBConnection.isDuplicate(e))throw new ValidationException("The seat or passenger is already booked on this flight. Please choose another seat or check the client's existing booking.");throw e;}
            catch(RuntimeException e){c.rollback();throw e;} finally{c.setAutoCommit(true);}
        }
    }

    private void validateAgentAndClient(Connection c,int agentId,int clientId) throws SQLException {
        try(PreparedStatement p=c.prepareStatement("SELECT UserID,Role,Status FROM Users WHERE UserID IN (?,?)")){
            p.setInt(1,agentId);p.setInt(2,clientId);boolean agent=false,client=false;
            try(ResultSet r=p.executeQuery()){while(r.next()){if(r.getInt("UserID")==agentId&&"TRAVEL_AGENT".equals(r.getString("Role"))&&"ACTIVE".equals(r.getString("Status")))agent=true;if(r.getInt("UserID")==clientId&&"PASSENGER".equals(r.getString("Role"))&&"ACTIVE".equals(r.getString("Status")))client=true;}}
            if(!agent) throw new ValidationException("Only an active Travel Agent can create a booking on behalf of a client.");
            if(!client) throw new ValidationException("Choose an active passenger account for the client.");
        }
    }

    private boolean passportUsed(Connection c,int flightId,String passport,int exclude) throws SQLException {
        try(PreparedStatement p=c.prepareStatement("SELECT 1 FROM Bookings WHERE FlightID=? AND PassportNo=? AND BookingStatus<>'CANCELLED' AND BookingID<>?")){p.setInt(1,flightId);p.setString(2,passport);p.setInt(3,exclude);try(ResultSet r=p.executeQuery()){return r.next();}}
    }

    public Set<String> takenSeats(int flightId) throws SQLException {
        try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement("SELECT SeatNumber FROM Bookings WHERE FlightID=? AND BookingStatus<>'CANCELLED'")){p.setInt(1,flightId);try(ResultSet r=p.executeQuery()){Set<String>s=new HashSet<>();while(r.next())s.add(r.getString(1).toUpperCase());return s;}}
    }

    public List<Booking> listForAgent(int agentId) throws SQLException{return list(BASE+" WHERE b.AgentUserID=? ORDER BY b.BookedAt DESC",agentId);}
    public List<Booking> listForClient(int clientId) throws SQLException{return list(BASE+" WHERE b.UserID=? ORDER BY b.BookedAt DESC",clientId);}
    public List<Booking> all() throws SQLException{return list(BASE+" ORDER BY b.BookedAt DESC");}
    public Booking find(int id) throws SQLException{List<Booking>l=list(BASE+" WHERE b.BookingID=?",id);return l.isEmpty()?null:l.get(0);}
    public int countForAgent(int id) throws SQLException{return count("SELECT COUNT(*) FROM Bookings WHERE AgentUserID=?",id);}
    public int countForClient(int id) throws SQLException{return count("SELECT COUNT(*) FROM Bookings WHERE UserID=?",id);}
    public int countAll() throws SQLException{return count("SELECT COUNT(*) FROM Bookings");}

    public Booking updateDetails(int id,int actorId,String actorRole,String passenger,String passport,String seat) throws SQLException {
        try(Connection c=DBConnection.getConnection()){
            c.setAutoCommit(false);
            try{
                int flightId,total;
                try(PreparedStatement p=c.prepareStatement("SELECT b.UserID,b.AgentUserID,b.BookingStatus,b.FlightID,f.TotalSeats,CASE WHEN f.DepartureTime<=SYSDATETIME() THEN 1 ELSE 0 END AS Departed FROM Bookings b WITH (UPDLOCK,HOLDLOCK) JOIN Flights f ON b.FlightID=f.FlightID WHERE b.BookingID=?")){
                    p.setInt(1,id);try(ResultSet r=p.executeQuery()){
                        if(!r.next()) throw new ValidationException("Booking not found.");
                        authorize(r.getInt("UserID"),r.getInt("AgentUserID"),actorId,actorRole);
                        if("CANCELLED".equals(r.getString("BookingStatus")))throw new ValidationException("A cancelled booking cannot be edited.");
                        if(r.getInt("Departed")==1)throw new ValidationException("This flight has already departed, so the booking can no longer be edited.");
                        flightId=r.getInt("FlightID");total=r.getInt("TotalSeats");
                    }
                }
                Validator.seatInLayout(seat,total);
                if(passportUsed(c,flightId,passport,id))throw new ValidationException("Another active booking on this flight already uses this passport / ID.");
                try(PreparedStatement p=c.prepareStatement("UPDATE Bookings SET PassengerName=?,PassportNo=?,SeatNumber=?,UpdatedAt=SYSDATETIME() WHERE BookingID=?")){p.setString(1,passenger);p.setString(2,passport);p.setString(3,seat);p.setInt(4,id);p.executeUpdate();}
                Booking current=findWithin(c,id);audit(c,current.getPnr(),actorId,"UPDATE","Passenger/ID/seat details updated.");c.commit();
            }catch(SQLException e){c.rollback();if(DBConnection.isDuplicate(e))throw new ValidationException("Seat "+seat+" is already booked. Please choose another seat.");throw e;}
            catch(RuntimeException e){c.rollback();throw e;}finally{c.setAutoCommit(true);}
        }
        return find(id);
    }

    public Booking cancel(int id,int actorId,String actorRole) throws SQLException {
        String pnr;
        try(Connection c=DBConnection.getConnection()){
            c.setAutoCommit(false);
            try{
                try(PreparedStatement p=c.prepareStatement("SELECT b.PNR,b.UserID,b.AgentUserID,b.BookingStatus,CASE WHEN f.DepartureTime<=SYSDATETIME() THEN 1 ELSE 0 END AS Departed FROM Bookings b WITH (UPDLOCK,HOLDLOCK) JOIN Flights f ON b.FlightID=f.FlightID WHERE b.BookingID=?")){
                    p.setInt(1,id);try(ResultSet r=p.executeQuery()){
                        if(!r.next())throw new ValidationException("Booking not found.");authorize(r.getInt("UserID"),r.getInt("AgentUserID"),actorId,actorRole);
                        if("CANCELLED".equals(r.getString("BookingStatus")))throw new ValidationException("This booking is already cancelled.");
                        if(r.getInt("Departed")==1&&!"ADMIN".equals(actorRole))throw new ValidationException("This flight has already departed and cannot be cancelled.");
                        pnr=r.getString("PNR");
                    }
                }
                try(PreparedStatement p=c.prepareStatement("UPDATE Bookings SET BookingStatus='CANCELLED',UpdatedAt=SYSDATETIME() WHERE BookingID=?")){p.setInt(1,id);p.executeUpdate();}
                audit(c,pnr,actorId,"CANCEL","Booking cancelled. Payment/refund processing is intentionally outside this module.");c.commit();
            }catch(SQLException|RuntimeException e){c.rollback();throw e;}finally{c.setAutoCommit(true);}
        }
        return find(id);
    }

    private void authorize(int clientId,int agentId,int actorId,String role){
        if("ADMIN".equals(role))return;
        if("TRAVEL_AGENT".equals(role)&&agentId==actorId)return;
        if("PASSENGER".equals(role)&&clientId==actorId)return;
        throw new ValidationException("You do not have permission to manage this booking.");
    }

    private void audit(Connection c,String pnr,int actorId,String action,String details) throws SQLException {
        try(PreparedStatement p=c.prepareStatement("INSERT INTO BookingAudit(PNR,ActorUserID,ActionType,Details) VALUES(?,?,?,?)")){p.setString(1,pnr);p.setInt(2,actorId);p.setString(3,action);p.setString(4,details);p.executeUpdate();}
    }

    private int count(String q,Object...args) throws SQLException{try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(q)){for(int i=0;i<args.length;i++)p.setObject(i+1,args[i]);try(ResultSet r=p.executeQuery()){r.next();return r.getInt(1);}}}
    private List<Booking> list(String q,Object...args) throws SQLException{try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(q)){for(int i=0;i<args.length;i++)p.setObject(i+1,args[i]);try(ResultSet r=p.executeQuery()){List<Booking>l=new ArrayList<>();while(r.next())l.add(map(r));return l;}}}
    private Booking findWithin(Connection c,int id) throws SQLException{try(PreparedStatement p=c.prepareStatement(BASE+" WHERE b.BookingID=?")){p.setInt(1,id);try(ResultSet r=p.executeQuery()){return r.next()?map(r):null;}}}
    private Booking map(ResultSet r) throws SQLException{
        Booking b=new Booking();b.setBookingId(r.getInt("BookingID"));b.setPnr(r.getString("PNR"));b.setUserId(r.getInt("UserID"));b.setAgentUserId(r.getInt("AgentUserID"));b.setFlightId(r.getInt("FlightID"));b.setPassengerName(r.getString("PassengerName"));b.setPassportNo(r.getString("PassportNo"));b.setSeatNumber(r.getString("SeatNumber"));b.setBookingStatus(r.getString("BookingStatus"));b.setPaymentStatus(r.getString("PaymentStatus"));b.setCommissionAmount(r.getBigDecimal("CommissionAmount"));b.setBookedAt(r.getTimestamp("BookedAt"));b.setUpdatedAt(r.getTimestamp("UpdatedAt"));b.setClientUsername(r.getString("ClientUsername"));b.setClientFullName(r.getString("ClientFullName"));b.setAgentUsername(r.getString("AgentUsername"));b.setAgentFullName(r.getString("AgentFullName"));b.setFlightNo(r.getString("FlightNo"));b.setOrigin(r.getString("Origin"));b.setDestination(r.getString("Destination"));b.setDepartureTime(r.getTimestamp("DepartureTime"));b.setFare(r.getBigDecimal("Fare"));b.setFlightStatus(r.getString("FlightStatus"));b.setDeparted(r.getInt("Departed")==1);return b;
    }
}

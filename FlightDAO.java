package com.lankawings.dao;

import com.lankawings.config.DBConnection;
import com.lankawings.model.Flight;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Read-only flight dependency used to find a flight before creating a client booking. */
public class FlightDAO {
    private static final String SELECT="SELECT f.*, (SELECT COUNT(*) FROM Bookings b WHERE b.FlightID=f.FlightID AND b.BookingStatus<>'CANCELLED') AS BookedSeats FROM Flights f";

    public List<Flight> search(String origin,String destination,java.sql.Date date) throws SQLException {
        StringBuilder q=new StringBuilder(SELECT+" WHERE f.DepartureTime>SYSDATETIME() AND f.Status IN ('SCHEDULED','DELAYED')");
        List<Object> args=new ArrayList<>();
        if(origin!=null&&!origin.isBlank()){q.append(" AND f.Origin LIKE ?");args.add("%"+origin.trim()+"%");}
        if(destination!=null&&!destination.isBlank()){q.append(" AND f.Destination LIKE ?");args.add("%"+destination.trim()+"%");}
        if(date!=null){q.append(" AND CAST(f.DepartureTime AS DATE)=?");args.add(date);}
        q.append(" ORDER BY f.DepartureTime");
        try(Connection c=DBConnection.getConnection(); PreparedStatement p=c.prepareStatement(q.toString())){
            for(int i=0;i<args.size();i++) p.setObject(i+1,args.get(i));
            try(ResultSet r=p.executeQuery()){List<Flight> l=new ArrayList<>();while(r.next())l.add(map(r));return l;}
        }
    }

    public Flight find(int id) throws SQLException {
        try(Connection c=DBConnection.getConnection(); PreparedStatement p=c.prepareStatement(SELECT+" WHERE f.FlightID=?")){
            p.setInt(1,id); try(ResultSet r=p.executeQuery()){return r.next()?map(r):null;}
        }
    }

    public int countUpcoming() throws SQLException {
        try(Connection c=DBConnection.getConnection(); Statement s=c.createStatement(); ResultSet r=s.executeQuery("SELECT COUNT(*) FROM Flights WHERE DepartureTime>SYSDATETIME() AND Status IN ('SCHEDULED','DELAYED')")){r.next();return r.getInt(1);}
    }

    private Flight map(ResultSet r) throws SQLException {
        Flight f=new Flight(); f.setFlightId(r.getInt("FlightID"));f.setFlightNo(r.getString("FlightNo"));f.setOrigin(r.getString("Origin"));f.setDestination(r.getString("Destination"));f.setDepartureTime(r.getTimestamp("DepartureTime"));f.setArrivalTime(r.getTimestamp("ArrivalTime"));f.setFare(r.getBigDecimal("Fare"));f.setTotalSeats(r.getInt("TotalSeats"));f.setBookedSeats(r.getInt("BookedSeats"));f.setStatus(r.getString("Status"));f.setAircraft(r.getString("Aircraft"));return f;
    }
}

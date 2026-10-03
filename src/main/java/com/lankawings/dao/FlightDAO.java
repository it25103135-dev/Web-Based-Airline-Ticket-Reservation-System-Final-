package com.lankawings.dao;

import com.lankawings.config.DBConnection;
import com.lankawings.model.Flight;
import java.sql.*;
import java.util.*;

/** Read-only flight discovery dependency used by the Ticket Reservation function. */
public class FlightDAO {
    private static final String SELECT = "SELECT f.*, (SELECT COUNT(*) FROM Bookings b WHERE b.FlightID=f.FlightID AND b.BookingStatus<>'CANCELLED') AS BookedSeats FROM Flights f";

    public List<Flight> search(String origin, String destination, java.sql.Date date, boolean upcomingOnly) throws SQLException {
        StringBuilder q=new StringBuilder(SELECT+" WHERE 1=1"); List<Object>a=new ArrayList<>();
        if(origin!=null&&!origin.isBlank()){q.append(" AND f.Origin LIKE ?");a.add("%"+origin.trim()+"%");}
        if(destination!=null&&!destination.isBlank()){q.append(" AND f.Destination LIKE ?");a.add("%"+destination.trim()+"%");}
        if(date!=null){q.append(" AND CAST(f.DepartureTime AS DATE)=?");a.add(date);}
        if(upcomingOnly)q.append(" AND f.DepartureTime>SYSDATETIME()");
        q.append(" ORDER BY f.DepartureTime");
        try(Connection c=DBConnection.getConnection(); PreparedStatement p=c.prepareStatement(q.toString())){
            for(int i=0;i<a.size();i++)p.setObject(i+1,a.get(i));
            try(ResultSet r=p.executeQuery()){List<Flight>l=new ArrayList<>();while(r.next())l.add(map(r));return l;}
        }
    }

    public Flight find(int id) throws SQLException {
        try(Connection c=DBConnection.getConnection(); PreparedStatement p=c.prepareStatement(SELECT+" WHERE f.FlightID=?")){
            p.setInt(1,id); try(ResultSet r=p.executeQuery()){return r.next()?map(r):null;}
        }
    }

    public List<Flight> next(int limit) throws SQLException {
        String q="SELECT TOP (?) f.*, (SELECT COUNT(*) FROM Bookings b WHERE b.FlightID=f.FlightID AND b.BookingStatus<>'CANCELLED') AS BookedSeats FROM Flights f WHERE f.DepartureTime>SYSDATETIME() AND f.Status IN ('SCHEDULED','DELAYED') ORDER BY f.DepartureTime";
        try(Connection c=DBConnection.getConnection(); PreparedStatement p=c.prepareStatement(q)){p.setInt(1,limit);try(ResultSet r=p.executeQuery()){List<Flight>l=new ArrayList<>();while(r.next())l.add(map(r));return l;}}
    }

    private Flight map(ResultSet r) throws SQLException {
        Flight f=new Flight(); f.setFlightId(r.getInt("FlightID")); f.setFlightNo(r.getString("FlightNo")); f.setOrigin(r.getString("Origin"));
        f.setDestination(r.getString("Destination")); f.setDepartureTime(r.getTimestamp("DepartureTime")); f.setArrivalTime(r.getTimestamp("ArrivalTime"));
        f.setFare(r.getBigDecimal("Fare")); f.setTotalSeats(r.getInt("TotalSeats")); f.setBookedSeats(r.getInt("BookedSeats")); f.setStatus(r.getString("Status")); f.setAircraft(r.getString("Aircraft")); return f;
    }
}

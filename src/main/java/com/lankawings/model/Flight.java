package com.lankawings.model;
import java.sql.Timestamp;
import java.math.BigDecimal;
public class Flight {
    private int flightId,totalSeats,bookedSeats; private String flightNo,origin,destination,status,aircraft; private Timestamp departureTime,arrivalTime; private BigDecimal fare;
    public int getFlightId(){return flightId;} public void setFlightId(int v){flightId=v;}
    public int getTotalSeats(){return totalSeats;} public void setTotalSeats(int v){totalSeats=v;}
    public int getBookedSeats(){return bookedSeats;} public void setBookedSeats(int v){bookedSeats=v;}
    public String getFlightNo(){return flightNo;} public void setFlightNo(String v){flightNo=v;}
    public String getOrigin(){return origin;} public void setOrigin(String v){origin=v;}
    public String getDestination(){return destination;} public void setDestination(String v){destination=v;}
    public String getStatus(){return status;} public void setStatus(String v){status=v;}
    public String getAircraft(){return aircraft;} public void setAircraft(String v){aircraft=v;}
    public Timestamp getDepartureTime(){return departureTime;} public void setDepartureTime(Timestamp v){departureTime=v;}
    public Timestamp getArrivalTime(){return arrivalTime;} public void setArrivalTime(Timestamp v){arrivalTime=v;}
    public BigDecimal getFare(){return fare;} public void setFare(BigDecimal v){fare=v;}
    public int getAvailableSeats(){return Math.max(0,totalSeats-bookedSeats);}
}

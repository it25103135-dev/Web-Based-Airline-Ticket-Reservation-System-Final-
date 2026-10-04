package com.lankawings.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

/** Read-only integration model used by Payment Management to identify the booking being paid. */
public class Booking {
    private int bookingId, userId, flightId;
    private String pnr, passengerName, seatNumber, bookingStatus, paymentStatus, flightNo, origin, destination, flightStatus;
    private Timestamp departureTime;
    private BigDecimal fare;
    private boolean departed;
    public int getBookingId(){return bookingId;} public void setBookingId(int v){bookingId=v;}
    public int getUserId(){return userId;} public void setUserId(int v){userId=v;}
    public int getFlightId(){return flightId;} public void setFlightId(int v){flightId=v;}
    public String getPnr(){return pnr;} public void setPnr(String v){pnr=v;}
    public String getPassengerName(){return passengerName;} public void setPassengerName(String v){passengerName=v;}
    public String getSeatNumber(){return seatNumber;} public void setSeatNumber(String v){seatNumber=v;}
    public String getBookingStatus(){return bookingStatus;} public void setBookingStatus(String v){bookingStatus=v;}
    public String getPaymentStatus(){return paymentStatus;} public void setPaymentStatus(String v){paymentStatus=v;}
    public String getFlightNo(){return flightNo;} public void setFlightNo(String v){flightNo=v;}
    public String getOrigin(){return origin;} public void setOrigin(String v){origin=v;}
    public String getDestination(){return destination;} public void setDestination(String v){destination=v;}
    public String getFlightStatus(){return flightStatus;} public void setFlightStatus(String v){flightStatus=v;}
    public Timestamp getDepartureTime(){return departureTime;} public void setDepartureTime(Timestamp v){departureTime=v;}
    public BigDecimal getFare(){return fare;} public void setFare(BigDecimal v){fare=v;}
    public boolean isDeparted(){return departed;} public void setDeparted(boolean v){departed=v;}
    public boolean isActive(){return !"CANCELLED".equals(bookingStatus) && !departed;}
}

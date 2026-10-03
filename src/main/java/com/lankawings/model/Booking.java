package com.lankawings.model;
import java.sql.Timestamp;
import java.math.BigDecimal;
public class Booking {
    private int bookingId,userId,flightId; private String pnr,passengerName,passportNo,seatNumber,bookingStatus,paymentStatus,flightNo,origin,destination; private Timestamp bookedAt,departureTime; private BigDecimal fare; private String username,flightStatus; private boolean departed;
    public int getBookingId(){return bookingId;} public void setBookingId(int v){bookingId=v;}
    public int getUserId(){return userId;} public void setUserId(int v){userId=v;}
    public int getFlightId(){return flightId;} public void setFlightId(int v){flightId=v;}
    public String getPnr(){return pnr;} public void setPnr(String v){pnr=v;}
    public String getPassengerName(){return passengerName;} public void setPassengerName(String v){passengerName=v;}
    public String getPassportNo(){return passportNo;} public void setPassportNo(String v){passportNo=v;}
    public String getSeatNumber(){return seatNumber;} public void setSeatNumber(String v){seatNumber=v;}
    public String getBookingStatus(){return bookingStatus;} public void setBookingStatus(String v){bookingStatus=v;}
    public String getPaymentStatus(){return paymentStatus;} public void setPaymentStatus(String v){paymentStatus=v;}
    public String getFlightNo(){return flightNo;} public void setFlightNo(String v){flightNo=v;}
    public String getOrigin(){return origin;} public void setOrigin(String v){origin=v;}
    public String getDestination(){return destination;} public void setDestination(String v){destination=v;}
    public Timestamp getBookedAt(){return bookedAt;} public void setBookedAt(Timestamp v){bookedAt=v;}
    public Timestamp getDepartureTime(){return departureTime;} public void setDepartureTime(Timestamp v){departureTime=v;}
    public BigDecimal getFare(){return fare;} public void setFare(BigDecimal v){fare=v;}
    public String getUsername(){return username;} public void setUsername(String v){username=v;}
    public String getFlightStatus(){return flightStatus;} public void setFlightStatus(String v){flightStatus=v;}
    public boolean isDeparted(){return departed;} public void setDeparted(boolean v){departed=v;}
    /** Can the passenger still edit / cancel / pay for this booking? */
    public boolean isActive(){return !"CANCELLED".equals(bookingStatus)&&!departed;}
}

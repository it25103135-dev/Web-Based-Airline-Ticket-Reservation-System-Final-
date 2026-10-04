package com.lankawings.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

/** A client booking optionally created and managed by a Travel Agent. */
public class Booking {
    private int bookingId,userId,agentUserId,flightId;
    private String pnr,passengerName,passportNo,seatNumber,bookingStatus,paymentStatus;
    private String clientUsername,clientFullName,agentUsername,agentFullName;
    private String flightNo,origin,destination,flightStatus;
    private Timestamp bookedAt,updatedAt,departureTime;
    private BigDecimal fare,commissionAmount;
    private boolean departed;

    public int getBookingId(){return bookingId;} public void setBookingId(int v){bookingId=v;}
    public int getUserId(){return userId;} public void setUserId(int v){userId=v;}
    public int getAgentUserId(){return agentUserId;} public void setAgentUserId(int v){agentUserId=v;}
    public int getFlightId(){return flightId;} public void setFlightId(int v){flightId=v;}
    public String getPnr(){return pnr;} public void setPnr(String v){pnr=v;}
    public String getPassengerName(){return passengerName;} public void setPassengerName(String v){passengerName=v;}
    public String getPassportNo(){return passportNo;} public void setPassportNo(String v){passportNo=v;}
    public String getSeatNumber(){return seatNumber;} public void setSeatNumber(String v){seatNumber=v;}
    public String getBookingStatus(){return bookingStatus;} public void setBookingStatus(String v){bookingStatus=v;}
    public String getPaymentStatus(){return paymentStatus;} public void setPaymentStatus(String v){paymentStatus=v;}
    public String getClientUsername(){return clientUsername;} public void setClientUsername(String v){clientUsername=v;}
    public String getClientFullName(){return clientFullName;} public void setClientFullName(String v){clientFullName=v;}
    public String getAgentUsername(){return agentUsername;} public void setAgentUsername(String v){agentUsername=v;}
    public String getAgentFullName(){return agentFullName;} public void setAgentFullName(String v){agentFullName=v;}
    public String getFlightNo(){return flightNo;} public void setFlightNo(String v){flightNo=v;}
    public String getOrigin(){return origin;} public void setOrigin(String v){origin=v;}
    public String getDestination(){return destination;} public void setDestination(String v){destination=v;}
    public String getFlightStatus(){return flightStatus;} public void setFlightStatus(String v){flightStatus=v;}
    public Timestamp getBookedAt(){return bookedAt;} public void setBookedAt(Timestamp v){bookedAt=v;}
    public Timestamp getUpdatedAt(){return updatedAt;} public void setUpdatedAt(Timestamp v){updatedAt=v;}
    public Timestamp getDepartureTime(){return departureTime;} public void setDepartureTime(Timestamp v){departureTime=v;}
    public BigDecimal getFare(){return fare;} public void setFare(BigDecimal v){fare=v;}
    public BigDecimal getCommissionAmount(){return commissionAmount;} public void setCommissionAmount(BigDecimal v){commissionAmount=v;}
    public boolean isDeparted(){return departed;} public void setDeparted(boolean v){departed=v;}
    public boolean isActive(){return !"CANCELLED".equals(bookingStatus) && !departed;}
}

package com.lankawings.model;

import java.sql.Timestamp;

public class Ticket {
    private int ticketId;
    private int bookingId;
    private String ticketNumber;
    private String ticketStatus;
    private Timestamp issuedAt;
    private Timestamp lastUpdatedAt;

    public int getTicketId(){return ticketId;}
    public void setTicketId(int v){ticketId=v;}
    public int getBookingId(){return bookingId;}
    public void setBookingId(int v){bookingId=v;}
    public String getTicketNumber(){return ticketNumber;}
    public void setTicketNumber(String v){ticketNumber=v;}
    public String getTicketStatus(){return ticketStatus;}
    public void setTicketStatus(String v){ticketStatus=v;}
    public Timestamp getIssuedAt(){return issuedAt;}
    public void setIssuedAt(Timestamp v){issuedAt=v;}
    public Timestamp getLastUpdatedAt(){return lastUpdatedAt;}
    public void setLastUpdatedAt(Timestamp v){lastUpdatedAt=v;}
}

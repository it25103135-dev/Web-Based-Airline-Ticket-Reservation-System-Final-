package com.lankawings.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

/** Member 1 domain model: a payment transaction. */
public class Payment {
    private int paymentId, bookingId;
    private String pnr, username, method, cardLast4, transactionRef, status;
    private BigDecimal amount;
    private Timestamp paidAt;
    public int getPaymentId(){return paymentId;} public void setPaymentId(int v){paymentId=v;}
    public int getBookingId(){return bookingId;} public void setBookingId(int v){bookingId=v;}
    public String getPnr(){return pnr;} public void setPnr(String v){pnr=v;}
    public String getUsername(){return username;} public void setUsername(String v){username=v;}
    public String getMethod(){return method;} public void setMethod(String v){method=v;}
    public String getCardLast4(){return cardLast4;} public void setCardLast4(String v){cardLast4=v;}
    public String getTransactionRef(){return transactionRef;} public void setTransactionRef(String v){transactionRef=v;}
    public String getStatus(){return status;} public void setStatus(String v){status=v;}
    public BigDecimal getAmount(){return amount;} public void setAmount(BigDecimal v){amount=v;}
    public Timestamp getPaidAt(){return paidAt;} public void setPaidAt(Timestamp v){paidAt=v;}
}

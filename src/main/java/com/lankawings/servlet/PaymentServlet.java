package com.lankawings.servlet;

import com.lankawings.dao.BookingDAO;
import com.lankawings.dao.PaymentDAO;
import com.lankawings.model.Booking;
import com.lankawings.model.Payment;
import com.lankawings.util.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

/** MEMBER 1 use case: Online Payment. */
@WebServlet("/payment")
public class PaymentServlet extends HttpServlet {
    private static final String DEMO_DECLINE_CARD="4000000000000002";
    private static final RateLimiter ATTEMPTS=new RateLimiter(6,15*60_000L,10*60_000L);
    private Booking ownBooking(HttpServletRequest r,int id)throws Exception{Booking b=new BookingDAO().find(id);return(b!=null&&b.getUserId()==Req.uid(r))?b:null;}
    @Override protected void doGet(HttpServletRequest r,HttpServletResponse s)throws ServletException,IOException{
        try{
            int id=Req.id(r,"bookingId","booking");Booking b=ownBooking(r,id);if(b==null){s.sendError(404);return;}
            if("PAID".equals(b.getPaymentStatus())){s.sendRedirect(r.getContextPath()+"/booking-confirmed?bookingId="+id);return;}
            r.setAttribute("booking",b);if(!b.isActive())r.setAttribute("error","This booking can no longer be paid (cancelled or the flight has departed).");else if("CANCELLED".equals(b.getFlightStatus()))r.setAttribute("error","This flight has been cancelled.");
        }catch(Exception e){Req.error(r,e);}
        r.getRequestDispatcher("/WEB-INF/views/payment.jsp").forward(r,s);
    }
    @Override protected void doPost(HttpServletRequest r,HttpServletResponse s)throws ServletException,IOException{
        int uid=Req.uid(r);String limitKey="pay|"+uid;
        try{
            int id=Req.id(r,"bookingId","booking");if(ATTEMPTS.isBlocked(limitKey))throw new ValidationException(ATTEMPTS.waitMessage(limitKey));
            Booking b=ownBooking(r,id);if(b==null)throw new ValidationException("Booking not found.");
            String method=Validator.oneOf(Req.param(r,"method"),"payment method","Visa","Mastercard","Debit Card");Validator.cardHolder(Req.param(r,"cardHolder"));
            String digits=Validator.cardNumber(Req.param(r,"cardNumber"),method);Validator.expiry(Req.param(r,"expiry"));Validator.cvv(Req.param(r,"cvv"));String last4=digits.substring(12);
            PaymentDAO dao=new PaymentDAO();
            if(DEMO_DECLINE_CARD.equals(digits)){dao.recordFailure(id,b.getFare(),method,last4);throw new ValidationException("Your bank declined this card. Please try a different card. (Demo decline card)");}
            Payment p=dao.pay(id,uid,method,last4);ATTEMPTS.reset(limitKey);Req.flash(r,"Payment successful. Transaction: "+p.getTransactionRef());
            s.sendRedirect(r.getContextPath()+"/booking-confirmed?bookingId="+id);
        }catch(Exception e){if(e instanceof ValidationException)ATTEMPTS.record(limitKey);Req.error(r,e);doGet(r,s);}
    }
}

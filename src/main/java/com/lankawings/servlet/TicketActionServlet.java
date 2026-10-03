package com.lankawings.servlet;

import com.lankawings.dao.TicketDAO;
import com.lankawings.util.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

/** Passenger one-click demo action: mark reservation paid, issue ticket, then open the ticket. */
@WebServlet("/mark-paid")
public class TicketActionServlet extends HttpServlet {
    @Override protected void doPost(HttpServletRequest r,HttpServletResponse s)throws ServletException,IOException{
        try{
            int bookingId=Req.id(r,"bookingId","booking");
            new TicketDAO().markPaidAndIssue(bookingId,Req.uid(r));
            Req.flash(r,"Reservation marked PAID. Your e-ticket is ready.");
            s.sendRedirect(r.getContextPath()+"/ticket?bookingId="+bookingId);
        }catch(Exception e){
            if(e instanceof ValidationException)Req.flashError(r,e.getMessage());
            else Req.flashError(r,"Unable to prepare the ticket right now. Please try again.");
            s.sendRedirect(r.getContextPath()+"/reservations");
        }
    }
}

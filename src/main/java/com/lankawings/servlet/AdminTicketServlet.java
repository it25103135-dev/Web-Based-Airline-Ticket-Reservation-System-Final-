package com.lankawings.servlet;

import com.lankawings.dao.*;
import com.lankawings.model.*;
import com.lankawings.util.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.*;

@WebServlet("/admin/tickets")
public class AdminTicketServlet extends HttpServlet {
    @Override protected void doGet(HttpServletRequest r,HttpServletResponse s)throws ServletException,IOException{
        try{
            TicketDAO td=new TicketDAO();BookingDAO bd=new BookingDAO();
            List<Ticket> tickets=td.all();Map<Integer,Booking> bookingMap=new HashMap<>();
            for(Ticket t:tickets){Booking b=bd.find(t.getBookingId());if(b!=null)bookingMap.put(t.getBookingId(),b);}
            r.setAttribute("tickets",tickets);r.setAttribute("ticketBookings",bookingMap);
            r.setAttribute("ticketCount",td.countAll());r.setAttribute("reservationCount",bd.countAll());
        }catch(Exception e){Req.error(r,e);}
        r.getRequestDispatcher("/WEB-INF/views/admin-tickets.jsp").forward(r,s);
    }

    @Override protected void doPost(HttpServletRequest r,HttpServletResponse s)throws ServletException,IOException{
        try{
            if(!Req.isAdmin(r)){s.sendError(403);return;}
            int bookingId=Req.id(r,"bookingId","booking");String action=Req.param(r,"action");
            if("update".equals(action)){
                new BookingDAO().updateTicketDetails(bookingId,
                        Validator.fullName(Req.param(r,"passengerName"),"Passenger name"),
                        Validator.passport(Req.param(r,"passportNo")),
                        Validator.seat(Req.param(r,"seatNumber")));
                Req.flash(r,"Ticket details updated successfully.");
            }else if("delete".equals(action)){
                new TicketDAO().deleteForAdmin(bookingId);
                Req.flash(r,"Ticket deleted. The reservation remains in the system.");
            }else throw new ValidationException("Unknown ticket action.");
            s.sendRedirect(r.getContextPath()+"/admin/tickets");
        }catch(Exception e){Req.error(r,e);doGet(r,s);}
    }
}

package com.lankawings.servlet;

import com.lankawings.dao.BookingDAO;
import com.lankawings.dao.TicketDAO;
import com.lankawings.model.Booking;
import com.lankawings.model.Ticket;
import com.lankawings.util.Req;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/ticket")
public class TicketServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest r, HttpServletResponse s) throws ServletException, IOException {
        try {
            Booking b = new BookingDAO().find(Req.id(r, "bookingId", "booking"));
            if (b == null || (!Req.isAdmin(r) && b.getUserId() != Req.uid(r))) { s.sendError(404); return; }
            TicketDAO td = new TicketDAO();
            Ticket t = td.findByBooking(b.getBookingId());
            r.setAttribute("booking", b);
            if (t == null) r.setAttribute("error", "No ticket exists. Open Ticket management to create a ticket for an eligible paid booking.");
            else r.setAttribute("ticket", t);
        } catch (Exception e) {
            Req.error(r, e);
        }
        r.getRequestDispatcher("/WEB-INF/views/ticket.jsp").forward(r, s);
    }
}

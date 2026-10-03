package com.lankawings.servlet;

import com.lankawings.dao.BookingDAO;
import com.lankawings.dao.TicketDAO;
import com.lankawings.model.Booking;
import com.lankawings.model.Ticket;
import com.lankawings.util.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.*;

@WebServlet("/tickets")
public class TicketManagementServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest r, HttpServletResponse s) throws ServletException, IOException {
        try {
            TicketDAO td = new TicketDAO();
            BookingDAO bd = new BookingDAO();
            List<Ticket> tickets = Req.isAdmin(r) ? td.all() : td.listForUser(Req.uid(r));
            Map<Integer, Booking> bookingMap = new HashMap<>();
            for (Ticket t : tickets) {
                Booking b = bd.find(t.getBookingId());
                if (b != null) bookingMap.put(t.getBookingId(), b);
            }
            Set<Integer> issued = new HashSet<>();
            for (Ticket t : tickets) issued.add(t.getBookingId());
            List<Booking> eligible = new ArrayList<>();
            for (Booking b : Req.isAdmin(r) ? bd.all() : bd.listForUser(Req.uid(r))) {
                if (!issued.contains(b.getBookingId()) && b.isActive() &&
                        "CONFIRMED".equals(b.getBookingStatus()) && "PAID".equals(b.getPaymentStatus()) &&
                        !"CANCELLED".equals(b.getFlightStatus())) eligible.add(b);
            }
            r.setAttribute("eligibleBookings", eligible);
            r.setAttribute("tickets", tickets);
            r.setAttribute("ticketBookings", bookingMap);
        } catch (Exception e) {
            Req.error(r, e);
        }
        r.getRequestDispatcher("/WEB-INF/views/tickets.jsp").forward(r, s);
    }

    @Override
    protected void doPost(HttpServletRequest r, HttpServletResponse s) throws ServletException, IOException {
        try {
            int id = Req.id(r, "bookingId", "booking");
            int uid = Req.uid(r);
            boolean admin = Req.isAdmin(r);
            TicketDAO td = new TicketDAO();
            switch (Req.param(r, "action")) {
                case "create" -> { td.create(id, uid, admin); Req.flash(r, "Ticket created. It is ready to view or print."); }
                case "update" -> {
                    new BookingDAO().updateDetails(id, uid, admin,
                        Validator.fullName(Req.param(r, "passengerName"), "Passenger name"),
                        Validator.passport(Req.param(r, "passportNo")), Validator.seat(Req.param(r, "seatNumber")), true);
                    Req.flash(r, "Ticket and booking details updated.");
                }
                case "delete" -> {
                    if (!admin) { s.sendError(403); return; }
                    td.delete(id, uid, admin);
                    Req.flash(r, "Ticket deleted. Your booking and payment are unchanged.");
                }
                default -> throw new ValidationException("Unknown ticket action.");
            }
            s.sendRedirect(r.getContextPath() + "/tickets");
        } catch (Exception e) { Req.error(r, e); doGet(r, s); }
    }
}

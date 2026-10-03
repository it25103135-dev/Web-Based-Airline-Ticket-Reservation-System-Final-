package com.lankawings.servlet;

import com.lankawings.dao.*;
import com.lankawings.model.Flight;
import com.lankawings.util.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.logging.Level;

/** Ticket reservation: pick a seat on a visual seat map and enter passenger details. */
@WebServlet("/reserve")
public class ReservationServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest r, HttpServletResponse s) throws ServletException, IOException {
        try {
            Flight f = new FlightDAO().find(Req.id(r, "flightId", "flight"));
            if (f == null) { s.sendError(404); return; }
            r.setAttribute("flight", f);
            r.setAttribute("takenSeats", new BookingDAO().takenSeats(f.getFlightId()));
            boolean future = f.getDepartureTime().after(new java.sql.Timestamp(System.currentTimeMillis()));
            boolean openStatus = "SCHEDULED".equals(f.getStatus()) || "DELAYED".equals(f.getStatus());
            if (!future) r.setAttribute("closed", "This flight has already departed.");
            else if (!openStatus) r.setAttribute("closed", "This flight is not open for booking (status: " + f.getStatus() + ").");
            else if (f.getAvailableSeats() == 0) r.setAttribute("closed", "This flight is fully booked.");
        } catch (Exception e) {
            Req.error(r, e);
        }
        r.getRequestDispatcher("/WEB-INF/views/reserve.jsp").forward(r, s);
    }

    @Override
    protected void doPost(HttpServletRequest r, HttpServletResponse s) throws ServletException, IOException {
        try {
            int uid = Req.uid(r);
            int flightId = Req.id(r, "flightId", "flight");
            String name = Validator.fullName(Req.param(r, "passengerName"), "Passenger name");
            String passport = Validator.passport(Req.param(r, "passportNo"));
            String seat = Validator.seat(Req.param(r, "seatNumber"));
            String pnr = new BookingDAO().create(uid, flightId, name, passport, seat);
            try {
                new NotificationDAO().add(uid, "Reservation created", "Reservation " + pnr + " (seat " + seat + ") is pending payment. Pay now to confirm and receive your e-ticket.", "BOOKING");
            } catch (Exception e) { Req.log().log(Level.WARNING, "notification failed", e); }
            Req.flash(r, "Seat " + seat + " reserved! Your booking reference is " + pnr + ". Complete payment to confirm it.");
            s.sendRedirect(r.getContextPath() + "/bookings");
        } catch (Exception e) {
            Req.error(r, e);
            doGet(r, s);
        }
    }
}

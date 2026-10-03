package com.lankawings.servlet;

import com.lankawings.dao.*;
import com.lankawings.model.Booking;
import com.lankawings.util.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.logging.Level;

/** Booking management: list / search, edit passenger details & seat, cancel (with automatic refund when paid). */
@WebServlet("/bookings")
public class BookingServlet extends HttpServlet {
    private final BookingDAO bookings = new BookingDAO();

    @Override
    protected void doGet(HttpServletRequest r, HttpServletResponse s) throws ServletException, IOException {
        try {
            r.setAttribute("bookings", Req.isAdmin(r) ? bookings.all() : bookings.listForUser(Req.uid(r)));
        } catch (Exception e) {
            Req.error(r, e);
        }
        r.getRequestDispatcher("/WEB-INF/views/bookings.jsp").forward(r, s);
    }

    @Override
    protected void doPost(HttpServletRequest r, HttpServletResponse s) throws ServletException, IOException {
        int uid = Req.uid(r);
        boolean admin = Req.isAdmin(r);
        try {
            int id = Req.id(r, "id", "booking");
            switch (Req.param(r, "action")) {
                case "cancel" -> {
                    Booking b = bookings.cancel(id, uid, admin);
                    boolean refunded = "REFUNDED".equals(b.getPaymentStatus());
                    notify(b.getUserId(), "Booking cancelled", "Booking " + b.getPnr() + " has been cancelled." + (refunded ? " Your payment has been refunded." : ""));
                    Req.flash(r, "Booking " + b.getPnr() + " cancelled." + (refunded ? " Your payment was refunded." : ""));
                }
                case "update" -> {
                    String name = Validator.fullName(Req.param(r, "passengerName"), "Passenger name");
                    String passport = Validator.passport(Req.param(r, "passportNo"));
                    String seat = Validator.seat(Req.param(r, "seatNumber"));
                    Booking b = bookings.updateDetails(id, uid, admin, name, passport, seat);
                    notify(b.getUserId(), "Booking updated", "Booking " + b.getPnr() + " was updated (passenger " + name + ", seat " + seat + ").");
                    Req.flash(r, "Booking " + b.getPnr() + " updated.");
                }
                default -> throw new ValidationException("Unknown action.");
            }
            s.sendRedirect(r.getContextPath() + "/bookings");
        } catch (Exception e) {
            Req.error(r, e);
            doGet(r, s);
        }
    }

    private void notify(int userId, String title, String msg) {
        try { new NotificationDAO().add(userId, title, msg, "BOOKING"); }
        catch (Exception e) { Req.log().log(Level.WARNING, "notification failed", e); }
    }
}

package com.lankawings.servlet;

import com.lankawings.dao.FlightDAO;
import com.lankawings.dao.NotificationDAO;
import com.lankawings.model.Flight;
import com.lankawings.util.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.logging.Level;

/** Manage flight schedules: create, update, cancel/status change and safe delete. */
@WebServlet("/admin/flights")
public class AdminFlightServlet extends HttpServlet {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");
    private static final Set<String> MAY_BE_IN_PAST = Set.of("BOARDING", "COMPLETED", "CANCELLED");
    private final FlightDAO flights = new FlightDAO();

    private static final class Form {
        String no, origin, dest, status, aircraft; LocalDateTime dep, arr; BigDecimal fare; int seats;
    }

    @Override protected void doGet(HttpServletRequest r, HttpServletResponse s) throws ServletException, IOException {
        try { r.setAttribute("flights", flights.all()); } catch (Exception e) { Req.error(r, e); }
        r.getRequestDispatcher("/WEB-INF/views/admin-flights.jsp").forward(r, s);
    }

    @Override protected void doPost(HttpServletRequest r, HttpServletResponse s) throws ServletException, IOException {
        String action = Req.param(r, "action");
        try {
            switch (action) {
                case "delete" -> { flights.delete(Req.id(r, "id", "flight")); Req.flash(r, "Flight deleted."); }
                case "create" -> {
                    Form f = parse(r);
                    flights.create(f.no, f.origin, f.dest, Timestamp.valueOf(f.dep), Timestamp.valueOf(f.arr), f.fare, f.seats, f.status, f.aircraft);
                    Req.flash(r, "Flight " + f.no + " created and schedule validated.");
                }
                case "update" -> {
                    int id = Req.id(r, "id", "flight"); Form f = parse(r); Flight before = flights.find(id);
                    if (before == null) throw new ValidationException("Flight not found.");
                    flights.update(id, f.no, f.origin, f.dest, Timestamp.valueOf(f.dep), Timestamp.valueOf(f.arr), f.fare, f.seats, f.status, f.aircraft);
                    notifyPassengers(id, before, f); Req.flash(r, "Flight " + f.no + " updated.");
                }
                default -> throw new ValidationException("Unknown action.");
            }
            s.sendRedirect(r.getContextPath() + "/admin/flights");
        } catch (Exception e) { Req.error(r, e); doGet(r, s); }
    }

    private Form parse(HttpServletRequest r) {
        Form f = new Form();
        f.no = Validator.flightNo(Req.param(r, "flightNo"));
        f.origin = Validator.place(Req.param(r, "origin"), "Origin");
        f.dest = Validator.place(Req.param(r, "destination"), "Destination");
        if (f.origin.equalsIgnoreCase(f.dest)) throw new ValidationException("Origin and destination must be different.");
        f.dep = Validator.dateTime(Req.param(r, "departure"), "Departure");
        f.arr = Validator.dateTime(Req.param(r, "arrival"), "Arrival");
        if (!f.arr.isAfter(f.dep)) throw new ValidationException("Arrival time must be after departure time.");
        long minutes = Duration.between(f.dep, f.arr).toMinutes();
        if (minutes < 20) throw new ValidationException("A flight must last at least 20 minutes.");
        if (minutes > 24 * 60) throw new ValidationException("A flight cannot last longer than 24 hours.");
        f.status = Validator.oneOf(Req.param(r, "status"), "status", Validator.FLIGHT_STATUSES);
        if (!MAY_BE_IN_PAST.contains(f.status) && !f.dep.isAfter(LocalDateTime.now()))
            throw new ValidationException("Departure must be in the future for a " + f.status + " flight.");
        f.fare = Validator.money(Req.param(r, "fare"), "Fare", 1, 1_000_000);
        f.seats = Validator.intRange(Req.param(r, "totalSeats"), "Total seats", 1, 500);
        f.aircraft = Validator.aircraft(Req.param(r, "aircraft"));
        return f;
    }

    private void notifyPassengers(int id, Flight before, Form after) {
        try {
            String route = after.origin + " to " + after.dest;
            String title = null, msg = null;
            if (!before.getStatus().equals(after.status) && ("DELAYED".equals(after.status) || "CANCELLED".equals(after.status))) {
                title = "Flight " + after.no + " " + after.status.toLowerCase();
                msg = "Your flight " + after.no + " (" + route + ") is now " + after.status + "." +
                        ("DELAYED".equals(after.status) ? " New departure: " + after.dep.format(FMT) + "." : " Please contact customer support for the next available action.");
            } else if (!before.getDepartureTime().toLocalDateTime().withSecond(0).withNano(0).equals(after.dep.withSecond(0).withNano(0))) {
                title = "Flight " + after.no + " rescheduled";
                msg = "Your flight " + after.no + " (" + route + ") now departs on " + after.dep.format(FMT) + ".";
            }
            if (title != null) new NotificationDAO().notifyFlightPassengers(id, title, msg);
        } catch (Exception e) { Req.log().log(Level.WARNING, "Could not create flight-change notification records", e); }
    }
}

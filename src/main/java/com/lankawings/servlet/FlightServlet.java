package com.lankawings.servlet;

import com.lankawings.dao.FlightDAO;
import com.lankawings.model.Flight;
import com.lankawings.util.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;

/** Passenger flight search (upcoming flights only). */
@WebServlet("/flights")
public class FlightServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest r, HttpServletResponse s) throws ServletException, IOException {
        try {
            String origin = Req.param(r, "origin").strip();
            String destination = Req.param(r, "destination").strip();
            String dateText = Req.param(r, "date").strip();
            if (!origin.isEmpty()) origin = Validator.text(origin, "From", 2, 80);
            if (!destination.isEmpty()) destination = Validator.text(destination, "To", 2, 80);
            java.sql.Date date = dateText.isEmpty() ? null : java.sql.Date.valueOf(Validator.date(dateText, "Date"));
            List<Flight> flights = new FlightDAO().search(origin, destination, date, true);
            r.setAttribute("flights", flights);
        } catch (Exception e) {
            Req.error(r, e);
        }
        r.getRequestDispatcher("/WEB-INF/views/flights.jsp").forward(r, s);
    }
}

package com.lankawings.servlet;

import com.lankawings.dao.FlightDAO;
import com.lankawings.util.Req;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {
    @Override protected void doGet(HttpServletRequest r, HttpServletResponse s) throws ServletException, IOException {
        try {
            FlightDAO dao = new FlightDAO();
            r.setAttribute("upcoming", dao.countUpcoming());
            r.setAttribute("scheduled", dao.countStatus("SCHEDULED"));
            r.setAttribute("delayed", dao.countStatus("DELAYED"));
            r.setAttribute("cancelled", dao.countStatus("CANCELLED"));
            r.setAttribute("nextFlights", dao.next(6));
        } catch (Exception e) { Req.error(r, e); }
        r.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(r, s);
    }
}

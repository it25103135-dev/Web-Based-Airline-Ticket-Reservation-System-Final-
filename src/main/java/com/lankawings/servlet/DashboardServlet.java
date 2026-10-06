package com.lankawings.servlet;

import com.lankawings.dao.*;
import com.lankawings.util.Req;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest r, HttpServletResponse s) throws ServletException, IOException {
        try {
            int uid = Req.uid(r);
            boolean admin = Req.isAdmin(r);
            BookingDAO b = new BookingDAO();
            r.setAttribute("bookingCount", admin ? b.countAll() : b.countForUser(uid));
            r.setAttribute("flightCount", new FlightDAO().countUpcoming());
            r.setAttribute("unread", new NotificationDAO().unread(uid));
            r.setAttribute("recentBookings", (admin ? b.all() : b.listForUser(uid)).stream().limit(5).toList());
            if (admin) {
                r.setAttribute("userCount", new UserDAO().count());
                r.setAttribute("revenue", new PaymentDAO().revenue());
                r.setAttribute("avgRating", new FeedbackDAO().stats().average);
            }
        } catch (Exception e) {
            Req.error(r, e);
        }
        r.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(r, s);
    }
}

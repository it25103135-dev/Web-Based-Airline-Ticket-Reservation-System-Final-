package com.lankawings.servlet;

import com.lankawings.dao.BookingDAO;
import com.lankawings.model.Booking;
import com.lankawings.util.Req;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/booking-confirmed")
public class BookingConfirmationServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest r, HttpServletResponse s) throws ServletException, IOException {
        try {
            Booking b = new BookingDAO().find(Req.id(r, "bookingId", "booking"));
            if (b == null || b.getUserId() != Req.uid(r)) { s.sendError(404); return; }
            if (!"PAID".equals(b.getPaymentStatus()) || !"CONFIRMED".equals(b.getBookingStatus())) {
                s.sendRedirect(r.getContextPath() + "/bookings"); return;
            }
            r.setAttribute("booking", b);
        } catch (Exception e) { Req.error(r, e); s.sendError(500); return; }
        r.getRequestDispatcher("/WEB-INF/views/booking-confirmed.jsp").forward(r, s);
    }
}

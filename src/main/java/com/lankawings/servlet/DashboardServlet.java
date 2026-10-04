package com.lankawings.servlet;

import com.lankawings.dao.BookingDAO;
import com.lankawings.dao.PaymentDAO;
import com.lankawings.util.Req;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

/** Member 1 landing page: exposes only payment-related entry points and read-only payable bookings. */
@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {
    @Override protected void doGet(HttpServletRequest r,HttpServletResponse s)throws ServletException,IOException{
        try{
            r.setAttribute("payableBookings",new BookingDAO().listPayableForUser(Req.uid(r)));
            if(Req.isAdmin(r))r.setAttribute("revenue",new PaymentDAO().revenue());
        }catch(Exception e){Req.error(r,e);}
        r.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(r,s);
    }
}

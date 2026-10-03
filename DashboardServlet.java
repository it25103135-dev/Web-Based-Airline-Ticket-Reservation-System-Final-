package com.lankawings.servlet;

import com.lankawings.dao.BookingDAO;
import com.lankawings.dao.FlightDAO;
import com.lankawings.util.Req;
import jakarta.servlet.ServletException;import jakarta.servlet.annotation.WebServlet;import jakarta.servlet.http.*;import java.io.IOException;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet{
 private final BookingDAO bookings=new BookingDAO(); private final FlightDAO flights=new FlightDAO();
 @Override protected void doGet(HttpServletRequest r,HttpServletResponse s)throws ServletException,IOException{
  try{
   int uid=Req.uid(r);String role=Req.role(r);int count="ADMIN".equals(role)?bookings.countAll():"TRAVEL_AGENT".equals(role)?bookings.countForAgent(uid):bookings.countForClient(uid);
   r.setAttribute("bookingCount",count);r.setAttribute("upcomingFlights",flights.countUpcoming());
  }catch(Exception e){Req.error(r,e);}r.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(r,s);
 }
}

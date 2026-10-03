package com.lankawings.servlet;

import com.lankawings.dao.FlightDAO;import com.lankawings.util.*;import jakarta.servlet.ServletException;import jakarta.servlet.annotation.WebServlet;import jakarta.servlet.http.*;import java.io.IOException;import java.sql.Date;
@WebServlet("/flights") public class FlightServlet extends HttpServlet{
 private final FlightDAO dao=new FlightDAO();
 @Override protected void doGet(HttpServletRequest r,HttpServletResponse s)throws ServletException,IOException{
  try{Date d=null;String raw=Req.param(r,"date").strip();if(!raw.isEmpty())try{d=Date.valueOf(raw);}catch(IllegalArgumentException e){throw new ValidationException("Enter a valid departure date.");}
   r.setAttribute("flights",dao.search(Req.param(r,"origin"),Req.param(r,"destination"),d));
  }catch(Exception e){Req.error(r,e);}r.getRequestDispatcher("/WEB-INF/views/flights.jsp").forward(r,s);
 }
}

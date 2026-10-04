package com.lankawings.servlet;

import com.lankawings.dao.BookingDAO;import com.lankawings.model.Booking;import com.lankawings.util.*;import jakarta.servlet.ServletException;import jakarta.servlet.annotation.WebServlet;import jakarta.servlet.http.*;import java.io.IOException;

@WebServlet("/bookings") public class BookingServlet extends HttpServlet{
 private final BookingDAO dao=new BookingDAO();
 @Override protected void doGet(HttpServletRequest r,HttpServletResponse s)throws ServletException,IOException{
  try{String role=Req.role(r);int uid=Req.uid(r);r.setAttribute("bookings","ADMIN".equals(role)?dao.all():"TRAVEL_AGENT".equals(role)?dao.listForAgent(uid):dao.listForClient(uid));}
  catch(Exception e){Req.error(r,e);}r.getRequestDispatcher("/WEB-INF/views/bookings.jsp").forward(r,s);
 }
 @Override protected void doPost(HttpServletRequest r,HttpServletResponse s)throws ServletException,IOException{
  try{int id=Req.id(r,"id","booking");String action=Req.param(r,"action");
   if("update".equals(action)){String name=Validator.fullName(Req.param(r,"passengerName"),"Passenger name");String passport=Validator.passport(Req.param(r,"passportNo"));String seat=Validator.seat(Req.param(r,"seatNumber"));Booking b=dao.updateDetails(id,Req.uid(r),Req.role(r),name,passport,seat);Req.flash(r,"Booking "+b.getPnr()+" updated.");}
   else if("cancel".equals(action)){Booking b=dao.cancel(id,Req.uid(r),Req.role(r));String note="PAID".equals(b.getPaymentStatus())?" Payment/refund handling is separate from this function.":"";Req.flash(r,"Booking "+b.getPnr()+" cancelled."+note);}
   else throw new ValidationException("Unknown booking action.");
   s.sendRedirect(r.getContextPath()+"/bookings");
  }catch(Exception e){Req.error(r,e);doGet(r,s);}
 }
}

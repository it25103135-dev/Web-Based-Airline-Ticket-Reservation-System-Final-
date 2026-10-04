package com.lankawings.servlet;

import com.lankawings.dao.*;import com.lankawings.model.*;import com.lankawings.util.*;import jakarta.servlet.ServletException;import jakarta.servlet.annotation.WebServlet;import jakarta.servlet.http.*;import java.io.IOException;

@WebServlet("/agent/book")
public class AgentBookingServlet extends HttpServlet{
 private final FlightDAO flights=new FlightDAO();private final BookingDAO bookings=new BookingDAO();private final AccountDAO accounts=new AccountDAO();
 @Override protected void doGet(HttpServletRequest r,HttpServletResponse s)throws ServletException,IOException{
  try{int flightId=Req.id(r,"flightId","flight");Flight f=flights.find(flightId);if(f==null)throw new ValidationException("Flight not found.");if(f.getDepartureTime().getTime()<=System.currentTimeMillis())throw new ValidationException("This flight has already departed.");
   if(!("SCHEDULED".equals(f.getStatus())||"DELAYED".equals(f.getStatus())))throw new ValidationException("This flight is not open for booking.");
   r.setAttribute("flight",f);r.setAttribute("takenSeats",bookings.takenSeats(flightId));r.setAttribute("clients",accounts.activePassengers());
  }catch(Exception e){Req.error(r,e);}r.getRequestDispatcher("/WEB-INF/views/agent-book.jsp").forward(r,s);
 }
 @Override protected void doPost(HttpServletRequest r,HttpServletResponse s)throws ServletException,IOException{
  try{
   if(!Req.isAgent(r))throw new ValidationException("Only a Travel Agent account can create a booking on behalf of a client.");
   int flightId=Req.id(r,"flightId","flight");int clientId=Req.id(r,"clientId","client");User client=accounts.findActivePassenger(clientId);if(client==null)throw new ValidationException("Choose a valid active passenger account.");
   String passenger=Validator.fullName(Req.param(r,"passengerName"),"Passenger name");String passport=Validator.passport(Req.param(r,"passportNo"));String seat=Validator.seat(Req.param(r,"seatNumber"));
   String pnr=bookings.createForClient(Req.uid(r),clientId,flightId,passenger,passport,seat);Req.flash(r,"Booking "+pnr+" created successfully for "+client.getFullName()+".");s.sendRedirect(r.getContextPath()+"/bookings");
  }catch(Exception e){Req.error(r,e);doGet(r,s);}
 }
}

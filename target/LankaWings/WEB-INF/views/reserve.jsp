<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.*,java.text.SimpleDateFormat,com.lankawings.model.Flight,com.lankawings.util.Validator" %>
<!doctype html>
<html lang="en">
<head><title>Reserve seat | Lanka Wings</title><%@ include file="/WEB-INF/jspf/head.jspf" %></head>
<body class="app-page">
<%@ include file="/WEB-INF/jspf/header.jspf" %>
<main class="container narrow page-pad">
  <% Flight f = (Flight) request.getAttribute("flight");
     Set<String> taken = (Set<String>) request.getAttribute("takenSeats");
     String closed = (String) request.getAttribute("closed");
     String chosen = request.getParameter("seatNumber") == null ? "" : request.getParameter("seatNumber").toUpperCase();
     if (err != null) { %><div class="alert error"><%=h(err)%></div><% } %>
  <% if (f != null) { SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy, HH:mm"); %>
  <div class="page-title"><span class="eyebrow navy">TICKET RESERVATION</span><h1>Select your seat</h1></div>
  <% if (closed != null) { %><div class="alert error"><%=h(closed)%> <a href="<%=cp%>/flights"><b>Back to flights</b></a></div><% } %>
  <div class="booking-layout">
    <section class="panel">
      <h2>Passenger details</h2>
      <form method="post" data-validate>
        <%@ include file="/WEB-INF/jspf/csrf.jspf" %>
        <input type="hidden" name="flightId" value="<%=f.getFlightId()%>">
        <label>Passenger full name<input name="passengerName" data-rule="name" required maxlength="80" value="<%= request.getParameter("passengerName") != null ? h(request.getParameter("passengerName")) : h(session.getAttribute("fullName")) %>"></label>
        <label>Passport / ID number<input name="passportNo" data-rule="passport" required maxlength="15" placeholder="e.g. N1234567" data-hint="6-15 letters or digits, no spaces" value="<%=h(request.getParameter("passportNo"))%>"></label>
        <fieldset class="seat-fieldset" data-require-radio="Please choose a seat on the map." style="border:0;padding:0;margin:0">
          <b style="font-size:.8rem;color:#526274">Choose your seat</b>
          <div class="seatmap">
            <div class="seatmap-nose">▲ FRONT</div>
            <% int rows = Validator.seatRows(f.getTotalSeats()); char[] cols = {'A','B','C','D','E','F'};
               for (int row = 1; row <= rows; row++) { %>
              <div class="seat-row"><span class="row-no"><%=row%></span>
              <% for (int c = 0; c < 6; c++) {
                   if (c == 3) { %><span></span><% }
                   String code = row + "" + cols[c];
                   boolean exists = ((row - 1) * 6 + c + 1) <= f.getTotalSeats();
                   boolean isTaken = taken != null && taken.contains(code);
                   if (!exists) { %><span class="seat ghost"></span><% continue; } %>
                <label class="seat <%= isTaken ? "taken" : "" %>" title="<%= isTaken ? "Taken" : "Seat " + code %>"><input type="radio" name="seatNumber" value="<%=code%>" <%= isTaken || closed != null ? "disabled" : "" %> <%= code.equals(chosen) && !isTaken ? "checked" : "" %>><span style="animation-delay:<%= (row * 25 + c * 15) %>ms"><%=code%></span></label>
              <% } %></div>
            <% } %>
            <div class="seat-legend"><span><i></i>Available</span><span><i class="sel"></i>Selected</span><span><i class="tk"></i>Taken</span></div>
          </div>
          <small class="field-msg group-msg"></small>
        </fieldset>
        <button class="btn btn-orange full" type="submit" <%= closed != null ? "disabled" : "" %>>Confirm Reservation</button>
      </form>
    </section>
    <aside class="trip-summary">
      <span class="eyebrow">YOUR FLIGHT</span>
      <h2><%=h(f.getFlightNo())%></h2>
      <div class="summary-route"><strong><%=h(f.getOrigin())%></strong><span class="ticket-plane">✈</span><strong><%=h(f.getDestination())%></strong></div>
      <p><%=df.format(f.getDepartureTime())%></p><hr>
      <div class="summary-line"><span>Aircraft</span><b><%=h(f.getAircraft())%></b></div>
      <div class="summary-line"><span>Available seats</span><b><%=f.getAvailableSeats()%></b></div>
      <div class="summary-line"><span>Your seat</span><b class="seat-picked" data-seat-picked>-</b></div>
      <div class="summary-line total"><span>Fare</span><b>LKR <%=String.format("%,.2f", f.getFare())%></b></div>
    </aside>
  </div>
  <% } else if (err == null) { %><div class="alert error">Flight not found.</div><% } %>
</main>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
</body></html>

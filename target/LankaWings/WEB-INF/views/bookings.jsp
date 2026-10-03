<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.*,java.text.SimpleDateFormat,com.lankawings.model.Booking" %>
<!doctype html>
<html lang="en">
<head><title>Bookings | Lanka Wings</title><%@ include file="/WEB-INF/jspf/head.jspf" %></head>
<body class="app-page">
<%@ include file="/WEB-INF/jspf/header.jspf" %>
<main class="container page-pad">
  <% boolean admin = "ADMIN".equals(role); %>
  <div class="page-title"><span class="eyebrow navy"><%= admin ? "ALL PASSENGERS" : "YOUR JOURNEYS" %></span><h1>Booking management</h1></div>
  <% if (err != null) { %><div class="alert error"><%=h(err)%></div><% } %>
  <div class="toolbar">
    <input type="search" id="bkSearch" placeholder="Search by PNR, passenger, flight, route..." data-filter="#bkList" data-filter-item=".booking-card" data-status-select="#bkStatus">
    <select id="bkStatus"><option value="">All statuses</option><option value="PENDING">Pending</option><option value="CONFIRMED">Confirmed</option><option value="CANCELLED">Cancelled</option></select>
  </div>
  <div class="booking-cards" id="bkList">
  <% List<Booking> bs = (List<Booking>) request.getAttribute("bookings");
     SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy, HH:mm");
     if (bs != null && !bs.isEmpty()) { for (Booking b : bs) { %>
    <article class="booking-card reveal" data-status="<%=h(b.getBookingStatus())%>">
      <div class="booking-top">
        <div><span class="eyebrow navy">PNR</span><h3><%=h(b.getPnr())%></h3><% if (admin) { %><small class="muted">by <%=h(b.getUsername())%></small><% } %></div>
        <div><span class="badge status-<%=h(b.getBookingStatus())%>"><%=h(b.getBookingStatus())%></span>
          <span class="badge <%= "PAID".equals(b.getPaymentStatus()) ? "good" : "REFUNDED".equals(b.getPaymentStatus()) ? "status-REFUNDED" : "warn" %>"><%=h(b.getPaymentStatus())%></span></div>
      </div>
      <div class="booking-route"><strong><%=h(b.getOrigin())%></strong><span class="ticket-plane">— ✈ —</span><strong><%=h(b.getDestination())%></strong></div>
      <div class="booking-info">
        <span><small>Flight</small><b><%=h(b.getFlightNo())%></b></span>
        <span><small>Departure</small><b><%=df.format(b.getDepartureTime())%></b></span>
        <span><small>Seat</small><b><%=h(b.getSeatNumber())%></b></span>
        <span><small>Passenger</small><b><%=h(b.getPassengerName())%></b></span>
      </div>
      <% if (b.isActive()) { %>
      <details><summary>Edit reservation details</summary>
        <form method="post" class="inline-form" data-validate>
          <%@ include file="/WEB-INF/jspf/csrf.jspf" %>
          <input type="hidden" name="id" value="<%=b.getBookingId()%>"><input type="hidden" name="action" value="update">
          <label>Name<input name="passengerName" data-rule="name" required maxlength="80" value="<%=h(b.getPassengerName())%>"></label>
          <label>Passport / ID<input name="passportNo" data-rule="passport" required maxlength="15" value="<%=h(b.getPassportNo())%>"></label>
          <label>Seat<input name="seatNumber" data-rule="seat" required maxlength="3" data-hint="e.g. 12A" value="<%=h(b.getSeatNumber())%>"></label>
          <button class="btn btn-navy small" type="submit">Save Changes</button>
        </form>
      </details>
      <% } %>
      <div class="booking-actions">
        <% if ("PAID".equals(b.getPaymentStatus()) || "REFUNDED".equals(b.getPaymentStatus())) { %><a class="btn btn-outline small" href="<%=cp%>/tickets">Manage tickets</a><% } %>
        <% if ("UNPAID".equals(b.getPaymentStatus()) && b.isActive() && !admin) { %><a class="btn btn-orange small" href="<%=cp%>/payment?bookingId=<%=b.getBookingId()%>">Pay Now</a><% } %>
        <% if (!"CANCELLED".equals(b.getBookingStatus()) && (admin || !b.isDeparted())) { %>
        <form method="post" data-confirm="Cancel booking <%=h(b.getPnr())%>?<%= "PAID".equals(b.getPaymentStatus()) ? " Your payment will be refunded." : "" %>">
          <%@ include file="/WEB-INF/jspf/csrf.jspf" %>
          <input type="hidden" name="id" value="<%=b.getBookingId()%>"><input type="hidden" name="action" value="cancel">
          <button class="btn btn-danger small" type="submit">Cancel</button>
        </form>
        <% } %>
      </div>
    </article>
  <% } } else { %>
    <div class="empty-state"><div>🎫</div><h3>No bookings found</h3><a class="btn btn-orange" href="<%=cp%>/flights">Find Flights</a></div>
  <% } %>
  </div>
  <div class="no-results">No bookings match your search.</div>
</main>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
</body></html>

<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.*,java.math.BigDecimal,com.lankawings.model.Booking" %>
<!doctype html>
<html lang="en">
<head><title>Dashboard | Lanka Wings</title><%@ include file="/WEB-INF/jspf/head.jspf" %></head>
<body class="app-page">
<%@ include file="/WEB-INF/jspf/header.jspf" %>
<main class="container page-pad">
  <% boolean admin = "ADMIN".equals(role); %>
  <div class="dashboard-hero reveal">
    <div><span class="eyebrow">GOOD TO SEE YOU</span><h1>Hello, <%=h(session.getAttribute("fullName"))%>.</h1>
      <p><%= admin ? "Here is the airline at a glance." : "Where to next?" %></p></div>
    <a class="btn btn-orange" href="<%=cp%>/flights">Search Flights →</a>
  </div>
  <% if (err != null) { %><div class="alert error"><%=h(err)%></div><% } %>

  <section class="stats-grid">
    <div class="stat-card reveal"><span><%= admin ? "All bookings" : "My bookings" %></span><strong data-count="<%=request.getAttribute("bookingCount")==null?0:request.getAttribute("bookingCount")%>">0</strong><small>Reservations in the system</small></div>
    <div class="stat-card reveal"><span>Upcoming flights</span><strong data-count="<%=request.getAttribute("flightCount")==null?0:request.getAttribute("flightCount")%>">0</strong><small>Open for booking</small></div>
    <div class="stat-card reveal"><span>Unread alerts</span><strong data-count="<%=request.getAttribute("unread")==null?0:request.getAttribute("unread")%>">0</strong><small>Notifications waiting</small></div>
  </section>
  <% if (admin) { BigDecimal rev = (BigDecimal) request.getAttribute("revenue"); Double avg = (Double) request.getAttribute("avgRating"); %>
  <section class="stats-grid">
    <div class="stat-card reveal"><span>Registered users</span><strong data-count="<%=request.getAttribute("userCount")==null?0:request.getAttribute("userCount")%>">0</strong><small><a href="<%=cp%>/admin/users">Manage users →</a></small></div>
    <div class="stat-card reveal"><span>Revenue (LKR)</span><strong data-format="money" data-count="<%= rev == null ? 0 : rev.longValue() %>">0</strong><small><a href="<%=cp%>/payments">View payments →</a></small></div>
    <div class="stat-card reveal"><span>Average rating</span><strong><%= avg == null ? "-" : String.format("%.1f", avg) %> ★</strong><small><a href="<%=cp%>/feedback">Read feedback →</a></small></div>
  </section>
  <% } %>

  <section class="panel reveal">
    <div class="panel-head"><div><span class="eyebrow navy">LATEST ACTIVITY</span><h2>Recent bookings</h2></div><a href="<%=cp%>/bookings">View all</a></div>
    <div class="table-wrap"><table>
      <thead><tr><th>PNR</th><th>Flight</th><th>Route</th><th>Seat</th><th>Status</th><th>Payment</th></tr></thead>
      <tbody>
      <% List<Booking> bs = (List<Booking>) request.getAttribute("recentBookings");
         if (bs != null && !bs.isEmpty()) { for (Booking b : bs) { %>
        <tr><td><b><%=h(b.getPnr())%></b></td><td><%=h(b.getFlightNo())%></td><td><%=h(b.getOrigin())%> → <%=h(b.getDestination())%></td><td><%=h(b.getSeatNumber())%></td>
          <td><span class="badge status-<%=h(b.getBookingStatus())%>"><%=h(b.getBookingStatus())%></span></td>
          <td><span class="badge <%= "PAID".equals(b.getPaymentStatus()) ? "good" : "warn" %>"><%=h(b.getPaymentStatus())%></span></td></tr>
      <% } } else { %><tr><td colspan="6" class="empty">No bookings yet. Search flights to create your first reservation.</td></tr><% } %>
      </tbody></table></div>
  </section>
</main>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
</body></html>

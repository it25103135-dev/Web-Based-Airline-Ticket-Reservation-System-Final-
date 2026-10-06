<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.*,java.text.SimpleDateFormat,com.lankawings.dao.FlightDAO,com.lankawings.model.Flight" %>
<%
    // Live departure board: next flights straight from the database. If the DB is down the board is simply hidden.
    List<Flight> board = null;
    try { board = new FlightDAO().search(null, null, null, true); if (board.size() > 6) board = board.subList(0, 6); } catch (Throwable ignored) { board = null; }
    SimpleDateFormat dfDay = new SimpleDateFormat("dd MMM"), dfTime = new SimpleDateFormat("HH:mm");
%>
<!doctype html>
<html lang="en">
<head>
  <title>Lanka Wings Airlines</title>
  <%@ include file="/WEB-INF/jspf/head.jspf" %>
  <link rel="stylesheet" href="<%=cp%>/assets/css/background-video.css">
  <script src="<%=cp%>/assets/js/background-video.js" defer></script>
</head>
<body class="home-page">
<%@ include file="/WEB-INF/jspf/flash.jspf" %>
<header class="home-nav">
  <a class="brand light" href="#"><span class="brand-mark">LW</span><span>Lanka <b>Wings</b></span></a>
  <nav>
    <a href="#board">Departures</a><a href="#services">Services</a>
    <% if (session.getAttribute("userId") != null) { %><a class="btn btn-orange small" href="<%=cp%>/dashboard">My Dashboard</a>
    <% } else { %><a href="<%=cp%>/login">Login</a><a class="btn btn-orange small" href="<%=cp%>/register">Create Account</a><% } %>
  </nav>
</header>
<section class="hero">
  <%@ include file="/WEB-INF/jspf/background-video.jspf" %>
  <div class="hero-overlay"></div><div class="hero-cloud c1"></div><div class="hero-cloud c2"></div><div class="flying-plane">✈</div>
  <div class="hero-content reveal">
    <span class="eyebrow">WELCOME ABOARD</span>
    <h1>Your world,<br><em>one flight away.</em></h1>
    <div class="hero-actions"><a class="btn btn-orange" href="<%=cp%>/flights">Book Your Journey</a><a class="btn btn-glass" href="#services">Explore Services</a></div>
    <div class="trust-row"><span>✓ Easy Booking</span><span>✓ Seat Selection</span><span>✓ Instant E-Ticket</span></div>
  </div>
  <div class="hero-search-card reveal">
    <div class="mini-title">Find your next flight</div>
    <form action="<%=cp%>/flights" method="get" data-validate>
      <div class="field"><label>From</label><input name="origin" data-rule="place" maxlength="80" placeholder="Colombo (CMB)"></div>
      <div class="swap">⇄</div>
      <div class="field"><label>To</label><input name="destination" data-rule="place" maxlength="80" placeholder="Dubai (DXB)"></div>
      <div class="field"><label>Departure</label><input type="date" name="date"></div>
      <button class="btn btn-navy" type="submit">Search Flights</button>
    </form>
  </div>
  <span class="scroll-hint">SCROLL ↓</span>
</section>

<% if (board != null && !board.isEmpty()) { %>
<section id="board" class="board-section">
  <div class="board reveal">
    <div class="board-head"><div><span class="board-live">LIVE DEPARTURES</span><h2>Next flights from Colombo</h2></div><div class="board-clock" data-clock>--:--:--</div></div>
    <div class="table-wrap"><table>
      <thead><tr><th>Flight</th><th>Destination</th><th>Date</th><th>Departs</th><th>Seats left</th><th>Status</th></tr></thead>
      <tbody>
      <% for (Flight f : board) { %>
        <tr><td class="fno"><%=h(f.getFlightNo())%></td><td class="to"><%=h(f.getDestination())%></td><td><%=dfDay.format(f.getDepartureTime())%></td><td class="time"><%=dfTime.format(f.getDepartureTime())%></td><td><%=f.getAvailableSeats()%></td><td><span class="badge status-<%=h(f.getStatus())%>"><%=h(f.getStatus())%></span></td></tr>
      <% } %>
      </tbody></table></div>
    <div class="board-foot"><span>Times are local to departure airport.</span><a href="<%=cp%>/flights">See all flights →</a></div>
  </div>
</section>
<% } %>

<section id="services" class="section">
  <div class="section-heading reveal"><span class="eyebrow navy">EVERYTHING IN ONE PLACE</span><h2>A smoother way to travel</h2><p>Designed for passengers and airline administrators from sign-in to post-flight feedback.</p></div>
  <div class="feature-grid">
    <div class="feature-card reveal"><div class="icon">👤</div><h3>Secure Accounts</h3><p>Registration, login, profile management and timely travel notifications.</p></div>
    <div class="feature-card reveal"><div class="icon">✈</div><h3>Live Flight Search</h3><p>Browse routes, schedules, fares, seat availability and current flight status.</p></div>
    <div class="feature-card reveal"><div class="icon">🎫</div><h3>Smart Reservations</h3><p>Pick your seat on a live seat map. Double booking is prevented automatically.</p></div>
    <div class="feature-card reveal"><div class="icon">💳</div><h3>Safe Payments</h3><p>Card checks before you pay, transaction history and instant confirmation.</p></div>
    <div class="feature-card reveal"><div class="icon">🧳</div><h3>Manage Bookings</h3><p>Edit passenger details, change seats, cancel with automatic refund and print e-tickets.</p></div>
    <div class="feature-card reveal"><div class="icon">★</div><h3>Passenger Feedback</h3><p>Submit ratings and reviews while administrators respond and improve service.</p></div>
  </div>
</section>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
</body>
</html>

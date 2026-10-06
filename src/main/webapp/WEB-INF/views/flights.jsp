<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.*,java.text.SimpleDateFormat,com.lankawings.model.Flight" %>
<!doctype html>
<html lang="en">
<head><title>Flights | Lanka Wings</title><%@ include file="/WEB-INF/jspf/head.jspf" %></head>
<body class="app-page">
<%@ include file="/WEB-INF/jspf/header.jspf" %>
<main class="container page-pad">
  <div class="page-title reveal"><span class="eyebrow navy">DISCOVER YOUR ROUTE</span><h1>Find a flight</h1></div>
  <form class="search-panel reveal" method="get" data-validate>
    <label>From<input name="origin" data-rule="place" maxlength="80" value="<%=h(request.getParameter("origin"))%>" placeholder="Colombo"></label>
    <label>To<input name="destination" data-rule="place" maxlength="80" value="<%=h(request.getParameter("destination"))%>" placeholder="Dubai"></label>
    <label>Date<input type="date" name="date" data-rule="date" value="<%=h(request.getParameter("date"))%>"></label>
    <button class="btn btn-orange" type="submit">Search Flights</button>
  </form>
  <% if (err != null) { %><div class="alert error"><%=h(err)%></div><% } %>
  <div class="flight-list">
  <% List<Flight> fs = (List<Flight>) request.getAttribute("flights");
     SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy, HH:mm");
     if (fs != null && !fs.isEmpty()) { for (Flight f : fs) { boolean open = ("SCHEDULED".equals(f.getStatus()) || "DELAYED".equals(f.getStatus())) && f.getAvailableSeats() > 0; %>
    <article class="flight-card reveal">
      <div class="flight-no"><span><%=h(f.getFlightNo())%></span><small><%=h(f.getAircraft())%></small></div>
      <div class="route">
        <div><strong><%=h(f.getOrigin())%></strong><small><%=df.format(f.getDepartureTime())%></small></div>
        <div class="route-line"><span>✈</span></div>
        <div><strong><%=h(f.getDestination())%></strong><small><%=df.format(f.getArrivalTime())%></small></div>
      </div>
      <div class="flight-meta"><span class="badge status-<%=h(f.getStatus())%>"><%=h(f.getStatus())%></span>
        <span class="seats-left"><b class="<%= f.getAvailableSeats() <= 5 ? "low" : "" %>"><%=f.getAvailableSeats()%></b> seats left</span></div>
      <div class="price"><small>from</small><strong>LKR <%=String.format("%,.2f", f.getFare())%></strong>
        <% if (open) { %><a class="btn btn-navy small" href="<%=cp%>/reserve?flightId=<%=f.getFlightId()%>">Select</a>
        <% } else { %><span class="badge bad"><%= f.getAvailableSeats() == 0 ? "SOLD OUT" : "NOT BOOKABLE" %></span><% } %></div>
    </article>
  <% } } else if (err == null) { %>
    <div class="empty-state reveal"><div>✈</div><h3>No matching flights</h3><p>Try another route or departure date.</p></div>
  <% } %>
  </div>
</main>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
</body></html>

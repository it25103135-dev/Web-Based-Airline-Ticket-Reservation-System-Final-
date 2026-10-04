<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.*,java.text.SimpleDateFormat,com.lankawings.model.Flight" %>
<!doctype html><html lang="en"><head><title>Flight Management Dashboard | Lanka Wings</title><%@ include file="/WEB-INF/jspf/head.jspf" %></head>
<body class="app-page"><%@ include file="/WEB-INF/jspf/header.jspf" %>
<main class="container page-pad">
  <div class="page-title reveal"><span class="eyebrow navy">FLIGHT OPERATIONS</span><h1>Flight management dashboard</h1></div>
  <% if (err != null) { %><div class="alert error"><%=h(err)%></div><% } %>
  <section class="stats-grid">
    <div class="stat-card reveal"><span>Upcoming active flights</span><strong data-count="<%=request.getAttribute("upcoming")%>">0</strong><small>Future, non-cancelled</small></div>
    <div class="stat-card reveal"><span>Scheduled</span><strong data-count="<%=request.getAttribute("scheduled")%>">0</strong><small>Normal schedule</small></div>
    <div class="stat-card reveal"><span>Delayed</span><strong data-count="<%=request.getAttribute("delayed")%>">0</strong><small>Requires attention</small></div>
    <div class="stat-card reveal"><span>Cancelled</span><strong data-count="<%=request.getAttribute("cancelled")%>">0</strong><small>Retained for traceability</small></div>
  </section>
  <section class="panel reveal">
    <div class="panel-head"><div><span class="eyebrow navy">NEXT DEPARTURES</span><h2>Upcoming flight schedule</h2></div><a class="btn btn-orange small" href="<%=cp%>/admin/flights">Manage flights</a></div>
    <div class="table-wrap"><table><thead><tr><th>Flight</th><th>Route</th><th>Departure</th><th>Aircraft</th><th>Capacity</th><th>Status</th></tr></thead><tbody>
    <% List<Flight> fs=(List<Flight>)request.getAttribute("nextFlights"); SimpleDateFormat df=new SimpleDateFormat("dd MMM yyyy, HH:mm");
       if(fs!=null&&!fs.isEmpty()){ for(Flight f:fs){ %>
      <tr><td><b><%=h(f.getFlightNo())%></b></td><td><%=h(f.getOrigin())%> → <%=h(f.getDestination())%></td><td><%=df.format(f.getDepartureTime())%></td><td><%=h(f.getAircraft())%></td><td><%=f.getBookedSeats()%> / <%=f.getTotalSeats()%></td><td><span class="badge status-<%=h(f.getStatus())%>"><%=h(f.getStatus())%></span></td></tr>
    <% }} else { %><tr><td colspan="6" class="empty">No upcoming flights.</td></tr><% } %>
    </tbody></table></div>
  </section>
</main><%@ include file="/WEB-INF/jspf/footer.jspf" %></body></html>

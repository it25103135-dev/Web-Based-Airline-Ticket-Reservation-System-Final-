<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.*,java.text.SimpleDateFormat,com.lankawings.model.Flight,com.lankawings.util.Validator" %>
<!doctype html>
<html lang="en">
<head><title>Manage flights | Lanka Wings</title><%@ include file="/WEB-INF/jspf/head.jspf" %></head>
<body class="app-page">
<%@ include file="/WEB-INF/jspf/header.jspf" %>
<main class="container page-pad">
  <div class="page-title"><span class="eyebrow navy">RESERVATION MANAGER</span><h1>Manage flight schedules</h1></div>
  <% if (err != null) { %><div class="alert error" role="alert"><%=h(err)%></div><% } %>
  <% boolean creating = "create".equals(request.getParameter("action")); %>

  <section class="panel"><h2>Create a flight schedule</h2>
    <form method="post" class="admin-flight-form" data-validate>
      <%@ include file="/WEB-INF/jspf/csrf.jspf" %><input type="hidden" name="action" value="create">
      <label>Flight no<input name="flightNo" data-rule="flightno" required maxlength="6" placeholder="LW101" value="<%= creating ? h(request.getParameter("flightNo")) : "" %>"></label>
      <label>Origin<input name="origin" data-rule="place" required maxlength="80" placeholder="Colombo (CMB)" value="<%= creating ? h(request.getParameter("origin")) : "Colombo (CMB)" %>"></label>
      <label>Destination<input name="destination" data-rule="place" required maxlength="80" placeholder="Dubai (DXB)" value="<%= creating ? h(request.getParameter("destination")) : "" %>"></label>
      <label>Departure<input type="datetime-local" id="dep" name="departure" data-rule="datetime" data-future required value="<%= creating ? h(request.getParameter("departure")) : "" %>"></label>
      <label>Arrival<input type="datetime-local" name="arrival" data-rule="datetime" data-after="#dep" required value="<%= creating ? h(request.getParameter("arrival")) : "" %>"></label>
      <label>Fare (LKR)<input name="fare" data-rule="money" data-min="1" data-max="1000000" required inputmode="decimal" value="<%= creating ? h(request.getParameter("fare")) : "" %>"></label>
      <label>Total seats<input name="totalSeats" data-rule="int" data-min="1" data-max="500" required inputmode="numeric" value="<%= creating ? h(request.getParameter("totalSeats")) : "72" %>"></label>
      <label>Status<select name="status"><% for (String s : Validator.FLIGHT_STATUSES) { %><option><%=s%></option><% } %></select></label>
      <label>Aircraft<input name="aircraft" data-rule="aircraft" required maxlength="80" value="<%= creating ? h(request.getParameter("aircraft")) : "Airbus A320" %>"></label>
      <button class="btn btn-orange" type="submit">Publish Flight</button>
    </form>
  </section>

  <section class="panel"><h2>Existing flights</h2>
    <div class="toolbar"><input type="search" placeholder="Search by flight, route, aircraft..." data-filter="#flBody" data-filter-item="tr" data-status-select="#flStatus">
      <select id="flStatus"><option value="">All statuses</option><% for (String s : Validator.FLIGHT_STATUSES) { %><option><%=s%></option><% } %></select></div>
    <div class="table-wrap"><table>
      <thead><tr><th>No.</th><th>Route</th><th>Departure</th><th>Fare</th><th>Seats</th><th>Status</th><th>Actions</th></tr></thead>
      <tbody id="flBody">
      <% List<Flight> fs = (List<Flight>) request.getAttribute("flights"); SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy, HH:mm");
         if (fs != null) { for (Flight f : fs) { %>
        <tr data-status="<%=h(f.getStatus())%>"><td><b><%=h(f.getFlightNo())%></b><br><small class="muted"><%=h(f.getAircraft())%></small></td>
          <td><%=h(f.getOrigin())%> → <%=h(f.getDestination())%></td><td><%=df.format(f.getDepartureTime())%></td>
          <td>LKR <%=String.format("%,.2f", f.getFare())%></td><td><%=f.getBookedSeats()%> booked / <%=f.getTotalSeats()%></td>
          <td><span class="badge status-<%=h(f.getStatus())%>"><%=h(f.getStatus())%></span></td>
          <td><div class="row-actions">
            <details><summary class="action-link">Edit</summary>
              <form method="post" class="edit-flight-form" data-validate>
                <%@ include file="/WEB-INF/jspf/csrf.jspf" %><input type="hidden" name="action" value="update"><input type="hidden" name="id" value="<%=f.getFlightId()%>">
                <label>Flight no<input name="flightNo" data-rule="flightno" required maxlength="6" value="<%=h(f.getFlightNo())%>"></label>
                <label>Origin<input name="origin" data-rule="place" required maxlength="80" value="<%=h(f.getOrigin())%>"></label>
                <label>Destination<input name="destination" data-rule="place" required maxlength="80" value="<%=h(f.getDestination())%>"></label>
                <label>Departure<input type="datetime-local" id="dep<%=f.getFlightId()%>" name="departure" data-rule="datetime" required value="<%=f.getDepartureTime().toLocalDateTime().withSecond(0).withNano(0)%>"></label>
                <label>Arrival<input type="datetime-local" name="arrival" data-rule="datetime" data-after="#dep<%=f.getFlightId()%>" required value="<%=f.getArrivalTime().toLocalDateTime().withSecond(0).withNano(0)%>"></label>
                <label>Fare (LKR)<input name="fare" data-rule="money" data-min="1" data-max="1000000" required value="<%=f.getFare()%>"></label>
                <label>Total seats (min <%=f.getBookedSeats()%>)<input name="totalSeats" data-rule="int" data-min="<%=Math.max(1, f.getBookedSeats())%>" data-max="500" required value="<%=f.getTotalSeats()%>"></label>
                <label>Status<select name="status"><% for (String s : Validator.FLIGHT_STATUSES) { %><option <%= s.equals(f.getStatus()) ? "selected" : "" %>><%=s%></option><% } %></select></label>
                <label>Aircraft<input name="aircraft" data-rule="aircraft" required maxlength="80" value="<%=h(f.getAircraft())%>"></label>
                <button class="btn btn-navy small" type="submit">Update</button>
              </form></details>
            <form method="post" data-confirm="Delete flight <%=h(f.getFlightNo())%>? This cannot be undone.">
              <%@ include file="/WEB-INF/jspf/csrf.jspf" %><input type="hidden" name="action" value="delete"><input type="hidden" name="id" value="<%=f.getFlightId()%>"><button class="link-danger" type="submit">Delete</button></form>
          </div></td></tr>
      <% } } %>
      </tbody></table></div>
    <div class="no-results">No flights match your search.</div>
  </section>
</main>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
</body></html>

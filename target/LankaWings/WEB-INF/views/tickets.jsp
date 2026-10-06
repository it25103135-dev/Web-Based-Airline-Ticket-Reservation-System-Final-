<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.*,java.text.SimpleDateFormat,com.lankawings.model.Ticket,com.lankawings.model.Booking" %>
<!doctype html>
<html lang="en">
<head><title>Ticket management | Lanka Wings</title><%@ include file="/WEB-INF/jspf/head.jspf" %></head>
<body class="app-page">
<%@ include file="/WEB-INF/jspf/header.jspf" %>
<main class="container page-pad">
  <div class="page-title"><span class="eyebrow navy">E-TICKETS</span><h1>Ticket management</h1><p><%= "ADMIN".equals(role) ? "Create, view, edit and delete tickets in one place." : "Create, view and edit your e-tickets in one place." %></p></div>
  <% if (err != null) { %><div class="alert error"><%=h(err)%></div><% } %>
  <section class="panel ticket-create">
    <div><span class="eyebrow navy">READY TO FLY</span><h2>Create a ticket</h2><p>Tickets are issued automatically after payment. Recreate a deleted ticket here for an upcoming paid booking.</p></div>
    <% List<Booking> eligible = (List<Booking>) request.getAttribute("eligibleBookings"); if (eligible != null && !eligible.isEmpty()) { %>
    <form method="post" action="<%=cp%>/tickets">
      <%@ include file="/WEB-INF/jspf/csrf.jspf" %><input type="hidden" name="action" value="create">
      <label>Paid booking<select name="bookingId" required><% for (Booking eb : eligible) { %><option value="<%=eb.getBookingId()%>"><%=h(eb.getPnr())%> · <%=h(eb.getPassengerName())%> · <%=h(eb.getFlightNo())%></option><% } %></select></label>
      <button class="btn btn-orange" type="submit">Create ticket</button>
    </form><% } else { %><p class="ticket-hint">No paid bookings need a ticket right now.</p><% } %>
    <% if ("ADMIN".equals(role)) { %><p class="ticket-hint">Deleting a ticket does not cancel the booking or refund payment. To cancel travel, use Bookings.</p><% } %>
  </section>
  <div class="toolbar"><input type="search" placeholder="Search by ticket, PNR, passenger, flight..." data-filter="#tkBody" data-filter-item="tr"></div>
  <div class="table-wrap panel"><table>
    <thead><tr><th>Ticket No.</th><th>PNR</th><th>Passenger</th><th>Flight</th><th>Seat</th><th>Status</th><th>Issued</th><th>Action</th></tr></thead>
    <tbody id="tkBody">
    <% List<Ticket> ts = (List<Ticket>) request.getAttribute("tickets"); Map<Integer, Booking> bm = (Map<Integer, Booking>) request.getAttribute("ticketBookings");
       SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy, HH:mm"); boolean any = false;
       if (ts != null) { for (Ticket t : ts) { Booking b = bm == null ? null : bm.get(t.getBookingId()); if (b == null) continue; any = true; %>
      <tr><td><b><%=h(t.getTicketNumber())%></b></td><td><%=h(b.getPnr())%></td><td><%=h(b.getPassengerName())%></td><td><%=h(b.getFlightNo())%></td><td><%=h(b.getSeatNumber())%></td>
        <td><span class="badge status-<%=h(t.getTicketStatus())%>"><%=h(t.getTicketStatus())%></span></td><td><%=df.format(t.getIssuedAt())%></td>
        <td class="ticket-actions"><a class="btn btn-outline small" href="<%=cp%>/ticket?bookingId=<%=b.getBookingId()%>">View / print</a>
          <% if (b.isActive() && "ISSUED".equals(t.getTicketStatus())) { %>
          <details><summary>Edit ticket</summary><form method="post" action="<%=cp%>/tickets" class="ticket-edit" data-validate>
            <%@ include file="/WEB-INF/jspf/csrf.jspf" %><input type="hidden" name="action" value="update"><input type="hidden" name="bookingId" value="<%=b.getBookingId()%>">
            <label>Passenger name<input name="passengerName" required maxlength="100" value="<%=h(b.getPassengerName())%>"></label>
            <label>Passport / ID<input name="passportNo" required maxlength="20" value="<%=h(b.getPassportNo())%>"></label>
            <label>Seat<input name="seatNumber" required maxlength="4" value="<%=h(b.getSeatNumber())%>"></label>
            <small>Changes also update the linked booking. The seat must be available.</small><button class="btn btn-navy small" type="submit">Save changes</button>
          </form></details><% } %>
          <% if ("ADMIN".equals(role)) { %>
          <form method="post" action="<%=cp%>/tickets" data-confirm="Delete this ticket document? Your booking and payment will remain unchanged.">
            <%@ include file="/WEB-INF/jspf/csrf.jspf" %><input type="hidden" name="action" value="delete"><input type="hidden" name="bookingId" value="<%=b.getBookingId()%>"><button class="btn btn-danger small" type="submit">Delete ticket</button>
          </form><% } %></td></tr>
    <% } } if (!any) { %><tr><td colspan="8" class="empty">No issued tickets found.</td></tr><% } %>
    </tbody></table></div>
</main>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
</body></html>

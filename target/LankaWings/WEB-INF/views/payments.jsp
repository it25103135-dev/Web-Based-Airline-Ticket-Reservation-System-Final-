<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.*,java.text.SimpleDateFormat,com.lankawings.model.Payment" %>
<!doctype html>
<html lang="en">
<head><title>Payments | Lanka Wings</title><%@ include file="/WEB-INF/jspf/head.jspf" %></head>
<body class="app-page">
<%@ include file="/WEB-INF/jspf/header.jspf" %>
<main class="container page-pad">
  <% boolean admin = "ADMIN".equals(role); %>
  <div class="page-title"><span class="eyebrow navy">TRANSACTIONS</span><h1>Payment history</h1><p>Only the last 4 card digits are ever stored.</p></div>
  <% if (admin) { %><p>Deleting a payment permanently removes it from everyone's payment history. Successful payments are also removed from the revenue total. Bookings and tickets stay unchanged; no refund is issued.</p><% } %>
  <% if (err != null) { %><div class="alert error"><%=h(err)%></div><% } %>
  <div class="toolbar"><input type="search" placeholder="Search by PNR, reference, method..." data-filter="#payBody" data-filter-item="tr"></div>
  <div class="table-wrap panel"><table>
    <thead><tr><th>Date</th><th>Transaction</th><th>PNR</th><% if (admin) { %><th>User</th><% } %><th>Method</th><th class="text-right">Amount</th><th>Status</th><% if (admin) { %><th>Actions</th><% } %></tr></thead>
    <tbody id="payBody">
    <% List<Payment> ps = (List<Payment>) request.getAttribute("payments"); SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy, HH:mm");
       if (ps != null && !ps.isEmpty()) { for (Payment p : ps) { %>
      <tr><td><%=df.format(p.getPaidAt())%></td><td class="mono"><%=h(p.getTransactionRef())%></td><td><b><%=h(p.getPnr())%></b></td>
        <% if (admin) { %><td><%=h(p.getUsername())%></td><% } %>
        <td><%=h(p.getMethod())%> •••• <%=h(p.getCardLast4())%></td><td class="text-right amount">LKR <%=String.format("%,.2f", p.getAmount())%></td>
        <td><span class="badge status-<%=h(p.getStatus())%>"><%=h(p.getStatus())%></span></td>
        <% if (admin) { %><td>
          <form method="post" action="<%=cp%>/payments" data-confirm="Permanently delete payment <%=h(p.getTransactionRef())%> for booking <%=h(p.getPnr())%>? This removes it from everyone's payment history and, if successful, the revenue total. The booking and ticket stay unchanged. No refund is issued.">
            <%@ include file="/WEB-INF/jspf/csrf.jspf" %>
            <input type="hidden" name="action" value="delete">
            <input type="hidden" name="paymentId" value="<%=p.getPaymentId()%>">
            <button class="btn btn-danger small" type="submit" aria-label="Delete payment <%=h(p.getTransactionRef())%>">Delete</button>
          </form>
        </td><% } %></tr>
    <% } } else { %><tr><td colspan="<%= admin ? 8 : 6 %>" class="empty">No payments yet.</td></tr><% } %>
    </tbody></table></div>
</main>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
</body></html>

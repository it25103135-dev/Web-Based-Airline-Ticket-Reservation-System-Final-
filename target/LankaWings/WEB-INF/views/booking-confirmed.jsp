<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.lankawings.model.Booking" %>
<!doctype html><html lang="en">
<head><title>Booking confirmed | Lanka Wings</title><%@ include file="/WEB-INF/jspf/head.jspf" %></head>
<body class="app-page"><%@ include file="/WEB-INF/jspf/header.jspf" %>
<main class="container confirmation-wrap">
  <% Booking b = (Booking) request.getAttribute("booking"); %>
  <section class="confirmation-card" aria-labelledby="confirmation-title">
    <div class="celebration" aria-hidden="true"><i></i><i></i><i></i><i></i><i></i><i></i><i></i><i></i></div>
    <div class="confirmation-icon" aria-hidden="true"><svg viewBox="0 0 80 80"><circle cx="40" cy="40" r="36"/><path d="M23 40l12 12 23-25"/></svg></div>
    <span class="eyebrow navy">YOUR NEXT CHAPTER AWAITS</span>
    <h1 id="confirmation-title">Booking confirmed!</h1>
    <p>Your payment was successful. We look forward to welcoming you aboard Lanka Wings.</p>
    <div class="confirmation-reference"><span>Booking reference</span><strong><%=h(b.getPnr())%></strong></div>
    <p class="muted">Your e-ticket is available on the Tickets page whenever you need it.</p>
    <div class="confirmation-actions"><a class="btn btn-orange" href="<%=cp%>/tickets">Go to Tickets <span aria-hidden="true">→</span></a><a class="btn btn-outline" href="<%=cp%>/bookings">My bookings</a></div>
    <div class="confirmation-flight" aria-hidden="true">✈</div>
  </section>
</main><%@ include file="/WEB-INF/jspf/footer.jspf" %></body></html>

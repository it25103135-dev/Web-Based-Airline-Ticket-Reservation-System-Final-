<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.*,com.lankawings.model.Feedback,com.lankawings.dao.FeedbackDAO" %>
<!doctype html>
<html lang="en">
<head><title>Feedback | Lanka Wings</title><%@ include file="/WEB-INF/jspf/head.jspf" %></head>
<body class="app-page">
<%@ include file="/WEB-INF/jspf/header.jspf" %>
<main class="container page-pad">
  <% boolean admin = "ADMIN".equals(role); FeedbackDAO.Stats st = (FeedbackDAO.Stats) request.getAttribute("stats"); %>
  <div class="page-title"><span class="eyebrow navy">PASSENGER VOICE</span><h1><%= admin ? "Passenger care" : "Feedback &amp; reviews" %></h1><p><%= admin ? "Thoughtful replies. Better journeys. Manage every passenger conversation here." : "Tell us about your journey with Lanka Wings." %></p></div>
  <% if (err != null) { %><div class="alert error"><%=h(err)%></div><% } %>

  <% if (st != null && st.total > 0) { %>
  <section class="panel reveal"><div class="rating-summary">
    <div class="rating-big"><strong><%=String.format("%.1f", st.average)%></strong><span class="stars"><% for (int i = 1; i <= 5; i++) { %><%= i <= Math.round(st.average) ? "★" : "☆" %><% } %></span><small><%=st.total%> review<%= st.total == 1 ? "" : "s" %></small></div>
    <div><% for (int star = 5; star >= 1; star--) { int pct = st.total == 0 ? 0 : st.perStar[star] * 100 / st.total; %>
      <div class="bar-row"><span><%=star%> ★</span><span class="bar-track"><span class="bar-fill" style="width:<%=pct%>%"></span></span><span><%=st.perStar[star]%></span></div><% } %></div>
  </div></section>
  <% } %>

  <% if (!admin) { %>
  <section class="panel reveal"><h2>Write a review</h2>
    <form method="post" data-validate class="compact-form">
      <%@ include file="/WEB-INF/jspf/csrf.jspf" %><input type="hidden" name="action" value="add">
      <div><b style="font-size:.8rem;color:#526274">Your rating</b>
        <div class="star-input" role="radiogroup" aria-label="Rating">
          <% String rSel = request.getParameter("rating") == null ? "5" : request.getParameter("rating");
             for (int i = 5; i >= 1; i--) { %><input type="radio" id="st<%=i%>" name="rating" value="<%=i%>" <%= String.valueOf(i).equals(rSel) ? "checked" : "" %>><label for="st<%=i%>" title="<%=i%> star<%= i > 1 ? "s" : "" %>">★</label><% } %>
        </div></div>
      <label>Subject<input name="subject" data-rule="text" data-min="3" data-max="120" required maxlength="120" placeholder="What is your feedback about?" value="<%=h(request.getParameter("subject"))%>"></label>
      <label>Message<textarea name="message" data-rule="text" data-min="10" data-max="1000" data-counter required maxlength="1000" rows="4" placeholder="Tell us about your experience (at least 10 characters)..."><%=h(request.getParameter("message"))%></textarea></label>
      <button class="btn btn-orange" type="submit">Submit Feedback</button>
    </form>
  </section>
  <% } %>

  <div class="toolbar"><input type="search" placeholder="Search feedback..." data-filter="#fbList" data-filter-item=".review-card" data-status-select="#fbStatus">
    <select id="fbStatus"><option value="">All statuses</option><option value="OPEN">Open</option><option value="RESPONDED">Responded</option><option value="CLOSED">Closed</option></select></div>
  <section class="review-list" id="fbList">
  <% List<Feedback> list = (List<Feedback>) request.getAttribute("feedbackList");
     if (list != null && !list.isEmpty()) { for (Feedback f : list) { boolean open = "OPEN".equals(f.getStatus()); %>
    <article class="review-card reveal" data-status="<%=h(f.getStatus())%>">
      <div class="review-head"><div><strong><%=h(f.getSubject())%></strong><span class="stars"><% for (int i = 0; i < f.getRating(); i++) { %>★<% } %></span></div><span class="badge status-<%=h(f.getStatus())%>"><%=h(f.getStatus())%></span></div>
      <small>by <%=h(f.getUsername())%> · <%=f.getCreatedAt()%></small>
      <p><%=h(f.getMessage())%></p>
      <% if (f.getAdminResponse() != null && !f.getAdminResponse().isBlank()) { %><div class="admin-reply"><b>Lanka Wings response</b><p><%=h(f.getAdminResponse())%></p></div><% } %>

      <% if (!admin && open) { %>
      <details><summary>Edit my review</summary>
        <form method="post" data-validate>
          <%@ include file="/WEB-INF/jspf/csrf.jspf" %><input type="hidden" name="action" value="update"><input type="hidden" name="id" value="<%=f.getFeedbackId()%>">
          <label>Rating<select name="rating"><% for (int i = 5; i >= 1; i--) { %><option value="<%=i%>" <%= i == f.getRating() ? "selected" : "" %>><%=i%> ★</option><% } %></select></label>
          <label>Subject<input name="subject" data-rule="text" data-min="3" data-max="120" required maxlength="120" value="<%=h(f.getSubject())%>"></label>
          <label>Message<textarea name="message" data-rule="text" data-min="10" data-max="1000" data-counter required maxlength="1000" rows="3"><%=h(f.getMessage())%></textarea></label>
          <button class="btn btn-navy small" type="submit">Save changes</button>
        </form>
      </details>
      <% } %>

      <div class="review-actions">
        <% if (admin) { %>
        <details class="response-composer"><summary><span class="reply-symbol" aria-hidden="true">↗</span><span><%= f.getAdminResponse() == null ? "Write a thoughtful reply" : "Edit your response" %><small>Official Lanka Wings passenger care</small></span><span class="compose-plus" aria-hidden="true">+</span></summary>
          <form method="post" class="response-form" data-validate>
            <%@ include file="/WEB-INF/jspf/csrf.jspf" %><input type="hidden" name="action" value="respond"><input type="hidden" name="id" value="<%=f.getFeedbackId()%>">
            <div class="composer-heading"><b>A personal response goes a long way.</b><p>Thank the passenger, address their experience and share a helpful next step.</p></div><label>Official response<textarea rows="5" name="response" data-rule="text" data-min="5" data-max="1000" data-counter required maxlength="1000" placeholder="Write an official response..."><%=h(f.getAdminResponse())%></textarea></label>
            <div class="composer-footer"><small>Your reply will be visible to the passenger.</small><button class="btn btn-orange" type="submit">Publish response <span aria-hidden="true">→</span></button></div>
          </form>
        </details>
        <% if (!"CLOSED".equals(f.getStatus())) { %><form method="post"><%@ include file="/WEB-INF/jspf/csrf.jspf" %><input type="hidden" name="action" value="close"><input type="hidden" name="id" value="<%=f.getFeedbackId()%>"><button class="link-btn" type="submit">Close</button></form><% } %>
        <% } %>
        <% if (admin || open) { %>
        <form method="post" data-confirm="Delete this feedback permanently?"><%@ include file="/WEB-INF/jspf/csrf.jspf" %><input type="hidden" name="action" value="delete"><input type="hidden" name="id" value="<%=f.getFeedbackId()%>"><button class="link-btn danger" type="submit">Delete</button></form>
        <% } %>
      </div>
    </article>
  <% } } else { %><div class="empty-state"><div>★</div><h3>No feedback yet</h3></div><% } %>
  </section>
  <div class="no-results">No feedback matches your search.</div>
</main>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
</body></html>

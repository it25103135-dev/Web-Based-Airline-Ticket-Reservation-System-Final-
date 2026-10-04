<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.*,java.text.SimpleDateFormat,com.lankawings.model.Notification" %>
<!doctype html>
<html lang="en">
<head><title>Notifications | Lanka Wings</title><%@ include file="/WEB-INF/jspf/head.jspf" %></head>
<body class="app-page">
<%@ include file="/WEB-INF/jspf/header.jspf" %>
<main class="container narrow page-pad">
  <div class="panel-head">
    <div class="page-title"><span class="eyebrow navy">ACCOUNT UPDATES</span><h1>Notifications</h1></div>
    <div class="panel-actions">
      <form method="post"><%@ include file="/WEB-INF/jspf/csrf.jspf" %><input type="hidden" name="action" value="readAll"><button class="btn btn-outline small" type="submit">Mark all read</button></form>
      <form method="post" data-confirm="Delete all read notifications?"><%@ include file="/WEB-INF/jspf/csrf.jspf" %><input type="hidden" name="action" value="deleteRead"><button class="btn btn-outline small" type="submit">Clear read</button></form>
    </div>
  </div>
  <% if (err != null) { %><div class="alert error"><%=h(err)%></div><% } %>

  <% if ("ADMIN".equals(role)) { %>
  <section class="panel"><h2>Send an announcement</h2>
    <form method="post" data-validate class="compact-form">
      <%@ include file="/WEB-INF/jspf/csrf.jspf" %><input type="hidden" name="action" value="send">
      <div class="form-grid">
        <label>Send to<select name="target" id="target"><option value="ALL" <%= "USER".equals(request.getParameter("target")) ? "" : "selected" %>>Everyone</option><option value="USER" <%= "USER".equals(request.getParameter("target")) ? "selected" : "" %>>One user</option></select></label>
        <label>Username (only for "One user")<input name="username" data-rule="username" maxlength="30" value="<%=h(request.getParameter("username"))%>"></label>
      </div>
      <label>Title<input name="title" data-rule="text" data-min="3" data-max="150" required maxlength="150" value="<%=h(request.getParameter("title"))%>"></label>
      <label>Message<textarea name="message" data-rule="text" data-min="5" data-max="800" data-counter required maxlength="800" rows="3"><%=h(request.getParameter("message"))%></textarea></label>
      <button class="btn btn-navy" type="submit">Send</button>
    </form>
  </section>
  <% } %>

  <div class="notification-list">
  <% List<Notification> ns = (List<Notification>) request.getAttribute("notifications"); SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy, HH:mm");
     if (ns != null && !ns.isEmpty()) { for (Notification n : ns) { %>
    <div class="notification type-<%=h(n.getType())%> <%= n.isRead() ? "read" : "" %>">
      <div class="note-icon">✈</div>
      <div>
        <div class="notification-title"><strong><% if (!n.isRead()) { %><span class="unread-dot"></span><% } %><%=h(n.getTitle())%></strong><span><%=df.format(n.getCreatedAt())%></span></div>
        <p><%=h(n.getMessage())%></p>
        <div class="note-actions">
          <% if (!n.isRead()) { %><form method="post"><%@ include file="/WEB-INF/jspf/csrf.jspf" %><input type="hidden" name="action" value="read"><input type="hidden" name="id" value="<%=n.getNotificationId()%>"><button type="submit">Mark read</button></form><% } %>
          <form method="post"><%@ include file="/WEB-INF/jspf/csrf.jspf" %><input type="hidden" name="action" value="delete"><input type="hidden" name="id" value="<%=n.getNotificationId()%>"><button class="danger" type="submit">Delete</button></form>
        </div>
      </div>
    </div>
  <% } } else { %><div class="empty-state">🔔<h3>No notifications yet</h3></div><% } %>
  </div>
</main>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
</body></html>

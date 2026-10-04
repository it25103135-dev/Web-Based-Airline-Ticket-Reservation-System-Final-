<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.lankawings.model.User" %>
<!doctype html>
<html lang="en">
<head>
  <title>User & Notification Dashboard | Lanka Wings</title>
  <%@ include file="/WEB-INF/jspf/head.jspf" %>
</head>
<body class="app-page">
<%@ include file="/WEB-INF/jspf/header.jspf" %>
<main class="container page-pad">
  <div class="page-title">
    <span class="eyebrow navy">USER + NOTIFICATION MANAGEMENT</span>
    <h1>Account dashboard</h1>
  </div>

  <% if (err != null) { %><div class="alert error"><%=h(err)%></div><% } %>

  <% User u=(User)request.getAttribute("profile"); if(u!=null){ %>
  <div class="profile-grid">
    <section class="panel">
      <h2>Your account</h2>
      <p><b><%=h(u.getFullName())%></b></p>
      <p class="muted"><%=h(u.getUsername())%> · <%=h(u.getEmail())%></p>
      <div class="role-chip">Role: <b><%=h(u.getRole())%></b> · Status: <b><%=h(u.getStatus())%></b></div>
      <p style="margin-top:18px">
        <a class="btn btn-orange" href="<%=cp%>/profile">Manage Profile</a>
      </p>
    </section>

    <section class="panel">
      <h2>Notifications</h2>
      <div class="stat-grid">
        <div class="stat">
          <b><%=request.getAttribute("unreadCount") == null ? 0 : request.getAttribute("unreadCount")%></b>
          <span>Unread</span>
        </div>
      </div>
      <p class="muted">View account alerts, security updates and administrator announcements.</p>
      <p style="margin-top:18px">
        <a class="btn btn-navy" href="<%=cp%>/notifications">Open Notifications</a>
      </p>
    </section>

    <% if("ADMIN".equals(u.getRole())){ %>
    <section class="panel">
      <h2>Administration overview</h2>
      <div class="stat-grid">
        <div class="stat"><b><%=request.getAttribute("userCount")%></b><span>Total accounts</span></div>
        <div class="stat"><b><%=request.getAttribute("activeCount")%></b><span>Active accounts</span></div>
        <div class="stat"><b><%=request.getAttribute("adminCount")%></b><span>Active admins</span></div>
      </div>
      <p style="margin-top:18px">
        <a class="btn btn-navy" href="<%=cp%>/admin/users">Open User Management</a>
        <a class="btn btn-outline" href="<%=cp%>/notifications">Send Announcement</a>
      </p>
    </section>
    <% } else { %>
    <section class="panel">
      <h2>Account security</h2>
      <p class="muted">Passwords are stored as salted PBKDF2 hashes. Protected changes require login, CSRF validation and server-side authorization.</p>
    </section>
    <% } %>
  </div>
  <% } %>
</main>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
</body>
</html>

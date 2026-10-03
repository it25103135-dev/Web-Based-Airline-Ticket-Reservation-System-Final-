<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.*,java.text.SimpleDateFormat,com.lankawings.model.User" %>
<!doctype html>
<html lang="en">
<head><title>Users | Lanka Wings</title><%@ include file="/WEB-INF/jspf/head.jspf" %></head>
<body class="app-page">
<%@ include file="/WEB-INF/jspf/header.jspf" %>
<main class="container page-pad">
  <div class="page-title"><span class="eyebrow navy">ADMINISTRATION</span><h1>User management</h1><p>Activate or deactivate accounts and change roles. You cannot change your own account.</p></div>
  <% if (err != null) { %><div class="alert error" role="alert"><%=h(err)%></div><% } %>
  <div class="toolbar"><input type="search" placeholder="Search by name, username, email..." data-filter="#usBody" data-filter-item="tr" data-status-select="#usStatus">
    <select id="usStatus"><option value="">All</option><option value="ACTIVE">Active</option><option value="INACTIVE">Inactive</option></select></div>
  <div class="table-wrap panel"><table>
    <thead><tr><th>Name</th><th>Username</th><th>Email / Mobile</th><th>Role</th><th>Status</th><th>Joined</th><th>Actions</th></tr></thead>
    <tbody id="usBody">
    <% List<User> us = (List<User>) request.getAttribute("users"); int me = (Integer) session.getAttribute("userId"); SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy");
       if (us != null) { for (User u : us) { boolean self = u.getUserId() == me; %>
      <tr data-status="<%=h(u.getStatus())%>"><td><b><%=h(u.getFullName())%></b><% if (self) { %><span class="self-tag">YOU</span><% } %></td><td><%=h(u.getUsername())%></td>
        <td><%=h(u.getEmail())%><br><small class="muted"><%=h(u.getPhone())%></small></td>
        <td><span class="badge"><%=h(u.getRole())%></span></td><td><span class="badge status-<%=h(u.getStatus())%>"><%=h(u.getStatus())%></span></td><td><%=df.format(u.getCreatedAt())%></td>
        <td><% if (!self) { %><div class="row-actions">
          <form method="post" class="inline-actions" data-confirm="Change the role of <%=h(u.getUsername())%>?"><%@ include file="/WEB-INF/jspf/csrf.jspf" %><input type="hidden" name="id" value="<%=u.getUserId()%>"><input type="hidden" name="action" value="role">
            <select name="value" class="inline-select"><% for (String r : new String[]{"PASSENGER", "FINANCE", "ADMIN"}) { %><option <%= r.equals(u.getRole()) ? "selected" : "" %>><%=r%></option><% } %></select>
            <button class="link-btn" type="submit">Set role</button></form>
          <form method="post" data-confirm="<%= "ACTIVE".equals(u.getStatus()) ? "Deactivate" : "Activate" %> <%=h(u.getUsername())%>?"><%@ include file="/WEB-INF/jspf/csrf.jspf" %><input type="hidden" name="id" value="<%=u.getUserId()%>"><input type="hidden" name="action" value="status"><input type="hidden" name="value" value="<%= "ACTIVE".equals(u.getStatus()) ? "INACTIVE" : "ACTIVE" %>">
            <button class="link-btn <%= "ACTIVE".equals(u.getStatus()) ? "danger" : "" %>" type="submit"><%= "ACTIVE".equals(u.getStatus()) ? "Deactivate" : "Activate" %></button></form>
        </div><% } else { %><small class="muted">-</small><% } %></td></tr>
    <% } } %>
    </tbody></table></div>
</main>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
</body></html>

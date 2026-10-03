<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.lankawings.model.User,com.lankawings.util.Validator" %>
<!doctype html>
<html lang="en">
<head><title>Profile | Lanka Wings</title><%@ include file="/WEB-INF/jspf/head.jspf" %></head>
<body class="app-page">
<%@ include file="/WEB-INF/jspf/header.jspf" %>
<main class="container narrow page-pad">
  <div class="page-title"><span class="eyebrow navy">ACCOUNT SETTINGS</span><h1>Your profile</h1></div>
  <% if (err != null) { %><div class="alert error" role="alert"><%=h(err)%></div><% } %>
  <% User u = (User) request.getAttribute("profile"); String posted = request.getParameter("action");
     if (u != null) { %>
  <div class="profile-grid">
    <section class="panel"><h2>Personal information</h2>
      <form method="post" data-validate>
        <%@ include file="/WEB-INF/jspf/csrf.jspf" %><input type="hidden" name="action" value="profile">
        <label>Full name<input name="fullName" data-rule="name" required maxlength="80" value="<%= "profile".equals(posted) ? h(request.getParameter("fullName")) : h(u.getFullName()) %>"></label>
        <label>Username<input disabled value="<%=h(u.getUsername())%>"></label>
        <label>Email<input type="email" name="email" data-rule="email" required maxlength="120" value="<%= "profile".equals(posted) ? h(request.getParameter("email")) : h(u.getEmail()) %>"></label>
        <label>Mobile number<input type="tel" name="phone" data-rule="phone" required inputmode="numeric" maxlength="13" data-hint="Exactly 10 digits" value="<%= "profile".equals(posted) ? h(request.getParameter("phone")) : h(Validator.phoneForDisplay(u.getPhone())) %>"></label>
        <button class="btn btn-orange" type="submit">Save Profile</button>
      </form>
    </section>
    <section class="panel"><h2>Change password</h2>
      <form method="post" data-validate>
        <%@ include file="/WEB-INF/jspf/csrf.jspf" %><input type="hidden" name="action" value="password">
        <label>Current password<input type="password" name="currentPassword" required maxlength="128" autocomplete="current-password" data-toggle></label>
        <label>New password<input type="password" id="newPw" name="newPassword" data-rule="newpassword" data-toggle required maxlength="64" autocomplete="new-password"></label>
        <label>Confirm new password<input type="password" name="confirmPassword" data-match="#newPw" data-toggle required maxlength="64" autocomplete="new-password"></label>
        <button class="btn btn-navy" type="submit">Update Password</button>
      </form>
      <div class="role-chip">Role: <b><%=h(u.getRole())%></b> · Member since <%=h(u.getCreatedAt() == null ? "" : u.getCreatedAt().toString().substring(0, 10))%></div>
    </section>
  </div>
  <% if (!"ADMIN".equals(u.getRole())) { %>
  <section class="panel danger-zone"><h2>Close my account</h2>
    <p class="muted">Your account will be deactivated and you will be signed out. Booking history is kept. You must cancel upcoming bookings first.</p>
    <form method="post" data-validate data-confirm="Close your Lanka Wings account? You will be signed out immediately.">
      <%@ include file="/WEB-INF/jspf/csrf.jspf" %><input type="hidden" name="action" value="deactivate">
      <label>Confirm with your password<input type="password" name="currentPassword" required maxlength="128" autocomplete="current-password" data-toggle></label>
      <label class="check-row"><input type="checkbox" name="confirmClose" required> <span>Yes, I want to close my account.</span></label>
      <button class="btn btn-danger" type="submit">Close account</button>
    </form>
  </section>
  <% } %>
  <% } %>
</main>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
</body></html>

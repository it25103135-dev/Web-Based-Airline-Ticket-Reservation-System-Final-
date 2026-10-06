<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html lang="en">
<head>
  <title>Create account | Lanka Wings</title>
  <%@ include file="/WEB-INF/jspf/head.jspf" %>
  <link rel="stylesheet" href="<%=cp%>/assets/css/background-video.css">
  <script src="<%=cp%>/assets/js/background-video.js" defer></script>
</head>
<body class="auth-page login-bg">
  <%@ include file="/WEB-INF/jspf/background-video.jspf" %>
  <% request.setAttribute("skyDark", true); %>
  <%@ include file="/WEB-INF/jspf/flash.jspf" %>
  <%@ include file="/WEB-INF/jspf/sky.jspf" %>
  <div class="auth-shade"></div>
  <a class="brand light auth-brand" href="<%=cp%>/"><span class="brand-mark">LW</span><span>Lanka <b>Wings</b></span></a>
  <main class="auth-wrap">
    <section class="auth-message">
      <span class="eyebrow">NEW JOURNEYS START HERE</span>
      <h1>Create your<br><em>travel account.</em></h1>
      <p>Save your bookings, receive notifications and manage every trip with ease.</p>
    </section>
    <section class="auth-card register-card">
      <span class="eyebrow navy">PASSENGER REGISTRATION</span>
      <h2>Join Lanka Wings</h2>
      <% if (err != null) { %><div class="alert error" role="alert"><%=h(err)%></div><% } %>
      <form method="post" action="<%=cp%>/register" data-validate>
        <%@ include file="/WEB-INF/jspf/csrf.jspf" %>
        <div class="hp" aria-hidden="true"><label>Website<input name="website" tabindex="-1" autocomplete="off"></label></div>
        <div class="form-grid">
          <label>Full name<input name="fullName" data-rule="name" required maxlength="80" autocomplete="name" value="<%=h(request.getParameter("fullName"))%>"></label>
          <label>Username<input name="username" data-rule="username" required maxlength="30" autocomplete="username" data-hint="Letters, numbers . _ -  (4-30)" value="<%=h(request.getParameter("username"))%>"></label>
          <label>Email<input type="email" name="email" data-rule="email" required maxlength="120" autocomplete="email" value="<%=h(request.getParameter("email"))%>"></label>
          <label>Mobile number<input type="tel" name="phone" data-rule="phone" required inputmode="numeric" maxlength="13" placeholder="0771234567" autocomplete="tel" data-hint="Exactly 10 digits" value="<%=h(request.getParameter("phone"))%>"></label>
        </div>
        <label>Password<input type="password" id="password" name="password" data-rule="newpassword" data-toggle required maxlength="64" autocomplete="new-password"></label>
        <label>Confirm password<input type="password" name="confirmPassword" data-match="#password" data-toggle required maxlength="64" autocomplete="new-password"></label>
        <label class="check-row"><input type="checkbox" name="terms" required> <span>I agree to the terms of service and privacy notice.</span></label>
        <button class="btn btn-orange full" type="submit">Create Account</button>
      </form>
      <p class="center muted">Already registered? <a href="<%=cp%>/login">Sign in</a></p>
    </section>
  </main>
</body>
</html>

<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html lang="en">
<head>
  <title>Sign in | Lanka Wings</title>
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
      <span class="eyebrow">FLY WITH CONFIDENCE</span>
      <h1>Welcome<br><em>back aboard.</em></h1>
      <p>Your bookings, flights and travel updates are waiting.</p>
      <ul class="auth-points"><li>Encrypted password storage</li><li>Automatic lock after repeated wrong passwords</li><li>Instant e-tickets and flight alerts</li></ul>
    </section>
    <section class="auth-card">
      <span class="eyebrow navy">PASSENGER PORTAL</span>
      <h2>Sign in</h2>
      <p class="muted">Use your username or email.</p>
      <% if (err != null) { %><div class="alert error" role="alert"><%=h(err)%></div><% } %>
      <form method="post" action="<%=cp%>/login" data-validate autocomplete="on">
        <%@ include file="/WEB-INF/jspf/csrf.jspf" %>
        <label>Username or email
          <input name="username" required maxlength="120" autocomplete="username" autofocus placeholder="Enter username or email" value="<%=h(request.getParameter("username"))%>">
        </label>
        <label>Password
          <input type="password" name="password" required maxlength="128" autocomplete="current-password" data-toggle placeholder="Enter password">
        </label>
        <button class="btn btn-orange full" type="submit">Sign In <span>→</span></button>
      </form>
      <p class="center muted">New to Lanka Wings? <a href="<%=cp%>/register">Create an account</a></p>
    </section>
  </main>
</body>
</html>

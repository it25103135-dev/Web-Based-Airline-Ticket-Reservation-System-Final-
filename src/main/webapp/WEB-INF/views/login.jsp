<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html lang="en">
<head><title>Feedback sign in | Lanka Wings</title><%@ include file="/WEB-INF/jspf/head.jspf" %></head>
<body class="auth-page login-bg">
  <%@ include file="/WEB-INF/jspf/flash.jspf" %>
  <div class="auth-shade"></div>
  <a class="brand light auth-brand" href="<%=cp%>/"><span class="brand-mark">LW</span><span>Lanka <b>Wings</b></span></a>
  <main class="auth-wrap">
    <section class="auth-message">
      <span class="eyebrow">PASSENGER VOICE</span>
      <h1>Share your<br><em>experience.</em></h1>
    </section>
    <section class="auth-card">
      <span class="eyebrow navy">FEEDBACK MANAGEMENT</span>
      <h2>Sign in</h2>
      <p class="muted">Use the supplied passenger or administrator account.</p>
      <% if (err != null) { %><div class="alert error" role="alert"><%=h(err)%></div><% } %>
      <form method="post" action="<%=cp%>/login" data-validate autocomplete="on">
        <%@ include file="/WEB-INF/jspf/csrf.jspf" %>
        <label>Username or email<input name="username" required maxlength="120" autocomplete="username" autofocus placeholder="Enter username or email" value="<%=h(request.getParameter("username"))%>"></label>
        <label>Password<input type="password" name="password" required maxlength="128" autocomplete="current-password" data-toggle placeholder="Enter password"></label>
        <button class="btn btn-orange full" type="submit">Sign In <span>→</span></button>
      </form>
      <div class="demo-box"><span>Passenger: <strong>passenger</strong></span><span>Admin: <strong>admin</strong></span></div>
    </section>
  </main>
</body></html>

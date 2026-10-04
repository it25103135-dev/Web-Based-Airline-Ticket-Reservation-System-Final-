<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html><html lang="en"><head><title>Flight Management Sign in | Lanka Wings</title><%@ include file="/WEB-INF/jspf/head.jspf" %></head>
<body class="auth-page login-bg">
<%@ include file="/WEB-INF/jspf/flash.jspf" %>
<div class="auth-shade"></div>
<a class="brand light auth-brand" href="<%=cp%>/flights"><span class="brand-mark">LW</span><span>Lanka <b>Wings</b></span></a>
<main class="auth-wrap">
  <section class="auth-message"><span class="eyebrow">FLIGHT OPERATIONS</span><h1>Flight<br><em>Management.</em></h1>
    <p>Create, validate, update and publish Lanka Wings flight schedules.</p>
  </section>
  <section class="auth-card"><span class="eyebrow navy">RESERVATION MANAGER</span><h2>Sign in</h2>
    <% if (err != null) { %><div class="alert error" role="alert"><%=h(err)%></div><% } %>
    <form method="post" action="<%=cp%>/login" data-validate autocomplete="on"><%@ include file="/WEB-INF/jspf/csrf.jspf" %>
      <label>Username or email<input name="username" required maxlength="120" autocomplete="username" autofocus placeholder="Enter username or email" value="<%=h(request.getParameter("username"))%>"></label>
      <label>Password<input type="password" name="password" required maxlength="128" autocomplete="current-password" placeholder="Enter password"></label>
      <button class="btn btn-orange full" type="submit">Sign In <span>→</span></button>
    </form>
    <p class="center muted"><a href="<%=cp%>/flights">View published flight schedule</a></p>
  </section>
</main></body></html>

<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html><html lang="en"><head><title>Sign in | Lanka Wings</title><%@ include file="/WEB-INF/jspf/head.jspf" %></head>
<body class="auth-page login-bg"><div class="auth-shade"></div><a class="brand light auth-brand" href="<%=cp%>/"><span class="brand-mark">LW</span><span>Lanka <b>Wings</b></span></a>
<main class="auth-wrap"><section class="auth-message"><span class="eyebrow">SECURE ACCOUNT ACCESS</span><h1>User<br><em>Management.</em></h1></section>
<section class="auth-card"><span class="eyebrow navy">ACCOUNT PORTAL</span><h2>Sign in</h2><p class="muted">Use your username or email.</p><% if (err != null) { %><div class="alert error" role="alert"><%=h(err)%></div><% } %>
<form method="post" action="<%=cp%>/login" data-validate autocomplete="on"><%@ include file="/WEB-INF/jspf/csrf.jspf" %><label>Username or email<input name="username" required maxlength="120" autocomplete="username" autofocus value="<%=h(request.getParameter("username"))%>"></label><label>Password<input type="password" name="password" required maxlength="128" autocomplete="current-password" data-toggle></label><button class="btn btn-orange full" type="submit">Sign In →</button></form>
<p class="center muted">Need a passenger account? <a href="<%=cp%>/register">Create an account</a></p></section></main></body></html>

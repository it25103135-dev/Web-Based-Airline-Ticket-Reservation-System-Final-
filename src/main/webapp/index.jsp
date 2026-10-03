<%@ page contentType="text/html;charset=UTF-8" %>
<%
  if (session.getAttribute("userId") != null) response.sendRedirect(request.getContextPath() + "/feedback");
  else response.sendRedirect(request.getContextPath() + "/login");
%>

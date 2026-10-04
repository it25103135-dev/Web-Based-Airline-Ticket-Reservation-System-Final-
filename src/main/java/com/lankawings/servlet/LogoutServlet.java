package com.lankawings.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {
    @Override protected void doPost(HttpServletRequest r, HttpServletResponse s) throws IOException {
        HttpSession x = r.getSession(false); if (x != null) x.invalidate();
        r.getSession(true).setAttribute("flash", "You have been signed out safely.");
        s.sendRedirect(r.getContextPath() + "/login");
    }
}

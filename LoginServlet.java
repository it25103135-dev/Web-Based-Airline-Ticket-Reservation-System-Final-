package com.lankawings.servlet;

import com.lankawings.dao.AccountDAO;
import com.lankawings.model.User;
import com.lankawings.util.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private static final long MIN=60_000L;
    private static final RateLimiter PER_ACCOUNT=new RateLimiter(5,15*MIN,15*MIN);
    private static final RateLimiter PER_IP=new RateLimiter(20,15*MIN,15*MIN);
    private final AccountDAO dao=new AccountDAO();
    @Override protected void doGet(HttpServletRequest r,HttpServletResponse s)throws ServletException,IOException{
        HttpSession session=r.getSession(false);if(session!=null&&session.getAttribute("userId")!=null){s.sendRedirect(r.getContextPath()+"/dashboard");return;}
        if(r.getParameter("expired")!=null&&r.getAttribute("error")==null)r.setAttribute("error","Your page expired for security reasons. Please sign in again.");
        r.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(r,s);
    }
    @Override protected void doPost(HttpServletRequest r,HttpServletResponse s)throws ServletException,IOException{
        String login=Req.param(r,"username").strip();String password=Req.param(r,"password");String ip=Req.ip(r);String key=ip+"|"+login.toLowerCase();
        try{
            if(login.isEmpty()||password.isEmpty())throw new ValidationException("Enter your username/email and password.");
            if(PER_ACCOUNT.isBlocked(key))throw new ValidationException(PER_ACCOUNT.waitMessage(key));if(PER_IP.isBlocked(ip))throw new ValidationException(PER_IP.waitMessage(ip));
            User u=dao.authenticate(login,password);if(u==null){PER_ACCOUNT.record(key);PER_IP.record(ip);throw new ValidationException("Invalid username or password.");}PER_ACCOUNT.reset(key);
            HttpSession old=r.getSession(false);String returnTo=old==null?null:(String)old.getAttribute("returnTo");if(old!=null)old.invalidate();
            HttpSession session=r.getSession(true);session.setMaxInactiveInterval(30*60);session.setAttribute("userId",u.getUserId());session.setAttribute("username",u.getUsername());session.setAttribute("fullName",u.getFullName());session.setAttribute("role",u.getRole());
            boolean safe=returnTo!=null&&returnTo.startsWith("/")&&!returnTo.startsWith("//")&&!returnTo.contains("\\")&&!returnTo.contains("\r")&&!returnTo.contains("\n");
            s.sendRedirect(r.getContextPath()+(safe?returnTo:"/dashboard"));
        }catch(Exception e){Req.error(r,e);r.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(r,s);}
    }
}

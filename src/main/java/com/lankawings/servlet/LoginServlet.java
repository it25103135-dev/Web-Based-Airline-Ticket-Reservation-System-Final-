package com.lankawings.servlet;

import com.lankawings.dao.UserDAO;
import com.lankawings.model.User;
import com.lankawings.util.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private static final long MIN=60_000L;
    private static final RateLimiter PER_ACCOUNT=new RateLimiter(5,15*MIN,15*MIN),PER_IP=new RateLimiter(20,15*MIN,15*MIN);
    private final UserDAO dao=new UserDAO();

    @Override protected void doGet(HttpServletRequest r,HttpServletResponse s)throws ServletException,IOException{
        HttpSession x=r.getSession(false);
        if(x!=null&&x.getAttribute("userId")!=null){redirectByRole(r,s,(String)x.getAttribute("role"));return;}
        if(r.getParameter("expired")!=null)r.setAttribute("error","Your page expired for security reasons. Please sign in again.");
        r.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(r,s);
    }

    @Override protected void doPost(HttpServletRequest r,HttpServletResponse s)throws ServletException,IOException{
        String login=Req.param(r,"username").strip(),password=Req.param(r,"password"),ip=Req.ip(r),key=ip+"|"+login.toLowerCase();
        try{
            if(login.isEmpty()||password.isEmpty())throw new ValidationException("Enter your username and password.");
            if(PER_ACCOUNT.isBlocked(key))throw new ValidationException(PER_ACCOUNT.waitMessage(key));
            if(PER_IP.isBlocked(ip))throw new ValidationException(PER_IP.waitMessage(ip));
            User u=dao.authenticate(login,password);
            if(u==null){PER_ACCOUNT.record(key);PER_IP.record(ip);throw new ValidationException("Invalid username or password.");}
            if(!"PASSENGER".equals(u.getRole())&&!"ADMIN".equals(u.getRole()))throw new ValidationException("This account does not have Ticket Reservation access.");
            PER_ACCOUNT.reset(key);
            HttpSession old=r.getSession(false);if(old!=null)old.invalidate();
            HttpSession session=r.getSession(true);session.setMaxInactiveInterval(30*60);
            session.setAttribute("userId",u.getUserId());session.setAttribute("username",u.getUsername());session.setAttribute("fullName",u.getFullName());session.setAttribute("role",u.getRole());
            redirectByRole(r,s,u.getRole());
        }catch(Exception e){Req.error(r,e);r.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(r,s);}
    }

    private void redirectByRole(HttpServletRequest r,HttpServletResponse s,String role)throws IOException{
        s.sendRedirect(r.getContextPath()+("ADMIN".equals(role)?"/admin/tickets":"/dashboard"));
    }
}

package com.lankawings.filter;

import com.lankawings.dao.AccountDAO;
import com.lankawings.util.Csrf;
import com.lankawings.util.Req;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Set;
import java.util.logging.Level;

/** Authentication, role checks and CSRF protection for Booking Management. */
@WebFilter(urlPatterns="/*")
public class SecurityFilter implements Filter {
    private static final Set<String> PUBLIC=Set.of("/","/index.jsp","/login","/logout");
    private final AccountDAO accounts=new AccountDAO();

    @Override public void doFilter(ServletRequest req,ServletResponse res,FilterChain chain) throws IOException,ServletException{
        HttpServletRequest r=(HttpServletRequest)req; HttpServletResponse s=(HttpServletResponse)res;
        r.setCharacterEncoding("UTF-8"); String path=r.getRequestURI().substring(r.getContextPath().length());
        s.setHeader("X-Content-Type-Options","nosniff");s.setHeader("X-Frame-Options","DENY");s.setHeader("Referrer-Policy","strict-origin-when-cross-origin");
        s.setHeader("Permissions-Policy","camera=(), microphone=(), geolocation=(), payment=()");
        s.setHeader("Content-Security-Policy","default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; font-src https://fonts.gstatic.com; img-src 'self' data:; object-src 'none'; base-uri 'self'; form-action 'self'; frame-ancestors 'none'");
        if(path.startsWith("/assets/")){chain.doFilter(req,res);return;}
        s.setHeader("Cache-Control","no-store, no-cache, must-revalidate, max-age=0");
        String method=r.getMethod(); boolean readOnly="GET".equals(method)||"HEAD".equals(method)||"OPTIONS".equals(method);
        if(!readOnly&&!Csrf.valid(r)){csrfFailed(r,s,path);return;}
        if(!PUBLIC.contains(path)){
            HttpSession session=r.getSession(false);Integer uid=session==null?null:(Integer)session.getAttribute("userId");
            if(uid==null){if("GET".equals(method)){String q=r.getQueryString();r.getSession(true).setAttribute("returnTo",path+(q==null?"":"?"+q));}s.sendRedirect(r.getContextPath()+"/login");return;}
            try{
                AccountDAO.SessionInfo info=accounts.sessionInfo(uid);
                if(info==null||!"ACTIVE".equals(info.status)){session.invalidate();Req.flashError(r,"Your account is no longer active.");s.sendRedirect(r.getContextPath()+"/login");return;}
                session.setAttribute("username",info.username);session.setAttribute("fullName",info.fullName);session.setAttribute("role",info.role);
            }catch(SQLException e){Req.log().log(Level.WARNING,"Session account check failed",e);}
            String role=String.valueOf(session.getAttribute("role"));
            if(path.startsWith("/agent/") && !("TRAVEL_AGENT".equals(role)||"ADMIN".equals(role))){s.sendError(403);return;}
        }
        chain.doFilter(req,res);
    }

    private void csrfFailed(HttpServletRequest r,HttpServletResponse s,String path) throws IOException{
        HttpSession session=r.getSession(false);boolean logged=session!=null&&session.getAttribute("userId")!=null;
        if("/login".equals(path)){s.sendRedirect(r.getContextPath()+"/login?expired=1");return;}
        if(!logged){s.sendRedirect(r.getContextPath()+"/login?expired=1");return;}
        s.sendError(403,"Security token missing or expired. Reload the page and try again.");
    }
}

package com.lankawings.filter;

import com.lankawings.dao.UserDAO;
import com.lankawings.util.Csrf;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.Set;

/** Security gate for the isolated Ticket Reservation function and its ticket-admin panel. */
@WebFilter(urlPatterns="/*")
public class SecurityFilter implements Filter {
    private static final Set<String> PUBLIC=Set.of("/","/index.jsp","/login","/flights");
    private final UserDAO users=new UserDAO();

    @Override public void doFilter(ServletRequest req,ServletResponse res,FilterChain chain)throws IOException,ServletException{
        HttpServletRequest r=(HttpServletRequest)req;HttpServletResponse s=(HttpServletResponse)res;r.setCharacterEncoding("UTF-8");
        String path=r.getRequestURI().substring(r.getContextPath().length());
        s.setHeader("X-Content-Type-Options","nosniff");s.setHeader("X-Frame-Options","DENY");s.setHeader("Referrer-Policy","strict-origin-when-cross-origin");
        s.setHeader("Permissions-Policy","camera=(), microphone=(), geolocation=(), payment=()");
        s.setHeader("Content-Security-Policy","default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; object-src 'none'; base-uri 'self'; form-action 'self'; frame-ancestors 'none'");
        if(path.startsWith("/assets/")){chain.doFilter(req,res);return;}
        s.setHeader("Cache-Control","no-store, no-cache, must-revalidate, max-age=0");
        boolean readOnly="GET".equals(r.getMethod())||"HEAD".equals(r.getMethod())||"OPTIONS".equals(r.getMethod());
        if(!readOnly&&!Csrf.valid(r)){if("/login".equals(path))s.sendRedirect(r.getContextPath()+"/login?expired=1");else s.sendError(403,"Security token missing or expired.");return;}

        if(!PUBLIC.contains(path)){
            HttpSession session=r.getSession(false);Integer uid=session==null?null:(Integer)session.getAttribute("userId");
            if(uid==null){s.sendRedirect(r.getContextPath()+"/login");return;}
            try{
                UserDAO.SessionInfo info=users.sessionInfo(uid);
                if(info==null||!"ACTIVE".equals(info.status)){session.invalidate();s.sendRedirect(r.getContextPath()+"/login");return;}
                session.setAttribute("role",info.role);
                if(!"PASSENGER".equals(info.role)&&!"ADMIN".equals(info.role)){s.sendError(403);return;}
                if(path.startsWith("/admin/")&&!"ADMIN".equals(info.role)){s.sendError(403);return;}
                if(!path.startsWith("/admin/")&&"ADMIN".equals(info.role)&&!"/logout".equals(path)){s.sendRedirect(r.getContextPath()+"/admin/tickets");return;}
            }catch(Exception e){throw new ServletException("Unable to verify current account.",e);}
        }
        chain.doFilter(req,res);
    }
}

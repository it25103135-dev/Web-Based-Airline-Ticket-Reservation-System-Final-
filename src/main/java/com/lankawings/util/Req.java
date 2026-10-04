package com.lankawings.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Req {
    private Req() {}
    private static final Logger LOG = Logger.getLogger("com.lankawings");
    public static int uid(HttpServletRequest r){ return (Integer) r.getSession().getAttribute("userId"); }
    public static String role(HttpServletRequest r){ Object v=r.getSession().getAttribute("role"); return v==null?"":String.valueOf(v); }
    public static boolean isAdmin(HttpServletRequest r){ return "ADMIN".equals(role(r)); }
    public static boolean isAgent(HttpServletRequest r){ return "TRAVEL_AGENT".equals(role(r)); }
    public static boolean isPassenger(HttpServletRequest r){ return "PASSENGER".equals(role(r)); }
    public static String param(HttpServletRequest r,String n){ String v=r.getParameter(n); return v==null?"":v; }
    public static int id(HttpServletRequest r,String n,String label){ return Validator.id(r.getParameter(n),label); }
    public static String ip(HttpServletRequest r){ return r.getRemoteAddr(); }
    public static void flash(HttpServletRequest r,String m){ r.getSession().setAttribute("flash",m); }
    public static void flashError(HttpServletRequest r,String m){ r.getSession().setAttribute("flashError",m); }
    public static void error(HttpServletRequest r,Exception e){
        if(e instanceof ValidationException) r.setAttribute("error",e.getMessage());
        else { LOG.log(Level.SEVERE,"Unexpected error on "+r.getMethod()+" "+r.getRequestURI(),e); r.setAttribute("error","Something went wrong on our side. Please try again."); }
    }
    public static Logger log(){ return LOG; }
}

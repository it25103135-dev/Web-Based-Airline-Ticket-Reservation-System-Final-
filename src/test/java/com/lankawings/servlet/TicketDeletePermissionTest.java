package com.lankawings.servlet;

import com.lankawings.dao.TicketDAO;
import jakarta.servlet.http.*;
import org.junit.Test;
import org.mockito.MockedConstruction;
import static org.mockito.Mockito.*;

public class TicketDeletePermissionTest {
    @Test public void passengerDeleteRequestReturnsForbidden() throws Exception {
        HttpServletRequest r=mock(HttpServletRequest.class);
        HttpServletResponse s=mock(HttpServletResponse.class);
        HttpSession session=mock(HttpSession.class);
        when(r.getSession()).thenReturn(session);when(r.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(7);
        when(session.getAttribute("role")).thenReturn("PASSENGER");
        when(r.getParameter("bookingId")).thenReturn("12");
        when(r.getParameter("action")).thenReturn("delete");
        try(MockedConstruction<TicketDAO> tickets=mockConstruction(TicketDAO.class)) {
            new TicketManagementServlet().doPost(r,s);
            verify(s).sendError(403);
            verifyNoInteractions(tickets.constructed().get(0));
        }
    }
    @Test public void adminDeleteRequestIsAllowed() throws Exception {
        HttpServletRequest r=mock(HttpServletRequest.class);
        HttpServletResponse s=mock(HttpServletResponse.class);
        HttpSession session=mock(HttpSession.class);
        when(r.getSession()).thenReturn(session);when(r.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(1);
        when(session.getAttribute("role")).thenReturn("ADMIN");
        when(r.getParameter("bookingId")).thenReturn("12");when(r.getParameter("action")).thenReturn("delete");
        when(r.getContextPath()).thenReturn("/LankaWings");
        try(MockedConstruction<TicketDAO> tickets=mockConstruction(TicketDAO.class)) {
            new TicketManagementServlet().doPost(r,s);
            verify(tickets.constructed().get(0)).delete(12,1,true);
            verify(s).sendRedirect("/LankaWings/tickets");
        }
    }
}

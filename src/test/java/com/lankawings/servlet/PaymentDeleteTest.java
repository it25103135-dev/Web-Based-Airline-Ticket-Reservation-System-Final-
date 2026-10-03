package com.lankawings.servlet;

import com.lankawings.dao.PaymentDAO;
import com.lankawings.util.ValidationException;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.*;
import org.junit.*;
import org.mockito.MockedConstruction;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class PaymentDeleteTest {
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;

    @Before public void setUp() {
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        when(request.getSession()).thenReturn(session);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(1);
        when(session.getAttribute("role")).thenReturn("ADMIN");
        when(request.getParameter("action")).thenReturn("delete");
        when(request.getParameter("paymentId")).thenReturn("12");
        when(request.getContextPath()).thenReturn("/LankaWings");
        when(request.getRequestDispatcher(anyString())).thenReturn(mock(RequestDispatcher.class));
    }

    @Test public void nonAdminsCannotDelete() throws Exception {
        for (String role : new String[]{"PASSENGER", "FINANCE", null}) {
            when(session.getAttribute("role")).thenReturn(role);
            try (MockedConstruction<PaymentDAO> payments = mockConstruction(PaymentDAO.class)) {
                new PaymentHistoryServlet().doPost(request, response);
                assertTrue(payments.constructed().isEmpty());
            }
        }
        verify(response, times(3)).sendError(403);
    }

    @Test public void adminCanDeleteAndIsRedirected() throws Exception {
        try (MockedConstruction<PaymentDAO> payments = mockConstruction(PaymentDAO.class)) {
            new PaymentHistoryServlet().doPost(request, response);
            verify(payments.constructed().get(0)).delete(12, true);
            verify(response).sendRedirect("/LankaWings/payments");
            verify(session).setAttribute(eq("flash"), contains("Payment record deleted"));
        }
    }

    @Test public void invalidIdDoesNotDelete() throws Exception {
        when(request.getParameter("paymentId")).thenReturn("-1");
        try (MockedConstruction<PaymentDAO> payments = mockConstruction(PaymentDAO.class)) {
            new PaymentHistoryServlet().doPost(request, response);
            for (PaymentDAO dao : payments.constructed()) verify(dao, never()).delete(anyInt(), anyBoolean());
            verify(request).setAttribute(eq("error"), anyString());
        }
    }

    @Test public void missingPaymentShowsError() throws Exception {
        try (MockedConstruction<PaymentDAO> payments = mockConstruction(PaymentDAO.class,
                (dao, context) -> doThrow(new ValidationException("Payment no longer exists. Reload the page.")).when(dao).delete(12, true))) {
            new PaymentHistoryServlet().doPost(request, response);
            verify(request).setAttribute("error", "Payment no longer exists. Reload the page.");
            verify(response, never()).sendRedirect(anyString());
        }
    }
}

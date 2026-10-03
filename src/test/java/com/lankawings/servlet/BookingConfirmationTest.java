package com.lankawings.servlet;

import com.lankawings.dao.BookingDAO;
import com.lankawings.dao.TicketDAO;
import com.lankawings.model.Booking;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.*;
import org.junit.*;
import org.mockito.MockedConstruction;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

public class BookingConfirmationTest {
    private HttpServletRequest request;
    private HttpServletResponse response;
    private Booking booking;
    private RequestDispatcher dispatcher;
    private MockedConstruction<BookingDAO> bookings;
    @Before public void setUp() throws Exception {
        request=mock(HttpServletRequest.class);response=mock(HttpServletResponse.class);
        HttpSession session=mock(HttpSession.class);dispatcher=mock(RequestDispatcher.class);
        when(request.getSession()).thenReturn(session);when(session.getAttribute("userId")).thenReturn(7);
        when(request.getParameter("bookingId")).thenReturn("12");when(request.getContextPath()).thenReturn("/LankaWings");
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
        booking=new Booking();booking.setBookingId(12);booking.setUserId(7);booking.setPaymentStatus("PAID");booking.setBookingStatus("CONFIRMED");
        bookings=mockConstruction(BookingDAO.class,(mock,context)->when(mock.find(12)).thenReturn(booking));
    }
    @After public void tearDown(){bookings.close();}
    @Test public void confirmedBookingShowsConfirmationPage() throws Exception {
        new BookingConfirmationServlet().doGet(request,response);
        verify(request).getRequestDispatcher("/WEB-INF/views/booking-confirmed.jsp");verify(dispatcher).forward(request,response);
    }
    @Test public void anotherPassengerCannotOpenConfirmation() throws Exception {
        booking.setUserId(99);new BookingConfirmationServlet().doGet(request,response);
        verify(response).sendError(404);verifyNoInteractions(dispatcher);
    }
    @Test public void unpaidBookingCannotShowSuccess() throws Exception {
        booking.setPaymentStatus("PENDING");new BookingConfirmationServlet().doGet(request,response);
        verify(response).sendRedirect("/LankaWings/bookings");verifyNoInteractions(dispatcher);
    }
    @Test public void paidCheckoutRedirectsToConfirmationNotTicket() throws Exception {
        new PaymentServlet().doGet(request,response);
        verify(response).sendRedirect("/LankaWings/booking-confirmed?bookingId=12");
    }
    @Test public void viewingMissingTicketDoesNotRecreateIt() throws Exception {
        try(MockedConstruction<TicketDAO> tickets=mockConstruction(TicketDAO.class)) {
            new TicketServlet().doGet(request,response);
            TicketDAO dao=tickets.constructed().get(0);
            verify(dao).findByBooking(12);verifyNoMoreInteractions(dao);
            verify(request).setAttribute(eq("error"),contains("No ticket exists"));
        }
    }
}

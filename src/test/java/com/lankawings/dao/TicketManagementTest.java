package com.lankawings.dao;

import com.lankawings.config.DBConnection;
import com.lankawings.util.ValidationException;
import org.junit.*;
import org.mockito.MockedStatic;
import java.sql.*;
import java.util.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

/** Transaction/authorization regression tests. JDBC is mocked; no real database is changed. */
public class TicketManagementTest {
    private Connection connection;
    private ResultSet booking;
    private PreparedStatement lock, write;
    private MockedStatic<DBConnection> db;
    private List<String> statements;

    @Before public void setUp() throws Exception {
        connection = mock(Connection.class); booking = mock(ResultSet.class);
        lock = mock(PreparedStatement.class); write = mock(PreparedStatement.class);
        statements = new ArrayList<>();
        db = mockStatic(DBConnection.class);
        db.when(DBConnection::getConnection).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenAnswer(invocation -> {
            String sql = invocation.getArgument(0); statements.add(sql);
            return sql.startsWith("SELECT") ? lock : write;
        });
        when(lock.executeQuery()).thenReturn(booking);
        when(booking.next()).thenReturn(true);
        when(booking.getInt("UserID")).thenReturn(7);
        when(booking.getInt("Future")).thenReturn(1);
        when(booking.getString("PNR")).thenReturn("LW12345678");
        when(booking.getString("PaymentStatus")).thenReturn("PAID");
        when(booking.getString("BookingStatus")).thenReturn("CONFIRMED");
        when(booking.getString("Status")).thenReturn("SCHEDULED");
        when(write.executeUpdate()).thenReturn(1);
    }
    @After public void tearDown() { db.close(); }

    @Test public void ownerCanCreateTicket() throws Exception {
        new TicketDAO().create(12,7,false);
        verify(write).setString(1,"TKT-LW12345678"); verify(connection).commit();
    }
    @Test public void anotherPassengerCannotCreate() throws Exception {
        assertThrows(ValidationException.class,()->new TicketDAO().create(12,99,false));
        verify(connection).rollback(); verify(write,never()).executeUpdate();
    }
    @Test public void adminCanCreateForPassenger() throws Exception {
        new TicketDAO().create(12,99,true); verify(connection).commit();
    }
    @Test public void unpaidBookingCannotCreate() throws Exception {
        when(booking.getString("PaymentStatus")).thenReturn("PENDING");
        assertThrows(ValidationException.class,()->new TicketDAO().create(12,7,false));
        verify(write,never()).executeUpdate();
    }
    @Test public void departedFlightCannotCreate() throws Exception {
        when(booking.getInt("Future")).thenReturn(0);
        assertThrows(ValidationException.class,()->new TicketDAO().create(12,7,false));
    }
    @Test public void cancelledFlightCannotCreate() throws Exception {
        when(booking.getString("Status")).thenReturn("CANCELLED");
        assertThrows(ValidationException.class,()->new TicketDAO().create(12,7,false));
    }
    @Test public void cancelledBookingCannotCreate() throws Exception {
        when(booking.getString("BookingStatus")).thenReturn("CANCELLED");
        assertThrows(ValidationException.class,()->new TicketDAO().create(12,7,false));
    }
    @Test public void deleteDoesNotCancelBookingOrPayment() throws Exception {
        new TicketDAO().delete(12,7,true);
        assertTrue(statements.contains("DELETE FROM Tickets WHERE BookingID=?"));
        assertFalse(statements.stream().anyMatch(sql->sql.startsWith("UPDATE")));
        verify(connection).commit();
    }
    @Test public void ownerCannotDeleteOwnTicket() throws Exception {
        assertThrows(ValidationException.class,()->new TicketDAO().delete(12,7,false));
        verifyNoInteractions(connection);
    }
    @Test public void anotherPassengerCannotDelete() throws Exception {
        assertThrows(ValidationException.class,()->new TicketDAO().delete(12,99,false));
        verify(write,never()).executeUpdate();
    }
    @Test public void duplicateCreationRollsBack() throws Exception {
        SQLException duplicate = new SQLException("duplicate","23000",2627);
        when(write.executeUpdate()).thenThrow(duplicate);
        db.when(()->DBConnection.isDuplicate(duplicate)).thenReturn(true);
        assertThrows(ValidationException.class,()->new TicketDAO().create(12,7,false));
        verify(connection).rollback(); verify(connection,never()).commit();
    }
    @Test public void missingDeleteIsReported() throws Exception {
        when(write.executeUpdate()).thenReturn(0);
        assertThrows(ValidationException.class,()->new TicketDAO().delete(12,7,true));
        verify(connection).rollback();
    }
    @Test public void updateRejectsOtherOwnerBeforeChangingDetails() throws Exception {
        assertThrows(ValidationException.class,()->new BookingDAO().updateDetails(12,99,false,"Jane Silva","N1234567","1A",true));
        verify(write,never()).executeUpdate(); verify(connection).rollback();
    }
    @Test public void updateRejectsCancelledBooking() throws Exception {
        when(booking.getString("BookingStatus")).thenReturn("CANCELLED");
        assertThrows(ValidationException.class,()->new BookingDAO().updateDetails(12,7,false,"Jane Silva","N1234567","1A",true));
        verify(write,never()).executeUpdate();
    }
    @Test public void updateRejectsDepartedBooking() throws Exception {
        when(booking.getInt("Departed")).thenReturn(1);
        assertThrows(ValidationException.class,()->new BookingDAO().updateDetails(12,7,false,"Jane Silva","N1234567","1A",true));
        verify(write,never()).executeUpdate();
    }
    @Test public void updateRejectsDeletedTicket() throws Exception {
        when(booking.next()).thenReturn(true,false);
        assertThrows(ValidationException.class,()->new BookingDAO().updateDetails(12,7,false,"Jane Silva","N1234567","1A",true));
        verify(write,never()).executeUpdate();
    }
    @Test public void validUpdateChangesBookingAndTicketTimestampTogether() throws Exception {
        when(booking.getInt("TotalSeats")).thenReturn(180);
        when(booking.getInt("FlightID")).thenReturn(1);
        when(booking.next()).thenReturn(true,true,false,false);
        new BookingDAO().updateDetails(12,7,false,"Jane Silva","N1234567","1A",true);
        assertTrue(statements.stream().anyMatch(sql->sql.startsWith("UPDATE Bookings SET PassengerName")));
        assertTrue(statements.contains("UPDATE Tickets SET LastUpdatedAt=SYSDATETIME() WHERE BookingID=?"));
        verify(connection).commit();
    }
}

package com.lankawings.dao;

import com.lankawings.config.DBConnection;
import com.lankawings.util.ValidationException;
import org.junit.Test;
import org.mockito.MockedStatic;
import java.sql.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class PaymentDeleteTest {
    @Test public void nonAdminAndInvalidIdNeverOpenDatabase() throws Exception {
        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class)) {
            assertThrows(ValidationException.class, () -> new PaymentDAO().delete(12, false));
            assertThrows(ValidationException.class, () -> new PaymentDAO().delete(0, true));
            db.verifyNoInteractions();
        }
    }

    @Test public void deletionOnlyTouchesSelectedPayment() throws Exception {
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeUpdate()).thenReturn(1);
        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenReturn(connection);
            new PaymentDAO().delete(12, true);
            verify(connection).prepareStatement("DELETE FROM Payments WHERE PaymentID=?");
            verify(connection).close();
            verifyNoMoreInteractions(connection);
            verify(statement).setInt(1, 12);
            verify(statement).executeUpdate();
            verify(statement).close();
            verifyNoMoreInteractions(statement);
        }
    }

    @Test public void alreadyDeletedPaymentIsReported() throws Exception {
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeUpdate()).thenReturn(0);
        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenReturn(connection);
            assertThrows(ValidationException.class, () -> new PaymentDAO().delete(12, true));
        }
    }
}

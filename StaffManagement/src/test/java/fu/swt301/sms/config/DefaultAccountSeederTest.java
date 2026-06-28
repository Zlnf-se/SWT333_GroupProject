package fu.swt301.sms.config;

import fu.swt301.sms.utils.PasswordUtils;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultAccountSeederTest {

    @Test
    void seedsAdminStaffAndUserAccountsIfTheyDoNotExist() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(startsWith("IF NOT EXISTS"))).thenReturn(ps);

        new DefaultAccountSeeder().seed(conn);

        verify(conn, times(6)).prepareStatement(startsWith("IF NOT EXISTS"));
        verify(ps, times(6)).executeUpdate();

        verify(ps, atLeastOnce()).setString(eq(2), eq("Admin"));
        verify(ps, atLeastOnce()).setString(eq(2), eq("Staff"));
        verify(ps, atLeastOnce()).setString(eq(2), eq("USER"));

        verify(ps, atLeastOnce()).setString(eq(1), eq("admin@example.com"));
        verify(ps, atLeastOnce()).setString(eq(1), eq("staff@example.com"));
        verify(ps, atLeastOnce()).setString(eq(1), eq("user@example.com"));

        verify(ps, atLeastOnce()).setString(eq(6), argThat(hash -> PasswordUtils.checkPassword("staff123", hash)));
        verify(ps, atLeastOnce()).setString(eq(6), argThat(hash -> PasswordUtils.checkPassword("user123", hash)));
        verify(ps, atLeastOnce()).setInt(eq(7), eq(2));
        verify(ps, atLeastOnce()).setInt(eq(7), eq(3));
    }
}

package fu.swt301.sms.service;

import fu.swt301.sms.dao.StaffDAO;
import fu.swt301.sms.entity.Staff;
import fu.swt301.sms.utils.PasswordUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-06-28T10:00:00Z"), ZoneId.of("UTC"));

    @Mock
    private StaffDAO staffDAO;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(staffDAO, FIXED_CLOCK);
    }

    @Test
    void loginSuccess() {
        Staff staff = createStaff("admin123");
        staff.setStaffID(1);
        staff.setFailedLoginAttempts(2);
        when(staffDAO.findByEmail("admin@gmail.com")).thenReturn(staff);

        Staff result = authService.login("admin@gmail.com", "admin123");

        assertSame(staff, result);
        assertEquals(0, staff.getFailedLoginAttempts());
        verify(staffDAO).resetLoginFailures(1);
    }

    @Test
    void loginFailsWithWrongPassword() {
        Staff staff = createStaff("admin123");
        staff.setStaffID(1);
        staff.setFailedLoginAttempts(1);
        when(staffDAO.findByEmail("admin@gmail.com")).thenReturn(staff);

        Staff result = authService.login("admin@gmail.com", "wrong");

        assertNull(result);
        verify(staffDAO).recordFailedLogin(1, 2, null);
    }

    @Test
    void loginLocksAccountAfterFifthFailure() {
        Staff staff = createStaff("admin123");
        staff.setStaffID(1);
        staff.setFailedLoginAttempts(4);
        when(staffDAO.findByEmail("admin@gmail.com")).thenReturn(staff);

        Staff result = authService.login("admin@gmail.com", "wrong");

        assertNull(result);
        verify(staffDAO).recordFailedLogin(
                1, 5, LocalDateTime.of(2026, 6, 28, 10, 5));
    }

    @Test
    void loginReturnsNullWhenAccountIsLocked() {
        Staff staff = createStaff("admin123");
        staff.setLockUntil(LocalDateTime.of(2026, 6, 28, 10, 1));
        when(staffDAO.findByEmail("admin@gmail.com")).thenReturn(staff);

        Staff result = authService.login("admin@gmail.com", "admin123");

        assertNull(result);
        verify(staffDAO, never()).resetLoginFailures(anyInt());
        verify(staffDAO, never()).recordFailedLogin(anyInt(), anyInt(), any());
    }

    private Staff createStaff(String password) {
        Staff staff = new Staff();
        staff.setEmail("admin@gmail.com");
        staff.setPassword(PasswordUtils.hashPassword(password));
        return staff;
    }
}

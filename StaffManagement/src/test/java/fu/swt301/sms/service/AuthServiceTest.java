package fu.swt301.sms.service;

import fu.swt301.sms.dao.StaffDAO;
import fu.swt301.sms.entity.Staff;
import fu.swt301.sms.utils.PasswordUtils;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthServiceTest {
    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-06-28T10:00:00Z"),
            ZoneId.of("UTC"));

    @Test
    void loginReturnsStaffAndResetsFailuresWhenPasswordMatches() {
        Staff staff = staffWithPassword("admin123");
        staff.setStaffID(12);
        staff.setFailedLoginAttempts(3);
        FakeStaffDAO staffDAO = new FakeStaffDAO(staff);

        AuthService authService = new AuthService(staffDAO, FIXED_CLOCK);
        Staff actualStaff = authService.login("admin@example.com", "admin123");

        assertSame(staff, actualStaff);
        assertEquals(12, staffDAO.resetStaffId);
    }

    @Test
    void loginRecordsFailedAttemptWhenPasswordDoesNotMatch() {
        Staff staff = staffWithPassword("admin123");
        staff.setStaffID(12);
        staff.setFailedLoginAttempts(2);
        FakeStaffDAO staffDAO = new FakeStaffDAO(staff);

        AuthService authService = new AuthService(staffDAO, FIXED_CLOCK);
        Staff actualStaff = authService.login("admin@example.com", "wrong");

        assertNull(actualStaff);
        assertEquals(12, staffDAO.failedStaffId);
        assertEquals(3, staffDAO.failedAttempts);
        assertNull(staffDAO.lockUntil);
    }

    @Test
    void loginLocksAccountForFiveMinutesAfterFifthFailedAttempt() {
        Staff staff = staffWithPassword("admin123");
        staff.setStaffID(12);
        staff.setFailedLoginAttempts(4);
        FakeStaffDAO staffDAO = new FakeStaffDAO(staff);

        AuthService authService = new AuthService(staffDAO, FIXED_CLOCK);
        Staff actualStaff = authService.login("admin@example.com", "wrong");

        assertNull(actualStaff);
        assertEquals(5, staffDAO.failedAttempts);
        assertEquals(LocalDateTime.of(2026, 6, 28, 10, 5), staffDAO.lockUntil);
    }

    @Test
    void loginRejectsLockedAccountWithoutChangingCounters() {
        Staff staff = staffWithPassword("admin123");
        staff.setStaffID(12);
        staff.setLockUntil(LocalDateTime.of(2026, 6, 28, 10, 1));
        FakeStaffDAO staffDAO = new FakeStaffDAO(staff);

        AuthService authService = new AuthService(staffDAO, FIXED_CLOCK);
        Staff actualStaff = authService.login("admin@example.com", "admin123");

        assertNull(actualStaff);
        assertEquals(0, staffDAO.resetStaffId);
        assertEquals(0, staffDAO.failedStaffId);
    }

    @Test
    void authenticateReturnsLockedMessageWhenAccountIsLocked() {
        Staff staff = staffWithPassword("admin123");
        staff.setStaffID(12);
        staff.setLockUntil(LocalDateTime.of(2026, 6, 28, 10, 1));
        FakeStaffDAO staffDAO = new FakeStaffDAO(staff);

        AuthService authService = new AuthService(staffDAO, FIXED_CLOCK);
        AuthResult result = authService.authenticate("admin@example.com", "admin123");

        assertFalse(result.isSuccess());
        assertNull(result.getStaff());
        assertEquals("Account is locked. Please try again later.", result.getMessage());
    }

    @Test
    void authenticateReturnsSuccessResultWhenPasswordMatches() {
        Staff staff = staffWithPassword("admin123");
        FakeStaffDAO staffDAO = new FakeStaffDAO(staff);

        AuthService authService = new AuthService(staffDAO, FIXED_CLOCK);
        AuthResult result = authService.authenticate("admin@example.com", "admin123");

        assertTrue(result.isSuccess());
        assertSame(staff, result.getStaff());
        assertNull(result.getMessage());
    }

    private static Staff staffWithPassword(String plainPassword) {
        Staff staff = new Staff();
        staff.setEmail("admin@example.com");
        staff.setPassword(PasswordUtils.hashPassword(plainPassword));
        return staff;
    }

    private static class FakeStaffDAO extends StaffDAO {
        private final Staff staff;
        private int resetStaffId;
        private int failedStaffId;
        private int failedAttempts;
        private LocalDateTime lockUntil;

        private FakeStaffDAO(Staff staff) {
            this.staff = staff;
        }

        @Override
        public Staff findByEmail(String email) {
            return staff;
        }

        @Override
        public void resetLoginFailures(int staffId) {
            resetStaffId = staffId;
        }

        @Override
        public void recordFailedLogin(int staffId, int failedAttempts, LocalDateTime lockUntil) {
            failedStaffId = staffId;
            this.failedAttempts = failedAttempts;
            this.lockUntil = lockUntil;
        }
    }
}

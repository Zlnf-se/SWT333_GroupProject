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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class AuthServiceTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-06-28T10:00:00Z"),
            ZoneId.of("UTC"));


    @Test
    void loginReturnsStaffAndResetsFailuresWhenPasswordMatches() {
        Staff staff = staffWithPassword("admin123");
        staff.setStaffID(12);
        staff.setFailedLoginAttempts(3);
        FakeStaffDAO dao = new FakeStaffDAO(staff);

        Staff result = new AuthService(dao, FIXED_CLOCK).login("admin@example.com", "admin123");

        assertSame(staff, result);
        assertEquals(12, dao.resetStaffId);
    }

    @Test
    void loginReturnedStaffHasFailedAttemptsResetToZero() {
        Staff staff = staffWithPassword("admin123");
        staff.setStaffID(5);
        staff.setFailedLoginAttempts(2);
        FakeStaffDAO dao = new FakeStaffDAO(staff);

        Staff result = new AuthService(dao, FIXED_CLOCK).login("admin@example.com", "admin123");

        assertEquals(0, result.getFailedLoginAttempts());
        assertNull(result.getLockUntil());
    }


    @Test
    void loginRecordsFailedAttemptWhenPasswordDoesNotMatch() {
        Staff staff = staffWithPassword("admin123");
        staff.setStaffID(12);
        staff.setFailedLoginAttempts(2);
        FakeStaffDAO dao = new FakeStaffDAO(staff);

        Staff result = new AuthService(dao, FIXED_CLOCK).login("admin@example.com", "wrong");

        assertNull(result);
        assertEquals(12, dao.failedStaffId);
        assertEquals(3, dao.failedAttempts);
        assertNull(dao.lockUntil);
    }


    @Test
    void loginLocksAccountForFiveMinutesAfterFifthFailedAttempt() {
        Staff staff = staffWithPassword("admin123");
        staff.setStaffID(12);
        staff.setFailedLoginAttempts(4);
        FakeStaffDAO dao = new FakeStaffDAO(staff);

        Staff result = new AuthService(dao, FIXED_CLOCK).login("admin@example.com", "wrong");

        assertNull(result);
        assertEquals(5, dao.failedAttempts);
        assertEquals(LocalDateTime.of(2026, 6, 28, 10, 5), dao.lockUntil);
    }

    @Test
    void loginStillLocksAccountOnSixthFailedAttempt() {
        Staff staff = staffWithPassword("admin123");
        staff.setStaffID(7);
        staff.setFailedLoginAttempts(5);
        staff.setLockUntil(LocalDateTime.of(2026, 6, 28, 9, 58)); // lock đã hết hạn
        FakeStaffDAO dao = new FakeStaffDAO(staff);

        Staff result = new AuthService(dao, FIXED_CLOCK).login("user@example.com", "wrong");

        assertNull(result);
        assertEquals(6, dao.failedAttempts); // vượt ngưỡng → lock lại
        assertEquals(LocalDateTime.of(2026, 6, 28, 10, 5), dao.lockUntil);
    }


    @Test
    void loginRejectsLockedAccountEvenWithCorrectPassword() {
        Staff staff = staffWithPassword("admin123");
        staff.setStaffID(12);
        staff.setLockUntil(LocalDateTime.of(2026, 6, 28, 10, 1)); // khóa đến 10:01, hiện 10:00
        FakeStaffDAO dao = new FakeStaffDAO(staff);

        Staff result = new AuthService(dao, FIXED_CLOCK).login("admin@example.com", "admin123");

        assertNull(result);
        assertEquals(0, dao.resetStaffId);  // không reset
        assertEquals(0, dao.failedStaffId); // không ghi thêm lần sai
    }

    @Test
    void loginAllowsAccountWhenLockHasExpired() {
        Staff staff = staffWithPassword("admin123");
        staff.setStaffID(9);
        staff.setFailedLoginAttempts(5);
        staff.setLockUntil(LocalDateTime.of(2026, 6, 28, 9, 55)); // lock hết hạn lúc 9:55, hiện 10:00
        FakeStaffDAO dao = new FakeStaffDAO(staff);

        Staff result = new AuthService(dao, FIXED_CLOCK).login("admin@example.com", "admin123");

        assertSame(staff, result);
        assertEquals(9, dao.resetStaffId);
    }


    @Test
    void loginReturnsNullWhenEmailNotFound() {
        FakeStaffDAO dao = new FakeStaffDAO(null);

        Staff result = new AuthService(dao, FIXED_CLOCK).login("unknown@example.com", "admin123");

        assertNull(result);
        assertEquals(0, dao.failedStaffId);
    }


    private static Staff staffWithPassword(String plainPassword) {
        Staff staff = new Staff();
        staff.setEmail("admin@example.com");
        staff.setPassword(PasswordUtils.hashPassword(plainPassword));
        return staff;
    }

    private static class FakeStaffDAO extends StaffDAO {
        private final Staff staff;
        int resetStaffId;
        int failedStaffId;
        int failedAttempts;
        LocalDateTime lockUntil;

        FakeStaffDAO(Staff staff) {
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

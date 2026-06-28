package fu.swt301.sms.service;

import fu.swt301.sms.dao.StaffDAO;
import fu.swt301.sms.entity.Staff;
import fu.swt301.sms.utils.PasswordUtils;

import java.time.Clock;
import java.time.LocalDateTime;

public class AuthService {
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_MINUTES = 5;

    private final StaffDAO staffDAO;
    private final Clock clock;

    public AuthService() {
        this(new StaffDAO());
    }

    public AuthService(StaffDAO staffDAO) {
        this(staffDAO, Clock.systemDefaultZone());
    }

    public AuthService(StaffDAO staffDAO, Clock clock) {
        this.staffDAO = staffDAO;
        this.clock = clock;
    }

    public Staff login(String email, String password) {
        Staff staff = staffDAO.findByEmail(email);
        if (staff == null || isLocked(staff)) {
            return null;
        }

        if (PasswordUtils.checkPassword(password, staff.getPassword())) {
            staffDAO.resetLoginFailures(staff.getStaffID());
            staff.setFailedLoginAttempts(0);
            staff.setLockUntil(null);
            return staff;
        }

        int failedAttempts = staff.getFailedLoginAttempts() + 1;
        LocalDateTime lockUntil = null;
        if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
            lockUntil = LocalDateTime.now(clock).plusMinutes(LOCK_MINUTES);
        }
        staffDAO.recordFailedLogin(staff.getStaffID(), failedAttempts, lockUntil);
        return null;
    }

    private boolean isLocked(Staff staff) {
        LocalDateTime lockUntil = staff.getLockUntil();
        return lockUntil != null && lockUntil.isAfter(LocalDateTime.now(clock));
    }
}

package fu.swt301.sms.service;

import fu.swt301.sms.dao.StaffDAO;
import fu.swt301.sms.entity.Staff;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class AuthServiceTest {

    @Test
    void loginReturnsStaffFromDao() {
        Staff expectedStaff = new Staff();
        FakeStaffDAO staffDAO = new FakeStaffDAO(expectedStaff);

        AuthService authService = new AuthService(staffDAO);
        Staff actualStaff = authService.login("admin@example.com", "admin123");

        assertSame(expectedStaff, actualStaff);
        assertEquals("admin@example.com", staffDAO.email);
        assertEquals("admin123", staffDAO.password);
    }

    private static class FakeStaffDAO extends StaffDAO {
        private final Staff staff;
        private String email;
        private String password;

        private FakeStaffDAO(Staff staff) {
            this.staff = staff;
        }

        @Override
        public Staff checkLogin(String email, String password) {
            this.email = email;
            this.password = password;
            return staff;
        }
    }
}

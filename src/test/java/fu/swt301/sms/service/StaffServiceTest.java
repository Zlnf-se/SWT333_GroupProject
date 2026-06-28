package fu.swt301.sms.service;

import fu.swt301.sms.dao.RoleDAO;
import fu.swt301.sms.dao.StaffDAO;
import fu.swt301.sms.entity.Staff;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class StaffServiceTest {

    @Test
    void saveStaffReturnsDuplicateEmailMessageAndDoesNotCreate() throws SQLException, ClassNotFoundException {
        FakeStaffDAO staffDAO = new FakeStaffDAO();
        Staff staff = new Staff();
        staff.setStaffID(0);
        staff.setEmail("duplicate@example.com");

        staffDAO.emailExists = true;

        StaffService staffService = new StaffService(staffDAO, new RoleDAO());
        String errorMessage = staffService.saveStaff("create", staff);

        assertEquals("Email already exists. Please choose another one.", errorMessage);
        assertEquals(0, staffDAO.createCalls);
    }

    @Test
    void saveStaffCreatesWhenValidationPasses() throws SQLException, ClassNotFoundException {
        FakeStaffDAO staffDAO = new FakeStaffDAO();
        Staff staff = new Staff();
        staff.setStaffID(0);
        staff.setEmail("new@example.com");
        staff.setFullName("New Staff");
        staff.setPhoneNumber("0123456789");

        StaffService staffService = new StaffService(staffDAO, new RoleDAO());
        String errorMessage = staffService.saveStaff("create", staff);

        assertNull(errorMessage);
        assertEquals(staff, staffDAO.createdStaff);
    }

    @Test
    void deleteStaffDelegatesToDao() {
        FakeStaffDAO staffDAO = new FakeStaffDAO();

        StaffService staffService = new StaffService(staffDAO, new RoleDAO());
        staffService.deleteStaff(7);

        assertEquals(7, staffDAO.deletedStaffId);
    }

    private static class FakeStaffDAO extends StaffDAO {
        private boolean emailExists;
        private int createCalls;
        private Staff createdStaff;
        private int deletedStaffId;

        @Override
        public boolean isEmailExists(String email, int currentStaffId) {
            return emailExists;
        }

        @Override
        public boolean isFullNameExists(String fullName, int currentStaffId) {
            return false;
        }

        @Override
        public boolean isPhoneNumberExists(String phoneNumber, int currentStaffId) {
            return false;
        }

        @Override
        public void createStaff(Staff staff) {
            createCalls++;
            createdStaff = staff;
        }

        @Override
        public void deleteStaff(int staffId) {
            deletedStaffId = staffId;
        }
    }
}

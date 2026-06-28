package fu.swt301.sms.service;

import fu.swt301.sms.dao.RoleDAO;
import fu.swt301.sms.dao.StaffDAO;
import fu.swt301.sms.entity.Staff;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

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

    @Test
    void getStaffPageNormalizesPagingAndReturnsTotalPages() {
        FakeStaffDAO staffDAO = new FakeStaffDAO();
        Staff firstStaff = new Staff();
        firstStaff.setStaffID(1);
        staffDAO.filteredStaff = List.of(firstStaff);
        staffDAO.totalStaff = 12;

        StaffService staffService = new StaffService(staffDAO, new RoleDAO());
        StaffPage staffPage = staffService.getStaffPage("An", "EMP", "IT", "true", 2, 5);

        assertEquals(List.of(firstStaff), staffPage.getStaffList());
        assertEquals(2, staffPage.getPage());
        assertEquals(5, staffPage.getPageSize());
        assertEquals(12, staffPage.getTotalItems());
        assertEquals(3, staffPage.getTotalPages());
        assertEquals("An", staffDAO.searchName);
        assertEquals("EMP", staffDAO.employeeCode);
        assertEquals("IT", staffDAO.department);
        assertEquals("true", staffDAO.searchStatus);
        assertEquals(2, staffDAO.page);
        assertEquals(5, staffDAO.pageSize);
    }

    private static class FakeStaffDAO extends StaffDAO {
        private boolean emailExists;
        private int createCalls;
        private Staff createdStaff;
        private int deletedStaffId;
        private List<Staff> filteredStaff = List.of();
        private int totalStaff;
        private String searchName;
        private String employeeCode;
        private String department;
        private String searchStatus;
        private int page;
        private int pageSize;

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

        @Override
        public List<Staff> getStaffByFilter(String searchName, String employeeCode, String department,
                                            String searchStatus, int page, int pageSize) {
            this.searchName = searchName;
            this.employeeCode = employeeCode;
            this.department = department;
            this.searchStatus = searchStatus;
            this.page = page;
            this.pageSize = pageSize;
            return filteredStaff;
        }

        @Override
        public int countStaffByFilter(String searchName, String employeeCode, String department, String searchStatus) {
            return totalStaff;
        }
    }
}

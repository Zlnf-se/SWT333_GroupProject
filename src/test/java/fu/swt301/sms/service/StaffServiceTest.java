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
        FakeStaffDAO dao = new FakeStaffDAO();
        dao.emailExists = true;

        String error = new StaffService(dao, new RoleDAO()).saveStaff("create", buildStaff(0));

        assertEquals("Email already exists. Please choose another one.", error);
        assertEquals(0, dao.createCalls);
    }

    @Test
    void saveStaffReturnsDuplicateFullNameMessage() throws SQLException, ClassNotFoundException {
        FakeStaffDAO dao = new FakeStaffDAO();
        dao.fullNameExists = true;

        String error = new StaffService(dao, new RoleDAO()).saveStaff("create", buildStaff(0));

        assertEquals("Full name already exists. Please choose another one.", error);
        assertEquals(0, dao.createCalls);
    }

    @Test
    void saveStaffReturnsDuplicatePhoneNumberMessage() throws SQLException, ClassNotFoundException {
        FakeStaffDAO dao = new FakeStaffDAO();
        dao.phoneExists = true;

        String error = new StaffService(dao, new RoleDAO()).saveStaff("create", buildStaff(0));

        assertEquals("Phone number already exists. Please choose another one.", error);
        assertEquals(0, dao.createCalls);
    }

    @Test
    void saveStaffReturnsDuplicateEmployeeCodeMessage() throws SQLException, ClassNotFoundException {
        FakeStaffDAO dao = new FakeStaffDAO();
        dao.employeeCodeExists = true;

        Staff staff = buildStaff(0);
        staff.setEmployeeCode("EMP001"); 
        String error = new StaffService(dao, new RoleDAO()).saveStaff("create", staff);

        assertEquals("Employee code already exists. Please choose another one.", error);
        assertEquals(0, dao.createCalls);
    }


    @Test
    void saveStaffCreatesWhenAllFieldsAreUnique() throws SQLException, ClassNotFoundException {
        FakeStaffDAO dao = new FakeStaffDAO();
        Staff staff = buildStaff(0);

        String error = new StaffService(dao, new RoleDAO()).saveStaff("create", staff);

        assertNull(error);
        assertEquals(staff, dao.createdStaff);
        assertEquals(1, dao.createCalls);
    }

    @Test
    void saveStaffSkipsEmployeeCodeCheckWhenCodeIsBlank() throws SQLException, ClassNotFoundException {
        FakeStaffDAO dao = new FakeStaffDAO();
        dao.employeeCodeExists = true;
        Staff staff = buildStaff(0);
        staff.setEmployeeCode(null); 

        String error = new StaffService(dao, new RoleDAO()).saveStaff("create", staff);

        assertNull(error);
        assertEquals(1, dao.createCalls);
    }


    @Test
    void saveStaffUpdatesStaffWhenValidationPasses() throws SQLException, ClassNotFoundException {
        FakeStaffDAO dao = new FakeStaffDAO();
        Staff staff = buildStaff(5);

        String error = new StaffService(dao, new RoleDAO()).saveStaff("update", staff);

        assertNull(error);
        assertEquals(staff, dao.updatedStaff);
    }


    @Test
    void deleteStaffDelegatesToDao() {
        FakeStaffDAO dao = new FakeStaffDAO();

        new StaffService(dao, new RoleDAO()).deleteStaff(7);

        assertEquals(7, dao.deletedStaffId);
    }


    @Test
    void getStaffByIdReturnsDelegatedResult() {
        FakeStaffDAO dao = new FakeStaffDAO();
        Staff expected = buildStaff(3);
        dao.staffById = expected;

        Staff actual = new StaffService(dao, new RoleDAO()).getStaffById(3);

        assertEquals(expected, actual);
    }


    @Test
    void getStaffPageCalculatesTotalPagesCorrectly() {
        FakeStaffDAO dao = new FakeStaffDAO();
        dao.totalStaff = 12;
        dao.filteredStaff = List.of(buildStaff(1));

        StaffPage page = new StaffService(dao, new RoleDAO())
                .getStaffPage("An", "EMP", "IT", "true", 2, 5);

        assertEquals(2, page.getPage());
        assertEquals(5, page.getPageSize());
        assertEquals(12, page.getTotalItems());
        assertEquals(3, page.getTotalPages()); 
    }

    @Test
    void getStaffPageNormalizesPageLessThanOneToOne() {
        FakeStaffDAO dao = new FakeStaffDAO();
        dao.totalStaff = 5;

        StaffPage page = new StaffService(dao, new RoleDAO())
                .getStaffPage(null, null, null, null, 0, 10);

        assertEquals(1, page.getPage()); 
    }

    @Test
    void getStaffPageNormalizesPageSizeLessThanOneToTen() {
        FakeStaffDAO dao = new FakeStaffDAO();
        dao.totalStaff = 5;

        StaffPage page = new StaffService(dao, new RoleDAO())
                .getStaffPage(null, null, null, null, 1, 0);

        assertEquals(10, page.getPageSize()); 
    }

    @Test
    void getStaffPageClampsPageToTotalPagesWhenPageExceedsMax() {
        FakeStaffDAO dao = new FakeStaffDAO();
        dao.totalStaff = 5;

        StaffPage page = new StaffService(dao, new RoleDAO())
                .getStaffPage(null, null, null, null, 99, 5); 

        assertEquals(1, page.getPage()); 
    }

    @Test
    void getStaffPagePassesAllFilterParametersToDao() {
        FakeStaffDAO dao = new FakeStaffDAO();
        dao.totalStaff = 1;
        Staff staff = buildStaff(1);
        dao.filteredStaff = List.of(staff);

        StaffPage page = new StaffService(dao, new RoleDAO())
                .getStaffPage("An", "EMP001", "IT", "true", 1, 5);

        assertEquals(List.of(staff), page.getStaffList());
        assertEquals("An", dao.searchName);
        assertEquals("EMP001", dao.employeeCode);
        assertEquals("IT", dao.department);
        assertEquals("true", dao.searchStatus);
    }


    private static Staff buildStaff(int id) {
        Staff staff = new Staff();
        staff.setStaffID(id);
        staff.setEmail("test@example.com");
        staff.setFullName("Test Staff");
        staff.setPhoneNumber("0123456789");
        return staff;
    }


    private static class FakeStaffDAO extends StaffDAO {
        boolean emailExists;
        boolean fullNameExists;
        boolean phoneExists;
        boolean employeeCodeExists;

        int createCalls;
        Staff createdStaff;
        Staff updatedStaff;
        int deletedStaffId;
        Staff staffById;

        List<Staff> filteredStaff = List.of();
        int totalStaff;
        String searchName;
        String employeeCode;
        String department;
        String searchStatus;

        @Override public boolean isEmailExists(String email, int id) { return emailExists; }
        @Override public boolean isFullNameExists(String name, int id) { return fullNameExists; }
        @Override public boolean isPhoneNumberExists(String phone, int id) { return phoneExists; }
        @Override public boolean isEmployeeCodeExists(String code, int id) { return employeeCodeExists; }

        @Override
        public void createStaff(Staff staff) {
            createCalls++;
            createdStaff = staff;
        }

        @Override
        public void updateStaff(Staff staff) {
            updatedStaff = staff;
        }

        @Override
        public void deleteStaff(int staffId) {
            deletedStaffId = staffId;
        }

        @Override
        public Staff getStaffById(int staffId) {
            return staffById;
        }

        @Override
        public List<Staff> getStaffByFilter(String name, String empCode, String dept,
                                            String status, int page, int pageSize) {
            this.searchName = name;
            this.employeeCode = empCode;
            this.department = dept;
            this.searchStatus = status;
            return filteredStaff;
        }

        @Override
        public int countStaffByFilter(String name, String empCode, String dept, String status) {
            return totalStaff;
        }
    }
}

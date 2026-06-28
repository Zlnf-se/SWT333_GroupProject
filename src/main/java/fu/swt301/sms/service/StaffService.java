package fu.swt301.sms.service;

import fu.swt301.sms.dao.RoleDAO;
import fu.swt301.sms.dao.StaffDAO;
import fu.swt301.sms.entity.Role;
import fu.swt301.sms.entity.Staff;
import fu.swt301.sms.utils.PasswordUtils;

import java.sql.SQLException;
import java.util.List;

public class StaffService {
    private static final String ACTION_CREATE = "create";
    private static final String ACTION_UPDATE = "update";

    private final StaffDAO staffDAO;
    private final RoleDAO roleDAO;

    public StaffService() {
        this(new StaffDAO(), new RoleDAO());
    }

    public StaffService(StaffDAO staffDAO, RoleDAO roleDAO) {
        this.staffDAO = staffDAO;
        this.roleDAO = roleDAO;
    }

    public List<Staff> getStaffByFilter(String searchName, String searchStatus) {
        return staffDAO.getStaffByFilter(searchName, searchStatus);
    }

    public StaffPage getStaffPage(String searchName, String employeeCode, String department,
                                  String searchStatus, int page, int pageSize) {
        int normalizedPage = page < 1 ? 1 : page;
        int normalizedPageSize = pageSize < 1 ? 10 : pageSize;
        int totalItems = staffDAO.countStaffByFilter(searchName, employeeCode, department, searchStatus);
        int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / normalizedPageSize));
        if (normalizedPage > totalPages) {
            normalizedPage = totalPages;
        }

        List<Staff> staffList = staffDAO.getStaffByFilter(
                searchName, employeeCode, department, searchStatus, normalizedPage, normalizedPageSize);
        return new StaffPage(staffList, normalizedPage, normalizedPageSize, totalItems);
    }

    public List<Role> getAllRoles() {
        return roleDAO.getAllRoles();
    }

    public Staff getStaffById(int staffId) {
        return staffDAO.getStaffById(staffId);
    }

    public void deleteStaff(int staffId) {
        staffDAO.deleteStaff(staffId);
    }

    public String saveStaff(String action, Staff staff) {
        String validationError = validateUniqueFields(staff);
        if (validationError != null) {
            return validationError;
        }

        if (ACTION_CREATE.equals(action)) {
            if (staff.getPassword() != null && !staff.getPassword().isBlank()
                    && !PasswordUtils.isBCryptHash(staff.getPassword())) {
                staff.setPassword(PasswordUtils.hashPassword(staff.getPassword()));
            }
            staffDAO.createStaff(staff);
        } else if (ACTION_UPDATE.equals(action)) {
            staffDAO.updateStaff(staff);
        }
        return null;
    }

    private String validateUniqueFields(Staff staff) {
        try {
            if (staffDAO.isEmailExists(staff.getEmail(), staff.getStaffID())) {
                return "Email already exists. Please choose another one.";
            }
            if (staffDAO.isFullNameExists(staff.getFullName(), staff.getStaffID())) {
                return "Full name already exists. Please choose another one.";
            }
            if (staffDAO.isPhoneNumberExists(staff.getPhoneNumber(), staff.getStaffID())) {
                return "Phone number already exists. Please choose another one.";
            }
            if (staff.getEmployeeCode() != null && !staff.getEmployeeCode().isBlank()
                    && staffDAO.isEmployeeCodeExists(staff.getEmployeeCode(), staff.getStaffID())) {
                return "Employee code already exists. Please choose another one.";
            }
            return null;
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            return "Database error during validation.";
        }
    }
}

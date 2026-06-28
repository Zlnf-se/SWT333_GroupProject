package fu.swt301.sms.service;

import fu.swt301.sms.dao.RoleDAO;
import fu.swt301.sms.dao.StaffDAO;
import fu.swt301.sms.entity.Role;
import fu.swt301.sms.entity.Staff;

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
            return null;
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            return "Database error during validation.";
        }
    }
}

package fu.swt301.sms.dao;

import fu.swt301.sms.entity.Role;
import fu.swt301.sms.entity.Staff;
import fu.swt301.sms.utils.DBUtils;
import fu.swt301.sms.utils.PasswordUtils;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class StaffDAO {

    private Staff extractStaffFromResultSet(ResultSet rs) throws SQLException {
        Staff staff = new Staff();
        staff.setStaffID(rs.getInt("StaffID"));
        staff.setEmployeeCode(rs.getString("EmployeeCode"));
        staff.setFullName(rs.getString("FullName"));
        staff.setGender(rs.getBoolean("Gender"));
        staff.setPhoneNumber(rs.getString("PhoneNumber"));
        staff.setEmail(rs.getString("Email"));
        staff.setPassword(rs.getString("Password"));
        staff.setIsActive(rs.getBoolean("IsActive"));
        staff.setDateOfBirth(toLocalDate(rs.getDate("DateOfBirth")));
        staff.setDepartment(rs.getString("Department"));
        staff.setPosition(rs.getString("Position"));
        staff.setSalary(rs.getBigDecimal("Salary"));
        staff.setHireDate(toLocalDate(rs.getDate("HireDate")));
        staff.setDeleted(rs.getBoolean("Deleted"));
        staff.setFailedLoginAttempts(rs.getInt("FailedLoginAttempts"));

        Timestamp lockUntil = rs.getTimestamp("LockUntil");
        if (lockUntil != null) {
            staff.setLockUntil(lockUntil.toLocalDateTime());
        }

        Role role = new Role();
        role.setRoleID(rs.getInt("Role_ID"));
        role.setRoleName(rs.getString("Role_Name"));
        staff.setRole(role);

        return staff;
    }

    public boolean isEmailExists(String email, int currentStaffId) throws SQLException, ClassNotFoundException {
        return existsByField("Email", email, currentStaffId);
    }

    public boolean isFullNameExists(String fullName, int currentStaffId) throws SQLException, ClassNotFoundException {
        return existsByField("FullName", fullName, currentStaffId);
    }

    public boolean isPhoneNumberExists(String phoneNumber, int currentStaffId) throws SQLException, ClassNotFoundException {
        return existsByField("PhoneNumber", phoneNumber, currentStaffId);
    }

    public boolean isEmployeeCodeExists(String employeeCode, int currentStaffId) throws SQLException, ClassNotFoundException {
        return existsByField("EmployeeCode", employeeCode, currentStaffId);
    }

    private boolean existsByField(String columnName, String value, int currentStaffId)
            throws SQLException, ClassNotFoundException {
        if (value == null || value.isBlank()) {
            return false;
        }

        String sql = "SELECT COUNT(*) FROM Staff WHERE " + columnName + " = ? AND StaffID != ? AND Deleted = 0";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, value);
            ps.setInt(2, currentStaffId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public Staff findByEmail(String email) {
        String sql = "SELECT s.*, r.Role_Name FROM Staff s JOIN Role r ON s.Role_ID = r.Role_ID "
                + "WHERE s.Email = ? AND s.Deleted = 0";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractStaffFromResultSet(rs);
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void recordFailedLogin(int staffId, int failedAttempts, LocalDateTime lockUntil) {
        String sql = "UPDATE Staff SET FailedLoginAttempts = ?, LockUntil = ? WHERE StaffID = ?";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, failedAttempts);
            if (lockUntil == null) {
                ps.setNull(2, Types.TIMESTAMP);
            } else {
                ps.setTimestamp(2, Timestamp.valueOf(lockUntil));
            }
            ps.setInt(3, staffId);
            ps.executeUpdate();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public void resetLoginFailures(int staffId) {
        String sql = "UPDATE Staff SET FailedLoginAttempts = 0, LockUntil = NULL WHERE StaffID = ?";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, staffId);
            ps.executeUpdate();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public Staff checkLogin(String email, String password) {
        Staff staff = findByEmail(email);
        if (staff != null && PasswordUtils.checkPassword(password, staff.getPassword())) {
            return staff;
        }
        return null;
    }

    public List<Staff> getStaffByFilter(String name, String status) {
        return getStaffByFilter(name, null, null, status, 1, Integer.MAX_VALUE);
    }

    public List<Staff> getStaffByFilter(String name, String employeeCode, String department,
                                        String status, int page, int pageSize) {
        List<Staff> staffList = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT s.*, r.Role_Name FROM Staff s "
                + "JOIN Role r ON s.Role_ID = r.Role_ID WHERE s.Deleted = 0");
        appendFilterConditions(sql, name, employeeCode, department, status);
        sql.append(" ORDER BY s.StaffID OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");

        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int paramIndex = setFilterParameters(ps, name, employeeCode, department, status);
            ps.setInt(paramIndex++, (page - 1) * pageSize);
            ps.setInt(paramIndex, pageSize);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    staffList.add(extractStaffFromResultSet(rs));
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return staffList;
    }

    public int countStaffByFilter(String name, String employeeCode, String department, String status) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM Staff s WHERE s.Deleted = 0");
        appendFilterConditions(sql, name, employeeCode, department, status);

        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            setFilterParameters(ps, name, employeeCode, department, status);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void createStaff(Staff staff) {
        String sql = "INSERT INTO Staff (EmployeeCode, FullName, Gender, PhoneNumber, Email, Password, Role_ID, "
                + "IsActive, DateOfBirth, Department, Position, Salary, HireDate, Deleted) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            setStaffWriteParameters(ps, staff, true);
            ps.executeUpdate();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateStaff(Staff staff) {
        String sql = "UPDATE Staff SET EmployeeCode = ?, FullName = ?, Gender = ?, PhoneNumber = ?, Email = ?, "
                + "Role_ID = ?, IsActive = ?, DateOfBirth = ?, Department = ?, Position = ?, Salary = ?, HireDate = ? "
                + "WHERE StaffID = ?";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            setStaffWriteParameters(ps, staff, false);
            ps.executeUpdate();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteStaff(int staffId) {
        String sql = "UPDATE Staff SET Deleted = 1 WHERE StaffID = ?";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, staffId);
            ps.executeUpdate();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public Staff getStaffById(int staffId) {
        String sql = "SELECT s.*, r.Role_Name FROM Staff s JOIN Role r ON s.Role_ID = r.Role_ID "
                + "WHERE s.StaffID = ? AND s.Deleted = 0";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, staffId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractStaffFromResultSet(rs);
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void appendFilterConditions(StringBuilder sql, String name, String employeeCode,
                                        String department, String status) {
        if (name != null && !name.isBlank()) {
            sql.append(" AND s.FullName LIKE ?");
        }
        if (employeeCode != null && !employeeCode.isBlank()) {
            sql.append(" AND s.EmployeeCode LIKE ?");
        }
        if (department != null && !department.isBlank()) {
            sql.append(" AND s.Department LIKE ?");
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND s.IsActive = ?");
        }
    }

    private int setFilterParameters(PreparedStatement ps, String name, String employeeCode,
                                    String department, String status) throws SQLException {
        int paramIndex = 1;
        if (name != null && !name.isBlank()) {
            ps.setString(paramIndex++, "%" + name.trim() + "%");
        }
        if (employeeCode != null && !employeeCode.isBlank()) {
            ps.setString(paramIndex++, "%" + employeeCode.trim() + "%");
        }
        if (department != null && !department.isBlank()) {
            ps.setString(paramIndex++, "%" + department.trim() + "%");
        }
        if (status != null && !status.isBlank()) {
            ps.setBoolean(paramIndex++, Boolean.parseBoolean(status));
        }
        return paramIndex;
    }

    private void setStaffWriteParameters(PreparedStatement ps, Staff staff, boolean includePassword) throws SQLException {
        ps.setString(1, staff.getEmployeeCode());
        ps.setString(2, staff.getFullName());
        ps.setBoolean(3, staff.isGender());
        ps.setString(4, staff.getPhoneNumber());
        ps.setString(5, staff.getEmail());

        int index = 6;
        if (includePassword) {
            ps.setString(index++, staff.getPassword());
        }
        ps.setInt(index++, staff.getRole().getRoleID());
        ps.setBoolean(index++, staff.isIsActive());
        setDateOrNull(ps, index++, staff.getDateOfBirth());
        ps.setString(index++, staff.getDepartment());
        ps.setString(index++, staff.getPosition());
        ps.setBigDecimal(index++, staff.getSalary());
        setDateOrNull(ps, index++, staff.getHireDate());
        if (!includePassword) {
            ps.setInt(index, staff.getStaffID());
        }
    }

    private void setDateOrNull(PreparedStatement ps, int parameterIndex, LocalDate value) throws SQLException {
        if (value == null) {
            ps.setNull(parameterIndex, Types.DATE);
        } else {
            ps.setDate(parameterIndex, Date.valueOf(value));
        }
    }

    private LocalDate toLocalDate(Date date) {
        return date == null ? null : date.toLocalDate();
    }
}

package fu.swt301.sms.config;

import fu.swt301.sms.utils.PasswordUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

class DefaultAccountSeeder {

    void seed(Connection conn) throws SQLException {
        ensureRole(conn, 1, "Admin");
        ensureRole(conn, 2, "Staff");
        ensureRole(conn, 3, "USER");

        ensureAccount(conn, "Admin User", true, "0123456789", "admin@example.com", "admin123", 1);
        ensureAccount(conn, "Staff User", true, "0987654321", "staff@example.com", "staff123", 2);
        ensureAccount(conn, "Normal User", true, "0111111111", "user@example.com", "user123", 3);
    }

    private void ensureRole(Connection conn, int roleId, String roleName) throws SQLException {
        String sql = "IF NOT EXISTS (SELECT 1 FROM Role WHERE Role_ID = ? OR Role_Name = ?) "
                + "INSERT INTO Role (Role_ID, Role_Name) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roleId);
            ps.setString(2, roleName);
            ps.setInt(3, roleId);
            ps.setString(4, roleName);
            ps.executeUpdate();
        }
    }

    private void ensureAccount(Connection conn, String fullName, boolean gender, String phoneNumber,
                               String email, String plainPassword, int roleId) throws SQLException {
        String sql = "IF NOT EXISTS (SELECT 1 FROM Staff WHERE Email = ?) "
                + "INSERT INTO Staff (FullName, Gender, PhoneNumber, Email, Password, Role_ID, IsActive) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, fullName);
            ps.setBoolean(3, gender);
            ps.setString(4, phoneNumber);
            ps.setString(5, email);
            ps.setString(6, PasswordUtils.hashPassword(plainPassword));
            ps.setInt(7, roleId);
            ps.setBoolean(8, true);
            ps.executeUpdate();
        }
    }
}

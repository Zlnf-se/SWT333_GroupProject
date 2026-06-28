package fu.swt301.sms.config;

import fu.swt301.sms.utils.DBUtils;
import fu.swt301.sms.utils.PasswordUtils;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This listener class is automatically instantiated and invoked by the web container when the application starts up.
 * Its primary purpose is to initialize the database by:
 * 1. Creating the necessary tables ('Role', 'Staff') if they do not already exist.
 * 2. Seeding the tables with default data (e.g., user roles and a default admin account) if they are empty.
 * This makes the application self-contained and easier to deploy.
 */
@WebListener
public class DataInitializer implements ServletContextListener {
    private final DefaultAccountSeeder defaultAccountSeeder = new DefaultAccountSeeder();


    /**
     * This method is called by the container when the web application is first started.
     * It orchestrates the database initialization process.
     * @param sce The event object containing the ServletContext.
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try (Connection conn = DBUtils.getConnection()) {
            // Step 1: Ensure database tables are created before proceeding.
            System.out.println("Checking database schema...");
            createRoleTableIfNotExists(conn);
            createStaffTableIfNotExists(conn);
            ensureStaffAuthColumns(conn);
            defaultAccountSeeder.seed(conn);
            hashPlainTextPasswords(conn);

        } catch (SQLException | ClassNotFoundException e) {
            // If any database error occurs during initialization, log it and throw a RuntimeException
            // to halt the application's startup, as it cannot function without a proper database setup.
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize database.", e);
        }
    }

    /**
     * Checks if the 'Role' table exists in the database. If not, it creates the table.
     * @param conn The active database connection.
     * @throws SQLException if a database access error occurs.
     */
    private void createRoleTableIfNotExists(Connection conn) throws SQLException {
        String tableName = "Role";
        String checkTableSQL = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = ?";
        boolean tableExists = false;

        try (PreparedStatement ps = conn.prepareStatement(checkTableSQL)) {
            ps.setString(1, tableName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    tableExists = true;
                }
            }
        }

        if (!tableExists) {
            System.out.println("Table 'Role' not found. Creating table...");
            String createSQL = "CREATE TABLE Role (" +
                               "Role_ID INT PRIMARY KEY, " +
                               "Role_Name NVARCHAR(50) NOT NULL UNIQUE" +
                               ")";
            try (PreparedStatement ps = conn.prepareStatement(createSQL)) {
                ps.execute();
                System.out.println("Table 'Role' created.");
            }
        }
    }

    /**
     * Checks if the 'Staff' table exists in the database. If not, it creates the table
     * with a foreign key constraint pointing to the 'Role' table.
     * @param conn The active database connection.
     * @throws SQLException if a database access error occurs.
     */
    private void createStaffTableIfNotExists(Connection conn) throws SQLException {
        String tableName = "Staff";
        String checkTableSQL = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = ?";
        boolean tableExists = false;

        try (PreparedStatement ps = conn.prepareStatement(checkTableSQL)) {
            ps.setString(1, tableName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    tableExists = true;
                }
            }
        }

        if (!tableExists) {
            System.out.println("Table 'Staff' not found. Creating table...");
            String createSQL = "CREATE TABLE Staff (" +
                               "StaffID INT PRIMARY KEY IDENTITY(1,1), " +
                               "FullName NVARCHAR(100) NOT NULL, " +
                               "EmployeeCode VARCHAR(50) NULL, " +
                               "Gender BIT NOT NULL, " +
                               "PhoneNumber VARCHAR(20), " +
                               "Email VARCHAR(100) NOT NULL UNIQUE, " +
                               "Password VARCHAR(255) NOT NULL, " +
                               "Role_ID INT NOT NULL, " +
                               "IsActive BIT NOT NULL, " +
                               "DateOfBirth DATE NULL, " +
                               "Department NVARCHAR(100) NULL, " +
                               "Position NVARCHAR(100) NULL, " +
                               "Salary DECIMAL(18, 2) NULL, " +
                               "HireDate DATE NULL, " +
                               "Deleted BIT NOT NULL CONSTRAINT DF_Staff_Deleted DEFAULT 0, " +
                               "FailedLoginAttempts INT NOT NULL CONSTRAINT DF_Staff_FailedLoginAttempts DEFAULT 0, " +
                               "LockUntil DATETIME2 NULL, " +
                               "CONSTRAINT FK_Staff_Role FOREIGN KEY (Role_ID) REFERENCES Role(Role_ID)" +
                               ")";
            try (PreparedStatement ps = conn.prepareStatement(createSQL)) {
                ps.execute();
                System.out.println("Table 'Staff' created.");
            }
        }
    }

    private void ensureStaffAuthColumns(Connection conn) throws SQLException {
        if (!columnExists(conn, "Staff", "FailedLoginAttempts")) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "ALTER TABLE Staff ADD FailedLoginAttempts INT NOT NULL CONSTRAINT DF_Staff_FailedLoginAttempts DEFAULT 0 WITH VALUES")) {
                ps.execute();
                System.out.println("Column 'FailedLoginAttempts' added to Staff.");
            }
        }

        if (!columnExists(conn, "Staff", "LockUntil")) {
            try (PreparedStatement ps = conn.prepareStatement("ALTER TABLE Staff ADD LockUntil DATETIME2 NULL")) {
                ps.execute();
                System.out.println("Column 'LockUntil' added to Staff.");
            }
        }

        ensureStaffProfileColumns(conn);
    }

    private void ensureStaffProfileColumns(Connection conn) throws SQLException {
        if (!columnExists(conn, "Staff", "EmployeeCode")) {
            addColumn(conn, "ALTER TABLE Staff ADD EmployeeCode VARCHAR(50) NULL", "EmployeeCode");
        }
        if (!columnExists(conn, "Staff", "DateOfBirth")) {
            addColumn(conn, "ALTER TABLE Staff ADD DateOfBirth DATE NULL", "DateOfBirth");
        }
        if (!columnExists(conn, "Staff", "Department")) {
            addColumn(conn, "ALTER TABLE Staff ADD Department NVARCHAR(100) NULL", "Department");
        }
        if (!columnExists(conn, "Staff", "Position")) {
            addColumn(conn, "ALTER TABLE Staff ADD Position NVARCHAR(100) NULL", "Position");
        }
        if (!columnExists(conn, "Staff", "Salary")) {
            addColumn(conn, "ALTER TABLE Staff ADD Salary DECIMAL(18, 2) NULL", "Salary");
        }
        if (!columnExists(conn, "Staff", "HireDate")) {
            addColumn(conn, "ALTER TABLE Staff ADD HireDate DATE NULL", "HireDate");
        }
        if (!columnExists(conn, "Staff", "Deleted")) {
            addColumn(conn, "ALTER TABLE Staff ADD Deleted BIT NOT NULL CONSTRAINT DF_Staff_Deleted DEFAULT 0 WITH VALUES", "Deleted");
        }
    }

    private void addColumn(Connection conn, String sql, String columnName) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.execute();
            System.out.println("Column '" + columnName + "' added to Staff.");
        }
    }

    private boolean columnExists(Connection conn, String tableName, String columnName) throws SQLException {
        String sql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = ? AND COLUMN_NAME = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tableName);
            ps.setString(2, columnName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private void hashPlainTextPasswords(Connection conn) throws SQLException {
        String selectSql = "SELECT StaffID, Password FROM Staff";
        try (PreparedStatement selectPs = conn.prepareStatement(selectSql);
             ResultSet rs = selectPs.executeQuery()) {
            while (rs.next()) {
                int staffId = rs.getInt("StaffID");
                String password = rs.getString("Password");
                if (!PasswordUtils.isBCryptHash(password)) {
                    updatePassword(conn, staffId, PasswordUtils.hashPassword(password));
                }
            }
        }
    }

    private void updatePassword(Connection conn, int staffId, String hashedPassword) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE Staff SET Password = ? WHERE StaffID = ?")) {
            ps.setString(1, hashedPassword);
            ps.setInt(2, staffId);
            ps.executeUpdate();
        }
    }

    /**
     * This method is called by the container when the web application is about to be shut down.
     * No cleanup action is needed in this case.
     * @param sce The event object containing the ServletContext.
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // No action needed on shutdown.
    }
}

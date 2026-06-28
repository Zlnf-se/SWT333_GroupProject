package fu.swt301.sms.utils;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBUtils {
    private static final String DB_PROPERTIES_FILE = "db.properties";

    public static Connection getConnection() throws ClassNotFoundException, SQLException {
        Properties properties = loadDatabaseProperties();
        Class.forName(properties.getProperty("db.driver"));
        return DriverManager.getConnection(
                properties.getProperty("db.url"),
                properties.getProperty("db.username"),
                properties.getProperty("db.password"));
    }

    private static Properties loadDatabaseProperties() throws SQLException {
        Properties properties = new Properties();
        ClassLoader classLoader = DBUtils.class.getClassLoader();

        try (InputStream inputStream = classLoader.getResourceAsStream(DB_PROPERTIES_FILE)) {
            if (inputStream == null) {
                throw new SQLException("Database configuration file not found: " + DB_PROPERTIES_FILE);
            }
            properties.load(inputStream);
            return properties;
        } catch (IOException e) {
            throw new SQLException("Failed to load database configuration.", e);
        }
    }
}

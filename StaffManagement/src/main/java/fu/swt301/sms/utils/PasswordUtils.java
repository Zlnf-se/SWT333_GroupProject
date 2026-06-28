package fu.swt301.sms.utils;

import org.mindrot.jbcrypt.BCrypt;

public final class PasswordUtils {
    private PasswordUtils() {
    }

    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null || !isBCryptHash(hashedPassword)) {
            return false;
        }
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }

    public static boolean isBCryptHash(String password) {
        return password != null
                && (password.startsWith("$2a$") || password.startsWith("$2b$") || password.startsWith("$2y$"));
    }
}

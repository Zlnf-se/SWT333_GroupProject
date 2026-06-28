package fu.swt301.sms.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordUtilsTest {

    @Test
    void hashPasswordCreatesBCryptHashThatMatchesOriginalPassword() {
        String hash = PasswordUtils.hashPassword("admin123");

        assertTrue(hash.startsWith("$2a$"));
        assertTrue(PasswordUtils.checkPassword("admin123", hash));
        assertFalse(PasswordUtils.checkPassword("wrong", hash));
    }
}

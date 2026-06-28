package fu.swt301.sms.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilsTest {

    @Test
    void hashAndCheckPasswordSuccessfully() {
        String hash = PasswordUtils.hashPassword("admin123");

        assertTrue(PasswordUtils.isBCryptHash(hash));
        assertTrue(PasswordUtils.checkPassword("admin123", hash));
        assertFalse(PasswordUtils.checkPassword("wrong", hash));
    }

    @Test
    void checkPasswordReturnsFalseForInvalidInput() {
        assertFalse(PasswordUtils.checkPassword(null, null));
        assertFalse(PasswordUtils.checkPassword("admin123", "plain-text"));
        assertFalse(PasswordUtils.isBCryptHash("plain-text"));
    }
}

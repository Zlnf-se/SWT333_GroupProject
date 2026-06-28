package fu.swt301.sms.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordUtilsTest {


    @Test
    void hashPasswordCreatesBCryptHashThatMatchesOriginalPassword() {
        String hash = PasswordUtils.hashPassword("admin123");

        assertNotNull(hash);
        assertTrue(hash.startsWith("$2a$"));
        assertTrue(PasswordUtils.checkPassword("admin123", hash));
    }

    @Test
    void hashPasswordProducesDifferentHashEachCall() {
        String hash1 = PasswordUtils.hashPassword("admin123");
        String hash2 = PasswordUtils.hashPassword("admin123");

        assertTrue(PasswordUtils.checkPassword("admin123", hash1));
        assertTrue(PasswordUtils.checkPassword("admin123", hash2));
    }


    @Test
    void checkPasswordReturnsTrueForCorrectPassword() {
        String hash = PasswordUtils.hashPassword("mySecret");
        assertTrue(PasswordUtils.checkPassword("mySecret", hash));
    }

    @Test
    void checkPasswordReturnsFalseForWrongPassword() {
        String hash = PasswordUtils.hashPassword("mySecret");
        assertFalse(PasswordUtils.checkPassword("wrongPass", hash));
    }


    @Test
    void checkPasswordReturnsFalseWhenPasswordIsNull() {
        String hash = PasswordUtils.hashPassword("admin123");
        assertFalse(PasswordUtils.checkPassword(null, hash));
    }

    @Test
    void checkPasswordReturnsFalseWhenHashIsNull() {
        assertFalse(PasswordUtils.checkPassword("admin123", null));
    }

    @Test
    void checkPasswordReturnsFalseWhenBothInputsAreNull() {
        assertFalse(PasswordUtils.checkPassword(null, null));
    }

    @Test
    void checkPasswordReturnsFalseWhenHashIsPlainText() {
        assertFalse(PasswordUtils.checkPassword("admin123", "admin123"));
    }

    @Test
    void checkPasswordReturnsFalseForEmptyPassword() {
        String hash = PasswordUtils.hashPassword("admin123");
        assertFalse(PasswordUtils.checkPassword("", hash));
    }


    @Test
    void isBCryptHashReturnsTrueForValidBCryptHash() {
        String hash = PasswordUtils.hashPassword("testPass");
        assertTrue(PasswordUtils.isBCryptHash(hash));
    }

    @Test
    void isBCryptHashReturnsFalseForPlainText() {
        assertFalse(PasswordUtils.isBCryptHash("plainpassword"));
    }

    @Test
    void isBCryptHashReturnsFalseForNull() {
        assertFalse(PasswordUtils.isBCryptHash(null));
    }

    @Test
    void isBCryptHashReturnsTrueForVariantPrefixes() {
        assertTrue(PasswordUtils.isBCryptHash("$2b$10$abcdefghijklmnopqrstuvwx"));
        assertTrue(PasswordUtils.isBCryptHash("$2y$10$abcdefghijklmnopqrstuvwx"));
    }
}

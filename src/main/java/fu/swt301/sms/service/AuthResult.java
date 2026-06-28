package fu.swt301.sms.service;

import fu.swt301.sms.entity.Staff;

public class AuthResult {
    private final Staff staff;
    private final String message;

    private AuthResult(Staff staff, String message) {
        this.staff = staff;
        this.message = message;
    }

    public static AuthResult success(Staff staff) {
        return new AuthResult(staff, null);
    }

    public static AuthResult failure(String message) {
        return new AuthResult(null, message);
    }

    public boolean isSuccess() {
        return staff != null;
    }

    public Staff getStaff() {
        return staff;
    }

    public String getMessage() {
        return message;
    }
}

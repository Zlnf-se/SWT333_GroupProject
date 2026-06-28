package fu.swt301.sms.service;

import fu.swt301.sms.dao.StaffDAO;
import fu.swt301.sms.entity.Staff;

public class AuthService {
    private final StaffDAO staffDAO;

    public AuthService() {
        this(new StaffDAO());
    }

    public AuthService(StaffDAO staffDAO) {
        this.staffDAO = staffDAO;
    }

    public Staff login(String email, String password) {
        return staffDAO.checkLogin(email, password);
    }
}

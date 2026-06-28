package fu.swt301.sms.service;

import fu.swt301.sms.dao.StaffDAO;
import fu.swt301.sms.entity.Staff;
import fu.swt301.sms.utils.PasswordUtils;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;


class AuthServiceTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(

    

    @Test


        assertSame(staff, result);
    }

    @Test


        assertNull(result);
    }

    @Test
        staff.setFailedLoginAttempts(4); 


        assertNull(result);
    }

    @Test
        staff.setLockUntil(LocalDateTime.of(2026, 6, 28, 10, 1)); 


        assertNull(result);
    }

        Staff staff = new Staff();
        return staff;
    }
}

package fu.swt301.sms.servlet;

import fu.swt301.sms.entity.Staff;
import fu.swt301.sms.service.StaffService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/staff-detail")
public class StaffDetailServlet extends HttpServlet {
    private final StaffService staffService;

    public StaffDetailServlet() {
        this(new StaffService());
    }

    StaffDetailServlet(StaffService staffService) {
        this.staffService = staffService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int staffId = parseInt(request.getParameter("id"), 0);
        if (staffId < 1) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        Staff staff = staffService.getStaffById(staffId);
        if (staff == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        request.setAttribute("staff", staff);
        request.getRequestDispatcher("staff-detail.jsp").forward(request, response);
    }

    private int parseInt(String value, int defaultValue) {
        try {
            return value == null || value.isBlank() ? defaultValue : Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}

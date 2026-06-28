package fu.swt301.sms.servlet;

import fu.swt301.sms.entity.Staff;
import fu.swt301.sms.service.StaffPage;
import fu.swt301.sms.service.StaffService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/staff-list")
public class StaffListServlet extends HttpServlet {
    private final StaffService staffService;

    public StaffListServlet() {
        this(new StaffService());
    }

    StaffListServlet(StaffService staffService) {
        this.staffService = staffService;
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String searchName = request.getParameter("searchName");
        String employeeCode = request.getParameter("employeeCode");
        String department = request.getParameter("department");
        String searchStatus = request.getParameter("searchStatus");
        int page = parseInt(request.getParameter("page"), 1);
        int pageSize = parseInt(request.getParameter("pageSize"), 10);

        StaffPage staffPage = staffService.getStaffPage(searchName, employeeCode, department, searchStatus, page, pageSize);
        List<Staff> staffList = staffPage.getStaffList();

        request.setAttribute("staffList", staffList);
        request.setAttribute("staffPage", staffPage);
        request.setAttribute("page", staffPage.getPage());
        request.setAttribute("pageSize", staffPage.getPageSize());
        request.setAttribute("totalPages", staffPage.getTotalPages());
        request.setAttribute("totalItems", staffPage.getTotalItems());
        request.getRequestDispatcher("staff-list.jsp").forward(request, response);
    }

    private int parseInt(String value, int defaultValue) {
        try {
            return value == null || value.isBlank() ? defaultValue : Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}

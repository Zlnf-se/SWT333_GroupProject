package fu.swt301.sms.servlet;

import fu.swt301.sms.entity.Staff;
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
        String searchStatus = request.getParameter("searchStatus");

        List<Staff> staffList = staffService.getStaffByFilter(searchName, searchStatus);

        request.setAttribute("staffList", staffList);
        request.getRequestDispatcher("staff-list.jsp").forward(request, response);
    }
}

package fu.swt301.sms.servlet;

import fu.swt301.sms.entity.Role;
import fu.swt301.sms.entity.Staff;
import fu.swt301.sms.service.StaffService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@WebServlet("/staff-crud")
public class StaffCrudServlet extends HttpServlet {
    private final StaffService staffService;

    public StaffCrudServlet() {
        this(new StaffService());
    }

    StaffCrudServlet(StaffService staffService) {
        this.staffService = staffService;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");

        Staff staff = new Staff();
        String staffIdParam = request.getParameter("staffID");
        int staffId = (staffIdParam != null && !staffIdParam.isEmpty()) ? Integer.parseInt(staffIdParam) : 0;
        staff.setStaffID(staffId);
        staff.setEmployeeCode(trimToNull(request.getParameter("employeeCode")));

        String fullName = request.getParameter("fullName");
        String phoneNumber = request.getParameter("phoneNumber");
        String email = request.getParameter("email");
        String salaryStr = request.getParameter("salary");
        String dateOfBirthStr = request.getParameter("dateOfBirth");
        String hireDateStr = request.getParameter("hireDate");

        if (fullName == null || fullName.isBlank()) {
            forwardWithError(request, response, "Full name is required.", staff, action);
            return;
        }
        if (phoneNumber == null || phoneNumber.isBlank()) {
            forwardWithError(request, response, "Phone number is required.", staff, action);
            return;
        }
        if (email == null || email.isBlank()) {
            forwardWithError(request, response, "Email is required.", staff, action);
            return;
        }
        if (salaryStr != null && !salaryStr.isBlank()) {
            try {
                BigDecimal salary = new BigDecimal(salaryStr);
                if (salary.compareTo(BigDecimal.ZERO) < 0) {
                    forwardWithError(request, response, "Salary must be a positive number.", staff, action);
                    return;
                }
                staff.setSalary(salary);
            } catch (NumberFormatException e) {
                forwardWithError(request, response, "Salary must be a valid number.", staff, action);
                return;
            }
        }
        if (dateOfBirthStr != null && !dateOfBirthStr.isBlank()) {
            try {
                LocalDate dob = LocalDate.parse(dateOfBirthStr);
                if (dob.isAfter(LocalDate.now())) {
                    forwardWithError(request, response, "Date of birth cannot be in the future.", staff, action);
                    return;
                }
                staff.setDateOfBirth(dob);
            } catch (Exception e) {
                forwardWithError(request, response, "Date of birth is invalid.", staff, action);
                return;
            }
        }
        if (hireDateStr != null && !hireDateStr.isBlank()) {
            try {
                staff.setHireDate(LocalDate.parse(hireDateStr));
            } catch (Exception e) {
                forwardWithError(request, response, "Hire date is invalid.", staff, action);
                return;
            }
        }

        staff.setFullName(fullName.trim());
        staff.setGender(Boolean.parseBoolean(request.getParameter("gender")));
        staff.setPhoneNumber(phoneNumber.trim());
        staff.setEmail(email.trim());
        staff.setIsActive(Boolean.parseBoolean(request.getParameter("isActive")));
        staff.setDepartment(trimToNull(request.getParameter("department")));
        staff.setPosition(trimToNull(request.getParameter("position")));

        if ("create".equals(action)) {
            String password = request.getParameter("password");
            if (password == null || password.isBlank()) {
                forwardWithError(request, response, "Password is required.", staff, action);
                return;
            }
            staff.setPassword(password);
        }

        Role role = new Role();
        role.setRoleID(Integer.parseInt(request.getParameter("roleID")));
        staff.setRole(role);

        String errorMessage = staffService.saveStaff(action, staff);

        if (errorMessage != null) {
            forwardWithError(request, response, errorMessage, staff, action);
            return;
        }

        response.sendRedirect("staff-list");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");

        if ("delete".equals(action)) {
            int staffId = Integer.parseInt(request.getParameter("id"));
            staffService.deleteStaff(staffId);
            response.sendRedirect("staff-list");
        } else {
            List<Role> roleList = staffService.getAllRoles();
            request.setAttribute("roleList", roleList);

            if ("edit".equals(action)) {
                int staffId = Integer.parseInt(request.getParameter("id"));
                Staff staff = staffService.getStaffById(staffId);
                request.setAttribute("staff", staff);
            }

            request.getRequestDispatcher("staff-form.jsp").forward(request, response);
        }
    }

    private void forwardWithError(HttpServletRequest request, HttpServletResponse response,
                                  String error, Staff staff, String action)
            throws ServletException, IOException {
        request.setAttribute("errorMessage", error);
        request.setAttribute("staff", staff);
        request.setAttribute("roleList", staffService.getAllRoles());
        request.getRequestDispatcher("staff-form.jsp").forward(request, response);
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        return value.trim();
    }
}

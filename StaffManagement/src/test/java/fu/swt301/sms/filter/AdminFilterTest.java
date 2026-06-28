package fu.swt301.sms.filter;

import fu.swt301.sms.entity.Role;
import fu.swt301.sms.entity.Staff;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminFilterTest {

    @Test
    void allowsAdminToCreateStaff() throws Exception {
        HttpServletRequest request = requestWithUser("Admin", "GET", "create");
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        new AdminFilter().doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(response, never()).sendError(any(Integer.class));
    }

    @Test
    void blocksNonAdminFromDeletingStaff() throws Exception {
        HttpServletRequest request = requestWithUser("Staff", "GET", "delete");
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        new AdminFilter().doFilter(request, response, chain);

        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN);
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    void redirectsToLoginWhenMutatingRequestHasNoSession() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(request.getMethod()).thenReturn("POST");
        when(request.getParameter("action")).thenReturn("update");
        when(request.getSession(false)).thenReturn(null);
        when(request.getContextPath()).thenReturn("/StaffManagement");

        new AdminFilter().doFilter(request, response, chain);

        verify(response).sendRedirect("/StaffManagement/login");
        verify(chain, never()).doFilter(any(), any());
    }

    private static HttpServletRequest requestWithUser(String roleName, String method, String action) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);

        when(request.getMethod()).thenReturn(method);
        when(request.getParameter("action")).thenReturn(action);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(staffWithRole(roleName));

        return request;
    }

    private static Staff staffWithRole(String roleName) {
        Role role = new Role();
        role.setRoleName(roleName);

        Staff staff = new Staff();
        staff.setRole(role);
        return staff;
    }
}

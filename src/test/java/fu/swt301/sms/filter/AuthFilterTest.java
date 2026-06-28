package fu.swt301.sms.filter;

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

class AuthFilterTest {

    @Test
    void redirectsToLoginWhenUserIsNotAuthenticated() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(request.getSession(false)).thenReturn(null);
        when(request.getContextPath()).thenReturn("/StaffManagement");

        new AuthFilter().doFilter(request, response, chain);

        verify(response).sendRedirect("/StaffManagement/login");
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    void continuesWhenUserIsAuthenticated() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        FilterChain chain = mock(FilterChain.class);

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(new Staff());

        new AuthFilter().doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(response, never()).sendRedirect(any());
    }
}

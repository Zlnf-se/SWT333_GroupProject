package fu.swt301.sms.filter;

import fu.swt301.sms.entity.Role;
import fu.swt301.sms.entity.Staff;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Set;

@WebFilter(urlPatterns = {"/staff-crud"})
public class AdminFilter implements Filter {
    private static final Set<String> ADMIN_ACTIONS = Set.of("create", "edit", "delete", "update");

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if (!requiresAdmin(httpRequest)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = httpRequest.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login");
            return;
        }

        if (!isAdmin(session.getAttribute("user"))) {
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean requiresAdmin(HttpServletRequest request) {
        String action = request.getParameter("action");
        return "POST".equalsIgnoreCase(request.getMethod())
                || action == null
                || ADMIN_ACTIONS.contains(action.toLowerCase());
    }

    private boolean isAdmin(Object user) {
        if (!(user instanceof Staff)) {
            return false;
        }

        Role role = ((Staff) user).getRole();
        return role != null && "Admin".equalsIgnoreCase(role.getRoleName());
    }
}

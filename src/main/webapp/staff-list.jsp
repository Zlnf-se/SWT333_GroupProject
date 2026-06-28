<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<html>
<head>
    <title>Staff List</title>
    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css">
</head>
<body>
<div class="container-fluid px-4">
    <c:set var="isAdmin" value="${not empty sessionScope.user and sessionScope.user.role.roleName == 'Admin'}" />
    <h2 class="text-center mt-5">Staff Management</h2>

    <div class="row mb-3">
        <div class="col-lg-9">
            <form class="form-inline" action="staff-list" method="get">
                <input type="hidden" name="page" value="1">
                <div class="form-group mr-2 mb-2">
                    <input type="text" class="form-control" name="searchName" placeholder="Name" value="${fn:escapeXml(param.searchName)}" maxlength="100">
                </div>
                <div class="form-group mr-2 mb-2">
                    <input type="text" class="form-control" name="employeeCode" placeholder="Employee code" value="${fn:escapeXml(param.employeeCode)}" maxlength="50">
                </div>
                <div class="form-group mr-2 mb-2">
                    <input type="text" class="form-control" name="department" placeholder="Department" value="${fn:escapeXml(param.department)}" maxlength="100">
                </div>
                <div class="form-group mr-2 mb-2">
                    <select class="form-control" name="searchStatus">
                        <option value="">All Statuses</option>
                        <option value="true" ${param.searchStatus == 'true' ? 'selected' : ''}>Active</option>
                        <option value="false" ${param.searchStatus == 'false' ? 'selected' : ''}>Inactive</option>
                    </select>
                </div>
                <div class="form-group mr-2 mb-2">
                    <select class="form-control" name="pageSize">
                        <option value="5" ${pageSize == 5 ? 'selected' : ''}>5/page</option>
                        <option value="10" ${pageSize == 10 ? 'selected' : ''}>10/page</option>
                        <option value="20" ${pageSize == 20 ? 'selected' : ''}>20/page</option>
                    </select>
                </div>
                <button type="submit" class="btn btn-primary mb-2">Filter</button>
            </form>
        </div>
        <div class="col-lg-3 text-right">
            <c:if test="${isAdmin}">
                <a href="staff-crud?action=create" class="btn btn-success">Add New Staff</a>
            </c:if>
            <a href="logout" class="btn btn-outline-secondary ml-2">Logout</a>
        </div>
    </div>

    <div class="mb-2 text-muted">
        Total staff: <c:out value="${totalItems}" />
    </div>

    <table class="table table-bordered table-sm">
        <thead>
        <tr>
            <th>ID</th>
            <th>Employee Code</th>
            <th>Full Name</th>
            <th>Gender</th>
            <th>Phone</th>
            <th>Email</th>
            <th>Department</th>
            <th>Position</th>
            <th>Role</th>
            <th>Status</th>
            <th>Actions</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="staff" items="${staffList}">
            <tr>
                <td><c:out value="${staff.staffID}" /></td>
                <td><c:out value="${staff.employeeCode}" /></td>
                <td><c:out value="${staff.fullName}" /></td>
                <td>${staff.gender ? 'Male' : 'Female'}</td>
                <td><c:out value="${staff.phoneNumber}" /></td>
                <td><c:out value="${staff.email}" /></td>
                <td><c:out value="${staff.department}" /></td>
                <td><c:out value="${staff.position}" /></td>
                <td><c:out value="${staff.role.roleName}" /></td>
                <td>${staff.isActive ? 'Active' : 'Inactive'}</td>
                <td>
                    <a href="staff-detail?id=${staff.staffID}" class="btn btn-sm btn-info">View</a>
                    <c:if test="${isAdmin}">
                        <a href="staff-crud?action=edit&id=${staff.staffID}" class="btn btn-sm btn-warning">Edit</a>
                        <a href="staff-crud?action=delete&id=${staff.staffID}" class="btn btn-sm btn-danger" onclick="return confirm('Are you sure?')">Delete</a>
                    </c:if>
                </td>
            </tr>
        </c:forEach>
        <c:if test="${empty staffList}">
            <tr>
                <td colspan="11" class="text-center text-muted">No staff found.</td>
            </tr>
        </c:if>
        </tbody>
    </table>

    <c:if test="${totalPages > 1}">
        <nav aria-label="Staff pagination">
            <ul class="pagination justify-content-center">
                <li class="page-item ${page == 1 ? 'disabled' : ''}">
                    <c:url var="previousUrl" value="staff-list">
                        <c:param name="searchName" value="${param.searchName}" />
                        <c:param name="employeeCode" value="${param.employeeCode}" />
                        <c:param name="department" value="${param.department}" />
                        <c:param name="searchStatus" value="${param.searchStatus}" />
                        <c:param name="pageSize" value="${pageSize}" />
                        <c:param name="page" value="${page - 1}" />
                    </c:url>
                    <a class="page-link" href="${previousUrl}">Previous</a>
                </li>

                <c:forEach begin="1" end="${totalPages}" var="pageNumber">
                    <c:url var="pageUrl" value="staff-list">
                        <c:param name="searchName" value="${param.searchName}" />
                        <c:param name="employeeCode" value="${param.employeeCode}" />
                        <c:param name="department" value="${param.department}" />
                        <c:param name="searchStatus" value="${param.searchStatus}" />
                        <c:param name="pageSize" value="${pageSize}" />
                        <c:param name="page" value="${pageNumber}" />
                    </c:url>
                    <li class="page-item ${page == pageNumber ? 'active' : ''}">
                        <a class="page-link" href="${pageUrl}"><c:out value="${pageNumber}" /></a>
                    </li>
                </c:forEach>

                <li class="page-item ${page == totalPages ? 'disabled' : ''}">
                    <c:url var="nextUrl" value="staff-list">
                        <c:param name="searchName" value="${param.searchName}" />
                        <c:param name="employeeCode" value="${param.employeeCode}" />
                        <c:param name="department" value="${param.department}" />
                        <c:param name="searchStatus" value="${param.searchStatus}" />
                        <c:param name="pageSize" value="${pageSize}" />
                        <c:param name="page" value="${page + 1}" />
                    </c:url>
                    <a class="page-link" href="${nextUrl}">Next</a>
                </li>
            </ul>
        </nav>
    </c:if>
</div>
</body>
</html>

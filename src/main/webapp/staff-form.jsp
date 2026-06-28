<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<html>
<head>
    <title>${empty staff ? 'Add Staff' : 'Edit Staff'}</title>
    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css">
</head>
<body>
<div class="container">
    <c:set var="isAdmin" value="${not empty sessionScope.user and sessionScope.user.role.roleName == 'Admin'}" />
    <h2 class="text-center mt-5">${empty staff ? 'Add New Staff' : 'Edit Staff'}</h2>

    <c:if test="${not isAdmin}">
        <div class="alert alert-danger text-center" role="alert">
            You do not have permission to access this page.
        </div>
        <div class="text-center">
            <a href="staff-list" class="btn btn-secondary">Back to Staff List</a>
        </div>
    </c:if>

    <c:if test="${isAdmin}">
    <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger text-center" role="alert">
            <c:out value="${errorMessage}" />
        </div>
    </c:if>

    <form action="staff-crud" method="post">
        <input type="hidden" name="action" value="${empty staff ? 'create' : 'update'}">
        <c:if test="${not empty staff}">
            <input type="hidden" name="staffID" value="${staff.staffID}">
        </c:if>

        <div class="form-row">
            <div class="form-group col-md-6">
                <label for="employeeCode">Employee Code</label>
                <input type="text" class="form-control" id="employeeCode" name="employeeCode"
                       value="${fn:escapeXml(staff.employeeCode)}" required maxlength="50"
                       pattern="[A-Za-z0-9-]{3,50}" title="Use 3-50 letters, numbers, or hyphens.">
            </div>
            <div class="form-group col-md-6">
                <label for="fullName">Full Name</label>
                <input type="text" class="form-control" id="fullName" name="fullName" value="${fn:escapeXml(staff.fullName)}" required maxlength="100">
            </div>
        </div>

        <div class="form-row">
            <div class="form-group col-md-6">
                <label>Gender</label><br>
                <div class="form-check form-check-inline">
                    <input class="form-check-input" type="radio" name="gender" id="male" value="true" ${staff.gender ? 'checked' : ''} required>
                    <label class="form-check-label" for="male">Male</label>
                </div>
                <div class="form-check form-check-inline">
                    <input class="form-check-input" type="radio" name="gender" id="female" value="false" ${!staff.gender ? 'checked' : ''}>
                    <label class="form-check-label" for="female">Female</label>
                </div>
            </div>
            <div class="form-group col-md-6">
                <label for="dateOfBirth">Date of Birth</label>
                <input type="date" class="form-control" id="dateOfBirth" name="dateOfBirth" value="${fn:escapeXml(staff.dateOfBirth)}" required>
            </div>
        </div>

        <div class="form-row">
            <div class="form-group col-md-6">
                <label for="phoneNumber">Phone Number</label>
                <input type="text" class="form-control" id="phoneNumber" name="phoneNumber" value="${fn:escapeXml(staff.phoneNumber)}"
                       required maxlength="10" pattern="0[0-9]{9}"
                       title="Phone number must be 10 digits and start with 0.">
            </div>
            <div class="form-group col-md-6">
                <label for="email">Email</label>
                <input type="email" class="form-control" id="email" name="email" value="${fn:escapeXml(staff.email)}" required maxlength="100">
            </div>
        </div>

        <c:if test="${empty staff || staff.staffID == 0}">
            <div class="form-group">
                <label for="password">Password</label>
                <input type="password" class="form-control" id="password" name="password" required maxlength="50">
            </div>
        </c:if>

        <div class="form-row">
            <div class="form-group col-md-6">
                <label for="department">Department</label>
                <input type="text" class="form-control" id="department" name="department" value="${fn:escapeXml(staff.department)}" required maxlength="100">
            </div>
            <div class="form-group col-md-6">
                <label for="position">Position</label>
                <input type="text" class="form-control" id="position" name="position" value="${fn:escapeXml(staff.position)}" required maxlength="100">
            </div>
        </div>

        <div class="form-row">
            <div class="form-group col-md-6">
                <label for="salary">Salary</label>
                <input type="number" class="form-control" id="salary" name="salary" value="${fn:escapeXml(staff.salary)}" required min="0" step="0.01">
            </div>
            <div class="form-group col-md-6">
                <label for="hireDate">Hire Date</label>
                <input type="date" class="form-control" id="hireDate" name="hireDate" value="${fn:escapeXml(staff.hireDate)}" required>
            </div>
        </div>

        <div class="form-row">
            <div class="form-group col-md-6">
                <label for="roleID">Role</label>
                <select class="form-control" id="roleID" name="roleID" required>
                    <c:forEach var="role" items="${roleList}">
                        <option value="${role.roleID}" ${staff.role.roleID == role.roleID ? 'selected' : ''}>
                            <c:out value="${role.roleName}" />
                        </option>
                    </c:forEach>
                </select>
            </div>
            <div class="form-group col-md-6">
                <label>Status</label><br>
                <div class="form-check form-check-inline">
                    <input class="form-check-input" type="radio" name="isActive" id="active" value="true" ${staff.isActive ? 'checked' : ''} required>
                    <label class="form-check-label" for="active">Active</label>
                </div>
                <div class="form-check form-check-inline">
                    <input class="form-check-input" type="radio" name="isActive" id="inactive" value="false" ${!staff.isActive ? 'checked' : ''}>
                    <label class="form-check-label" for="inactive">Inactive</label>
                </div>
            </div>
        </div>

        <button type="submit" class="btn btn-primary">${empty staff ? 'Create' : 'Update'}</button>
        <a href="staff-list" class="btn btn-secondary">Cancel</a>
    </form>
    </c:if>
</div>
</body>
</html>

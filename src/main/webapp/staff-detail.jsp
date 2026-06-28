<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<head>
    <title>Staff Detail</title>
    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css">
</head>
<body>
<div class="container">
    <h2 class="text-center mt-5">Staff Detail</h2>

    <table class="table table-bordered">
        <tr><th>ID</th><td>${staff.staffID}</td></tr>
        <tr><th>Employee Code</th><td>${staff.employeeCode}</td></tr>
        <tr><th>Full Name</th><td>${staff.fullName}</td></tr>
        <tr><th>Gender</th><td>${staff.gender ? 'Male' : 'Female'}</td></tr>
        <tr><th>Date of Birth</th><td>${staff.dateOfBirth}</td></tr>
        <tr><th>Phone Number</th><td>${staff.phoneNumber}</td></tr>
        <tr><th>Email</th><td>${staff.email}</td></tr>
        <tr><th>Department</th><td>${staff.department}</td></tr>
        <tr><th>Position</th><td>${staff.position}</td></tr>
        <tr><th>Salary</th><td>${staff.salary}</td></tr>
        <tr><th>Hire Date</th><td>${staff.hireDate}</td></tr>
        <tr><th>Role</th><td>${staff.role.roleName}</td></tr>
        <tr><th>Status</th><td>${staff.isActive ? 'Active' : 'Inactive'}</td></tr>
    </table>

    <a href="staff-list" class="btn btn-secondary">Back to Staff List</a>
    <c:if test="${sessionScope.user.role.roleName == 'Admin'}">
        <a href="staff-crud?action=edit&id=${staff.staffID}" class="btn btn-warning">Edit</a>
    </c:if>
</div>
</body>
</html>

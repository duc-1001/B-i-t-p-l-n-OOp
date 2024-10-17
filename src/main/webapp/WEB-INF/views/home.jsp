<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %><html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Danh sách người dùng</title>
</head>
<body>
    <h1>Danh sách ${mess}</h1>
    <ul>
        <c:forEach var="user" items="${users}">
            <li>${user.getUsername()} - ${user.getEmail()}</li>
        </c:forEach>
    </ul>
</body>
</html>

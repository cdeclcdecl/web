<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head><meta charset="UTF-8"><title>Должности</title>
<style>body{font-family:Arial,sans-serif;margin:0;padding:0}table{border-collapse:collapse;width:100%}th,td{border:1px solid #ccc;padding:8px;text-align:left}th{background:#34495e;color:#fff}tr:hover{background:#f5f5f5}.btn{padding:6px 12px;text-decoration:none;border-radius:3px;border:none;cursor:pointer}.btn-primary{background:#3498db;color:#fff}.btn-danger{background:#e74c3c;color:#fff}form{display:inline}.content{padding:0 20px}input{padding:6px}.error{color:#e74c3c;background:#fdf2f2;padding:10px;border-radius:3px;margin-bottom:15px}</style>
</head>
<body>
<jsp:include page="/WEB-INF/jsp/layout/nav.jsp"/>
<div class="content">
<h2>Должности</h2>
<c:if test="${not empty error}"><div class="error">${error}</div></c:if>

<form method="post" action="${pageContext.request.contextPath}/positions">
  <input type="text" name="name" placeholder="Название должности" required/>
  <button type="submit" class="btn btn-primary">+ Добавить</button>
</form>
<br/><br/>
<table>
  <tr><th>ID</th><th>Название</th><th></th></tr>
  <c:forEach var="pos" items="${positions}">
  <tr>
    <td>${pos.positionId}</td>
    <td>${pos.name}</td>
    <td>
      <form method="post" action="${pageContext.request.contextPath}/positions/${pos.positionId}/delete">
        <button type="submit" class="btn btn-danger">Удалить</button>
      </form>
    </td>
  </tr>
  </c:forEach>
</table>
</div>
</body>
</html>

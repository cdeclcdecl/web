<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head><meta charset="UTF-8"><title>Сотрудник</title>
<style>body{font-family:Arial,sans-serif;margin:0;padding:0}label{display:block;margin-top:10px;font-weight:bold}input,select{padding:6px;width:300px;box-sizing:border-box}.btn{padding:8px 16px;border:none;border-radius:3px;cursor:pointer;text-decoration:none}.btn-primary{background:#3498db;color:#fff}.error{color:#e74c3c;background:#fdf2f2;padding:10px;border-radius:3px;margin-bottom:15px}.content{padding:0 20px}</style>
</head>
<body>
<jsp:include page="/WEB-INF/jsp/layout/nav.jsp"/>
<div class="content">
<h2><c:choose><c:when test="${employee.employeeId != null}">Изменить сотрудника</c:when><c:otherwise>Добавить сотрудника</c:otherwise></c:choose></h2>
<c:if test="${not empty error}"><div class="error">${error}</div></c:if>

<form method="post">
  <label>ФИО *</label>
  <input type="text" name="fullName" value="${employee.fullName}" required/>
  <label>Email</label>
  <input type="email" name="email" value="${employee.email}"/>
  <label>Телефон</label>
  <input type="text" name="phoneNumber" value="${employee.phoneNumber}"/>
  <label>Адрес</label>
  <input type="text" name="address" value="${employee.address}"/>
  <label>Дата рождения *</label>
  <input type="date" name="birthDate" value="${employee.birthDate}" required/>
  <label>Учёная степень</label>
  <input type="text" name="degree" value="${employee.degree}"/>
  <label>Дата найма *</label>
  <input type="date" name="hireDate" value="${employee.hireDate}" required/>
  <br/><br/>
  <button type="submit" class="btn btn-primary">Сохранить</button>
  <a href="${pageContext.request.contextPath}/employees" class="btn" style="background:#95a5a6;color:#fff;margin-left:10px">Отмена</a>
</form>
</div>
</body>
</html>

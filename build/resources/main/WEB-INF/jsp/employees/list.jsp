<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head><meta charset="UTF-8"><title>Сотрудники</title>
<style>body{font-family:Arial,sans-serif;margin:0;padding:0}table{border-collapse:collapse;width:100%}th,td{border:1px solid #ccc;padding:8px;text-align:left}th{background:#34495e;color:#fff}tr:hover{background:#f5f5f5}.btn{padding:6px 12px;text-decoration:none;border-radius:3px;border:none;cursor:pointer}.btn-primary{background:#3498db;color:#fff}.btn-danger{background:#e74c3c;color:#fff}.badge-active{background:#27ae60;color:#fff;padding:2px 8px;border-radius:10px}.badge-former{background:#7f8c8d;color:#fff;padding:2px 8px;border-radius:10px}form{display:block}.content{padding:0 20px}input,select{padding:6px;margin:0 8px 8px 0}</style>
</head>
<body>
<jsp:include page="/WEB-INF/jsp/layout/nav.jsp"/>
<div class="content">
<h2>Сотрудники</h2>
<form method="get" action="">
  <input type="text" name="search" value="${search}" placeholder="Поиск по ФИО"/>
  <input type="number" name="seniorityMin" value="${seniorityMin}" min="0" placeholder="Стаж от" style="width:110px"/>
  <input type="number" name="seniorityMax" value="${seniorityMax}" min="0" placeholder="Стаж до" style="width:110px"/>
  <select name="positionId">
    <option value="">Все должности</option>
    <c:forEach var="pos" items="${positions}">
    <option value="${pos.positionId}" ${positionId == pos.positionId ? 'selected' : ''}>${pos.name}</option>
    </c:forEach>
  </select>
  <select name="projectId">
    <option value="">Все проекты</option>
    <c:forEach var="project" items="${projects}">
    <option value="${project.projectId}" ${projectId == project.projectId ? 'selected' : ''}>${project.projectName}</option>
    </c:forEach>
  </select>
  <select name="status">
    <option value="all" ${status == 'all' ? 'selected' : ''}>Все</option>
    <option value="active" ${status == 'active' ? 'selected' : ''}>Действующие</option>
    <option value="former" ${status == 'former' ? 'selected' : ''}>Уволенные</option>
  </select>
  <select name="sort">
    <option value="nameAsc" ${sort == 'nameAsc' ? 'selected' : ''}>ФИО А-Я</option>
    <option value="nameDesc" ${sort == 'nameDesc' ? 'selected' : ''}>ФИО Я-А</option>
    <option value="hireDateAsc" ${sort == 'hireDateAsc' ? 'selected' : ''}>Дата найма ↑</option>
    <option value="hireDateDesc" ${sort == 'hireDateDesc' ? 'selected' : ''}>Дата найма ↓</option>
    <option value="birthDateAsc" ${sort == 'birthDateAsc' ? 'selected' : ''}>Дата рождения ↑</option>
    <option value="birthDateDesc" ${sort == 'birthDateDesc' ? 'selected' : ''}>Дата рождения ↓</option>
    <option value="positionAsc" ${sort == 'positionAsc' ? 'selected' : ''}>По должности</option>
    <option value="projectsDesc" ${sort == 'projectsDesc' ? 'selected' : ''}>По количеству проектов</option>
  </select>
  <button type="submit" class="btn btn-primary">Найти</button>
  <a href="${pageContext.request.contextPath}/employees" class="btn btn-primary">Сбросить</a>
  <a href="${pageContext.request.contextPath}/employees/new" class="btn btn-primary" style="margin-left:20px">+ Добавить</a>
</form>
<br/><br/>
<table>
  <tr><th>ID</th><th>ФИО</th><th>Текущая должность</th><th>Проектов</th><th>Email</th><th>Дата найма</th><th>Статус</th><th></th></tr>
  <c:forEach var="emp" items="${employees}">
  <tr>
    <td>${emp.employeeId}</td>
    <td><a href="${pageContext.request.contextPath}/employees/${emp.employeeId}">${emp.fullName}</a></td>
    <td>${currentPositions[emp.employeeId]}</td>
    <td>${projectCounts[emp.employeeId]}</td>
    <td>${emp.email}</td>
    <td>${emp.hireDate}</td>
    <td><c:choose><c:when test="${emp.active}"><span class="badge-active">Работает</span></c:when><c:otherwise><span class="badge-former">Уволен</span></c:otherwise></c:choose></td>
    <td><a href="${pageContext.request.contextPath}/employees/${emp.employeeId}/edit" class="btn btn-primary">Изменить</a></td>
  </tr>
  </c:forEach>
</table>
</div>
</body>
</html>

<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head><meta charset="UTF-8"><title>Проект</title>
<style>body{font-family:Arial,sans-serif;margin:0;padding:0}table{border-collapse:collapse;width:100%}th,td{border:1px solid #ccc;padding:8px;text-align:left}th{background:#34495e;color:#fff}.btn{padding:6px 12px;text-decoration:none;border-radius:3px;border:none;cursor:pointer}.btn-primary{background:#3498db;color:#fff}.btn-danger{background:#e74c3c;color:#fff}form{display:inline}.content{padding:0 20px}.info-row{margin-bottom:8px}.label{font-weight:bold;display:inline-block;width:150px}select,input{padding:6px}.row-archive{background:#f5f6f8;color:#555}.badge-active{background:#27ae60;color:#fff;padding:2px 8px;border-radius:10px}.badge-archive{background:#7f8c8d;color:#fff;padding:2px 8px;border-radius:10px}</style>
</head>
<body>
<jsp:include page="/WEB-INF/jsp/layout/nav.jsp"/>
<div class="content">
<h2>${project.projectName}</h2>
<p><a href="${pageContext.request.contextPath}/projects">&larr; Назад к списку</a></p>

<div class="info-row"><span class="label">ID:</span> ${project.projectId}</div>
<div class="info-row"><span class="label">Название:</span> ${project.projectName}</div>
<div class="info-row"><span class="label">Статус:</span> ${project.projectStatus}</div>
<div class="info-row"><span class="label">Начало:</span> ${project.startDate}</div>
<div class="info-row"><span class="label">Конец:</span> ${project.endDate}</div>

<br/>
<a href="${pageContext.request.contextPath}/projects/${project.projectId}/edit" class="btn btn-primary">Изменить</a>
<a href="${pageContext.request.contextPath}/policies?projectId=${project.projectId}" class="btn btn-primary" style="margin-left:10px">Политики выплат</a>

<h3 style="margin-top:30px">Текущие участники</h3>
<table>
  <tr><th>Сотрудник</th><th>Должность</th><th>Часов/нед</th><th>С</th><th></th></tr>
  <c:forEach var="a" items="${assignments}">
  <tr>
    <td><a href="${pageContext.request.contextPath}/employees/${a.employee.employeeId}">${a.employee.fullName}</a></td>
    <td>${a.position.name}</td>
    <td>${a.weeklyHours}</td>
    <td>${a.startDate}</td>
    <td>
      <form method="post" action="${pageContext.request.contextPath}/assignments/${a.assignmentId}/close">
        <input type="date" name="endDate" required/>
        <input type="hidden" name="projectId" value="${project.projectId}"/>
        <button type="submit" class="btn btn-danger">Завершить</button>
      </form>
    </td>
  </tr>
  </c:forEach>
</table>

<h3 style="margin-top:30px">Назначить сотрудника</h3>
<form method="post" action="${pageContext.request.contextPath}/assignments">
  <input type="hidden" name="projectId" value="${project.projectId}"/>
  <select name="employeeId" required>
    <option value="">-- Сотрудник --</option>
    <c:forEach var="emp" items="${employees}">
    <option value="${emp.employeeId}">${emp.fullName}</option>
    </c:forEach>
  </select>
  <select name="positionId" required>
    <option value="">-- Должность --</option>
    <c:forEach var="pos" items="${positions}">
    <option value="${pos.positionId}">${pos.name}</option>
    </c:forEach>
  </select>
  <input type="number" name="weeklyHours" value="40" min="1" max="80" style="width:70px"/> ч/нед
  <input type="date" name="startDate" required/>
  <button type="submit" class="btn btn-primary">Назначить</button>
</form>

<h3 style="margin-top:30px">Все назначения</h3>
<table>
  <tr><th>Сотрудник</th><th>Должность</th><th>Начало</th><th>Конец</th><th>Статус</th></tr>
  <c:forEach var="a" items="${allAssignments}">
  <tr class="${a.active ? '' : 'row-archive'}">
    <td>${a.employee.fullName}</td>
    <td>${a.position.name}</td>
    <td>${a.startDate}</td>
    <td>${a.endDate}</td>
    <td><c:choose><c:when test="${a.active}"><span class="badge-active">Активно</span></c:when><c:otherwise><span class="badge-archive">Архив</span></c:otherwise></c:choose></td>
  </tr>
  </c:forEach>
</table>
</div>
</body>
</html>

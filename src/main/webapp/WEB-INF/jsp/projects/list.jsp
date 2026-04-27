<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head><meta charset="UTF-8"><title>Проекты</title>
<style>body{font-family:Arial,sans-serif;margin:0;padding:0}table{border-collapse:collapse;width:100%}th,td{border:1px solid #ccc;padding:8px;text-align:left}th{background:#34495e;color:#fff}tr:hover{background:#f5f5f5}.btn{padding:6px 12px;text-decoration:none;border-radius:3px;border:none;cursor:pointer}.btn-primary{background:#3498db;color:#fff}form{display:block}.content{padding:0 20px}input,select{padding:6px;margin:0 8px 8px 0}</style>
</head>
<body>
<jsp:include page="/WEB-INF/jsp/layout/nav.jsp"/>
<div class="content">
<h2>Проекты</h2>
<form method="get" action="">
  <input type="text" name="search" value="${search}" placeholder="Поиск по названию"/>
  <input type="date" name="startFrom" value="${startFrom}"/>
  <input type="date" name="startTo" value="${startTo}"/>
  <input type="date" name="endFrom" value="${endFrom}"/>
  <input type="date" name="endTo" value="${endTo}"/>
  <select name="status">
    <option value="">Все статусы</option>
    <c:forEach var="s" items="${statuses}">
    <option value="${s}" ${status == s.name() ? 'selected' : ''}>${s}</option>
    </c:forEach>
  </select>
  <select name="sort">
    <option value="nameAsc" ${sort == 'nameAsc' ? 'selected' : ''}>Название А-Я</option>
    <option value="nameDesc" ${sort == 'nameDesc' ? 'selected' : ''}>Название Я-А</option>
    <option value="startDateAsc" ${sort == 'startDateAsc' ? 'selected' : ''}>Дата начала ↑</option>
    <option value="startDateDesc" ${sort == 'startDateDesc' ? 'selected' : ''}>Дата начала ↓</option>
    <option value="endDateAsc" ${sort == 'endDateAsc' ? 'selected' : ''}>Дата конца ↑</option>
    <option value="endDateDesc" ${sort == 'endDateDesc' ? 'selected' : ''}>Дата конца ↓</option>
  </select>
  <button type="submit" class="btn btn-primary">Фильтр</button>
  <a href="${pageContext.request.contextPath}/projects" class="btn btn-primary">Сбросить</a>
  <a href="${pageContext.request.contextPath}/projects/new" class="btn btn-primary" style="margin-left:20px">+ Добавить</a>
</form>
<br/><br/>
<table>
  <tr><th>ID</th><th>Название</th><th>Статус</th><th>Начало</th><th>Конец</th><th></th></tr>
  <c:forEach var="p" items="${projects}">
  <tr>
    <td>${p.projectId}</td>
    <td><a href="${pageContext.request.contextPath}/projects/${p.projectId}">${p.projectName}</a></td>
    <td>${p.projectStatus}</td>
    <td>${p.startDate}</td>
    <td>${p.endDate}</td>
    <td><a href="${pageContext.request.contextPath}/projects/${p.projectId}/edit" class="btn btn-primary">Изменить</a></td>
  </tr>
  </c:forEach>
</table>
</div>
</body>
</html>

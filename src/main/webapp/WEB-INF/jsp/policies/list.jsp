<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head><meta charset="UTF-8"><title>Политики выплат</title>
<style>body{font-family:Arial,sans-serif;margin:0;padding:0}table{border-collapse:collapse;width:100%}th,td{border:1px solid #ccc;padding:8px;text-align:left}th{background:#34495e;color:#fff}tr:hover{background:#f5f5f5}.btn{padding:6px 12px;text-decoration:none;border-radius:3px;border:none;cursor:pointer}.btn-primary{background:#3498db;color:#fff}.btn-danger{background:#e74c3c;color:#fff}.content{padding:0 20px}.filters input,.filters select{padding:6px;margin:0 8px 8px 0}.summary{display:grid;grid-template-columns:repeat(auto-fit,minmax(240px,1fr));gap:12px;margin:20px 0}.card{background:#f5f7fa;border-radius:8px;padding:14px}.error{color:#e74c3c;background:#fdf2f2;padding:10px;border-radius:3px;margin-bottom:15px}</style>
</head>
<body>
<jsp:include page="/WEB-INF/jsp/layout/nav.jsp"/>
<div class="content">
<h2>Политики выплат</h2>
<c:if test="${not empty error}"><div class="error">${error}</div></c:if>
<form method="get" class="filters">
  <select name="type">
    <option value="">Все типы</option>
    <c:forEach var="policyType" items="${policyTypes}">
    <option value="${policyType}" ${type == policyType.name() ? 'selected' : ''}>${policyType}</option>
    </c:forEach>
  </select>
  <input type="number" name="amountMin" value="${amountMin}" step="0.01" placeholder="Сумма от" style="width:120px"/>
  <input type="number" name="amountMax" value="${amountMax}" step="0.01" placeholder="Сумма до" style="width:120px"/>
  <select name="projectId">
    <option value="">Все проекты</option>
    <c:forEach var="project" items="${projects}">
    <option value="${project.projectId}" ${projectId == project.projectId ? 'selected' : ''}>${project.projectName}</option>
    </c:forEach>
  </select>
  <select name="positionId">
    <option value="">Все должности</option>
    <c:forEach var="position" items="${positions}">
    <option value="${position.positionId}" ${positionId == position.positionId ? 'selected' : ''}>${position.name}</option>
    </c:forEach>
  </select>
  <input type="number" name="minYears" value="${minYears}" min="0" placeholder="Стаж от" style="width:120px"/>
  <input type="text" name="event" value="${event}" placeholder="Событие или повод"/>
  <select name="sort">
    <option value="amountDesc" ${sort == 'amountDesc' ? 'selected' : ''}>Сумма ↓</option>
    <option value="amountAsc" ${sort == 'amountAsc' ? 'selected' : ''}>Сумма ↑</option>
    <option value="minYearsAsc" ${sort == 'minYearsAsc' ? 'selected' : ''}>Мин. стаж ↑</option>
    <option value="minYearsDesc" ${sort == 'minYearsDesc' ? 'selected' : ''}>Мин. стаж ↓</option>
  </select>
  <button type="submit" class="btn btn-primary">Применить</button>
  <a href="${pageContext.request.contextPath}/policies" class="btn btn-primary">Сбросить</a>
  <a href="${pageContext.request.contextPath}/policies/new" class="btn btn-primary">+ Добавить политику</a>
</form>
<div class="summary">
  <div class="card"><div>Проекты без salary-политики</div><strong>${missingProjects.size()}</strong><div><c:forEach var="project" items="${missingProjects}">${project.projectName}<br/></c:forEach></div></div>
  <div class="card"><div>Должности без salary-политики</div><strong>${missingPositions.size()}</strong><div><c:forEach var="position" items="${missingPositions}">${position.name}<br/></c:forEach></div></div>
</div>
<br/><br/>
<table>
  <tr><th>ID</th><th>Тип</th><th>Фиксированная</th><th>Сумма</th><th>Проект</th><th>Должность</th><th>Мин. стаж</th><th>Событие/повод</th><th>Описание</th><th>JSON</th><th></th></tr>
  <c:forEach var="pol" items="${policies}">
  <tr>
    <td>${pol.policyId}</td>
    <td>${pol.policyType}</td>
    <td>${pol.fixed ? 'Да' : 'Нет'}</td>
    <td>${pol.amount}</td>
    <td>${policyMeta[pol.policyId].projectName}</td>
    <td>${policyMeta[pol.policyId].positionName}</td>
    <td>${policyMeta[pol.policyId].minYears}</td>
    <td>${policyMeta[pol.policyId].event}</td>
    <td>${policyMeta[pol.policyId].description}</td>
    <td>${policyMeta[pol.policyId].info}</td>
    <td>
      <a href="${pageContext.request.contextPath}/policies/${pol.policyId}/edit" class="btn btn-primary">Изменить</a>
      <form method="post" action="${pageContext.request.contextPath}/policies/${pol.policyId}/delete" style="display:inline">
        <button type="submit" class="btn btn-danger">Удалить</button>
      </form>
    </td>
  </tr>
  </c:forEach>
</table>
</div>
</body>
</html>

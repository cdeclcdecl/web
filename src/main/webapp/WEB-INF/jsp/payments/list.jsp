<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head><meta charset="UTF-8"><title>Выплаты</title>
<style>body{font-family:Arial,sans-serif;margin:0;padding:0}table{border-collapse:collapse;width:100%}th,td{border:1px solid #ccc;padding:8px;text-align:left}th{background:#34495e;color:#fff}tr:hover{background:#f5f5f5}.btn{padding:6px 12px;text-decoration:none;border-radius:3px;border:none;cursor:pointer}.btn-primary{background:#3498db;color:#fff}.badge-paid{background:#27ae60;color:#fff;padding:2px 8px;border-radius:10px}.badge-unpaid{background:#e74c3c;color:#fff;padding:2px 8px;border-radius:10px}.content{padding:0 20px}.filters input,.filters select{padding:6px;margin:0 8px 8px 0}.summary{display:grid;grid-template-columns:repeat(auto-fit,minmax(170px,1fr));gap:12px;margin:20px 0}.card{background:#f5f7fa;border-radius:8px;padding:14px}</style>
<c:set var="printPath" value="${pageContext.request.contextPath}/payments/print?dateFrom=${dateFrom}&dateTo=${dateTo}&amountMin=${amountMin}&amountMax=${amountMax}&type=${type}&employeeId=${employeeId}&policyId=${policyId}&projectId=${projectId}&sort=${sort}"/>
<c:set var="exportPath" value="${pageContext.request.contextPath}/payments/export?dateFrom=${dateFrom}&dateTo=${dateTo}&amountMin=${amountMin}&amountMax=${amountMax}&type=${type}&employeeId=${employeeId}&policyId=${policyId}&projectId=${projectId}&sort=${sort}"/>
</head>
<body>
<c:if test="${not printMode}"><jsp:include page="/WEB-INF/jsp/layout/nav.jsp"/></c:if>
<div class="content">
<h2>${reportTitle}</h2>
<c:if test="${not printMode}">
<form method="get" class="filters">
  <input type="date" name="dateFrom" value="${dateFrom}"/>
  <input type="date" name="dateTo" value="${dateTo}"/>
  <input type="number" name="amountMin" value="${amountMin}" step="0.01" placeholder="Сумма от" style="width:120px"/>
  <input type="number" name="amountMax" value="${amountMax}" step="0.01" placeholder="Сумма до" style="width:120px"/>
  <select name="type">
    <option value="">Все типы</option>
    <c:forEach var="policyType" items="${policyTypes}">
    <option value="${policyType}" ${type == policyType.name() ? 'selected' : ''}>${policyType}</option>
    </c:forEach>
  </select>
  <select name="employeeId">
    <option value="">Все сотрудники</option>
    <c:forEach var="employee" items="${employees}">
    <option value="${employee.employeeId}" ${employeeId == employee.employeeId ? 'selected' : ''}>${employee.fullName}</option>
    </c:forEach>
  </select>
  <select name="projectId">
    <option value="">Все проекты</option>
    <c:forEach var="project" items="${projects}">
    <option value="${project.projectId}" ${projectId == project.projectId ? 'selected' : ''}>${project.projectName}</option>
    </c:forEach>
  </select>
  <select name="policyId">
    <option value="">Все политики</option>
    <c:forEach var="policy" items="${policies}">
    <option value="${policy.policyId}" ${policyId == policy.policyId ? 'selected' : ''}>${policy.policyType} #${policy.policyId}</option>
    </c:forEach>
  </select>
  <select name="sort">
    <option value="dateDesc" ${sort == 'dateDesc' ? 'selected' : ''}>Дата выплаты ↓</option>
    <option value="dateAsc" ${sort == 'dateAsc' ? 'selected' : ''}>Дата выплаты ↑</option>
    <option value="amountDesc" ${sort == 'amountDesc' ? 'selected' : ''}>Сумма ↓</option>
    <option value="amountAsc" ${sort == 'amountAsc' ? 'selected' : ''}>Сумма ↑</option>
    <option value="employeeAsc" ${sort == 'employeeAsc' ? 'selected' : ''}>ФИО А-Я</option>
    <option value="employeeDesc" ${sort == 'employeeDesc' ? 'selected' : ''}>ФИО Я-А</option>
  </select>
  <button type="submit" class="btn btn-primary">Применить</button>
  <a href="${pageContext.request.contextPath}/payments" class="btn btn-primary">Сбросить</a>
  <a href="${pageContext.request.contextPath}/payments/new" class="btn btn-primary">+ Добавить выплату</a>
  <a href="${exportPath}" class="btn btn-primary">Экспорт CSV</a>
  <a href="${printPath}" class="btn btn-primary" target="_blank">Печать</a>
</form>
</c:if>
<div class="summary">
  <div class="card"><div>Всего начислений</div><strong>${payments.size()}</strong></div>
  <div class="card"><div>Сумма</div><strong>${totalAmount}</strong></div>
  <div class="card"><div>Зарплата</div><strong>${salaryTotal}</strong></div>
  <div class="card"><div>За стаж</div><strong>${seniorityTotal}</strong></div>
  <div class="card"><div>Календарные премии</div><strong>${calendarBonusTotal}</strong></div>
  <div class="card"><div>Разовые бонусы</div><strong>${oneTimeBonusTotal}</strong></div>
  <div class="card"><div>Невыплачено</div><strong>${unpaidCount}</strong></div>
  <c:if test="${employeeId != null}"><div class="card"><div>Среднемесячно</div><strong>${averageMonthly}</strong></div></c:if>
</div>
<br/><br/>
<table>
  <tr><th>ID</th><th>Сотрудник</th><th>Проект</th><th>Должность</th><th>Тип</th><th>Политика</th><th>Сумма</th><th>Причина/повод</th><th>Период начала</th><th>Период конца</th><th>Дата выплаты</th><th>Статус</th><th></th></tr>
  <c:forEach var="p" items="${payments}">
  <tr>
    <td>${p.paymentId}</td>
    <td>${paymentMeta[p.paymentId].employeeName}</td>
    <td>${paymentMeta[p.paymentId].projectName}</td>
    <td>${paymentMeta[p.paymentId].positionName}</td>
    <td>${paymentMeta[p.paymentId].typeLabel}</td>
    <td>${paymentMeta[p.paymentId].policyLabel}</td>
    <td>${p.amount}</td>
    <td>${paymentMeta[p.paymentId].reason}</td>
    <td>${p.periodStart}</td>
    <td>${p.periodEnd}</td>
    <td>${p.paymentDate}</td>
    <td><c:choose><c:when test="${p.transactioned}"><span class="badge-paid">Выплачено</span></c:when><c:otherwise><span class="badge-unpaid">Не выплачено</span></c:otherwise></c:choose></td>
    <td><c:if test="${not printMode}"><a href="${pageContext.request.contextPath}/payments/${p.paymentId}/edit" class="btn btn-primary">Изменить</a></c:if></td>
  </tr>
  </c:forEach>
</table>
</div>
<c:if test="${printMode}"><script>window.print()</script></c:if>
</body>
</html>

<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head><meta charset="UTF-8"><title>Главная</title>
<style>body{font-family:Arial,sans-serif;margin:0;padding:0;background:#f3f5f8}.content{padding:0 20px 30px}.hero{background:#1f3c5a;color:#fff;padding:28px 20px;margin-bottom:24px}.grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(220px,1fr));gap:16px;margin-bottom:24px}.card{background:#fff;border-radius:8px;padding:18px;box-shadow:0 8px 24px rgba(31,60,90,.08)}.value{font-size:28px;font-weight:bold;margin:10px 0}.links a{display:inline-block;margin:0 12px 12px 0;padding:10px 16px;background:#3498db;color:#fff;border-radius:6px;text-decoration:none}</style>
</head>
<body>
<jsp:include page="/WEB-INF/jsp/layout/nav.jsp"/>
<div class="hero">
  <h1 style="margin:0 0 8px">Зарплатная ведомость</h1>
  <div>Главная панель для сотрудников, проектов, начислений и политик выплат.</div>
</div>
<div class="content">
  <div class="grid">
    <div class="card"><div>Сотрудники</div><div class="value">${employeesTotal}</div><div>Активных: ${activeEmployees}</div></div>
    <div class="card"><div>Проекты</div><div class="value">${projectsTotal}</div><div>Активных: ${activeProjects}</div></div>
    <div class="card"><div>Невыплаченные начисления</div><div class="value">${unpaidPayments}</div><div>Требуют контроля</div></div>
    <div class="card"><div>Политики выплат</div><div class="value">${policiesTotal}</div><div>Доступно для проверки</div></div>
  </div>
  <div class="links">
    <a href="${pageContext.request.contextPath}/employees">Список сотрудников</a>
    <a href="${pageContext.request.contextPath}/projects">Список проектов</a>
    <a href="${pageContext.request.contextPath}/payments">История начислений</a>
    <a href="${pageContext.request.contextPath}/policies">Политики выплат</a>
    <a href="${pageContext.request.contextPath}/payments?type=salary">Отчёт по зарплате</a>
    <a href="${pageContext.request.contextPath}/payments?type=calendar_bonus">Отчёт по премиям</a>
  </div>
</div>
</body>
</html>
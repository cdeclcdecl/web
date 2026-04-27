<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head><meta charset="UTF-8"><title>Выплата</title>
<style>body{font-family:Arial,sans-serif;margin:0;padding:0}label{display:block;margin-top:10px;font-weight:bold}input,select{padding:6px;width:300px;box-sizing:border-box}.btn{padding:8px 16px;border:none;border-radius:3px;cursor:pointer;text-decoration:none}.btn-primary{background:#3498db;color:#fff}.content{padding:0 20px}</style>
</head>
<body>
<jsp:include page="/WEB-INF/jsp/layout/nav.jsp"/>
<div class="content">
<h2><c:choose><c:when test="${payment.paymentId != null}">Изменить выплату</c:when><c:otherwise>Добавить выплату</c:otherwise></c:choose></h2>
<c:if test="${not empty error}"><div style="color:#e74c3c;background:#fdf2f2;padding:10px;border-radius:3px;margin-bottom:15px">${error}</div></c:if>

<form method="post">
  <label>Сотрудник</label>
  <select name="employeeId" id="employeeSelect" onchange="loadAssignments(this.value)">
    <option value="">-- Выберите сотрудника --</option>
    <c:forEach var="emp" items="${employees}">
    <option value="${emp.employeeId}" ${selectedEmployeeId == emp.employeeId ? 'selected' : ''}>${emp.fullName}</option>
    </c:forEach>
  </select>
  <label>Назначение</label>
  <select name="assignmentId" id="assignmentSelect">
    <option value="">-- Выберите назначение --</option>
  </select>
  <label>Политика выплаты</label>
  <select name="policyId">
    <option value="">-- Без политики --</option>
    <c:forEach var="pol" items="${policies}">
    <option value="${pol.policyId}" ${selectedPolicyId == pol.policyId ? 'selected' : ''}>${pol.policyType} — ${pol.amount}</option>
    </c:forEach>
  </select>
  <label>Сумма *</label>
  <input type="number" name="amount" step="0.01" min="0" value="${payment.amount}" required/>
  <label>Начало периода</label>
  <input type="date" name="periodStart" value="${payment.periodStart}"/>
  <label>Конец периода</label>
  <input type="date" name="periodEnd" value="${payment.periodEnd}"/>
  <label>Дата выплаты</label>
  <input type="date" name="paymentDate" value="${payment.paymentDate}"/>
  <label><input type="checkbox" name="transactioned" value="true" ${payment.transactioned ? 'checked' : ''}/> Выплачено</label>
  <br/><br/>
  <button type="submit" class="btn btn-primary">Сохранить</button>
  <a href="${pageContext.request.contextPath}/payments" class="btn" style="background:#95a5a6;color:#fff;margin-left:10px">Отмена</a>
</form>

<script>
const selectedAssignmentId = '${selectedAssignmentId}';
function loadAssignments(employeeId) {
  var sel = document.getElementById('assignmentSelect');
  sel.innerHTML = '<option value="">-- Выберите назначение --</option>';
  if (!employeeId) return;
  fetch('${pageContext.request.contextPath}/payments/new/assignments?employeeId=' + employeeId)
    .then(r => r.json())
    .then(data => data.forEach(a => {
      var opt = document.createElement('option');
      opt.value = a.assignmentId;
      opt.text = a.project.projectName + ' / ' + a.position.name;
      if (String(a.assignmentId) === String(selectedAssignmentId)) opt.selected = true;
      sel.appendChild(opt);
    }));
}
if (document.getElementById('employeeSelect').value) loadAssignments(document.getElementById('employeeSelect').value);
</script>
</div>
</body>
</html>

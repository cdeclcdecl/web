<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head><meta charset="UTF-8"><title>Политика выплат</title>
<style>body{font-family:Arial,sans-serif;margin:0;padding:0}label{display:block;margin-top:10px;font-weight:bold}input,select,textarea{padding:6px;width:300px;box-sizing:border-box}.btn{padding:8px 16px;border:none;border-radius:3px;cursor:pointer;text-decoration:none}.btn-primary{background:#3498db;color:#fff}.error{color:#e74c3c;background:#fdf2f2;padding:10px;border-radius:3px;margin-bottom:15px}.content{padding:0 20px}</style>
</head>
<body>
<jsp:include page="/WEB-INF/jsp/layout/nav.jsp"/>
<div class="content">
<h2><c:choose><c:when test="${policy.policyId != null}">Изменить политику выплат</c:when><c:otherwise>Добавить политику выплат</c:otherwise></c:choose></h2>
<c:if test="${not empty error}"><div class="error">${error}</div></c:if>

<form method="post">
  <label>Тип политики *</label>
  <select name="policyType" required>
    <c:forEach var="t" items="${policyTypes}">
    <option value="${t}" ${policy.policyType == t ? 'selected' : ''}>${t}</option>
    </c:forEach>
  </select>
  <label><input type="checkbox" name="fixed" value="true" ${policy.fixed ? 'checked' : ''}/> Фиксированная</label>
  <label>Сумма *</label>
  <input type="number" name="amount" step="0.01" min="0" value="${policy.amount}" required/>
  <label>Проект</label>
  <select name="projectId">
    <option value="">-- Не указан --</option>
    <c:forEach var="project" items="${projects}">
    <option value="${project.projectId}" ${selectedProjectId == project.projectId ? 'selected' : ''}>${project.projectName}</option>
    </c:forEach>
  </select>
  <label>Должность</label>
  <select name="positionId">
    <option value="">-- Не указана --</option>
    <c:forEach var="position" items="${positions}">
    <option value="${position.positionId}" ${selectedPositionId == position.positionId ? 'selected' : ''}>${position.name}</option>
    </c:forEach>
  </select>
  <label>Минимальный стаж</label>
  <input type="number" name="minYears" min="0" value="${selectedMinYears}"/>
  <label>Тип события</label>
  <input type="text" name="eventType" value="${selectedEventType}"/>
  <label>Повод</label>
  <input type="text" name="reason" value="${selectedReason}"/>
  <label>Описание</label>
  <textarea name="description" rows="3" style="width:300px">${selectedDescription}</textarea>
  <label>Текущее JSON</label>
  <textarea rows="3" style="width:300px" readonly>${rawInfo}</textarea>
  <br/><br/>
  <button type="submit" class="btn btn-primary">Сохранить</button>
  <a href="${pageContext.request.contextPath}/policies" class="btn" style="background:#95a5a6;color:#fff;margin-left:10px">Отмена</a>
</form>
</div>
</body>
</html>

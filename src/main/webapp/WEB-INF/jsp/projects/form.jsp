<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head><meta charset="UTF-8"><title>Проект</title>
<style>body{font-family:Arial,sans-serif;margin:0;padding:0}label{display:block;margin-top:10px;font-weight:bold}input,select{padding:6px;width:300px;box-sizing:border-box}.btn{padding:8px 16px;border:none;border-radius:3px;cursor:pointer;text-decoration:none}.btn-primary{background:#3498db;color:#fff}.error{color:#e74c3c;background:#fdf2f2;padding:10px;border-radius:3px;margin-bottom:15px}.content{padding:0 20px}</style>
</head>
<body>
<jsp:include page="/WEB-INF/jsp/layout/nav.jsp"/>
<div class="content">
<h2><c:choose><c:when test="${project.projectId != null}">Изменить проект</c:when><c:otherwise>Добавить проект</c:otherwise></c:choose></h2>
<c:if test="${not empty error}"><div class="error">${error}</div></c:if>

<form method="post">
  <label>Название *</label>
  <input type="text" name="projectName" value="${project.projectName}" required/>
  <label>Статус</label>
  <select name="projectStatus">
    <c:forEach var="s" items="${statuses}">
    <option value="${s}" ${project.projectStatus == s ? 'selected' : ''}>${s}</option>
    </c:forEach>
  </select>
  <label>Дата начала *</label>
  <input type="date" name="startDate" value="${project.startDate}" required/>
  <label>Дата окончания</label>
  <input type="date" name="endDate" value="${project.endDate}"/>
  <br/><br/>
  <button type="submit" class="btn btn-primary">Сохранить</button>
  <a href="${pageContext.request.contextPath}/projects" class="btn" style="background:#95a5a6;color:#fff;margin-left:10px">Отмена</a>
</form>
</div>
</body>
</html>

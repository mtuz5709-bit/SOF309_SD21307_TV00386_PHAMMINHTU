<%@ page contentType="text/html;charset=UTF-8" isELIgnored="false" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <title>Quản lý User – TiTanGEAR</title>
    <link href="https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@400;500;600;700&family=IBM+Plex+Mono:wght@400;500;600&display=swap" rel="stylesheet"/>
    <link rel="stylesheet" href="/static/css/tokens.css"/>
    <link rel="stylesheet" href="/static/css/main.css"/>
    <link rel="stylesheet" href="/static/css/admin.css"/>
</head>
<body>
<div class="admin-layout">
    <c:set var="adminActive" value="users"/>
    <%@ include file="/WEB-INF/views/common/admin-sidebar.jsp" %>

    <div class="admin-main">
        <c:set var="adminTitle" value="Quản lý Người dùng"/>
        <c:set var="adminCrumb" value="ADMIN / USERS"/>
        <c:set var="adminSearchUrl" value="/admin/users"/>
        <c:set var="adminSearchPlaceholder" value="Tìm theo tên hoặc email…"/>
        <c:set var="adminAction" value="+ Thêm tài khoản"/>
        <c:set var="adminActionUrl" value="/admin/users/add"/>
        <%@ include file="/WEB-INF/views/common/admin-topbar.jsp" %>

        <c:if test="${param.created == 'true'}">
            <div class="admin-alert-ok">Tạo tài khoản thành công!</div>
        </c:if>
        <c:if test="${not empty loi}">
            <div class="admin-alert-err">${loi}</div>
        </c:if>

        <table class="admin-table">
            <thead>
            <tr>
                <th>ID</th>
                <th>Họ tên</th>
                <th>Email</th>
                <th>Số điện thoại</th>
                <th>Role</th>
                <th>Trạng thái</th>
                <th>Ngày tạo</th>
                <th>Thao tác</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="u" items="${users}">
                <tr>
                    <td>${u.id}</td>
                    <td>${fn:escapeXml(u.fullName)}</td>
                    <td>${fn:escapeXml(u.email)}</td>
                    <td>${fn:escapeXml(u.phone)}</td>
                    <td>
                        <span class="role-badge role-${u.role}">${u.role}</span>
                    </td>
                    <td>
                        <c:choose>
                            <c:when test="${u.isActive}">
                                <span class="user-status-ok">Hoạt động</span>
                            </c:when>
                            <c:otherwise>
                                <span class="user-status-lock">Đã khóa</span>
                            </c:otherwise>
                        </c:choose>
                    </td>
                    <td>${u.createdAt != null ? u.createdAt.toString().substring(0,10) : ''}</td>
                    <td>
                        <form action="/admin/users/${u.id}/toggle" method="post" style="display:inline">
    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                            <button type="submit" class="${u.isActive ? 'btn-edit' : 'btn-primary'}"
                                    style="font-size:12px;padding:4px 10px;">
                                    ${u.isActive ? 'Khóa' : 'Mở khóa'}
                            </button>
                        </form>
                        <a href="/admin/users/${u.id}/delete" data-post
                           data-confirm="Xóa tài khoản này?"
                           class="btn-delete" style="font-size:12px;padding:4px 10px;">Xóa</a>
                    </td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </div>
</div>
</body>
</html>

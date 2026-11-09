<%@ page contentType="text/html;charset=UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <title>Thêm tài khoản – TiTanGEAR</title>
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
        <c:set var="adminTitle" value="Thêm tài khoản"/>
        <c:set var="adminCrumb" value="ADMIN / USERS / NEW"/>
        <%@ include file="/WEB-INF/views/common/admin-topbar.jsp" %>

        <div style="margin-bottom:16px;">
            <a href="/admin/users" class="btn-edit">← Quay lại danh sách</a>
        </div>

        <div class="admin-form">
            <c:if test="${not empty error}">
                <div class="admin-alert-err">${error}</div>
            </c:if>

            <form action="/admin/users/add" method="post">
    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                <div class="form-group">
                    <label>Họ và tên *</label>
                    <input type="text" name="fullName" required placeholder="Nhập họ tên"/>
                </div>
                <div class="form-group">
                    <label>Email *</label>
                    <input type="email" name="email" required placeholder="Nhập email"/>
                </div>
                <div class="form-group">
                    <label>Số điện thoại</label>
                    <input type="text" name="phone" placeholder="Nhập số điện thoại" pattern="[0-9]{10}"/>
                </div>
                <div class="form-group">
                    <label>Mật khẩu *</label>
                    <input type="password" name="password" required placeholder="Tối thiểu 6 ký tự" minlength="6"/>
                </div>
                <div class="form-group">
                    <label>Role *</label>
                    <select name="role" required>
                        <option value="">-- Chọn role --</option>
                        <option value="ADMIN">ADMIN</option>
                        <option value="WAREHOUSE">WAREHOUSE</option>
                        <option value="CUSTOMER">CUSTOMER</option>
                    </select>
                </div>
                <button type="submit" class="btn-primary">Tạo tài khoản</button>
            </form>
        </div>
    </div>
</div>
</body>
</html>

<%@ page contentType="text/html;charset=UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>Đặt Lại Mật Khẩu – TiTanGEAR</title>
    <link rel="preconnect" href="https://fonts.googleapis.com"/>
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin/>
    <link href="https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@400;500;600;700&family=IBM+Plex+Mono:wght@400;500;600&display=swap" rel="stylesheet"/>
    <link rel="stylesheet" href="/static/css/tokens.css"/>
    <link rel="stylesheet" href="/static/css/main.css"/>
    <link rel="stylesheet" href="/static/css/auth.css"/>
</head>
<body class="auth-page">

<div class="auth-container">

    <%@ include file="/WEB-INF/views/common/auth-side.jsp" %>

    <div class="auth-form-side">
        <div class="auth-form-wrap">

            <h1 class="auth-title">Đặt mật khẩu mới</h1>
            <p class="auth-sub">Cho tài khoản <strong>${email}</strong></p>

            <c:if test="${not empty error}">
                <div class="auth-error">${error}</div>
            </c:if>

            <form action="/quen-mat-khau/dat-lai" method="post">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                <div class="auth-field">
                    <label>Mật khẩu mới</label>
                    <input type="password" name="matKhau" placeholder="Ít nhất 6 ký tự"
                           minlength="6" required autofocus/>
                </div>
                <div class="auth-field">
                    <label>Nhập lại mật khẩu</label>
                    <input type="password" name="matKhauLai" placeholder="Gõ lại cho chắc"
                           minlength="6" required/>
                </div>
                <div class="auth-actions">
                    <button type="submit" class="btn-auth">Đổi mật khẩu</button>
                </div>
            </form>

            <p class="auth-switch">
                <a href="/login">← Quay lại đăng nhập</a>
            </p>

        </div>
    </div>
</div>

</body>
</html>

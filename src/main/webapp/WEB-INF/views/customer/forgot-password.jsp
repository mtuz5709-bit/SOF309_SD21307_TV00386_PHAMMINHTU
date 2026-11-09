<%@ page contentType="text/html;charset=UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>Quên Mật Khẩu – TiTanGEAR</title>
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

            <h1 class="auth-title">Quên mật khẩu</h1>
            <p class="auth-sub">Nhập email đăng ký, chúng tôi sẽ gửi mã xác nhận 6 số về hộp thư của bạn.</p>

            <c:if test="${not empty error}">
                <div class="auth-error">${error}</div>
            </c:if>

            <form action="/quen-mat-khau" method="post">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                <div class="auth-field">
                    <label>Email</label>
                    <input type="email" name="email" placeholder="email@cua.ban" required autofocus/>
                </div>
                <div class="auth-actions">
                    <button type="submit" class="btn-auth">Gửi mã xác nhận</button>
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

<%@ page contentType="text/html;charset=UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>Đăng Nhập – TiTanGEAR</title>
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

            <div class="auth-tabs">
                <a href="/login" class="auth-tab active">Đăng nhập</a>
                <a href="/register" class="auth-tab">Đăng ký</a>
            </div>

            <h1 class="auth-title">Đăng nhập</h1>
            <p class="auth-sub">Chào mừng trở lại với TiTanGEAR.</p>

            <c:if test="${param.error != null}">
                <div class="auth-error">Email hoặc mật khẩu không đúng!</div>
            </c:if>
            <c:if test="${param.locked != null}">
                <div class="auth-error">
                    Tài khoản của bạn đã bị khóa. Vui lòng liên hệ <strong>0900 000 011</strong> để được hỗ trợ.
                </div>
            </c:if>
            <c:if test="${param.logout != null}">
                <div class="auth-success">Đăng xuất thành công!</div>
            </c:if>
            <c:if test="${param.reset != null}">
                <div class="auth-success">Đổi mật khẩu thành công! Đăng nhập lại bằng mật khẩu mới nhé.</div>
            </c:if>

            <form method="post" action="/login">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>

                <div class="auth-field">
                    <label class="auth-field-label">Email</label>
                    <input type="text" name="username" placeholder="you@email.com"
                           class="auth-input" required autocomplete="email"/>
                </div>

                <div class="auth-field">
                    <label class="auth-field-label">Mật khẩu</label>
                    <input type="password" name="password" placeholder="••••••••"
                           class="auth-input" required autocomplete="current-password"/>
                </div>

                <div class="auth-actions">
                    <button type="submit" class="btn-auth">Đăng nhập</button>
                </div>
            </form>

            <p class="auth-switch" style="margin-top:14px;">
                <a href="/quen-mat-khau">Quên mật khẩu?</a>
            </p>
            <p class="auth-switch">
                Chưa có tài khoản?
                <a href="/register">Đăng ký ngay</a>
            </p>
        </div>
    </div>

</div>

</body>
</html>

<%@ page contentType="text/html;charset=UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>Đăng Ký – TiTanGEAR</title>
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
                <a href="/login" class="auth-tab">Đăng nhập</a>
                <a href="/register" class="auth-tab active">Đăng ký</a>
            </div>

            <h1 class="auth-title">Tạo tài khoản</h1>
            <p class="auth-sub">Tham gia cộng đồng TiTanGEAR.</p>

            <c:if test="${not empty error}">
                <div class="auth-error">${error}</div>
            </c:if>

            <form action="/register" method="post">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>

                <div class="auth-field">
                    <label class="auth-field-label">Họ và tên</label>
                    <input type="text" name="fullName" placeholder="Nguyễn Văn A"
                           class="auth-input" required minlength="2"
                           title="Họ tên phải có ít nhất 2 ký tự"/>
                </div>

                <div class="auth-field">
                    <label class="auth-field-label">Email</label>
                    <input type="email" name="email" placeholder="you@email.com"
                           class="auth-input" required
                           title="Email phải có dạng abc@gmail.com" autocomplete="email"/>
                </div>

                <div class="auth-field">
                    <label class="auth-field-label">Số điện thoại</label>
                    <input type="tel" name="phone" placeholder="0912 345 678"
                           class="auth-input" pattern="[0-9]{10}"
                           title="Số điện thoại phải đúng 10 chữ số" maxlength="10"/>
                </div>

                <div class="auth-field">
                    <label class="auth-field-label">Mật khẩu</label>
                    <input type="password" name="password" placeholder="Tối thiểu 6 ký tự"
                           class="auth-input" required minlength="6"
                           title="Mật khẩu phải có ít nhất 6 ký tự" autocomplete="new-password"/>
                </div>

                <div class="auth-actions">
                    <button type="submit" class="btn-auth">Đăng ký tài khoản</button>
                </div>
            </form>

            <p class="auth-switch">
                Đã có tài khoản?
                <a href="/login">Đăng nhập</a>
            </p>
        </div>
    </div>

</div>

</body>
</html>

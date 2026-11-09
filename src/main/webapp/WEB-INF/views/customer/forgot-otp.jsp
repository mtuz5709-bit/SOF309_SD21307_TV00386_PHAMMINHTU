<%@ page contentType="text/html;charset=UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>Xác Nhận Mã – TiTanGEAR</title>
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

            <h1 class="auth-title">Nhập mã xác nhận</h1>
            <p class="auth-sub">
                Nếu <strong>${email}</strong> có tài khoản tại TiTanGEAR,<br/>
                mã 6 số đã được gửi tới hộp thư đó.
            </p>

            <c:if test="${not empty error}">
                <div class="auth-error">${error}</div>
            </c:if>

            <form action="/quen-mat-khau/xac-nhan" method="post">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                <div class="auth-field">
                    <input type="text" name="otp" placeholder="000000"
                           class="auth-otp-input"
                           maxlength="6" pattern="[0-9]{6}" required autofocus/>
                </div>

                <p class="auth-countdown">
                    Mã hết hạn sau: <span id="countdown">5:00</span>
                </p>

                <div class="auth-actions">
                    <button type="submit" class="btn-auth">Xác nhận</button>
                </div>
            </form>

            <p class="auth-switch">
                <a href="/quen-mat-khau">← Nhập lại email</a>
            </p>

        </div>
    </div>
</div>

<script>
    let seconds = 300;
    const el = document.getElementById('countdown');
    if (el) {
        const timer = setInterval(() => {
            seconds--;
            if (seconds <= 0) {
                clearInterval(timer);
                el.textContent = '0:00';
                el.style.color = '#8A9099';
            } else {
                const m = Math.floor(seconds / 60);
                const s = seconds % 60;
                el.textContent = m + ':' + String(s).padStart(2, '0');
            }
        }, 1000);
    }
</script>

</body>
</html>

<%@ page contentType="text/html;charset=UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>Xác Nhận Email – TiTanGEAR</title>
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

            <h1 class="auth-title">Xác nhận email</h1>
            <p class="auth-sub">
                Mã xác nhận 6 số đã được gửi đến<br/>
                <strong>${email}</strong>
            </p>

            <c:if test="${not empty error}">
                <div class="auth-error">${error}</div>
            </c:if>

            <c:if test="${param.resent == 'true'}">
                <div class="auth-success">Đã gửi lại mã xác nhận!</div>
            </c:if>

            <c:choose>
                <c:when test="${expired != true}">
                    <!-- Form nhập OTP -->
                    <form action="/verify-otp" method="post">
                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                        <div class="auth-field">
                            <input type="text" name="otp" placeholder="000000"
                                   class="auth-otp-input"
                                   maxlength="6" pattern="[0-9]{6}" required autofocus/>
                        </div>

                        <!-- Countdown 5 phút -->
                        <p class="auth-countdown">
                            Mã hết hạn sau: <span id="countdown">5:00</span>
                        </p>

                        <div class="auth-actions">
                            <button type="submit" class="btn-auth">Xác nhận</button>
                        </div>
                    </form>

                    <!-- Gửi lại OTP -->
                    <form action="/resend-otp" method="post" class="auth-resend">
                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                        <button type="submit">Gửi lại mã xác nhận</button>
                    </form>
                </c:when>
                <c:otherwise>
                    <!-- OTP hết hạn -->
                    <div class="auth-actions">
                        <a href="/register" class="btn-auth" style="display:block;text-align:center;text-decoration:none;">Đăng ký lại</a>
                    </div>
                </c:otherwise>
            </c:choose>

            <p class="auth-switch">
                <a href="/register">← Quay lại đăng ký</a>
            </p>

        </div>
    </div>
</div>

<script>
    // Countdown 5 phút
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

<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>404 – Không tìm thấy trang</title>
    <link href="https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@400;500;600;700&family=IBM+Plex+Mono:wght@400;500;600&display=swap" rel="stylesheet"/>
    <link rel="stylesheet" href="/static/css/tokens.css"/>
    <link rel="stylesheet" href="/static/css/main.css"/>
</head>
<body>
<div class="err-page" style="--err-accent:#7A5CC7;--err-tint:#F1ECFB;">
    <div class="err-card">
        <div class="err-nav">
            <a href="/home"><img src="/static/images/logo-lockup.svg" alt="TiTanGEAR"/></a>
        </div>

        <div class="err-body">
            <span class="err-glow"></span>
            <div class="err-in">
                <div class="err-ico">
                    <svg width="34" height="34" viewBox="0 0 24 24" fill="none" stroke="#7A5CC7" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="7"/><path d="M16 16l5 5"/></svg>
                </div>
                <div class="err-code">404</div>
                <div class="err-tag">ERROR 404 — NOT FOUND</div>
                <h1 class="err-title">Không tìm thấy trang</h1>
                <p class="err-desc">Trang bạn tìm không tồn tại hoặc đã được di chuyển. Kiểm tra lại đường dẫn hoặc khám phá sản phẩm bên dưới.</p>
                <div class="err-acts">
                    <a href="/home" class="err-btn err-main">Về trang chủ</a>
                    <a href="/products" class="err-btn err-alt">Xem sản phẩm</a>
                </div>
                <div class="err-hint">
                    <span class="err-hint-l">GỢI Ý:</span>
                    <a href="/products?category=1" class="err-chip">Chuột Gaming</a>
                    <a href="/products?category=2" class="err-chip">Bàn Phím Cơ</a>
                    <a href="/products?category=3" class="err-chip">Tai Nghe</a>
                    <a href="/bao-hanh" class="err-chip">Tra cứu bảo hành</a>
                </div>
            </div>
        </div>

        <div class="err-foot">
            <span>© 2026 TiTanGEAR</span>
            <span class="tg-mono" style="font-family:var(--font-mono);">Cần hỗ trợ? 0900 000 011 · support@titangear.vn</span>
        </div>
    </div>
</div>
</body>
</html>

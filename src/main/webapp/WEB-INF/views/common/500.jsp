<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>500 – Lỗi hệ thống</title>
    <link href="https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@400;500;600;700&family=IBM+Plex+Mono:wght@400;500;600&display=swap" rel="stylesheet"/>
    <link rel="stylesheet" href="/static/css/tokens.css"/>
    <link rel="stylesheet" href="/static/css/main.css"/>
</head>
<body>
<div class="err-page" style="--err-accent:#C62828;--err-tint:#FDECEC;">
    <div class="err-card">
        <div class="err-nav">
            <a href="/home"><img src="/static/images/logo-lockup.svg" alt="TiTanGEAR"/></a>
        </div>

        <div class="err-body">
            <span class="err-glow"></span>
            <div class="err-in">
                <div class="err-ico">
                    <svg width="34" height="34" viewBox="0 0 24 24" fill="none" stroke="#C62828" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><rect x="2.5" y="4" width="19" height="7" rx="2"/><rect x="2.5" y="13" width="19" height="7" rx="2"/><path d="M6.5 7.5v.01M6.5 16.5v.01"/></svg>
                </div>
                <div class="err-code">500</div>
                <div class="err-tag">ERROR 500 — SERVER ERROR</div>
                <h1 class="err-title">Lỗi hệ thống</h1>
                <p class="err-desc">Máy chủ đang gặp sự cố khi xử lý yêu cầu. Đội kỹ thuật đã được thông báo — vui lòng thử lại sau ít phút.</p>
                <div class="err-acts">
                    <a href="/home" class="err-btn err-main">Về trang chủ</a>
                    <a href="/home" class="err-btn err-alt">Tải lại trang</a>
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

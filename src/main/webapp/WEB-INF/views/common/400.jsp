<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>400 – Yêu cầu không hợp lệ</title>
    <link href="https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@400;500;600;700&family=IBM+Plex+Mono:wght@400;500;600&display=swap" rel="stylesheet"/>
    <link rel="stylesheet" href="/static/css/tokens.css"/>
    <link rel="stylesheet" href="/static/css/main.css"/>
</head>
<body>
<div class="err-page" style="--err-accent:#E08600;--err-tint:#FFF4E2;">
    <div class="err-card">
        <div class="err-nav">
            <a href="/home"><img src="/static/images/logo-lockup.svg" alt="TiTanGEAR"/></a>
        </div>

        <div class="err-body">
            <span class="err-glow"></span>
            <div class="err-in">
                <div class="err-ico">
                    <svg width="34" height="34" viewBox="0 0 24 24" fill="none" stroke="#E08600" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M12 3 2 20h20L12 3Z"/><path d="M12 10v4M12 16.5v.5"/></svg>
                </div>
                <div class="err-code">400</div>
                <div class="err-tag">ERROR 400 — BAD REQUEST</div>
                <h1 class="err-title">Yêu cầu không hợp lệ</h1>
                <p class="err-desc">Dữ liệu gửi lên bị thiếu hoặc không đúng định dạng. Vui lòng quay lại và điền đầy đủ thông tin trong biểu mẫu.</p>
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

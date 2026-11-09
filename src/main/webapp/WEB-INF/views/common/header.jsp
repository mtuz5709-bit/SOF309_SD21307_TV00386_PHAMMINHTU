<%@ page contentType="text/html;charset=UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>${pageTitle != null ? pageTitle : 'TiTanGEAR – ATK Gaming Gear'}</title>
    <link rel="preconnect" href="https://fonts.googleapis.com"/>
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin/>
    <link href="https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@400;500;600;700&family=IBM+Plex+Mono:wght@400;500;600&display=swap" rel="stylesheet"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/tokens.css"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/main.css"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/tg.css"/>
    <c:if test="${not empty extraCss}">
        <style>${extraCss}</style>
    </c:if>
</head>
<body>

<%-- TICKER CHẠY NGANG — nội dung lặp 2 lần vì hiệu ứng marquee dịch -50%
     thì nửa sau phải giống hệt nửa đầu mới nối liền được, không giật. --%>
<div class="tg-ticker">
    <div class="tg-marq">
        <c:forEach begin="1" end="2">
            <%-- Ba câu cam kết luôn chạy trên thanh, không phụ thuộc có đợt giảm
                 giá hay không. Chỉ nêu những gì hệ thống làm được thật — tránh
                 hứa cứng kiểu "giảm tới 30%" rồi khách bấm vào chẳng thấy gì. --%>
            <span style="color:#fff;">MIỄN PHÍ VẬN CHUYỂN ĐƠN TỪ 500K</span>
            <span style="color:#7C9BFF;">BẢO HÀNH CHÍNH HÃNG 12 THÁNG</span>
            <%-- Thanh chỉ quảng cáo ba dịch vụ này, không có "đổi mới trong 7 ngày":
                 hệ thống chưa có luồng đổi/trả hàng nên không hứa dịch vụ đó. --%>
            <span style="color:#fff;">HÀNG CHÍNH HÃNG 100%</span>

            <%-- Nội dung admin gõ ở Flash Sale: CHẠY CÙNG 3 câu trên chứ không
                 thay thế, và bấm được để sang trang Flash Sale.
                 Không có đợt nào đang chạy -> vòng lặp không in gì, thanh chỉ
                 còn 3 câu cam kết. --%>
            <c:forEach var="bar" items="${activeFlashTextBars}">
                <a href="/flash-sale" class="tg-tick-sale">
                    <svg width="11" height="11" viewBox="0 0 16 16" fill="none" aria-hidden="true">
                        <path d="M9 1.5L3.5 8.5H7L6.5 14.5L12.5 7H8.5L9 1.5Z" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/>
                    </svg>
                    ${bar.textBarContent}
                </a>
            </c:forEach>
        </c:forEach>
    </div>
</div>

<!-- NAVBAR -->
<nav class="tg-nav-bar">
    <a href="/home">
        <img src="${pageContext.request.contextPath}/static/images/logo-lockup.svg" alt="TiTanGEAR"
             style="height:48px;display:block;"/>
    </a>
    <div class="tg-nav-links">
        <a href="/home"                class="tg-nav ${currentPage == 'home'       ? 'is-on' : ''}">Trang chủ</a>
        <a href="/products"            class="tg-nav ${currentPage == 'products'   ? 'is-on' : ''}">Tất cả sản phẩm</a>
        <a href="/flash-sale"          class="tg-nav ${currentPage == 'flash-sale' ? 'is-on' : ''}">Flash Sale</a>
        <a href="/combo"               class="tg-nav ${currentPage == 'combo'      ? 'is-on' : ''}">Combo</a>
        <a href="/products?category=1" class="tg-nav ${currentPage == 'mouse'      ? 'is-on' : ''}">Chuột</a>
        <a href="/products?category=2" class="tg-nav ${currentPage == 'keyboard'   ? 'is-on' : ''}">Bàn phím</a>
        <a href="/products?category=3" class="tg-nav ${currentPage == 'headset'    ? 'is-on' : ''}">Tai nghe</a>
    </div>
    <div class="nav-icons" style="display:flex;align-items:center;gap:10px;">
        <div class="search-wrap">
            <button type="button" class="tg-btn tg-icon-btn" onclick="toggleSearch()" aria-label="Tìm kiếm">
                <svg width="16" height="16" viewBox="0 0 16 16" fill="none"><circle cx="7" cy="7" r="5" stroke="currentColor" stroke-width="1.6"></circle><path d="M11 11L15 15" stroke="currentColor" stroke-width="1.6"></path></svg>
            </button>
            <form id="searchForm" action="/products" method="get" autocomplete="off" class="search-form"
                  style="display:none;">
                <input type="text" id="searchInput" name="keyword" placeholder="Tìm kiếm..." class="search-input"/>
                <button type="submit" class="search-btn" aria-label="Tìm">
                    <svg width="15" height="15" viewBox="0 0 16 16" fill="none"><circle cx="7" cy="7" r="5" stroke="currentColor" stroke-width="1.6"></circle><path d="M11 11L15 15" stroke="currentColor" stroke-width="1.6"></path></svg>
                </button>
            </form>
            <div id="searchDropdown" class="search-dropdown"></div>
        </div>
        <a href="/cart" class="tg-btn tg-icon-btn" title="Giỏ hàng">
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none"><path d="M2 3h2l1.2 8h7L14 5H5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"></path><circle cx="6.5" cy="14" r="0.9" fill="currentColor"></circle><circle cx="12" cy="14" r="0.9" fill="currentColor"></circle></svg>
            <%-- Giỏ trống thì không hiện số 0 cho đỡ rối mắt --%>
            <c:if test="${soGioHang > 0}">
                <span class="tg-badge">${soGioHang > 99 ? '99+' : soGioHang}</span>
            </c:if>
        </a>
        <c:choose>
            <c:when test="${pageContext.request.userPrincipal != null}">
                <%-- Đã đăng nhập: chữ cái đầu của TÊN trên nền màu riêng
                     (${me} do GlobalModelAdvice bơm vào mọi trang). --%>
                <c:choose>
                    <c:when test="${not empty me}">
                        <a href="/profile" class="tg-btn tg-icon-btn avatar-badge" title="${me.ten}"
                           style="background:${me.mau};border-color:${me.mau};">${me.chuCai}</a>
                    </c:when>
                    <c:otherwise>
                        <a href="/profile" class="tg-btn tg-icon-btn" title="Tài khoản">
                            <svg width="16" height="16" viewBox="0 0 16 16" fill="none"><circle cx="8" cy="5" r="3" stroke="currentColor" stroke-width="1.6"></circle><path d="M2 15c0-3 2.7-5 6-5s6 2 6 5" stroke="currentColor" stroke-width="1.6"></path></svg>
                        </a>
                    </c:otherwise>
                </c:choose>
            </c:when>
            <c:otherwise>
                <a href="/login" class="tg-btn tg-primary" style="padding:11px 20px;font-size:13px;border-radius:var(--r-md);">Đăng nhập</a>
            </c:otherwise>
        </c:choose>
    </div>
</nav>

<script>
    /* ===== SEARCH ===== */
    (function () {
        var dropdown = document.getElementById('searchDropdown');
        var lastQ = '';

        window.toggleSearch = function () {
            var form = document.getElementById('searchForm');
            var inp  = document.getElementById('searchInput');
            if (form.style.display === 'none' || form.style.display === '') {
                form.style.display = 'flex';
                inp.focus();
            } else {
                form.style.display = 'none';
                closeDropdown();
            }
        };

        document.addEventListener('input', function (e) {
            if (e.target.id !== 'searchInput') return;
            var q = e.target.value.trim();
            if (q === lastQ) return;
            lastQ = q;
            if (q.length < 1) { closeDropdown(); return; }
            fetch('/api/search?q=' + encodeURIComponent(q))
                .then(function (r) { return r.json(); })
                .then(function (d) { renderDropdown(d, q); })
                .catch(function () { closeDropdown(); });
        });

        function renderDropdown(items, q) {
            if (!items.length) {
                dropdown.innerHTML = '<div class="search-empty">Không tìm thấy sản phẩm nào</div>';
                dropdown.classList.add('open');
                return;
            }
            var html = items.map(function (p) {
                return '<a class="search-item" href="/products/' + p.id + '">'
                    + '<img class="search-item-img" src="/static/images/' + p.imageUrl + '" onerror="this.src=\'/static/images/banner.png\'">'
                    + '<span class="search-item-name">' + p.name + '</span>'
                    + '<span class="search-item-price">' + Number(p.price).toLocaleString('vi-VN') + 'đ</span>'
                    + (p.inStock ? '' : '<span class="search-item-out">Hết hàng</span>')
                    + '</a>';
            }).join('');
            html += '<a class="search-see-all" href="/products?keyword=' + encodeURIComponent(q) + '">Xem tất cả kết quả →</a>';
            dropdown.innerHTML = html;
            dropdown.classList.add('open');
        }

        function closeDropdown() {
            dropdown.classList.remove('open');
            dropdown.innerHTML = '';
        }

        document.addEventListener('click', function (e) {
            if (!e.target.closest('.search-wrap')) {
                closeDropdown();
                var f = document.getElementById('searchForm');
                if (f) f.style.display = 'none';
            }
        });
    })();
</script>

<%@ page contentType="text/html;charset=UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="TiTanGEAR – ATK Gaming Gear"/>
<c:set var="currentPage" value="home"/>
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<div class="tg-page">

<!-- ================= HERO ================= -->
<section id="hpHero" style="position:relative;overflow:hidden;background:var(--surface);border-bottom:1px solid var(--line-new);">
    <div style="position:absolute;top:-140px;right:6%;width:560px;height:560px;border-radius:50%;background:var(--accent-soft-2);filter:blur(20px);animation:tg-pulse 7s ease-in-out infinite;"></div>
    <div style="position:absolute;bottom:-180px;left:-80px;width:420px;height:420px;border-radius:50%;background:var(--violet-bg);filter:blur(30px);opacity:.7;"></div>

    <%-- Slide 0: hero cố định --%>
    <div class="hp-slide is-active">
        <div class="tg-hero-grid">
            <div class="tg-rise">
                <div style="display:inline-flex;align-items:center;gap:9px;background:var(--accent-soft-2);border:1px solid var(--accent-line);border-radius:var(--r-pill);padding:7px 16px;margin-bottom:26px;">
                    <span style="width:7px;height:7px;border-radius:50%;background:var(--accent);box-shadow:0 0 0 4px rgba(27,73,255,.18);"></span>
                    <span class="tg-mono" style="font-size:11px;letter-spacing:.12em;color:var(--accent);font-weight:600;">NHÀ PHÂN PHỐI ATK CHÍNH HÃNG</span>
                </div>
                <h1 style="font-size:66px;font-weight:700;line-height:1.05;letter-spacing:-.035em;margin:0 0 20px;">
                    Gear Up.<br>
                    <span style="background:linear-gradient(100deg,var(--accent),var(--violet));-webkit-background-clip:text;background-clip:text;color:transparent;">Game On.</span>
                </h1>
                <p style="font-size:16px;line-height:1.75;color:var(--ink-2);max-width:440px;margin:0 0 32px;">
                    Chuột, bàn phím, tai nghe hiệu năng cao — tuyển chọn cho những ai coi thiết bị ngoại vi là vũ khí, không phải phụ kiện.
                </p>
                <div style="display:flex;gap:12px;margin-bottom:40px;flex-wrap:wrap;">
                    <a href="/products" class="tg-btn tg-primary" style="border-radius:14px;padding:16px 34px;">Khám phá sản phẩm</a>
                    <a href="/flash-sale" class="tg-btn tg-ghost" style="border-radius:14px;padding:16px 30px;">Xem Flash Sale →</a>
                </div>
                <div style="display:flex;gap:34px;flex-wrap:wrap;">
                    <div>
                        <div style="font-size:26px;font-weight:700;letter-spacing:-.02em;color:var(--accent);">8000Hz</div>
                        <div style="font-size:12px;color:var(--ink-3);margin-top:2px;">Polling tối đa</div>
                    </div>
                    <div>
                        <div style="font-size:26px;font-weight:700;letter-spacing:-.02em;color:var(--violet);">49g</div>
                        <div style="font-size:12px;color:var(--ink-3);margin-top:2px;">Chuột nhẹ nhất</div>
                    </div>
                    <div>
                        <div style="font-size:26px;font-weight:700;letter-spacing:-.02em;color:var(--ok-2);">12 tháng</div>
                        <div style="font-size:12px;color:var(--ink-3);margin-top:2px;">Bảo hành chính hãng</div>
                    </div>
                </div>
            </div>

            <div class="tg-rise" style="position:relative;display:flex;align-items:center;justify-content:center;animation-delay:.12s;">
                <div style="position:absolute;width:400px;height:400px;border-radius:50%;background:radial-gradient(circle,#DDE6FF,transparent 68%);"></div>
                <img src="/static/images/atk_f1_white.png" alt="ATK Blazing Sky F1" class="tg-float"
                     onerror="this.onerror=null;this.src='/static/images/banner_f1.png';"
                     style="position:relative;max-width:470px;max-height:400px;object-fit:contain;filter:drop-shadow(0 30px 50px rgba(20,23,26,.2));"/>
                <div class="tg-card" style="position:absolute;top:14px;right:0;padding:14px 18px;border-radius:var(--r-lg);">
                    <div class="tg-mono" style="font-size:10px;letter-spacing:.12em;color:var(--ink-4);">TRỌNG LƯỢNG</div>
                    <div style="font-size:22px;font-weight:700;color:var(--accent);margin-top:2px;">55g</div>
                </div>
                <div class="tg-card" style="position:absolute;bottom:70px;left:0;padding:14px 18px;border-radius:var(--r-lg);">
                    <div class="tg-mono" style="font-size:10px;letter-spacing:.12em;color:var(--ink-4);">POLLING RATE</div>
                    <div style="font-size:22px;font-weight:700;color:var(--violet);margin-top:2px;">8000Hz</div>
                </div>
            </div>
        </div>
    </div>

    <%-- Slide 1 trở đi: banner của các đợt flash sale đang chạy. Tiêu đề, phụ đề,
         ảnh và nút bấm đều do admin đặt ở trang Flash Sale. --%>
    <c:forEach var="banner" items="${activeFlashBanners}">
        <div class="hp-slide">
            <div style="position:relative;min-height:520px;display:flex;align-items:center;justify-content:center;overflow:hidden;">
                <img src="/static/images/${banner.bannerImageUrl}" alt="${banner.bannerTitle}"
                     onerror="this.onerror=null;this.src='/static/images/banner.png';"
                     style="position:absolute;inset:0;width:100%;height:100%;object-fit:cover;"/>
                <div style="position:absolute;inset:0;background:linear-gradient(180deg,rgba(14,16,19,.25),rgba(14,16,19,.85));"></div>
                <div style="position:relative;text-align:center;color:#fff;padding:40px;max-width:760px;">
                    <span class="tg-mono" style="font-size:11px;letter-spacing:.18em;color:#7C9BFF;">FLASH SALE</span>
                    <h2 style="font-size:44px;font-weight:700;letter-spacing:-.03em;margin:12px 0 10px;">${banner.bannerTitle}</h2>
                    <c:if test="${not empty banner.bannerSubtitle}">
                        <p style="font-size:15px;color:rgba(255,255,255,.72);margin:0 0 26px;line-height:1.7;">${banner.bannerSubtitle}</p>
                    </c:if>
                    <div style="display:flex;gap:12px;justify-content:center;flex-wrap:wrap;">
                        <c:if test="${not empty banner.bannerCtaText && not empty banner.bannerCtaLink}">
                            <a href="${banner.bannerCtaLink}" class="tg-btn tg-primary" style="border-radius:14px;padding:15px 32px;">${banner.bannerCtaText}</a>
                        </c:if>
                        <a href="/flash-sale" class="tg-btn" style="background:rgba(255,255,255,.16);border:1px solid rgba(255,255,255,.3);color:#fff;border-radius:14px;padding:15px 30px;font-size:14px;font-weight:600;">Xem Flash Sale →</a>
                    </div>
                </div>
            </div>
        </div>
    </c:forEach>

    <%-- Chấm điều hướng: chỉ hiện khi có nhiều hơn 1 slide --%>
    <c:if test="${not empty activeFlashBanners}">
        <div style="position:absolute;bottom:22px;left:50%;transform:translateX(-50%);display:flex;gap:8px;z-index:3;">
            <button type="button" class="hp-hero-dot is-on" onclick="hpHeroGo(0)"></button>
            <c:forEach var="banner" items="${activeFlashBanners}" varStatus="loop">
                <button type="button" class="hp-hero-dot" onclick="hpHeroGo(${loop.index + 1})"></button>
            </c:forEach>
        </div>
    </c:if>
</section>

<!-- ================= DẢI CAM KẾT ================= -->
<section style="background:var(--surface);border-bottom:1px solid var(--line-new);padding:26px 60px;">
    <div class="tg-wrap tg-trust">
        <div style="display:flex;align-items:center;gap:14px;">
            <span style="width:38px;height:38px;flex-shrink:0;border-radius:var(--r-md);background:var(--accent-soft-2);display:inline-flex;align-items:center;justify-content:center;">
                <svg width="17" height="17" viewBox="0 0 16 16" fill="none" stroke="#1B49FF" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M2 11.5h9V4H2zM11 7h2.2L15 9.4v2.1h-4z"/><circle cx="4.5" cy="13" r="1.1"/><circle cx="12" cy="13" r="1.1"/></svg>
            </span>
            <div>
                <div style="font-size:14px;font-weight:600;">Freeship 500k+</div>
                <div style="font-size:12px;color:var(--ink-3);margin-top:2px;">Toàn quốc, 2–4 ngày</div>
            </div>
        </div>
        <div style="display:flex;align-items:center;gap:14px;">
            <span style="width:38px;height:38px;flex-shrink:0;border-radius:var(--r-md);background:var(--ok-bg-2);display:inline-flex;align-items:center;justify-content:center;">
                <svg width="17" height="17" viewBox="0 0 16 16" fill="none" stroke="#10A268" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M8 1.8 3 3.4v4c0 3 2.1 5.3 5 6.6 2.9-1.3 5-3.6 5-6.6v-4L8 1.8Z"/><path d="M5.9 7.7 7.4 9.2l2.9-2.9"/></svg>
            </span>
            <div>
                <div style="font-size:14px;font-weight:600;">Bảo hành 12 tháng</div>
                <div style="font-size:12px;color:var(--ink-3);margin-top:2px;">Chính hãng ATK</div>
            </div>
        </div>
        <div style="display:flex;align-items:center;gap:14px;">
            <span style="width:38px;height:38px;flex-shrink:0;border-radius:var(--r-md);background:var(--violet-bg);display:inline-flex;align-items:center;justify-content:center;">
                <svg width="17" height="17" viewBox="0 0 16 16" fill="none" stroke="#7A5CC7" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="8" cy="8" r="6"/><path d="M8 4.8V8l2.2 1.3"/></svg>
            </span>
            <div>
                <div style="font-size:14px;font-weight:600;">Đổi mới 7 ngày</div>
                <div style="font-size:12px;color:var(--ink-3);margin-top:2px;">Lỗi nhà sản xuất</div>
            </div>
        </div>
        <div style="display:flex;align-items:center;gap:14px;">
            <span style="width:38px;height:38px;flex-shrink:0;border-radius:var(--r-md);background:var(--warn-bg-2);display:inline-flex;align-items:center;justify-content:center;">
                <svg width="17" height="17" viewBox="0 0 16 16" fill="none" stroke="#E08600" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="4" width="12" height="8" rx="1.5"/><path d="M2 7h12"/><path d="M4.5 9.8h2"/></svg>
            </span>
            <div>
                <div style="font-size:14px;font-weight:600;">Trả góp 0%</div>
                <div style="font-size:12px;color:var(--ink-3);margin-top:2px;">Qua thẻ tín dụng</div>
            </div>
        </div>
    </div>
</section>

<!-- ================= LƯỚI DANH MỤC (BENTO) ================= -->
<section class="tg-sect">
    <div class="tg-wrap tg-bento">
        <a href="/products?category=1" class="tg-tile" style="grid-row:span 2;min-height:400px;">
            <img src="/static/images/banner_f1.png" alt="Chuột Gaming" style="opacity:.62;"
                 onerror="this.onerror=null;this.src='/static/images/banner.png';"/>
            <span class="tg-tile-veil" style="background:linear-gradient(180deg,rgba(14,16,19,.1) 40%,rgba(14,16,19,.92));"></span>
            <div class="tg-tile-body">
                <div class="tg-mono" style="font-size:10px;letter-spacing:.2em;color:#7C9BFF;margin-bottom:8px;">DANH MỤC 01</div>
                <div style="font-size:32px;font-weight:700;color:#fff;letter-spacing:-.02em;">Chuột Gaming</div>
                <div style="font-size:13px;color:rgba(255,255,255,.7);margin-top:6px;">Siêu nhẹ · Hall Effect · 8000Hz</div>
            </div>
        </a>
        <a href="/products?category=2" class="tg-tile" style="min-height:190px;">
            <img src="/static/images/banner_hex80.png" alt="Bàn Phím Cơ" style="opacity:.6;"
                 onerror="this.onerror=null;this.src='/static/images/banner.png';"/>
            <span class="tg-tile-veil" style="background:linear-gradient(180deg,transparent 30%,rgba(14,16,19,.9));"></span>
            <div class="tg-tile-body" style="padding:24px;">
                <div class="tg-mono" style="font-size:10px;letter-spacing:.2em;color:#C0A8F0;margin-bottom:6px;">DANH MỤC 02</div>
                <div style="font-size:23px;font-weight:700;color:#fff;">Bàn Phím Cơ</div>
            </div>
        </a>
        <a href="/products?category=3" class="tg-tile" style="min-height:190px;">
            <img src="/static/images/banner_zero.png" alt="Tai Nghe" style="opacity:.6;"
                 onerror="this.onerror=null;this.src='/static/images/banner.png';"/>
            <span class="tg-tile-veil" style="background:linear-gradient(180deg,transparent 30%,rgba(14,16,19,.9));"></span>
            <div class="tg-tile-body" style="padding:24px;">
                <div class="tg-mono" style="font-size:10px;letter-spacing:.2em;color:#F5C888;margin-bottom:6px;">DANH MỤC 03</div>
                <div style="font-size:23px;font-weight:700;color:#fff;">Tai Nghe</div>
            </div>
        </a>
        <a href="/flash-sale" class="tg-tile" style="grid-column:span 2;min-height:190px;background:linear-gradient(115deg,var(--accent),var(--accent-2));display:flex;align-items:center;justify-content:space-between;padding:30px 34px;color:#fff;">
            <div>
                <div class="tg-mono" style="font-size:10px;letter-spacing:.2em;color:rgba(255,255,255,.72);margin-bottom:8px;">ĐANG GIẢM GIÁ</div>
                <div style="font-size:26px;font-weight:700;letter-spacing:-.02em;line-height:1.25;">Flash Sale<br>giá tốt trong ngày</div>
            </div>
            <span class="tg-btn" style="background:rgba(255,255,255,.16);border:1px solid rgba(255,255,255,.3);border-radius:var(--r-md);padding:12px 22px;font-size:13px;font-weight:600;">Xem ngay →</span>
        </a>
    </div>
</section>

<!-- ================= SẢN PHẨM NỔI BẬT (tab) ================= -->
<c:if test="${not empty featured}">
<section class="tg-sect" style="padding-top:64px;">
    <div class="tg-wrap">
        <div style="display:flex;align-items:flex-end;justify-content:space-between;margin-bottom:22px;gap:20px;flex-wrap:wrap;">
            <div>
                <div class="tg-eye">FLAGSHIP 2026</div>
                <h2 class="tg-h2">Sản phẩm nổi bật</h2>
            </div>
            <div style="display:flex;gap:8px;flex-wrap:wrap;">
                <c:forEach var="p" items="${featured}" varStatus="loop">
                    <button type="button" class="tg-btn hp-fp-tab ${loop.first ? 'is-on' : ''}" data-idx="${loop.index}"
                            onclick="hpFp(${loop.index})">${p.name}</button>
                </c:forEach>
            </div>
        </div>

        <c:forEach var="p" items="${featured}" varStatus="loop">
            <div class="tg-card tg-rise hp-fp-panel ${loop.first ? 'is-active' : ''}" data-idx="${loop.index}"
                 style="grid-template-columns:1fr 1fr;overflow:hidden;align-items:stretch;">
                <div style="background:linear-gradient(150deg,#F7F9FF,var(--accent-soft-2));display:flex;align-items:center;justify-content:center;padding:44px;min-height:380px;position:relative;">
                    <div style="position:absolute;width:300px;height:300px;border-radius:50%;background:#fff;opacity:.55;filter:blur(10px);"></div>
                    <c:choose>
                        <c:when test="${not empty p.imageUrl}">
                            <img src="/static/images/${p.imageUrl}" alt="${p.name}"
                                 onerror="this.onerror=null;this.src='/static/images/banner.png';"
                                 style="position:relative;max-width:320px;max-height:300px;object-fit:contain;filter:drop-shadow(0 20px 34px rgba(20,23,26,.16));"/>
                        </c:when>
                        <c:otherwise>
                            <img src="/static/images/banner.png" alt="${p.name}" style="position:relative;max-width:320px;max-height:300px;object-fit:contain;"/>
                        </c:otherwise>
                    </c:choose>
                    <c:if test="${priceMap[p.id].hasSale}">
                        <span class="tg-mono" style="position:absolute;top:22px;left:22px;background:var(--accent);color:#fff;border-radius:var(--r-pill);font-size:10px;font-weight:600;letter-spacing:.1em;padding:6px 14px;">GIẢM ${priceMap[p.id].percentOff}%</span>
                    </c:if>
                </div>
                <div style="padding:44px 48px;display:flex;flex-direction:column;justify-content:center;">
                    <c:if test="${not empty p.category}">
                        <div class="tg-mono" style="font-size:11px;letter-spacing:.14em;color:var(--accent);font-weight:600;margin-bottom:10px;">${p.category.name}</div>
                    </c:if>
                    <h3 style="font-size:32px;font-weight:700;margin:0 0 12px;letter-spacing:-.025em;line-height:1.15;">${p.name}</h3>
                    <c:if test="${not empty p.description}">
                        <p style="font-size:14px;line-height:1.75;color:var(--ink-2);margin:0 0 24px;">${p.description}</p>
                    </c:if>
                    <c:if test="${not empty p.specs}">
                        <div style="display:grid;grid-template-columns:repeat(3,1fr);gap:10px;margin-bottom:26px;">
                            <c:forEach var="spec" items="${p.specs}" varStatus="s">
                                <c:if test="${s.index < 3}">
                                    <div style="background:var(--bg-page);border-radius:14px;padding:14px 16px;">
                                        <div class="tg-mono" style="font-size:10px;letter-spacing:.1em;color:var(--ink-4);">${spec.specName}</div>
                                        <div style="font-size:18px;font-weight:700;margin-top:4px;">${spec.specValue}</div>
                                    </div>
                                </c:if>
                            </c:forEach>
                        </div>
                    </c:if>
                    <div style="display:flex;align-items:center;gap:20px;flex-wrap:wrap;">
                        <div>
                            <div style="font-size:28px;font-weight:700;color:var(--accent);letter-spacing:-.02em;">
                                <c:choose>
                                    <c:when test="${priceMap[p.id].hasSale}"><fmt:formatNumber value="${priceMap[p.id].salePrice}" pattern="#,##0"/>đ</c:when>
                                    <c:otherwise><fmt:formatNumber value="${p.price}" pattern="#,##0"/>đ</c:otherwise>
                                </c:choose>
                            </div>
                            <c:if test="${priceMap[p.id].hasSale}">
                                <div style="font-size:12px;color:var(--ink-4);text-decoration:line-through;margin-top:1px;"><fmt:formatNumber value="${p.price}" pattern="#,##0"/>đ</div>
                            </c:if>
                        </div>
                        <a href="/products/${p.id}" class="tg-btn tg-primary" style="border-radius:14px;padding:15px 32px;">Mua ngay</a>
                    </div>
                </div>
            </div>
        </c:forEach>
    </div>
</section>
</c:if>

<!-- ================= HÀNG MỚI VỀ ================= -->
<c:if test="${not empty newArrivals}">
<section class="tg-sect">
    <div class="tg-wrap">
        <div style="display:flex;align-items:flex-end;justify-content:space-between;margin-bottom:22px;">
            <h2 class="tg-h3">Hàng mới về</h2>
            <a href="/products" class="tg-link">Xem tất cả →</a>
        </div>
        <div class="tg-grid-4">
            <c:forEach var="p" items="${newArrivals}" varStatus="loop">
                <a href="/products/${p.id}" class="tg-card tg-lift tg-rise"
                   style="padding:16px;display:block;text-decoration:none;color:inherit;animation-delay:${loop.index * 80}ms;">
                    <div style="background:var(--surface-2);border-radius:var(--r-lg);padding:22px;margin-bottom:16px;position:relative;overflow:hidden;">
                        <c:if test="${priceMap[p.id].hasSale}">
                            <span class="tg-mono" style="position:absolute;top:12px;left:12px;background:var(--err);color:#fff;font-size:10px;font-weight:600;letter-spacing:.06em;padding:4px 11px;border-radius:var(--r-pill);z-index:2;">−${priceMap[p.id].percentOff}%</span>
                        </c:if>
                        <c:if test="${p.stockQuantity == 0}">
                            <span class="tg-mono" style="position:absolute;top:12px;right:12px;background:var(--ink-2);color:#fff;font-size:10px;font-weight:600;padding:4px 11px;border-radius:var(--r-pill);z-index:2;">HẾT HÀNG</span>
                        </c:if>
                        <c:choose>
                            <c:when test="${not empty p.imageUrl}">
                                <img src="/static/images/${p.imageUrl}" alt="${p.name}" class="tg-zoom"
                                     onerror="this.onerror=null;this.src='/static/images/banner.png';"
                                     style="width:100%;height:150px;object-fit:contain;mix-blend-mode:multiply;display:block;"/>
                            </c:when>
                            <c:otherwise>
                                <img src="/static/images/banner.png" alt="${p.name}" class="tg-zoom"
                                     style="width:100%;height:150px;object-fit:contain;display:block;"/>
                            </c:otherwise>
                        </c:choose>
                    </div>
                    <c:if test="${not empty p.category}">
                        <div class="tg-mono" style="font-size:10px;letter-spacing:.1em;color:var(--accent);font-weight:600;">${p.category.name}</div>
                    </c:if>
                    <div style="font-size:15px;font-weight:600;margin:6px 0 8px;line-height:1.35;">${p.name}</div>
                    <div style="display:flex;gap:6px;margin-bottom:14px;flex-wrap:wrap;min-height:22px;">
                        <c:if test="${not empty p.switchType}"><span class="tg-chip">${p.switchType}</span></c:if>
                        <c:if test="${not empty p.connectivity}"><span class="tg-chip">${p.connectivity}</span></c:if>
                    </div>
                    <div style="display:flex;align-items:center;justify-content:space-between;gap:10px;">
                        <span style="font-size:18px;font-weight:700;color:var(--accent);">
                            <c:choose>
                                <c:when test="${priceMap[p.id].hasSale}"><fmt:formatNumber value="${priceMap[p.id].salePrice}" pattern="#,##0"/>đ</c:when>
                                <c:otherwise><fmt:formatNumber value="${p.price}" pattern="#,##0"/>đ</c:otherwise>
                            </c:choose>
                        </span>
                        <span class="tg-cta" style="border:1px solid var(--line-new);border-radius:11px;font-size:12px;font-weight:600;padding:8px 15px;white-space:nowrap;">Xem chi tiết</span>
                    </div>
                </a>
            </c:forEach>
        </div>
    </div>
</section>
</c:if>

<!-- ================= GÓC MÁY ĐỒNG BỘ (COMBO) ================= -->
<c:if test="${not empty combos}">
<section class="tg-sect">
    <div class="tg-wrap">
        <div style="display:flex;align-items:flex-end;justify-content:space-between;margin-bottom:8px;">
            <div>
                <div class="tg-mono" style="font-size:11px;letter-spacing:.16em;color:var(--violet);margin-bottom:8px;">SETUP INSPIRATION</div>
                <h2 class="tg-h3">Góc máy đồng bộ</h2>
            </div>
            <a href="/combo" class="tg-link">Tất cả combo →</a>
        </div>
        <p style="font-size:14px;color:var(--ink-3);margin:0 0 22px;max-width:520px;line-height:1.7;">
            Những bộ ghép sẵn theo phong cách chơi — mua trọn bộ tiết kiệm hơn mua lẻ.
        </p>
        <div class="tg-combo-grid">
            <c:forEach var="cv" items="${combos}" varStatus="loop">
                <c:set var="cvDelay" value="${loop.index * 80}"/>
                <%@ include file="/WEB-INF/views/common/combo-card.jsp" %>
            </c:forEach>
        </div>
    </div>
</section>
</c:if>

<!-- ================= NEWSLETTER ================= -->
<section class="tg-sect" style="padding-bottom:0;">
    <div class="tg-wrap tg-news" style="background:linear-gradient(115deg,var(--ink-dark),#1A2740);border-radius:var(--r-2xl);padding:48px 52px;display:flex;align-items:center;justify-content:space-between;gap:40px;position:relative;overflow:hidden;flex-wrap:wrap;">
        <div style="position:absolute;top:-90px;right:80px;width:300px;height:300px;border-radius:50%;background:var(--accent);opacity:.24;filter:blur(50px);"></div>
        <div style="position:relative;">
            <div class="tg-mono" style="font-size:11px;letter-spacing:.18em;color:#7C9BFF;margin-bottom:12px;">TITANGEAR INSIDER</div>
            <h2 style="font-size:28px;font-weight:700;color:#fff;margin:0 0 8px;letter-spacing:-.02em;">Nhận tin hàng mới &amp; deal sớm 24h</h2>
            <p style="font-size:14px;color:rgba(255,255,255,.62);margin:0;line-height:1.7;">Không spam. Chỉ gửi khi có hàng mới hoặc flash sale thật.</p>
        </div>
        <div style="position:relative;display:flex;gap:10px;flex-shrink:0;flex-wrap:wrap;">
            <input type="email" placeholder="email@cua.ban"
                   style="border:1px solid rgba(255,255,255,.2);background:rgba(255,255,255,.08);border-radius:13px;padding:15px 20px;font-size:14px;color:#fff;width:270px;outline:none;font-family:inherit;"/>
            <button type="button" class="tg-btn tg-primary" style="border-radius:13px;padding:15px 28px;">Đăng ký</button>
        </div>
    </div>
</section>

</div><!-- /.tg-page -->

<style>
    /* Bố cục riêng của trang chủ — để ở đây vì chỉ trang này dùng */
    .tg-hero-grid { position:relative; display:grid; grid-template-columns:1fr 1fr; gap:40px;
                    align-items:center; padding:76px 60px 84px; max-width:1340px; margin:0 auto; }
    .tg-trust { display:grid; grid-template-columns:repeat(4,1fr); gap:18px; }
    .tg-bento { display:grid; grid-template-columns:1.35fr 1fr 1fr; gap:20px; }
    .tg-grid-4 { display:grid; grid-template-columns:repeat(4,1fr); gap:20px; }

    /* Tab sản phẩm nổi bật */
    .hp-fp-tab { padding:9px 17px; border-radius:var(--r-pill); border:1px solid var(--line-new);
                 background:var(--surface); color:var(--ink-2); font-size:13px; font-weight:500; }
    .hp-fp-tab:hover { border-color:var(--accent); color:var(--accent); }
    .hp-fp-tab.is-on { background:var(--accent); border-color:var(--accent); color:#fff; font-weight:600; }

    /* Chỉ 1 panel hiện tại một thời điểm */
    .hp-fp-panel { display:none; }
    .hp-fp-panel.is-active { display:grid; }

    /* Slide hero */
    .hp-slide { display:none; }
    .hp-slide.is-active { display:block; }
    .hp-hero-dot { width:9px; height:9px; border-radius:var(--r-pill); border:none; padding:0;
                   cursor:pointer; background:#CFD4DC;
                   transition:width .3s var(--ease), background .3s ease; }
    .hp-hero-dot.is-on { width:26px; background:var(--accent); }

    @media (max-width:1100px) {
        .tg-hero-grid { grid-template-columns:1fr; padding:48px 28px 56px; }
        .tg-trust  { grid-template-columns:repeat(2,1fr); }
        .tg-bento  { grid-template-columns:1fr; }
        .tg-grid-4 { grid-template-columns:repeat(2,1fr); }
        .tg-sect   { padding-left:28px; padding-right:28px; }
        .hp-fp-panel.is-active { grid-template-columns:1fr; }
    }
</style>

<script>
    /* ===== HERO SLIDER (slide tĩnh + banner flash sale) ===== */
    (function () {
        var hero = document.getElementById('hpHero');
        if (!hero) return;
        var slides = hero.querySelectorAll('.hp-slide');
        var dots   = hero.querySelectorAll('.hp-hero-dot');
        var cur = 0;
        window.hpHeroGo = function (i) {
            i = (i + slides.length) % slides.length;
            if (i === cur) return;
            slides[cur].classList.remove('is-active');
            if (dots.length) dots[cur].classList.remove('is-on');
            cur = i;
            slides[cur].classList.add('is-active');
            if (dots.length) dots[cur].classList.add('is-on');
        };
        window.hpHeroMove = function (d) { window.hpHeroGo(cur + d); };
        if (slides.length > 1) setInterval(function () { window.hpHeroMove(1); }, 6000);
    })();

    /* ===== TAB SẢN PHẨM NỔI BẬT ===== */
    window.hpFp = function (i) {
        document.querySelectorAll('.hp-fp-panel').forEach(function (p) {
            p.classList.toggle('is-active', Number(p.dataset.idx) === i);
        });
        document.querySelectorAll('.hp-fp-tab').forEach(function (t) {
            t.classList.toggle('is-on', Number(t.dataset.idx) === i);
        });
    };
</script>
<%@ include file="/WEB-INF/views/common/footer.jsp" %>

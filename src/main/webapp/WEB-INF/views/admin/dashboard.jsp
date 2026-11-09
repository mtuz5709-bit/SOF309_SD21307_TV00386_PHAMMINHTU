<%@ page contentType="text/html;charset=UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>Tổng Quan – TiTanGEAR</title>
    <link href="https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@400;500;600;700&family=IBM+Plex+Mono:wght@400;500;600&display=swap" rel="stylesheet"/>
    <link rel="stylesheet" href="/static/css/tokens.css"/>
    <link rel="stylesheet" href="/static/css/main.css"/>
    <link rel="stylesheet" href="/static/css/tg.css"/>
    <link rel="stylesheet" href="/static/css/admin.css"/>
    <style>
        .db-grid-4  { display:grid; grid-template-columns:repeat(4,1fr); gap:20px; margin-bottom:24px; }
        .db-grid-21 { display:grid; grid-template-columns:1.7fr 1fr; gap:20px; margin-bottom:24px; }
        .db-grid-3  { display:grid; grid-template-columns:1fr 1fr 1fr; gap:20px; }
        .db-pad     { padding:24px 28px; }

        /* Biểu đồ cột 14 ngày — CSS thuần, bỏ được thư viện Chart.js tải từ mạng */
        .db-bars  { display:flex; align-items:flex-end; gap:8px; height:200px; padding:0 2px; }
        .db-bcol  { flex:1; display:flex; flex-direction:column; align-items:center; justify-content:flex-end;
                    height:100%; position:relative; cursor:pointer; }
        .db-bar   { width:100%; border-radius:8px 8px 4px 4px;
                    background:linear-gradient(180deg,var(--accent),var(--accent-2));
                    transition:filter .2s ease;
                    /* Mọc lên từ đáy, so le từng cột theo thứ tự ngày.
                       transform-origin phải là bottom, không thì cột phình ra từ giữa. */
                    transform-origin:bottom; animation:db-moc .5s var(--ease) both; }
        @keyframes db-moc { from { transform:scaleY(0); } to { transform:scaleY(1); } }
        .db-bcol:hover .db-bar { filter:brightness(1.15); }
        /* Cột đang rê chuột nhô lên nhẹ để biết mình đang xem ngày nào */
        .db-bcol:hover { transform:translateY(-2px); transition:transform .18s ease; }

        /* Donut trạng thái: vẽ dần theo chiều kim đồng hồ */
        #dbDonut circle[data-veDan] { transition:stroke-dasharray .8s var(--ease); }
        /* Chú giải hiện lần lượt, khớp nhịp với từng mảnh donut đang được vẽ */
        .db-legend > div { animation:tg-rise .45s var(--ease) both; }
        .db-legend > div:nth-child(1) { animation-delay:.10s; }
        .db-legend > div:nth-child(2) { animation-delay:.26s; }
        .db-legend > div:nth-child(3) { animation-delay:.42s; }
        .db-legend > div:nth-child(4) { animation-delay:.58s; }
        .db-legend > div:nth-child(5) { animation-delay:.74s; }
        .db-legend > div { transition:background .18s ease; border-radius:7px; padding:2px 6px; margin:-2px -6px; }
        .db-legend > div:hover { background:var(--surface-3); }
        .db-tip   { position:absolute; bottom:calc(100% + 6px); background:var(--ink); color:#fff;
                    font-family:var(--font-mono); font-size:10px; padding:4px 8px; border-radius:7px;
                    white-space:nowrap; z-index:5; box-shadow:0 6px 16px rgba(20,23,26,.22);
                    opacity:0; pointer-events:none; transition:opacity .16s ease; }
        .db-bcol:hover .db-tip { opacity:1; }
        .db-blabels { display:flex; gap:8px; padding-top:10px; border-top:1px solid var(--line-soft); margin-top:2px; }
        .db-blabels span { flex:1; text-align:center; font-family:var(--font-mono); font-size:9px; color:#B4BAC1; }

        /* Dòng hoạt động */
        .db-feed { display:flex; gap:12px; align-items:flex-start; padding:11px 8px;
                   border-radius:var(--r-md); transition:background .2s ease; }
        .db-feed:hover { background:#F8F9FC; }
        .db-dot  { width:9px; height:9px; border-radius:50%; flex-shrink:0; margin-top:5px; }

        /* Cần chú ý */
        .db-alert { display:flex; align-items:center; gap:11px; padding:12px 14px; border-radius:var(--r-md);
                    text-decoration:none; transition:transform .2s ease, box-shadow .2s ease; }
        .db-alert:hover { transform:translateX(3px); box-shadow:0 6px 16px rgba(20,23,26,.08); }

        @media (max-width:1200px) {
            .db-grid-4 { grid-template-columns:repeat(2,1fr); }
            .db-grid-21, .db-grid-3 { grid-template-columns:1fr; }
        }
    </style>
</head>
<body>
<div class="admin-layout">
    <c:set var="adminActive" value="dashboard"/>
    <%@ include file="/WEB-INF/views/common/admin-sidebar.jsp" %>

    <div class="admin-main">
        <c:set var="adminTitle" value="Tổng Quan"/>
        <c:set var="adminCrumb" value="ADMIN / TỔNG QUAN"/>
        <%@ include file="/WEB-INF/views/common/admin-topbar.jsp" %>

        <!-- ===== LỜI CHÀO ===== -->
        <div class="tg-rise" style="display:flex;align-items:flex-end;justify-content:space-between;margin-bottom:24px;gap:20px;flex-wrap:wrap;">
            <div>
                <div class="tg-mono" style="font-size:11px;letter-spacing:.16em;color:var(--accent);margin-bottom:8px;">
                    CẬP NHẬT LÚC ${capNhatLuc}
                </div>
                <h2 style="font-size:30px;font-weight:700;margin:0;letter-spacing:-.02em;">
                    Chào ${not empty me ? me.ten : 'Quản trị viên'}
                </h2>
            </div>
            <div class="tg-card" style="display:flex;overflow:hidden;">
                <div style="padding:12px 22px;border-right:1px solid var(--line-soft);">
                    <div class="tg-mono" style="font-size:10px;color:var(--ink-4);letter-spacing:.1em;">KHÁCH MỚI HÔM NAY</div>
                    <div style="font-size:18px;font-weight:700;color:var(--ok-2);">${khachMoiHomNay}</div>
                </div>
                <div style="padding:12px 22px;">
                    <div class="tg-mono" style="font-size:10px;color:var(--ink-4);letter-spacing:.1em;">GIỎ ĐANG MỞ</div>
                    <div style="font-size:18px;font-weight:700;color:var(--accent);">${gioHangDangMo}</div>
                </div>
            </div>
        </div>

        <!-- ===== 4 THẺ CHỈ SỐ ===== -->
        <div class="db-grid-4">
            <div class="tg-rise tg-card tg-lift db-pad">
                <div style="height:4px;border-radius:var(--r-pill);margin-bottom:18px;background:var(--accent);"></div>
                <div style="display:flex;align-items:center;justify-content:space-between;">
                    <span class="tg-mono" style="font-size:10px;letter-spacing:.14em;color:var(--ink-4);">DOANH THU HÔM NAY</span>
                    <span style="width:30px;height:30px;border-radius:var(--r-sm);background:var(--accent-soft-2);display:inline-flex;align-items:center;justify-content:center;">
                        <svg width="15" height="15" viewBox="0 0 16 16" fill="none" stroke="#1B49FF" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="8" cy="8" r="6"/><path d="M8 4.8v6.4M6.3 6.4h3M6.3 9.6h3"/></svg>
                    </span>
                </div>
                <div style="font-size:32px;font-weight:700;margin-top:14px;letter-spacing:-.02em;">
                    <fmt:formatNumber value="${doanhThuHomNayM}" pattern="#,##0.0"/>M
                </div>
                <%-- ${soSanhHomQua} là CHUỖI do controller dựng sẵn ("+12.5% so với
                     hôm qua" / "chưa có doanh thu"), KHÔNG phải số — nên chỉ in ra và
                     chọn màu theo dấu đầu chuỗi, không đem đi so sánh số học. --%>
                <div style="display:flex;align-items:center;gap:6px;margin-top:6px;flex-wrap:wrap;">
                    <c:choose>
                        <c:when test="${fn:startsWith(soSanhHomQua, '-')}">
                            <span class="tg-mono" style="font-size:11px;font-weight:600;border-radius:var(--r-pill);padding:3px 9px;color:var(--err);background:var(--err-bg-2);">
                                ▼ ${soSanhHomQua}
                            </span>
                        </c:when>
                        <c:when test="${fn:startsWith(soSanhHomQua, '+')}">
                            <span class="tg-mono" style="font-size:11px;font-weight:600;border-radius:var(--r-pill);padding:3px 9px;color:var(--ok-deep);background:var(--ok-bg-2);">
                                ▲ ${soSanhHomQua}
                            </span>
                        </c:when>
                        <c:otherwise>
                            <span class="tg-mono" style="font-size:11px;font-weight:600;border-radius:var(--r-pill);padding:3px 9px;color:var(--ink-2);background:var(--surface-3);">
                                ${soSanhHomQua}
                            </span>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <div class="tg-rise tg-card tg-lift db-pad" style="animation-delay:80ms;">
                <div style="height:4px;border-radius:var(--r-pill);margin-bottom:18px;background:var(--violet);"></div>
                <div style="display:flex;align-items:center;justify-content:space-between;">
                    <span class="tg-mono" style="font-size:10px;letter-spacing:.14em;color:var(--ink-4);">ĐƠN MỚI HÔM NAY</span>
                    <span style="width:30px;height:30px;border-radius:var(--r-sm);background:var(--violet-bg);display:inline-flex;align-items:center;justify-content:center;">
                        <svg width="15" height="15" viewBox="0 0 16 16" fill="none" stroke="#7A5CC7" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="2.5" width="10" height="11" rx="1.6"/><path d="M5.6 6h4.8M5.6 8.6h4.8M5.6 11.2h3"/></svg>
                    </span>
                </div>
                <div style="font-size:32px;font-weight:700;margin-top:14px;letter-spacing:-.02em;">${donMoiHomNay}</div>
                <div style="font-size:12px;color:var(--ink-3);margin-top:6px;">đơn đặt trong hôm nay</div>
            </div>

            <div class="tg-rise tg-card tg-lift db-pad" style="animation-delay:160ms;">
                <div style="height:4px;border-radius:var(--r-pill);margin-bottom:18px;background:var(--warn-2);"></div>
                <div style="display:flex;align-items:center;justify-content:space-between;">
                    <span class="tg-mono" style="font-size:10px;letter-spacing:.14em;color:var(--ink-4);">CHỜ XỬ LÝ</span>
                    <span style="width:30px;height:30px;border-radius:var(--r-sm);background:var(--warn-bg-2);display:inline-flex;align-items:center;justify-content:center;">
                        <svg width="15" height="15" viewBox="0 0 16 16" fill="none" stroke="#E08600" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="8" cy="8" r="6"/><path d="M8 4.8V8l2.2 1.3"/></svg>
                    </span>
                </div>
                <div style="font-size:32px;font-weight:700;margin-top:14px;letter-spacing:-.02em;">${donChoXuLy}</div>
                <div style="font-size:12px;color:var(--ink-3);margin-top:6px;">cần xác nhận / giao</div>
            </div>

            <div class="tg-rise tg-card tg-lift db-pad" style="animation-delay:240ms;">
                <div style="height:4px;border-radius:var(--r-pill);margin-bottom:18px;background:var(--ok-2);"></div>
                <div style="display:flex;align-items:center;justify-content:space-between;">
                    <span class="tg-mono" style="font-size:10px;letter-spacing:.14em;color:var(--ink-4);">DOANH THU 14 NGÀY</span>
                    <span style="width:30px;height:30px;border-radius:var(--r-sm);background:var(--ok-bg-2);display:inline-flex;align-items:center;justify-content:center;">
                        <svg width="15" height="15" viewBox="0 0 16 16" fill="none" stroke="#10A268" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M2 13h12"/><path d="M4 10.5V7M7.3 10.5V4.5M10.6 10.5V8"/></svg>
                    </span>
                </div>
                <div style="font-size:32px;font-weight:700;margin-top:14px;letter-spacing:-.02em;">
                    <fmt:formatNumber value="${tong14NgayM}" pattern="#,##0.0"/>M
                </div>
                <div style="font-size:12px;color:var(--ink-3);margin-top:6px;">tổng 14 ngày gần nhất</div>
            </div>
        </div>

        <!-- ===== BIỂU ĐỒ + HOẠT ĐỘNG ===== -->
        <div class="db-grid-21">
            <div class="tg-rise tg-card db-pad">
                <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:6px;gap:12px;flex-wrap:wrap;">
                    <h3 style="font-size:16px;font-weight:700;margin:0;">Doanh thu 14 ngày gần nhất</h3>
                    <a href="/admin/report" class="tg-link">Xem báo cáo đầy đủ →</a>
                </div>
                <div style="display:flex;align-items:baseline;gap:12px;margin-bottom:22px;">
                    <span style="font-size:28px;font-weight:700;letter-spacing:-.02em;">
                        <fmt:formatNumber value="${tong14Ngay}" pattern="#,##0"/>đ
                    </span>
                </div>
                <div class="db-bars"    id="dbBars"></div>
                <div class="db-blabels" id="dbLabels"></div>
            </div>

            <div class="tg-rise tg-card db-pad">
                <h3 style="font-size:16px;font-weight:700;margin:0 0 18px;">Hoạt động gần đây</h3>
                <div style="display:flex;flex-direction:column;">
                    <c:forEach var="h" items="${hoatDong}">
                        <div class="db-feed">
                            <c:choose>
                                <c:when test="${h.loai == 'order'}"><span class="db-dot" style="background:var(--accent);"></span></c:when>
                                <c:when test="${h.loai == 'review'}"><span class="db-dot" style="background:var(--warn-2);"></span></c:when>
                                <c:otherwise><span class="db-dot" style="background:var(--ok-2);"></span></c:otherwise>
                            </c:choose>
                            <div style="flex:1;min-width:0;">
                                <div style="font-size:13px;line-height:1.4;">
                                    <span style="font-weight:600;">${h.ai}</span> ${h.hanh_dong}
                                </div>
                                <div class="tg-mono" style="font-size:11px;color:var(--ink-3);margin-top:2px;">${h.chi_tiet}</div>
                            </div>
                            <c:if test="${not empty h.thoi_diem}">
                                <span class="tg-mono" style="font-size:10px;color:#B4BAC1;white-space:nowrap;">
                                    ${h.thoi_diem.toString().substring(5,16).replace("T"," ")}
                                </span>
                            </c:if>
                        </div>
                    </c:forEach>
                    <c:if test="${empty hoatDong}">
                        <p style="font-size:13px;color:var(--ink-3);margin:0;">Chưa có hoạt động nào.</p>
                    </c:if>
                </div>
            </div>
        </div>

        <!-- ===== BÁN CHẠY + DONUT + CẦN CHÚ Ý ===== -->
        <div class="db-grid-3">
            <div class="tg-rise tg-card db-pad">
                <h3 style="font-size:16px;font-weight:700;margin:0 0 18px;">Bán chạy nhất</h3>
                <div style="display:flex;flex-direction:column;gap:16px;">
                    <c:forEach var="p" items="${banChay}" varStatus="loop">
                        <c:choose>
                            <c:when test="${loop.index == 0}"><c:set var="mau" value="#1B49FF"/></c:when>
                            <c:when test="${loop.index == 1}"><c:set var="mau" value="#7A5CC7"/></c:when>
                            <c:when test="${loop.index == 2}"><c:set var="mau" value="#10A268"/></c:when>
                            <c:otherwise><c:set var="mau" value="#8A9099"/></c:otherwise>
                        </c:choose>
                        <div>
                            <div style="display:flex;justify-content:space-between;align-items:baseline;margin-bottom:6px;gap:10px;">
                                <span style="font-size:13px;font-weight:600;min-width:0;">
                                    <span class="tg-mono" style="color:${mau};">0${loop.index + 1}.</span> ${p.ten_sp}
                                </span>
                                <span class="tg-mono" style="font-size:12px;color:var(--ink-3);white-space:nowrap;">${p.da_ban} cái</span>
                            </div>
                            <div class="tg-track">
                                <span style="width:${(p.da_ban * 100) / banNhieuNhat}%;background:${mau};"></span>
                            </div>
                        </div>
                    </c:forEach>
                    <c:if test="${empty banChay}">
                        <p style="font-size:13px;color:var(--ink-3);margin:0;">Chưa bán được sản phẩm nào.</p>
                    </c:if>
                </div>
            </div>

            <div class="tg-rise tg-card db-pad">
                <h3 style="font-size:16px;font-weight:700;margin:0 0 18px;">Trạng thái đơn hàng</h3>
                <div style="display:flex;align-items:center;gap:22px;flex-wrap:wrap;">
                    <div style="position:relative;width:120px;height:120px;flex-shrink:0;">
                        <svg width="120" height="120" viewBox="0 0 120 120" style="transform:rotate(-90deg);" id="dbDonut">
                            <circle cx="60" cy="60" r="48" fill="none" stroke="#F0F1F4" stroke-width="16"></circle>
                        </svg>
                        <div style="position:absolute;inset:0;display:flex;flex-direction:column;align-items:center;justify-content:center;">
                            <span style="font-size:24px;font-weight:700;letter-spacing:-.02em;">${tongDon}</span>
                            <span class="tg-mono" style="font-size:9px;color:var(--ink-4);">TỔNG ĐƠN</span>
                        </div>
                    </div>
                    <div class="db-legend" style="display:flex;flex-direction:column;gap:10px;flex:1;min-width:150px;">
                        <div style="display:flex;align-items:center;gap:9px;font-size:12px;">
                            <span style="width:10px;height:10px;border-radius:50%;background:#E08600;flex-shrink:0;"></span>
                            <span style="flex:1;color:var(--ink-2);">Chờ xác nhận</span>
                            <span class="tg-mono" style="font-weight:600;">${ttCho}</span>
                        </div>
                        <div style="display:flex;align-items:center;gap:9px;font-size:12px;">
                            <span style="width:10px;height:10px;border-radius:50%;background:#7A5CC7;flex-shrink:0;"></span>
                            <span style="flex:1;color:var(--ink-2);">Chờ hàng</span>
                            <span class="tg-mono" style="font-weight:600;">${ttChoHang}</span>
                        </div>
                        <div style="display:flex;align-items:center;gap:9px;font-size:12px;">
                            <span style="width:10px;height:10px;border-radius:50%;background:#1B49FF;flex-shrink:0;"></span>
                            <span style="flex:1;color:var(--ink-2);">Đang giao</span>
                            <span class="tg-mono" style="font-weight:600;">${ttDangGiao}</span>
                        </div>
                        <div style="display:flex;align-items:center;gap:9px;font-size:12px;">
                            <span style="width:10px;height:10px;border-radius:50%;background:#10A268;flex-shrink:0;"></span>
                            <span style="flex:1;color:var(--ink-2);">Hoàn thành</span>
                            <span class="tg-mono" style="font-weight:600;">${ttHoanTat}</span>
                        </div>
                        <div style="display:flex;align-items:center;gap:9px;font-size:12px;">
                            <span style="width:10px;height:10px;border-radius:50%;background:#C62828;flex-shrink:0;"></span>
                            <span style="flex:1;color:var(--ink-2);">Đã hủy</span>
                            <span class="tg-mono" style="font-weight:600;">${ttDaHuy}</span>
                        </div>
                    </div>
                </div>
            </div>

            <div class="tg-rise tg-card db-pad">
                <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:18px;">
                    <h3 style="font-size:16px;font-weight:700;margin:0;">Cần chú ý</h3>
                    <c:if test="${soCanhBao > 0}">
                        <span class="tg-mono" style="font-size:11px;font-weight:600;color:var(--err);background:var(--err-bg-2);border-radius:var(--r-pill);padding:3px 10px;">${soCanhBao}</span>
                    </c:if>
                </div>
                <div style="display:flex;flex-direction:column;gap:10px;">
                    <c:forEach var="a" items="${canChuY}">
                        <c:choose>
                            <c:when test="${a.ton == 0}">
                                <c:set var="aBg"   value="var(--err-bg-2)"/>
                                <c:set var="aLine" value="var(--err-line-2)"/>
                                <c:set var="aInk"  value="var(--err)"/>
                            </c:when>
                            <c:otherwise>
                                <c:set var="aBg"   value="var(--warn-bg-2)"/>
                                <c:set var="aLine" value="var(--warn-line-2)"/>
                                <c:set var="aInk"  value="var(--warn-deep)"/>
                            </c:otherwise>
                        </c:choose>
                        <a href="/warehouse/stock" class="db-alert"
                           style="background:${aBg};border:1px solid ${aLine};color:${aInk};">
                            <svg width="16" height="16" viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" style="flex-shrink:0;"><path d="M8 2 1.5 13.5h13L8 2Z"/><path d="M8 6.5v3.2M8 11.6v.4"/></svg>
                            <span style="flex:1;font-size:13px;font-weight:500;min-width:0;">${a.ten_sp}</span>
                            <span class="tg-mono" style="font-size:11px;font-weight:600;white-space:nowrap;">
                                ${a.ton == 0 ? 'HẾT HÀNG' : a.ton}
                            </span>
                        </a>
                    </c:forEach>
                    <c:if test="${empty canChuY}">
                        <p style="font-size:13px;color:var(--ink-3);margin:0;">Không có sản phẩm nào sắp hết hàng.</p>
                    </c:if>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
    /* ===== BIỂU ĐỒ CỘT 14 NGÀY =====
       Dùng lại đúng 2 mảng controller đã dựng sẵn cho biểu đồ Chart.js cũ
       (nhan14Ngay / giaTri14Ngay) nên KHÔNG phải sửa controller. Vẽ bằng CSS
       nên bỏ luôn được thư viện Chart.js tải từ mạng ngoài. */
    (function () {
        var nhan   = ${nhan14Ngay};
        var giaTri = ${giaTri14Ngay};
        var oBars  = document.getElementById('dbBars');
        var oLabel = document.getElementById('dbLabels');
        if (!oBars || !giaTri || !giaTri.length) return;

        var max = Math.max.apply(null, giaTri) || 1;
        var dinhDang = new Intl.NumberFormat('vi-VN');

        giaTri.forEach(function (v, i) {
            var cot = document.createElement('div');
            cot.className = 'db-bcol';

            var tip = document.createElement('span');
            tip.className = 'db-tip';
            tip.textContent = nhan[i] + ' · ' + dinhDang.format(v) + 'đ';

            var bar = document.createElement('div');
            bar.className = 'db-bar';
            /* Ngày không bán được gì vẫn chừa 2px cho thấy có cột, không phải mất dữ liệu */
            bar.style.height = (v > 0 ? Math.max(4, (v / max) * 100) : 1) + '%';
            /* So le 45ms/cột: 14 cột mọc xong trong ~1,1 giây — đủ thấy chuyển
               động mà không bắt người xem phải chờ. */
            bar.style.animationDelay = (i * 45) + 'ms';

            cot.appendChild(tip);
            cot.appendChild(bar);
            oBars.appendChild(cot);

            var nh = document.createElement('span');
            nh.textContent = nhan[i];
            oLabel.appendChild(nh);
        });
    })();

    /* ===== DONUT TRẠNG THÁI ĐƠN ===== */
    (function () {
        var svg = document.getElementById('dbDonut');
        if (!svg) return;
        var phan = [
            { v: ${ttCho},      c: '#E08600' },
            { v: ${ttChoHang},  c: '#7A5CC7' },
            { v: ${ttDangGiao}, c: '#1B49FF' },
            { v: ${ttHoanTat},  c: '#10A268' },
            { v: ${ttDaHuy},    c: '#C62828' }
        ];
        var tong = phan.reduce(function (s, p) { return s + p.v; }, 0);
        if (!tong) return;

        var R = 48, CHU_VI = 2 * Math.PI * R, daVe = 0;
        var canVe = [];

        phan.forEach(function (p) {
            if (!p.v) return;
            var dai = (p.v / tong) * CHU_VI;
            var c = document.createElementNS('http://www.w3.org/2000/svg', 'circle');
            c.setAttribute('cx', 60); c.setAttribute('cy', 60); c.setAttribute('r', R);
            c.setAttribute('fill', 'none');
            c.setAttribute('stroke', p.c);
            c.setAttribute('stroke-width', 16);
            c.setAttribute('data-veDan', '1');
            /* Bắt đầu bằng cung dài 0 rồi mới giãn ra -> trông như đang vẽ */
            c.setAttribute('stroke-dasharray', '0 ' + CHU_VI);
            c.setAttribute('stroke-dashoffset', -daVe);
            svg.appendChild(c);
            canVe.push({ el: c, dai: dai });
            daVe += dai;
        });

        /* Ép trình duyệt tính lại layout để nó ghi nhận giá trị đầu (cung dài 0),
           rồi mới đặt giá trị cuối -> transition chạy, mỗi mảnh nối tiếp mảnh trước.
           KHÔNG dùng requestAnimationFrame: tab đang ẩn thì rAF không chạy, biểu đồ
           sẽ TRỐNG TRƠN cho tới khi người dùng bấm vào tab. Cách này tab ẩn vẫn đặt
           đúng giá trị cuối, chỉ là không có hiệu ứng — dữ liệu quan trọng hơn hiệu ứng. */
        void svg.getBoundingClientRect();
        canVe.forEach(function (m, i) {
            m.el.style.transitionDelay = (i * 160) + 'ms';
            m.el.setAttribute('stroke-dasharray', m.dai + ' ' + (CHU_VI - m.dai));
        });
    })();
</script>
</body>
</html>

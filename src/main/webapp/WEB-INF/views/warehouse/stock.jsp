<%@ page contentType="text/html;charset=UTF-8" isELIgnored="false" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <title>Kho Hàng – TiTanGEAR</title>
    <link href="https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@400;500;600;700&family=IBM+Plex+Mono:wght@400;500;600&display=swap" rel="stylesheet"/>
    <link rel="stylesheet" href="/static/css/tokens.css"/>
    <link rel="stylesheet" href="/static/css/main.css"/>
    <link rel="stylesheet" href="/static/css/admin.css"/>
    <style>
        .tab-bar { display:flex; gap:8px; margin-bottom:20px; flex-wrap:wrap; }
        .tab-btn { padding:9px 18px; border:1px solid var(--line-new); border-radius:var(--r-pill);
            background:var(--surface); color:var(--ink-2); cursor:pointer; font-size:13px; font-family:inherit;
            transition:background .2s ease, color .2s ease, border-color .2s ease; }
        .tab-btn:hover { border-color:var(--accent); color:var(--accent); }
        .tab-btn.active { background:var(--accent); color:#fff; border-color:var(--accent); font-weight:600; }
        .tab-content { display:none; }
        .tab-content.active { display:block; }
        .low-stock-alert { background:var(--warn-bg-2); border:1px solid var(--warn-line-2);
            border-radius:var(--r-lg); padding:18px 22px; margin-bottom:24px; }
        .low-stock-alert h3 { font-size:15px; font-weight:700; color:var(--warn-deep); margin-bottom:12px; }
        .badge-fifo { background:var(--accent-soft-2); color:var(--accent); padding:3px 10px; border-radius:20px; font-size:12px; font-weight:600; }
        .badge-empty { background:var(--err-bg-2); color:var(--err); padding:3px 10px; border-radius:20px; font-size:12px; font-weight:600; }

        /* ---- Ô chọn vị trí ở form nhập kho ---- */
        .vitri-row { display:flex; gap:10px; }
        .vitri-row select { flex:2; }
        .vitri-row input  { flex:1; }
        .vitri-hint { display:block; margin-top:6px; font-size:12px; color:var(--ink-3); }

        /* ---- TEM DÁN THÙNG ----
           Chữ to, đậm, tương phản cao: thủ kho đọc trên màn hình rồi chép tay
           lên thùng bằng bút lông, nên phải nhìn rõ từ xa. */
        .tem-wrap { display:flex; flex-direction:column; align-items:center; gap:14px;
            background:var(--surface); border:1px solid var(--line-new);
            border-radius:var(--r-lg); padding:26px 20px; margin-bottom:24px; }
        .tem-huongdan { text-align:center; }
        .tem-huongdan strong { display:block; font-size:15px; color:var(--ink-1); margin-bottom:4px; }
        .tem-huongdan span   { font-size:12.5px; color:var(--ink-3); }
        .tem-box { border:4px solid #000; padding:22px 40px; text-align:center;
            background:#fff; min-width:330px; }
        .tem-ma   { font-size:40px; font-weight:800; letter-spacing:2px; color:#000; line-height:1.1; }
        .tem-sp   { font-size:18px; font-weight:700; color:#000; margin-top:12px; }
        .tem-bt   { font-size:15px; color:#333; margin-top:3px; }
        .tem-sl   { font-size:26px; font-weight:800; color:#000; margin-top:12px; }
        .tem-ngay { font-size:14px; color:#333; margin-top:8px; }
        .tem-vitri { font-size:14px; color:var(--ink-2); }
        .tem-vitri strong { color:var(--accent); font-size:16px; }

        /* ---- Ô vị trí trong bảng lô: mặc định là nhãn gọn, bấm mới mở ô sửa ---- */
        .vt-cell  { position:relative; }
        .vt-chip  { display:inline-flex; align-items:center; gap:5px; padding:4px 11px;
            border:1px solid var(--accent-line, #cdd6e6); border-radius:var(--r-pill, 999px);
            background:var(--accent-soft-2); color:var(--accent);
            font-family:inherit; font-size:12px; font-weight:600; cursor:pointer; white-space:nowrap;
            transition:background .15s ease, border-color .15s ease; }
        .vt-chip:hover { background:var(--accent); color:#fff; border-color:var(--accent); }
        .vt-chip b   { font-weight:800; }
        .vt-sep      { opacity:.5; }
        .vt-chip-trong { background:var(--warn-bg-2); color:var(--warn-deep);
            border-color:var(--warn-line-2); font-weight:500; }
        .vt-form  { display:none; gap:4px; align-items:center; }
        .vt-cell.dang-sua .vt-chip { display:none; }
        .vt-cell.dang-sua .vt-form { display:flex; }
        .vt-khu   { width:50px;  padding:5px 4px; font-size:12px; border-radius:6px; }
        .vt-ke    { width:62px;  padding:5px 6px; font-size:12px; border-radius:6px; }
        .vt-luu   { padding:5px 10px; font-size:12px; border-radius:6px; border:1px solid var(--accent);
            background:var(--accent); color:#fff; cursor:pointer; font-family:inherit; font-weight:600; }
        .vt-huy   { padding:5px 8px; font-size:14px; line-height:1; border-radius:6px;
            border:1px solid var(--line-new); background:var(--surface); color:var(--ink-3);
            cursor:pointer; font-family:inherit; }
        .vt-huy:hover { color:var(--err); border-color:var(--err-line-2); }

        /* ---- Ô tìm lô ---- */
        .lo-search { display:flex; align-items:center; gap:10px; margin-bottom:16px; flex-wrap:wrap; }
        .lo-search-box { position:relative; display:flex; align-items:center; flex:1; min-width:260px; max-width:420px; }
        .lo-search-box svg { position:absolute; left:12px; color:var(--ink-3); pointer-events:none; }
        .lo-search-box input { width:100%; padding:9px 14px 9px 34px; font-size:13px;
            border:1px solid var(--line-new); border-radius:var(--r-pill, 999px);
            background:var(--surface); color:var(--ink-1); font-family:inherit; }
        .lo-search-box input:focus { outline:none; border-color:var(--accent); }
        .lo-search-go { padding:9px 22px; font-size:13px; border-radius:var(--r-pill, 999px); }
        .lo-search-clear { font-size:13px; color:var(--ink-3); text-decoration:none; }
        .lo-search-clear:hover { color:var(--err); }
        .lo-search-count { font-size:12.5px; color:var(--ink-3); }
    </style>
</head>
<body>

<div class="admin-layout">
    <c:set var="adminActive" value="warehouse"/>
    <%@ include file="/WEB-INF/views/common/admin-sidebar.jsp" %>

    <div class="admin-main">
        <c:set var="adminTitle" value="Quản lý Kho Hàng"/>
        <c:set var="adminCrumb" value="WAREHOUSE / STOCK"/>
        <%@ include file="/WEB-INF/views/common/admin-topbar.jsp" %>

        <!-- THÔNG BÁO NHẬP KHO THÀNH CÔNG -->
        <c:if test="${param.imported == 'true'}">
            <div class="admin-alert-ok">Nhập kho thành công!</div>
        </c:if>
        <c:if test="${param.error == 'needVariant'}">
            <div class="admin-alert-err">Sản phẩm này có biến thể — vui lòng chọn biến thể khi nhập kho.</div>
        </c:if>
        <c:if test="${param.error == 'needViTri'}">
            <div class="admin-alert-err">Phải chọn khu và số kệ — lô không có vị trí là lô không tìm được ngoài kho.</div>
        </c:if>
        <c:if test="${not empty thanhCong}"><div class="admin-alert-ok">${thanhCong}</div></c:if>
        <c:if test="${not empty loi}"><div class="admin-alert-err">${loi}</div></c:if>

        <%-- TEM DÁN THÙNG.
             Chỉ ghi SỐ LÔ, sản phẩm, số lượng, ngày nhập — KHÔNG ghi vị trí kệ.
             Lý do: vị trí đổi khi dọn kho, mà tem đã dán lên thùng thì không bóc
             ra sửa được. Số lô thì không bao giờ đổi, nên tem luôn đúng.
             Thủ kho chép nội dung này lên thùng bằng bút lông. --%>
        <c:if test="${not empty tem}">
            <div class="tem-wrap">
                <div class="tem-huongdan">
                    <strong>Chép nội dung dưới đây lên thùng hàng</strong>
                    <span>Không cần ghi vị trí kệ lên thùng — vị trí tra trên máy, vì dọn kho là nó đổi.</span>
                </div>
                <div class="tem-box">
                    <div class="tem-ma">LÔ #${tem.id}</div>
                    <div class="tem-sp">${tem.product.name}</div>
                    <c:if test="${not empty tem.variant}">
                        <div class="tem-bt">${tem.variant.name}<c:if test="${not empty tem.variant.color}"> · ${tem.variant.color}</c:if></div>
                    </c:if>
                    <div class="tem-sl">${tem.quantity} cái</div>
                    <div class="tem-ngay">Nhập ${tem.ngayNhapVN}</div>
                </div>
                <div class="tem-vitri">Cất tại: <strong>${tem.viTri}</strong></div>
            </div>
        </c:if>

        <%-- 4 thẻ chỉ số — đếm ngay trên các danh sách controller đã nạp sẵn cho
             các tab bên dưới, không phát sinh truy vấn mới. --%>
        <div class="stat-cards">
            <div class="stat-card tg-rise">
                <div class="ad-bar" style="background:var(--accent);"></div>
                <div class="ad-top">
                    <span class="stat-label">MẶT HÀNG TRONG KHO</span>
                    <span class="ad-ico" style="background:var(--accent-soft-2);">
                        <svg width="15" height="15" viewBox="0 0 16 16" fill="none" stroke="#1B49FF" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M2 6.5 8 2.5l6 4V14H2V6.5Z"/><path d="M5 14V9h6v5"/></svg>
                    </span>
                </div>
                <div class="stat-value">${stockReport.size()}</div>
                <div class="stat-sub">có phát sinh nhập/xuất</div>
            </div>

            <div class="stat-card tg-rise" style="animation-delay:80ms;">
                <div class="ad-bar" style="background:var(--ok-2);"></div>
                <div class="ad-top">
                    <span class="stat-label">LƯỢT NHẬP KHO</span>
                    <span class="ad-ico" style="background:var(--ok-bg-2);">
                        <svg width="15" height="15" viewBox="0 0 16 16" fill="none" stroke="#10A268" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M8 2.5v7M5 6.8 8 9.8l3-3"/><path d="M2.5 12.5h11"/></svg>
                    </span>
                </div>
                <%-- Đếm TỔNG số lô, không dùng stockInHistory vì danh sách đó bị bộ lọc
                     ở tab lịch sử cắt bớt — thẻ chỉ số phải luôn nói về toàn kho. --%>
                <div class="stat-value">${not empty tongSoLo ? tongSoLo : stockInHistory.size()}</div>
                <div class="stat-sub">lô hàng đã nhập</div>
            </div>

            <div class="stat-card tg-rise" style="animation-delay:160ms;">
                <div class="ad-bar" style="background:var(--violet);"></div>
                <div class="ad-top">
                    <span class="stat-label">LƯỢT XUẤT KHO</span>
                    <span class="ad-ico" style="background:var(--violet-bg);">
                        <svg width="15" height="15" viewBox="0 0 16 16" fill="none" stroke="#7A5CC7" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M8 9.8v-7M5 5.5 8 2.5l3 3"/><path d="M2.5 12.5h11"/></svg>
                    </span>
                </div>
                <div class="stat-value">${stockOutHistory.size()}</div>
                <div class="stat-sub">đã giao cho đơn hàng</div>
            </div>

            <div class="stat-card tg-rise" style="animation-delay:240ms;">
                <div class="ad-bar" style="background:${empty lowStock ? 'var(--ok-2)' : 'var(--warn-2)'};"></div>
                <div class="ad-top">
                    <span class="stat-label">SẮP HẾT HÀNG</span>
                    <span class="ad-ico" style="background:${empty lowStock ? 'var(--ok-bg-2)' : 'var(--warn-bg-2)'};">
                        <svg width="15" height="15" viewBox="0 0 16 16" fill="none" stroke="${empty lowStock ? '#10A268' : '#E08600'}" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M8 2 1.5 13.5h13L8 2Z"/><path d="M8 6.5v3.2M8 11.6v.4"/></svg>
                    </span>
                </div>
                <div class="stat-value">${lowStock.size()}</div>
                <div class="stat-sub">${empty lowStock ? 'kho đang ổn' : 'cần nhập bổ sung'}</div>
            </div>
        </div>

        <!-- CẢNH BÁO HÀNG SẮP HẾT -->
        <c:if test="${not empty lowStock}">
            <div class="low-stock-alert">
                <h3>Cảnh báo: ${lowStock.size()} sản phẩm sắp hết hàng (tồn ≤ 10)</h3>
                <table class="admin-table" style="margin:0;">
                    <thead>
                    <tr>
                        <th>Tên sản phẩm</th>
                        <th>Danh mục</th>
                        <th>Tồn kho</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="p" items="${lowStock}">
                        <tr>
                            <td style="font-weight:500;">${p.ten_sp}</td>
                            <td>${p.danh_muc}</td>
                            <td>
                                <c:choose>
                                    <c:when test="${p.ton_hien_tai == 0}">
                                        <span class="badge-red">Hết hàng</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="badge-yellow">Còn ${p.ton_hien_tai}</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:if>

        <!-- TAB BAR -->
        <div class="tab-bar">
            <button class="tab-btn active" onclick="showTab('tab-stock', this)">Tồn kho</button>
            <button class="tab-btn" onclick="showTab('tab-import-history', this)">Lịch sử nhập lô hàng</button>
            <button class="tab-btn" onclick="showTab('tab-export-history', this)">Lịch sử xuất</button>
            <button class="tab-btn" onclick="showTab('tab-import-form', this)">Nhập kho</button>
        </div>

        <!-- TAB 1: BẢNG TỒN KHO -->
        <div id="tab-stock" class="tab-content active">

            <!-- CHỌN THÁNG XEM BÁO CÁO -->
            <form method="get" action="/warehouse/stock" class="ym-filter">
                <input type="hidden" name="tab" value="stock"/>
                <label for="ymPicker">Xem tổng nhập/xuất của tháng</label>
                <input type="month" id="ymPicker" name="ym"
                       value="${reportYear}-${reportMonth < 10 ? '0' : ''}${reportMonth}"
                       onchange="var v=this.value.split('-'); location.href='/warehouse/stock?year='+v[0]+'&month='+v[1]+'#tab-stock';"/>
                <c:if test="${!isCurrentMonth}">
                    <a href="/warehouse/stock" class="ym-reset">↺ Về tháng này</a>
                </c:if>
            </form>

            <table class="admin-table">
                <thead>
                <tr>
                    <th>Mã SP</th>
                    <th>Tên sản phẩm</th>
                    <th>Danh mục</th>
                    <th>Tổng nhập (T${reportMonth}/${reportYear})</th>
                    <th>Tổng xuất (T${reportMonth}/${reportYear})</th>
                    <th>Tồn hiện tại</th>
                    <th>Trạng thái</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="row" items="${stockReport}">
                    <tr>
                        <td>${row.ma_sp}</td>
                        <td>${row.ten_sp}</td>
                        <td>${row.danh_muc}</td>
                        <td style="color:var(--ok-deep);font-weight:600;">${row.tong_nhap}</td>
                        <td style="color:var(--err);font-weight:600;">${row.tong_xuat}</td>
                        <td style="font-weight:700;">${row.ton_hien_tai}</td>
                        <td>
                            <c:choose>
                                <c:when test="${row.ton_hien_tai > 10}">
                                    <span class="badge-green">Đủ hàng</span>
                                </c:when>
                                <c:when test="${row.ton_hien_tai > 0}">
                                    <span class="badge-yellow">Sắp hết</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge-red">Hết hàng</span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>

        <!-- TAB 2: LỊCH SỬ NHẬP KHO (có cột Còn lại từ stock_batches) -->
        <div id="tab-import-history" class="tab-content">

            <%-- Tìm lô theo tên sản phẩm hoặc số lô. Lọc chạy ở tầng CSDL vì bảng
                 chỉ lấy 100 dòng gần nhất — lọc sau khi lấy sẽ bỏ sót lô cũ hơn. --%>
            <form class="lo-search" method="get" action="/warehouse/stock">
                <input type="hidden" name="tab" value="import-history"/>
                <div class="lo-search-box">
                    <svg width="15" height="15" viewBox="0 0 16 16" fill="none" stroke="currentColor"
                         stroke-width="1.6" stroke-linecap="round"><circle cx="7" cy="7" r="4.5"/><path d="m10.5 10.5 3 3"/></svg>
                    <input type="text" name="timLo" value="${fn:escapeXml(timLo)}"
                           placeholder="Tìm theo tên sản phẩm hoặc số lô…" autocomplete="off"/>
                </div>
                <button type="submit" class="btn-primary lo-search-go">Tìm</button>
                <c:if test="${not empty timLo}">
                    <a href="/warehouse/stock" class="lo-search-clear">Xóa lọc</a>
                    <span class="lo-search-count">${stockInHistory.size()} lô khớp</span>
                </c:if>
            </form>

            <table class="admin-table">
                <thead>
                <tr>
                    <th>Mã lô</th>
                    <th>Thời gian</th>
                    <th>Sản phẩm</th>
                    <th>Số lượng nhập</th>
                    <th>Còn lại</th>
                    <th>Vị trí</th>
                    <th>Tem</th>
                    <th>Giá nhập</th>
                    <th>Nhà cung cấp</th>
                    <th>Người nhập</th>
                    <th>Ghi chú</th>
                </tr>
                </thead>
                <tbody>
                <c:choose>
                    <c:when test="${empty stockInHistory}">
                        <tr><td colspan="11" style="text-align:center;color:var(--ink-3);padding:24px;">Chưa có lịch sử nhập kho</td></tr>
                    </c:when>
                    <c:otherwise>
                        <c:forEach var="h" items="${stockInHistory}">
                            <tr>
                                <td><span class="badge-fifo">#LÔ-${h.ma_lo}</span></td>
                                <td style="font-size:12px;color:var(--ink-3);"><fmt:formatDate value="${h.imported_at}" pattern="dd/MM/yyyy HH:mm"/></td>
                                <td style="font-weight:500;">${h.ten_sp}</td>
                                <td style="color:var(--ok-deep);font-weight:600;">+${h.quantity}</td>
                                <td>
                                    <c:choose>
                                        <c:when test="${h.quantity_left == 0}">
                                            <span class="badge-empty">Đã xuất hết</span>
                                        </c:when>
                                        <c:when test="${h.quantity_left == h.quantity}">
                                            <span class="badge-fifo">Còn đủ ${h.quantity_left}</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge-yellow">Còn ${h.quantity_left}</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <%-- Bình thường chỉ hiện vị trí dạng nhãn cho gọn; bấm nhãn
                                         mới mở ô sửa. Dùng khi dọn kho hoặc gõ nhầm lúc nhập.
                                         Số lô không đổi nên tem trên thùng vẫn đúng. --%>
                                    <div class="vt-cell">
                                        <button type="button" class="vt-chip ${empty h.khu or empty h.ke ? 'vt-chip-trong' : ''}"
                                                onclick="moSuaViTri(this)">
                                            <c:choose>
                                                <c:when test="${empty h.khu or empty h.ke}">chưa gán</c:when>
                                                <c:otherwise><b>${h.khu}</b><span class="vt-sep">·</span>kệ ${h.ke}</c:otherwise>
                                            </c:choose>
                                        </button>
                                        <form action="/warehouse/lo/${h.ma_lo}/vi-tri" method="post" class="vt-form">
                                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                                            <select name="khu" class="vt-khu">
                                                <c:forEach var="k" items="${cacKhu}">
                                                    <option value="${k.key}" ${h.khu == k.key ? 'selected' : ''}>${k.key}</option>
                                                </c:forEach>
                                            </select>
                                            <input type="number" name="ke" min="1" max="${soKeMoiKhu}" list="dsKe"
                                                   value="${h.ke}" class="vt-ke" placeholder="kệ"/>
                                            <button type="submit" class="vt-luu">Lưu</button>
                                            <button type="button" class="vt-huy" onclick="dongSuaViTri(this)">×</button>
                                        </form>
                                    </div>
                                </td>
                                <td><a href="/warehouse/stock?lo=${h.ma_lo}" class="btn-edit" style="padding:4px 10px;font-size:12px;">Xem tem</a></td>
                                <td><fmt:formatNumber value="${h.import_price}" pattern="#,##0"/>đ</td>
                                <td>${h.supplier}</td>
                                <td>${h.nguoi_nhap}</td>
                                <td style="color:var(--ink-3);font-size:13px;">${fn:escapeXml(h.note)}</td>
                            </tr>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
                </tbody>
            </table>
        </div>

        <!-- TAB 3: LỊCH SỬ XUẤT KHO (có cột Giá vốn) -->
        <div id="tab-export-history" class="tab-content">
            <table class="admin-table">
                <thead>
                <tr>
                    <th>Mã lô (xuất từ)</th>
                    <th>Thời gian</th>
                    <th>Sản phẩm</th>
                    <th>Số lượng</th>
                    <th>Giá vốn/cái</th>
                    <th>Mã đơn</th>
                    <th>Người xuất</th>
                    <th>Trạng thái</th>
                    <th>Ghi chú</th>
                </tr>
                </thead>
                <tbody>
                <c:choose>
                    <c:when test="${empty stockOutHistory}">
                        <tr><td colspan="9" style="text-align:center;color:var(--ink-3);padding:24px;">Chưa có lịch sử xuất kho</td></tr>
                    </c:when>
                    <c:otherwise>
                        <c:forEach var="h" items="${stockOutHistory}">
                            <tr>
                                <td><span class="badge-fifo">#LÔ-${h.ma_lo}</span></td>
                                <td style="font-size:12px;color:var(--ink-3);"><fmt:formatDate value="${h.exported_at}" pattern="dd/MM/yyyy HH:mm"/></td>
                                <td style="font-weight:500;">${h.ten_sp}</td>
                                <td style="color:var(--err);font-weight:600;">-${h.quantity}</td>
                                <td>
                                    <c:choose>
                                        <c:when test="${not empty h.import_cost}">
                                            <span style="color:var(--accent);font-weight:600;">
                                                <fmt:formatNumber value="${h.import_cost}" pattern="#,##0"/>đ
                                            </span>
                                        </c:when>
                                        <c:otherwise>
                                            <span style="color:var(--ink-3);">—</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td><a href="/admin/orders" style="color:var(--accent);">#${h.ma_don}</a></td>
                                <td>${h.nguoi_xuat}</td>
                                <td>
                                    <c:choose>
                                        <c:when test="${h.da_hoan}">
                                            <span class="badge-yellow">Đã hoàn kho</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge-neutral">Đã xuất</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td style="color:var(--ink-3);font-size:13px;">${fn:escapeXml(h.note)}</td>
                            </tr>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
                </tbody>
            </table>
        </div>

        <!-- TAB 4: FORM NHẬP KHO -->
        <div id="tab-import-form" class="tab-content">
            <div class="admin-form" style="max-width:600px;">
                <form method="post" action="/warehouse/stock/in">
    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                    <div class="form-group">
                        <label>Sản phẩm *</label>
                        <input type="text" id="productSearchInput" list="productDatalist"
                               placeholder="Gõ để tìm sản phẩm..." autocomplete="off" required
                               class="filter-select" style="width:100%;padding:8px 12px;"/>
                        <datalist id="productDatalist">
                            <c:forEach var="p" items="${products}">
                                <option data-id="${p.id}" value="${p.name} (Tồn: ${p.stockQuantity})"></option>
                            </c:forEach>
                        </datalist>
                        <input type="hidden" name="productId" id="productIdHidden"/>
                    </div>
                    <div class="form-group vr-box" id="variantGroup" style="display:none;">
                        <label style="margin-bottom:4px;">Biến thể *</label>
                        <div class="vr-hint">Sản phẩm này có biến thể — chọn đúng loại/màu cần nhập.</div>
                        <%-- Danh sách thẻ chọn do JS dựng; <select> bên dưới bị ẩn,
                             chỉ để giữ giá trị gửi lên server (name="variantId"). --%>
                        <div id="variantList" class="vr-list"></div>
                        <select name="variantId" id="variantSelect" style="display:none;">
                            <option value=""></option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label>Số lượng nhập *</label>
                        <input type="text" class="js-money-display" inputmode="numeric" placeholder="VD: 50" required/>
                        <input type="hidden" name="quantity" class="js-money-raw"/>
                    </div>
                    <div class="form-group">
                        <label>Giá nhập (VNĐ) *</label>
                        <input type="text" class="js-money-display" inputmode="numeric" placeholder="VD: 1 100 000" required/>
                        <input type="hidden" name="importPrice" class="js-money-raw"/>
                    </div>
                    <div class="form-group">
                        <label>Vị trí cất hàng *</label>
                        <div class="vitri-row">
                            <select name="khu" required>
                                <option value="">— Chọn khu —</option>
                                <c:forEach var="k" items="${cacKhu}">
                                    <option value="${k.key}">${k.value}</option>
                                </c:forEach>
                            </select>
                            <%-- Gõ số là danh sách tự lọc, khỏi phải kéo chọn trong 50 kệ --%>
                            <input type="number" name="ke" min="1" max="${soKeMoiKhu}" list="dsKe"
                                   placeholder="Kệ (1–${soKeMoiKhu})" required autocomplete="off"/>
                        </div>
                        <small class="vitri-hint">Mỗi khu có ${soKeMoiKhu} kệ. Nhập xong sẽ hiện tem để chép lên thùng.</small>
                    </div>
                    <div class="form-group">
                        <label>Nhà cung cấp</label>
                        <input type="text" name="supplier" placeholder="Tên nhà cung cấp"/>
                    </div>
                    <div class="form-group">
                        <label>Ghi chú</label>
                        <textarea name="note" placeholder="Ghi chú..."></textarea>
                    </div>
                    <button type="submit" class="btn-primary">Xác nhận nhập kho</button>
                </form>
            </div>
        </div>

    </div>
</div>

<script>
    function showTab(id, btn) {
        document.querySelectorAll('.tab-content').forEach(t => t.classList.remove('active'));
        document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
        document.getElementById(id).classList.add('active');
        btn.classList.add('active');
    }

    // Sau khi tìm lô hoặc đổi vị trí, trang tải lại nên phải mở đúng tab cũ,
    // không thì người dùng bị đá về tab Tồn kho và tưởng thao tác không ăn.
    (function () {
        var tab = new URLSearchParams(location.search).get('tab');
        if (!tab) return;
        var nut = document.querySelector('.tab-btn[onclick*="tab-' + tab + '"]');
        if (nut) showTab('tab-' + tab, nut);
    })();
</script>
<script src="/static/js/price-format.js"></script>
<script>
    // Gõ tìm sản phẩm -> map ngược lại đúng productId ẩn để gửi lên server
    // + nạp danh sách BIẾN THỂ của sản phẩm đó (Model 2: nhập kho theo biến thể)
    (function () {
        const searchInput  = document.getElementById('productSearchInput');
        const hiddenInput  = document.getElementById('productIdHidden');
        const variantGroup = document.getElementById('variantGroup');
        const variantSelect= document.getElementById('variantSelect');
        if (!searchInput) return;

        let lastLoadedId = null;

        /* Danh sách biến thể dạng thẻ chọn (thay cho <select> cũ):
           mỗi dòng có chấm màu + tên + tồn kho, dòng đang chọn viền xanh.
           variantSelect vẫn là <select> ẩn giữ giá trị gửi lên server, nên
           toàn bộ phần kiểm tra bên dưới không phải sửa gì. */
        const variantList = document.getElementById('variantList');

        function veDanhSach(list) {
            variantList.innerHTML = '';
            list.forEach(function (v) {
                const nhan = v.name + (v.color ? ' — ' + v.color : '');
                const o = document.createElement('button');
                o.type = 'button';
                o.className = 'vr-opt';
                o.dataset.id = v.id;
                o.innerHTML =
                    '<span class="vr-dot" style="background:' + (v.colorCode || '#CFD4DC') + ';"></span>' +
                    '<span class="vr-name">' + nhan + '</span>' +
                    '<span class="vr-stock">Tồn: ' + (v.stock != null ? v.stock : 0) + '</span>';
                o.onclick = function () { chon(v.id); };
                variantList.appendChild(o);
            });
        }

        function chon(id) {
            variantSelect.value = id;
            variantList.querySelectorAll('.vr-opt').forEach(function (b) {
                b.classList.toggle('is-on', Number(b.dataset.id) === Number(id));
            });
        }

        function loadVariants(productId) {
            if (productId === lastLoadedId) return;
            lastLoadedId = productId;
            if (!productId) {
                variantGroup.style.display = 'none';
                variantSelect.required = false;
                variantSelect.innerHTML = '<option value=""></option>';
                variantList.innerHTML = '';
                return;
            }
            fetch('/admin/products/api/variants-lite/' + productId)
                .then(r => r.json())
                .then(list => {
                    variantSelect.innerHTML = '<option value=""></option>';
                    variantList.innerHTML = '';
                    if (list && list.length > 0) {
                        list.forEach(v => {
                            const opt = document.createElement('option');
                            opt.value = v.id;
                            opt.textContent = v.name;
                            variantSelect.appendChild(opt);
                        });
                        veDanhSach(list);
                        variantGroup.style.display = 'block';
                        variantSelect.required = true;
                    } else {
                        variantGroup.style.display = 'none';
                        variantSelect.required = false;
                    }
                })
                .catch(() => { variantGroup.style.display = 'none'; variantSelect.required = false; });
        }

        function syncHiddenId() {
            let matchedId = '';
            document.querySelectorAll('#productDatalist option').forEach(opt => {
                if (opt.value === searchInput.value) matchedId = opt.getAttribute('data-id');
            });
            hiddenInput.value = matchedId;
            loadVariants(matchedId);
        }
        searchInput.addEventListener('input', syncHiddenId);

        searchInput.closest('form').addEventListener('submit', function (e) {
            syncHiddenId();
            if (!hiddenInput.value) {
                e.preventDefault();
                alert('Vui lòng chọn đúng 1 sản phẩm có trong danh sách gợi ý.');
                return;
            }
            if (variantGroup.style.display !== 'none' && !variantSelect.value) {
                e.preventDefault();
                alert('Sản phẩm này có biến thể — vui lòng chọn biến thể cần nhập.');
            }
        });
    })();

    // Mở / đóng ô sửa vị trí ngay trong dòng. Mỗi lúc chỉ mở một ô cho đỡ rối bảng.
    function moSuaViTri(nut) {
        document.querySelectorAll('.vt-cell.dang-sua').forEach(function (o) { o.classList.remove('dang-sua'); });
        var o = nut.closest('.vt-cell');
        o.classList.add('dang-sua');
        var ke = o.querySelector('.vt-ke');
        if (ke) { ke.focus(); ke.select(); }
    }
    function dongSuaViTri(nut) {
        nut.closest('.vt-cell').classList.remove('dang-sua');
    }
</script>

<%-- Danh sách số kệ dùng chung cho form nhập kho và các ô sửa vị trí trong bảng.
     Gõ số là trình duyệt tự lọc, khỏi phải kéo tìm trong 50 kệ. --%>
<datalist id="dsKe">
    <c:forEach var="k" items="${danhSachKe}"><option value="${k}"></option></c:forEach>
</datalist>

</body>
</html>

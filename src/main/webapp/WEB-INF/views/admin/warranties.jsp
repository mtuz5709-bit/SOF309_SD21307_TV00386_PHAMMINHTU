<%@ page contentType="text/html;charset=UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>Bảo Hành – TiTanGEAR</title>
    <link href="https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@400;500;600;700&family=IBM+Plex+Mono:wght@400;500;600&display=swap" rel="stylesheet"/>
    <link rel="stylesheet" href="/static/css/tokens.css"/>
    <link rel="stylesheet" href="/static/css/main.css"/>
    <link rel="stylesheet" href="/static/css/admin.css"/>
    <style>
        .filter-bar { display: flex; gap: 8px; margin-bottom: 12px; flex-wrap: wrap; align-items: center; }
        .filter-tab { padding: 7px 15px; font-size: 13px; text-decoration: none; border: 1px solid var(--line-new); color: var(--ink-2); background: var(--surface); }
        .filter-tab:hover { border-color: var(--ink); color: var(--ink); }
        .filter-tab.active { background: var(--accent-soft-2); color: var(--accent); border-color: var(--accent); font-weight: 600; }
        .con-lai { font-family: var(--font-mono); font-size: 11px; color: var(--ink-3); display: block; margin-top: 2px; }
    </style>
</head>
<body>
<div class="admin-layout">
    <c:set var="adminActive" value="warranties"/>
    <%@ include file="/WEB-INF/views/common/admin-sidebar.jsp" %>

    <div class="admin-main">
        <c:set var="adminTitle" value="Bảo Hành"/>
        <c:set var="adminCrumb" value="ADMIN / WARRANTIES"/>
        <c:set var="adminSearchUrl" value="/admin/warranties"/>
        <c:set var="adminSearchPlaceholder" value="Tìm theo mã bảo hành…"/>
        <%@ include file="/WEB-INF/views/common/admin-topbar.jsp" %>

        <%-- 4 thẻ chỉ số — dùng lại đúng các con số controller đã tính sẵn cho
             tab lọc bên dưới, không phải đếm thêm lần nào. --%>
        <div class="stat-cards">
            <div class="stat-card tg-rise">
                <div class="ad-bar" style="background:var(--accent);"></div>
                <div class="ad-top">
                    <span class="stat-label">TỔNG PHIẾU</span>
                    <span class="ad-ico" style="background:var(--accent-soft-2);">
                        <svg width="15" height="15" viewBox="0 0 16 16" fill="none" stroke="#1B49FF" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M8 1.8 3 3.4v4c0 3 2.1 5.3 5 6.6 2.9-1.3 5-3.6 5-6.6v-4L8 1.8Z"/></svg>
                    </span>
                </div>
                <div class="stat-value">${tong}</div>
                <div class="stat-sub">đã phát cho khách</div>
            </div>

            <div class="stat-card tg-rise" style="animation-delay:80ms;">
                <div class="ad-bar" style="background:var(--ok-2);"></div>
                <div class="ad-top">
                    <span class="stat-label">CÒN HẠN</span>
                    <span class="ad-ico" style="background:var(--ok-bg-2);">
                        <svg width="15" height="15" viewBox="0 0 16 16" fill="none" stroke="#10A268" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="8" cy="8" r="6"/><path d="M5.4 8.2 7.2 10l3.4-3.6"/></svg>
                    </span>
                </div>
                <div class="stat-value">${soConHan}</div>
                <div class="stat-sub">đang được bảo hành</div>
            </div>

            <div class="stat-card tg-rise" style="animation-delay:160ms;">
                <div class="ad-bar" style="background:var(--warn-2);"></div>
                <div class="ad-top">
                    <span class="stat-label">SẮP HẾT HẠN</span>
                    <span class="ad-ico" style="background:var(--warn-bg-2);">
                        <svg width="15" height="15" viewBox="0 0 16 16" fill="none" stroke="#E08600" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="8" cy="8" r="6"/><path d="M8 4.8V8l2.2 1.3"/></svg>
                    </span>
                </div>
                <div class="stat-value">${soSapHet}</div>
                <div class="stat-sub">còn dưới 30 ngày</div>
            </div>

            <div class="stat-card tg-rise" style="animation-delay:240ms;">
                <div class="ad-bar" style="background:var(--err);"></div>
                <div class="ad-top">
                    <span class="stat-label">HẾT HẠN</span>
                    <span class="ad-ico" style="background:var(--err-bg-2);">
                        <svg width="15" height="15" viewBox="0 0 16 16" fill="none" stroke="#C62828" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M8 2 1.5 13.5h13L8 2Z"/><path d="M8 6.5v3.2M8 11.6v.4"/></svg>
                    </span>
                </div>
                <div class="stat-value">${soHetHan}</div>
                <div class="stat-sub">không nhận sửa nữa</div>
            </div>
        </div>

        <%-- Trạng thái tính theo ngày hôm nay, KHÔNG lưu trong CSDL nên phiếu tự
             rớt sang "Hết hạn" đúng ngày, admin không phải dò tay. --%>
        <div class="filter-bar">
            <a href="<c:url value='/admin/warranties'><c:param name='q' value='${param.q}'/></c:url>"
               class="filter-tab ${empty param.trangThai ? 'active' : ''}">
                Tất cả <span class="tab-count">${tong}</span></a>
            <a href="<c:url value='/admin/warranties'><c:param name='trangThai' value='CON_HAN'/><c:param name='q' value='${param.q}'/></c:url>"
               class="filter-tab ${param.trangThai == 'CON_HAN' ? 'active' : ''}">
                Còn hạn <span class="tab-count">${soConHan}</span></a>
            <a href="<c:url value='/admin/warranties'><c:param name='trangThai' value='SAP_HET'/><c:param name='q' value='${param.q}'/></c:url>"
               class="filter-tab ${param.trangThai == 'SAP_HET' ? 'active' : ''}">
                Sắp hết (≤30 ngày) <span class="tab-count">${soSapHet}</span></a>
            <a href="<c:url value='/admin/warranties'><c:param name='trangThai' value='HET_HAN'/><c:param name='q' value='${param.q}'/></c:url>"
               class="filter-tab ${param.trangThai == 'HET_HAN' ? 'active' : ''}">
                Hết hạn <span class="tab-count">${soHetHan}</span></a>
            <span class="filter-count">Đang xem: ${warranties.size()} phiếu</span>
        </div>

        <table class="admin-table">
            <thead>
            <tr>
                <th>Mã bảo hành</th>
                <th>Sản phẩm</th>
                <th>Khách hàng</th>
                <th>Bắt đầu</th>
                <th>Hết hạn</th>
                <th>Trạng thái</th>
                <th>Thao tác</th>
            </tr>
            </thead>
            <tbody>
            <c:choose>
                <c:when test="${empty warranties}">
                    <tr><td colspan="7" style="text-align:center;padding:40px;color:var(--ink-3);">
                        Chưa có phiếu bảo hành nào. Phiếu được tạo tự động khi đơn hàng chuyển sang
                        <strong>Hoàn thành</strong> (với sản phẩm có khai số tháng bảo hành).
                    </td></tr>
                </c:when>
                <c:otherwise>
                    <c:forEach var="w" items="${warranties}">
                        <tr>
                            <td><strong style="font-family:var(--font-mono);">${w.serialNo}</strong></td>
                            <td>${tenSanPham[w.productId]}</td>
                            <td>${tenKhach[w.userId]}</td>
                            <td style="font-family:var(--font-mono);font-size:12px;">${w.startDate}</td>
                            <td style="font-family:var(--font-mono);font-size:12px;">
                                ${w.endDate}
                                <span class="con-lai">
                                    <c:choose>
                                        <c:when test="${w.conHan}">còn ${w.soNgayConLai} ngày</c:when>
                                        <c:otherwise>quá ${-w.soNgayConLai} ngày</c:otherwise>
                                    </c:choose>
                                </span>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${w.sapHetHan}"><span class="badge-yellow">Sắp hết</span></c:when>
                                    <c:when test="${w.conHan}"><span class="badge-green">Còn hạn</span></c:when>
                                    <c:otherwise><span class="badge-red">Hết hạn</span></c:otherwise>
                                </c:choose>
                            </td>
                            <td><a href="/admin/warranties/${w.id}" class="btn-edit">Chi tiết</a></td>
                        </tr>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
            </tbody>
        </table>
    </div>
</div>
</body>
</html>

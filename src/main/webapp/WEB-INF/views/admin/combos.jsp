<%@ page contentType="text/html;charset=UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>Quản lý Combo – TiTanGEAR</title>
    <link href="https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@400;500;600;700&family=IBM+Plex+Mono:wght@400;500;600&display=swap" rel="stylesheet"/>
    <link rel="stylesheet" href="/static/css/tokens.css"/>
    <link rel="stylesheet" href="/static/css/main.css"/>
    <link rel="stylesheet" href="/static/css/admin.css"/>
    <style>
        .cb-mon      { display:flex; gap:6px; align-items:center; flex-wrap:wrap; }
        .cb-mon-chip { display:inline-flex; align-items:center; gap:6px; background:var(--surface-3);
                       border:1px solid var(--line-new); border-radius:var(--r-pill);
                       padding:3px 10px; font-size:12px; color:var(--ink-2); }
        .cb-dot      { width:10px; height:10px; border-radius:50%; border:1px solid var(--line-new); }
        .cb-vach     { display:inline-block; width:10px; height:10px; border-radius:3px; margin-right:8px; }
    </style>
</head>
<body>
<div class="admin-layout">
    <c:set var="adminActive" value="combos"/>
    <%@ include file="/WEB-INF/views/common/admin-sidebar.jsp" %>

    <div class="admin-main">
        <c:set var="adminTitle" value="Quản lý Combo"/>
        <c:set var="adminCrumb" value="ADMIN / COMBOS"/>
        <%@ include file="/WEB-INF/views/common/admin-topbar.jsp" %>

        <c:if test="${not empty comboError}">
            <div class="admin-alert-err">${comboError}</div>
        </c:if>

        <div class="stat-cards">
            <div class="stat-card">
                <div class="stat-label">Tổng combo</div>
                <div class="stat-value">${tongCombo}</div>
                <div class="stat-sub">Gồm cả combo đã tắt</div>
            </div>
            <div class="stat-card">
                <div class="stat-label">Đang bán</div>
                <div class="stat-value">${comboDangBan}</div>
                <div class="stat-sub">Hiện trên trang khách</div>
            </div>
            <div class="stat-card">
                <div class="stat-label">Đã tắt</div>
                <div class="stat-value">${comboDaTat}</div>
                <div class="stat-sub">Không hiện cho khách</div>
            </div>
            <div class="stat-card">
                <div class="stat-label">Mức giảm cao nhất</div>
                <div class="stat-value"><fmt:formatNumber value="${giamCaoNhat}" pattern="#,##0"/>đ</div>
                <div class="stat-sub">Tổng ${tongMon} món trong các bộ</div>
            </div>
        </div>

        <div style="display:flex;justify-content:flex-end;margin-bottom:14px;">
            <a href="/admin/combos/add" class="btn-primary">+ Thêm combo</a>
        </div>

        <div class="admin-card">
            <table class="admin-table">
                <thead>
                <tr>
                    <th>ID</th>
                    <th>Tên combo</th>
                    <th>Nhãn</th>
                    <th>Các món trong bộ</th>
                    <th>Tiền giảm</th>
                    <th>Trạng thái</th>
                    <th>Thao tác</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="cb" items="${combos}">
                    <tr>
                        <td class="tg-mono">#${cb.id}</td>
                        <td>
                            <span class="cb-vach" style="background:${empty cb.accentColor ? '#1B49FF' : cb.accentColor};"></span>
                            <strong>${cb.name}</strong>
                        </td>
                        <td>
                            <c:if test="${not empty cb.tag}"><span class="badge-neutral">${cb.tag}</span></c:if>
                        </td>
                        <td>
                            <div class="cb-mon">
                                <c:forEach var="it" items="${cb.items}">
                                    <span class="cb-mon-chip">
                                        <c:if test="${it.variant != null}">
                                            <span class="cb-dot" style="background:${empty it.variant.colorCode ? '#CFD4DC' : it.variant.colorCode};"></span>
                                        </c:if>
                                        ${it.product.name}<c:if test="${it.quantity > 1}"> × ${it.quantity}</c:if>
                                    </span>
                                </c:forEach>
                                <c:if test="${empty cb.items}">
                                    <span style="color:var(--err);font-size:12px;">Chưa có món nào</span>
                                </c:if>
                            </div>
                        </td>
                        <td class="tg-mono"><fmt:formatNumber value="${cb.discountAmount}" pattern="#,##0"/>đ</td>
                        <td>
                            <c:choose>
                                <c:when test="${cb.isActive}"><span class="badge-green">Đang bán</span></c:when>
                                <c:otherwise><span class="badge-neutral">Đã tắt</span></c:otherwise>
                            </c:choose>
                        </td>
                        <td style="white-space:nowrap;">
                            <a href="/admin/combos/edit/${cb.id}" class="btn-edit">Sửa</a>
                            <a href="/admin/combos/toggle/${cb.id}" class="btn-delete" data-post
                               data-confirm="${cb.isActive ? 'Tắt' : 'Bật lại'} combo này?">
                                ${cb.isActive ? 'Tắt' : 'Bật'}
                            </a>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty combos}">
                    <tr><td colspan="7" style="text-align:center;padding:40px;color:var(--ink-3);">
                        Chưa có combo nào. Bấm <strong>+ Thêm combo</strong> để tạo bộ đầu tiên.
                    </td></tr>
                </c:if>
                </tbody>
            </table>
        </div>

        <p style="font-size:12px;color:var(--ink-3);margin-top:14px;line-height:1.7;">
            Combo chỉ hiện với khách khi <strong>mọi món trong bộ còn hàng và đang bán</strong>.
            Combo có món hết hàng sẽ tự ẩn khỏi trang chủ, không cần tắt tay.
        </p>
    </div>
</div>
</body>
</html>

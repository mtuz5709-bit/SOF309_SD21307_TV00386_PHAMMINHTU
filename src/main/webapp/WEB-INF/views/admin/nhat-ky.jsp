<%@ page contentType="text/html;charset=UTF-8" isELIgnored="false" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>Nhật ký thao tác – TiTanGEAR</title>
    <link href="https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@400;500;600;700&family=IBM+Plex+Mono:wght@400;500;600&display=swap" rel="stylesheet"/>
    <link rel="stylesheet" href="/static/css/tokens.css"/>
    <link rel="stylesheet" href="/static/css/main.css"/>
    <link rel="stylesheet" href="/static/css/admin.css"/>
    <style>
        .nk-act {
            font-family: var(--font-mono); font-size: 10px; font-weight: 600; letter-spacing: .06em;
            padding: 4px 10px; border-radius: var(--r-pill); white-space: nowrap;
            background: var(--surface-3); color: var(--ink-2); border: 1px solid var(--line-new);
        }
        .nk-act.la-tien  { background: var(--err-bg-2);    color: var(--err);       border-color: var(--err-line-2); }
        .nk-act.la-kho   { background: var(--accent-soft-2); color: var(--accent);  border-color: var(--accent-line); }
        .nk-act.la-huy   { background: var(--warn-bg-2);   color: var(--warn-deep); border-color: var(--warn-line-2); }
        .nk-doi { font-family: var(--font-mono); font-size: 12px; }
        .nk-cu  { color: var(--ink-3); text-decoration: line-through; }
        .nk-moi { color: var(--ok-deep); font-weight: 600; }
        .nk-ip  { font-family: var(--font-mono); font-size: 11px; color: var(--ink-4); }
        .nk-note{ font-size: 12px; color: var(--ink-3); margin-top: 3px; }
    </style>
</head>
<body>
<div class="admin-layout">
    <c:set var="adminActive" value="nhatky"/>
    <%@ include file="/WEB-INF/views/common/admin-sidebar.jsp" %>

    <div class="admin-main">
        <c:set var="adminTitle" value="Nhật ký thao tác"/>
        <c:set var="adminCrumb" value="ADMIN / AUDIT LOG"/>
        <%@ include file="/WEB-INF/views/common/admin-topbar.jsp" %>

        <div style="background:var(--accent-soft-2);border:1px solid var(--accent-line);color:var(--accent);
                    border-radius:var(--r-md);padding:12px 16px;font-size:13px;line-height:1.7;margin-bottom:18px;">
            Ghi lại mọi thao tác đụng tới <strong>tiền, tồn kho và cấu hình hệ thống</strong>.
            Nhật ký chỉ ghi thêm — không sửa, không xóa được, kể cả bằng tài khoản quản trị.
        </div>

        <div class="filter-bar">
            <a href="/admin/nhat-ky" class="filter-tab ${empty hanhDongLoc ? 'active' : ''}">
                Tất cả <span class="tab-count">${tongDong}</span>
            </a>
            <c:forEach var="loai" items="${cacLoai}">
                <a href="/admin/nhat-ky?hanhDong=${loai.key}"
                   class="filter-tab ${hanhDongLoc == loai.key ? 'active' : ''}">${loai.value}</a>
            </c:forEach>
        </div>

        <table class="admin-table">
            <thead>
            <tr>
                <th style="width:150px;">Thời điểm</th>
                <th style="width:210px;">Người thao tác</th>
                <th style="width:150px;">Hành động</th>
                <th>Nội dung</th>
                <th style="width:200px;">Thay đổi</th>
                <th style="width:120px;">Địa chỉ IP</th>
            </tr>
            </thead>
            <tbody>
            <c:choose>
                <c:when test="${empty nhatKy}">
                    <tr><td colspan="6" style="text-align:center;padding:44px;color:var(--ink-3);">
                        Chưa có thao tác nào được ghi lại
                    </td></tr>
                </c:when>
                <c:otherwise>
                    <c:forEach var="nk" items="${nhatKy}">
                        <tr>
                            <td class="nk-doi" style="color:var(--ink-2);">
                                <fmt:parseDate value="${nk.thoiDiem}" pattern="yyyy-MM-dd'T'HH:mm:ss" var="td" type="both"/>
                                <fmt:formatDate value="${td}" pattern="dd/MM/yyyy"/><br/>
                                <span style="color:var(--ink-4);"><fmt:formatDate value="${td}" pattern="HH:mm:ss"/></span>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${not empty nk.userEmail}">${fn:escapeXml(nk.userEmail)}</c:when>
                                    <c:otherwise><span style="color:var(--ink-4);">(hệ thống)</span></c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <%-- Tô màu theo mức nhạy cảm: đỏ = đụng tiền, xanh = đụng kho, cam = hủy --%>
                                <c:set var="lop" value=""/>
                                <c:if test="${nk.hanhDong == 'SUA_GIA' || nk.hanhDong == 'DOI_CAI_DAT'}"><c:set var="lop" value="la-tien"/></c:if>
                                <c:if test="${nk.hanhDong == 'DUYET_DON' || nk.hanhDong == 'NHAP_KHO'}"><c:set var="lop" value="la-kho"/></c:if>
                                <c:if test="${nk.hanhDong == 'HUY_DON'}"><c:set var="lop" value="la-huy"/></c:if>
                                <span class="nk-act ${lop}">${nk.tenHanhDong}</span>
                            </td>
                            <td>
                                ${fn:escapeXml(nk.moTa)}
                                <c:if test="${not empty nk.doiTuong}">
                                    <div class="nk-note">${nk.doiTuong}<c:if test="${not empty nk.doiTuongId}"> #${nk.doiTuongId}</c:if></div>
                                </c:if>
                            </td>
                            <td class="nk-doi">
                                <c:choose>
                                    <c:when test="${not empty nk.giaTriCu or not empty nk.giaTriMoi}">
                                        <span class="nk-cu">${fn:escapeXml(nk.giaTriCu)}</span>
                                        <span style="color:var(--ink-4);"> → </span>
                                        <span class="nk-moi">${fn:escapeXml(nk.giaTriMoi)}</span>
                                    </c:when>
                                    <c:otherwise><span style="color:var(--ink-4);">—</span></c:otherwise>
                                </c:choose>
                            </td>
                            <td class="nk-ip">${fn:escapeXml(nk.diaChiIp)}</td>
                        </tr>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
            </tbody>
        </table>

        <c:if test="${tongTrang > 1}">
            <div class="pagination">
                <c:forEach begin="0" end="${tongTrang - 1}" var="i">
                    <a href="/admin/nhat-ky?page=${i}<c:if test='${not empty hanhDongLoc}'>&hanhDong=${hanhDongLoc}</c:if>"
                       class="page-btn ${i == trangHienTai ? 'active' : ''}">${i + 1}</a>
                </c:forEach>
            </div>
        </c:if>
    </div>
</div>
</body>
</html>

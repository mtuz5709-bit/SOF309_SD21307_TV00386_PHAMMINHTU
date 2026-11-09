<%@ page contentType="text/html;charset=UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <title>Quản lý Flash Sale – TiTanGEAR</title>
    <link href="https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@400;500;600;700&family=IBM+Plex+Mono:wght@400;500;600&display=swap" rel="stylesheet"/>
    <link rel="stylesheet" href="/static/css/tokens.css"/>
    <link rel="stylesheet" href="/static/css/main.css"/>
    <link rel="stylesheet" href="/static/css/admin.css"/>
</head>
<body>

<div class="admin-layout">
    <c:set var="adminActive" value="flashsale"/>
    <%@ include file="/WEB-INF/views/common/admin-sidebar.jsp" %>

    <div class="admin-main">
        <c:set var="adminTitle" value="Quản lý Flash Sale"/>
        <c:set var="adminCrumb" value="ADMIN / FLASH-SALES"/>
        <c:set var="adminAction" value="+ Tạo Flash Sale"/>
        <c:set var="adminActionUrl" value="/admin/flash-sales/add"/>
        <%@ include file="/WEB-INF/views/common/admin-topbar.jsp" %>

        <c:if test="${param.created == 'true'}">
            <div class="admin-alert-ok">Đã tạo flash sale thành công.</div>
        </c:if>
        <c:if test="${param.updated == 'true'}">
            <div class="admin-alert-ok">Đã cập nhật flash sale.</div>
        </c:if>
        <c:if test="${not empty loi}">
            <div class="admin-alert-err">${loi}</div>
        </c:if>

        <table class="admin-table">
            <thead>
            <tr>
                <th>ID</th>
                <th>Tên chương trình</th>
                <th>Kiểu hiển thị</th>
                <th>Bắt đầu</th>
                <th>Kết thúc</th>
                <th>Số SP áp dụng</th>
                <th>Trạng thái</th>
                <th>Thao tác</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="fs" items="${flashSales}">
                <tr>
                    <td>${fs.id}</td>
                    <td>
                        ${fs.name}
                        <c:if test="${fs.displayType == 'BANNER'}">
                            <div style="font-size:12px;color:var(--ink-3);">${fs.bannerTitle}</div>
                        </c:if>
                        <c:if test="${fs.displayType == 'TEXT_BAR'}">
                            <div style="font-size:12px;color:var(--ink-3);">"${fs.textBarContent}"</div>
                        </c:if>
                    </td>
                    <td>
                        <c:choose>
                            <c:when test="${fs.displayType == 'BANNER'}"><span class="badge-blue">Banner</span></c:when>
                            <c:otherwise><span class="badge-neutral">Text bar</span></c:otherwise>
                        </c:choose>
                    </td>
                    <td>${fs.startAt.dayOfMonth}/${fs.startAt.monthValue}/${fs.startAt.year} ${fs.startAt.hour}:${fs.startAt.minute < 10 ? '0' : ''}${fs.startAt.minute}</td>
                    <td>${fs.endAt.dayOfMonth}/${fs.endAt.monthValue}/${fs.endAt.year} ${fs.endAt.hour}:${fs.endAt.minute < 10 ? '0' : ''}${fs.endAt.minute}</td>
                    <td>${itemCounts[fs.id] != null ? itemCounts[fs.id] : 0}</td>
                    <td>
                        <c:choose>
                            <c:when test="${!fs.isActive}">
                                <span class="badge-red">Đã tắt</span>
                            </c:when>
                            <c:when test="${fs.startAt.compareTo(now) > 0}">
                                <span class="badge-yellow">Sắp diễn ra</span>
                            </c:when>
                            <c:when test="${fs.endAt.compareTo(now) < 0}">
                                <span class="badge-red">Đã hết hạn</span>
                            </c:when>
                            <c:otherwise>
                                <span class="badge-green">Đang chạy</span>
                            </c:otherwise>
                        </c:choose>
                    </td>
                    <td>
                        <a href="/admin/flash-sales/edit/${fs.id}" class="btn-edit">Sửa</a>
                        <a href="/admin/flash-sales/toggle/${fs.id}" class="btn-edit" data-post
                           data-confirm="${fs.isActive ? 'Tắt' : 'Bật lại'} đợt flash sale này?">
                            ${fs.isActive ? 'Tắt' : 'Bật'}
                        </a>
                        <a href="/admin/flash-sales/delete/${fs.id}" class="btn-delete" data-post
                           data-confirm="Xóa flash sale này?">Xóa</a>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty flashSales}">
                <tr><td colspan="8" style="text-align:center;color:var(--ink-3);padding:24px;">Chưa có flash sale nào.</td></tr>
            </c:if>
            </tbody>
        </table>
    </div>
</div>

</body>
</html>

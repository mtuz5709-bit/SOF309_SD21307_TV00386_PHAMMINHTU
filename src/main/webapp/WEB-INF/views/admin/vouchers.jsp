<%@ page contentType="text/html;charset=UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>Mã Giảm Giá – TiTanGEAR</title>
    <link href="https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@400;500;600;700&family=IBM+Plex+Mono:wght@400;500;600&display=swap" rel="stylesheet"/>
    <link rel="stylesheet" href="/static/css/tokens.css"/>
    <link rel="stylesheet" href="/static/css/main.css"/>
    <link rel="stylesheet" href="/static/css/admin.css"/>
</head>
<body>
<div class="admin-layout">
    <c:set var="adminActive" value="vouchers"/>
    <%@ include file="/WEB-INF/views/common/admin-sidebar.jsp" %>

    <div class="admin-main">
        <c:set var="adminTitle" value="Mã Giảm Giá"/>
        <c:set var="adminCrumb" value="ADMIN / VOUCHERS"/>
        <c:set var="adminAction" value="+ Tạo mã mới"/>
        <c:set var="adminActionUrl" value="/admin/vouchers/add"/>
        <%@ include file="/WEB-INF/views/common/admin-topbar.jsp" %>

        <c:if test="${param.saved == 'true'}">
            <div class="admin-alert-ok">Đã lưu mã giảm giá!</div>
        </c:if>

        <!-- ===== 4 THẺ CHỈ SỐ ===== -->
        <div class="stat-cards">
            <div class="stat-card tg-rise">
                <div class="ad-bar" style="background:var(--accent);"></div>
                <div class="ad-top">
                    <span class="stat-label">TỔNG SỐ MÃ</span>
                    <span class="ad-ico" style="background:var(--accent-soft-2);">
                        <svg width="15" height="15" viewBox="0 0 16 16" fill="none" stroke="#1B49FF" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M2 5.5h12V7a1.5 1.5 0 0 0 0 2v1.5H2V9a1.5 1.5 0 0 0 0-2V5.5Z"/><path d="M9.5 5.5v5" stroke-dasharray="1.6 1.6"/></svg>
                    </span>
                </div>
                <div class="stat-value">${tongMa}</div>
                <div class="stat-sub">đã tạo từ trước tới nay</div>
            </div>

            <div class="stat-card tg-rise" style="animation-delay:80ms;">
                <div class="ad-bar" style="background:var(--ok-2);"></div>
                <div class="ad-top">
                    <span class="stat-label">ĐANG CHẠY</span>
                    <span class="ad-ico" style="background:var(--ok-bg-2);">
                        <svg width="15" height="15" viewBox="0 0 16 16" fill="none" stroke="#10A268" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="8" cy="8" r="6"/><path d="M5.4 8.2 7.2 10l3.4-3.6"/></svg>
                    </span>
                </div>
                <div class="stat-value">${maDangChay}</div>
                <div class="stat-sub">khách đang dùng được</div>
            </div>

            <div class="stat-card tg-rise" style="animation-delay:160ms;">
                <div class="ad-bar" style="background:var(--ink-4);"></div>
                <div class="ad-top">
                    <span class="stat-label">HẾT HẠN</span>
                    <span class="ad-ico" style="background:var(--surface-3);">
                        <svg width="15" height="15" viewBox="0 0 16 16" fill="none" stroke="#8A9099" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="8" cy="8" r="6"/><path d="M8 4.8V8l2.2 1.3"/></svg>
                    </span>
                </div>
                <div class="stat-value">${maHetHan}</div>
                <div class="stat-sub">đã qua ngày kết thúc</div>
            </div>

            <div class="stat-card tg-rise" style="animation-delay:240ms;">
                <div class="ad-bar" style="background:var(--violet);"></div>
                <div class="ad-top">
                    <span class="stat-label">TỔNG LƯỢT DÙNG</span>
                    <span class="ad-ico" style="background:var(--violet-bg);">
                        <svg width="15" height="15" viewBox="0 0 16 16" fill="none" stroke="#7A5CC7" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M2 13h12"/><path d="M4 10.5V7M7.3 10.5V4.5M10.6 10.5V8"/></svg>
                    </span>
                </div>
                <div class="stat-value">${tongLuotDung}</div>
                <div class="stat-sub">lượt khách đã áp mã</div>
            </div>
        </div>

        <table class="admin-table">
            <thead>
            <tr>
                <th>Mã</th>
                <th>Giảm</th>
                <th>Điều kiện</th>
                <th>Hiệu lực</th>
                <th>Đã dùng</th>
                <th>Trạng thái</th>
                <th>Thao tác</th>
            </tr>
            </thead>
            <tbody>
            <c:choose>
                <c:when test="${empty vouchers}">
                    <tr><td colspan="7" style="text-align:center;padding:40px;color:var(--ink-3);">
                        Chưa có mã giảm giá nào. Bấm "+ Tạo mã mới" để thêm.
                    </td></tr>
                </c:when>
                <c:otherwise>
                    <c:forEach var="v" items="${vouchers}">
                        <tr>
                            <td>
                                <strong style="font-family:var(--font-mono);letter-spacing:0.04em;">${v.code}</strong>
                                <c:if test="${not empty v.description}">
                                    <div style="font-size:12px;color:var(--ink-3);margin-top:2px;">${v.description}</div>
                                </c:if>
                            </td>
                            <td style="color:var(--accent);font-weight:700;white-space:nowrap;">
                                <c:choose>
                                    <c:when test="${v.discountType == 'PERCENT'}">
                                        <fmt:formatNumber value="${v.discountValue}" pattern="#,##0.##"/>%
                                        <c:if test="${not empty v.maxDiscountAmount}">
                                            <div style="font-size:11px;color:var(--ink-3);font-weight:400;">
                                                tối đa <fmt:formatNumber value="${v.maxDiscountAmount}" pattern="#,##0"/>đ
                                            </div>
                                        </c:if>
                                    </c:when>
                                    <c:otherwise>
                                        <fmt:formatNumber value="${v.discountValue}" pattern="#,##0"/>đ
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td style="font-size:12px;color:var(--ink-2);">
                                <c:choose>
                                    <c:when test="${not empty v.minOrderAmount}">
                                        Đơn từ <fmt:formatNumber value="${v.minOrderAmount}" pattern="#,##0"/>đ
                                    </c:when>
                                    <c:otherwise>Không yêu cầu</c:otherwise>
                                </c:choose>
                                <c:if test="${v.oncePerUser}">
                                    <div style="color:var(--ink-3);">1 lần / khách</div>
                                </c:if>
                            </td>
                            <td style="font-size:12px;color:var(--ink-2);white-space:nowrap;">
                                <c:choose>
                                    <c:when test="${empty v.startAt && empty v.endAt}">Không giới hạn</c:when>
                                    <c:otherwise>
                                        <c:if test="${not empty v.startAt}">${v.startAt.toString().substring(0,10)}</c:if>
                                        →
                                        <c:choose>
                                            <c:when test="${not empty v.endAt}">${v.endAt.toString().substring(0,10)}</c:when>
                                            <c:otherwise>…</c:otherwise>
                                        </c:choose>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td style="font-family:var(--font-mono);">
                                ${v.usedCount}<c:if test="${not empty v.usageLimit}"> / ${v.usageLimit}</c:if>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${v.active}"><span class="badge-green">Đang bật</span></c:when>
                                    <c:otherwise><span class="badge-neutral">Đã tắt</span></c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <a href="/admin/vouchers/edit/${v.id}" class="btn-edit">Sửa</a>
                                <a href="/admin/vouchers/toggle/${v.id}" class="btn-variants" data-post
                                   data-confirm="${v.active ? 'Tắt' : 'Bật lại'} voucher này?">
                                    ${v.active ? 'Tắt' : 'Bật'}
                                </a>
                            </td>
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

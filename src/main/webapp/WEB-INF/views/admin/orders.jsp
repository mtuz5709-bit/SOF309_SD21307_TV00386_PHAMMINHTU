<%@ page contentType="text/html;charset=UTF-8" isELIgnored="false" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>Quản lý Đơn hàng – TiTanGEAR</title>
    <link href="https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@400;500;600;700&family=IBM+Plex+Mono:wght@400;500;600&display=swap" rel="stylesheet"/>
    <link rel="stylesheet" href="/static/css/tokens.css"/>
    <link rel="stylesheet" href="/static/css/main.css"/>
    <link rel="stylesheet" href="/static/css/admin.css"/>
    <style>
        /* Kiểu dáng của .filter-bar / .filter-tab / .page-btn / .badge-blue /
           .admin-alert-err lấy hết từ admin.css, khối này không khai lại.
           Khai lại ở đây sẽ đè ngược admin.css vì <style> nạp sau <link>.
           Dưới đây chỉ giữ phần kiểu dáng riêng của trang đơn hàng. */
        .filter-label { font-size: 13px; color: var(--ink-2); font-weight: 500; }
        .pagination { display: flex; gap: 6px; justify-content: center; margin-top: 20px; }
        .order-count { font-family: var(--font-mono); font-size: 12px; color: var(--ink-4); margin-left: auto; letter-spacing: 0.06em; }
        .paid-badge { padding: 3px 9px; border-radius: var(--r-pill); font-size: 11px; font-weight: 600; white-space: nowrap; }
        .paid-yes { background: var(--ok-bg-2); color: var(--ok-deep); border: 1px solid var(--ok-line-2); }
        .paid-no { background: var(--warn-bg-2); color: var(--warn-deep); border: 1px solid var(--warn-line-2); }
        .confirm-ck-btn { font-size: 11px; padding: 4px 10px; background: var(--accent); color: #fff; border: 1px solid var(--accent); border-radius: var(--r-sm); cursor: pointer; margin-left: 4px; transition: box-shadow .2s ease; }
        .confirm-ck-btn:hover { box-shadow: var(--sh-blue); }
        .status-select { padding: 6px 10px; border: 1px solid var(--line-new); border-radius: var(--r-md); font-size: 12px; font-family: inherit; outline: none; transition: border-color .2s ease; }
        .status-select:focus { border-color: var(--accent); }
        .action-done { font-size: 12px; color: var(--ink-3); }
    </style>
</head>
<body>
<div class="admin-layout">
    <c:set var="adminActive" value="orders"/>
    <%@ include file="/WEB-INF/views/common/admin-sidebar.jsp" %>

    <div class="admin-main">
        <c:set var="adminTitle" value="Quản lý Đơn hàng"/>
        <c:set var="adminCrumb" value="ADMIN / ORDERS"/>
        <c:set var="adminSearchUrl" value="/admin/orders"/>
        <c:set var="adminSearchPlaceholder" value="Tìm tên, SĐT, mã đơn…"/>
        <%@ include file="/WEB-INF/views/common/admin-topbar.jsp" %>

        <c:if test="${not empty errorMsg}">
            <div class="admin-alert-err">${errorMsg}</div>
        </c:if>

        <!-- THẺ THỐNG KÊ -->
        <div class="stat-cards">
            <div class="stat-card">
                <div class="stat-label">Chờ xác nhận</div>
                <div class="stat-value">${statPending}</div>
                <div class="stat-sub">Cần xử lý hôm nay</div>
            </div>
            <div class="stat-card">
                <div class="stat-label">Đang giao</div>
                <div class="stat-value">${statShipping}</div>
                <div class="stat-sub">Trên đường tới khách</div>
            </div>
            <div class="stat-card">
                <div class="stat-label">Hoàn thành / tháng</div>
                <div class="stat-value">${statCompletedMonth}</div>
                <div class="stat-sub">Tháng ${statMonthLabel}</div>
            </div>
            <div class="stat-card">
                <div class="stat-label">Doanh thu / tháng</div>
                <div class="stat-value"><fmt:formatNumber value="${statRevenueMonthM}" pattern="#,##0.0"/>M</div>
                <div class="stat-sub">VNĐ — tháng ${statMonthLabel}</div>
            </div>
        </div>

        <!-- FILTER TRẠNG THÁI -->
        <div class="filter-bar">
            <a href="/admin/orders" class="filter-tab ${param.status == null ? 'active' : ''}">Tất cả <span class="tab-count">${totalOrders}</span></a>
            <a href="/admin/orders?status=PENDING" class="filter-tab ${param.status == 'PENDING' ? 'active' : ''}">Chờ xác nhận <span class="tab-count">${cntPending}</span></a>
            <a href="/admin/orders?status=SHIPPING" class="filter-tab ${param.status == 'SHIPPING' ? 'active' : ''}">Đang giao <span class="tab-count">${cntShipping}</span></a>
            <a href="/admin/orders?status=COMPLETED" class="filter-tab ${param.status == 'COMPLETED' ? 'active' : ''}">Hoàn thành <span class="tab-count">${cntCompleted}</span></a>
            <a href="/admin/orders?status=CANCELLED" class="filter-tab ${param.status == 'CANCELLED' ? 'active' : ''}">Đã hủy <span class="tab-count">${cntCancelled}</span></a>
            <a href="/admin/orders?status=AWAITING_STOCK" class="filter-tab ${param.status == 'AWAITING_STOCK' ? 'active' : ''}">Chờ hàng <span class="tab-count">${cntAwaiting}</span></a>
            <span class="order-count">Tổng: ${totalOrders} đơn</span>
        </div>

        <!-- FILTER THANH TOÁN -->
        <div class="filter-bar">
            <span class="filter-label">Thanh toán:</span>
            <a href="/admin/orders?status=${param.status}" class="filter-tab ${selectedPayment == null ? 'active' : ''}">Tất cả</a>
            <a href="/admin/orders?status=${param.status}&paymentMethod=COD" class="filter-tab ${selectedPayment == 'COD' ? 'active' : ''}">COD</a>
            <a href="/admin/orders?status=${param.status}&paymentMethod=VNPAY" class="filter-tab ${selectedPayment == 'VNPAY' ? 'active' : ''}">Chuyển khoản</a>
            <span class="filter-label" style="margin-left:12px;">Tình trạng CK:</span>
            <a href="/admin/orders?status=${param.status}&paymentMethod=${selectedPayment}&paid=unpaid" class="filter-tab ${selectedPaid == 'unpaid' ? 'active' : ''}">Chưa CK</a>
            <a href="/admin/orders?status=${param.status}&paymentMethod=${selectedPayment}&paid=paid" class="filter-tab ${selectedPaid == 'paid' ? 'active' : ''}">Đã CK</a>
        </div>

        <table class="admin-table">
            <thead>
            <tr>
                <th>ID</th>
                <th>Khách hàng</th>
                <th>SĐT</th>
                <th>Tổng tiền</th>
                <th>Thanh toán</th>
                <th>Tình trạng CK</th>
                <th>Trạng thái</th>
                <th>Ngày đặt</th>
                <th>Thao tác</th>
            </tr>
            </thead>
            <tbody>
            <c:choose>
                <c:when test="${empty orders}">
                    <tr><td colspan="9" style="text-align:center;padding:40px;color:var(--ink-3);">Không có đơn hàng nào</td></tr>
                </c:when>
                <c:otherwise>
                    <c:forEach var="o" items="${orders}">
                        <tr>
                            <td><strong>#${o.id}</strong></td>
                            <td>${fn:escapeXml(o.receiverName)}</td>
                            <td>${fn:escapeXml(o.receiverPhone)}</td>
                            <td style="color:var(--accent);font-weight:700;"><fmt:formatNumber value="${o.totalAmount}" pattern="#,##0"/>đ</td>
                            <td>${o.paymentMethod}</td>
                            <td>
                                <c:choose>
                                    <c:when test="${o.isPaid}">
                                        <span class="paid-badge paid-yes">Đã thanh toán</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="paid-badge paid-no">Chưa thanh toán</span>
                                        <c:if test="${o.paymentMethod == 'VNPAY'}">
                                            <form action="/admin/orders/${o.id}/confirm-payment" method="post" style="display:inline;">
                                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                                                <button type="submit" class="confirm-ck-btn">Xác nhận CK</button>
                                            </form>
                                        </c:if>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${o.status == 'PENDING' && o.isPaid}"><span class="badge-blue">Đang đóng gói</span></c:when>
                                    <c:when test="${o.status == 'PENDING'}"><span class="badge-yellow">Chờ xác nhận</span></c:when>
                                    <c:when test="${o.status == 'SHIPPING'}"><span class="badge-blue">Đang giao</span></c:when>
                                    <c:when test="${o.status == 'COMPLETED'}"><span class="badge-green">Hoàn thành</span></c:when>
                                    <c:when test="${o.status == 'CANCELLED'}"><span class="badge-red">Đã hủy</span></c:when>
                                    <c:when test="${o.status == 'AWAITING_STOCK'}"><span class="badge-yellow">Chờ hàng</span></c:when>
                                    <c:otherwise><span>${o.status}</span></c:otherwise>
                                </c:choose>
                            </td>
                            <td>${o.createdAt.toString().substring(0,16).replace("T"," ")}</td>
                            <td>
                                <a href="/admin/orders/${o.id}" class="btn-edit" style="display:inline-block;margin-bottom:6px;">Chi tiết</a>
                                <c:choose>
                                    <c:when test="${o.status == 'COMPLETED' || o.status == 'CANCELLED'}">
                                        <span class="action-done">Đã kết thúc</span>
                                    </c:when>
                                    <c:when test="${o.status == 'SHIPPING'}">
                                        <form method="post" action="/admin/orders/${o.id}/status" style="display:flex;gap:4px;">
                                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                                            <select name="status" class="status-select">
                                                <option value="SHIPPING" selected>Đang giao</option>
                                                <option value="COMPLETED">Hoàn thành (đã giao xong)</option>
                                                <option value="CANCELLED">Hủy (giao thất bại/hoàn hàng)</option>
                                            </select>
                                            <button type="submit" class="btn-edit" style="font-size:12px;padding:4px 8px;">Cập nhật</button>
                                        </form>
                                    </c:when>
                                    <c:otherwise>
                                        <%-- Đơn Chờ xác nhận / Chờ hàng: CHỈ được duyệt xuất kho hoặc hủy.
                                             KHÔNG có lựa chọn "Hoàn thành" ở đây — nhảy thẳng sang Hoàn thành
                                             là đơn xong mà hàng chưa hề xuất kho: tồn kho không giảm và báo cáo
                                             lấy giá vốn = 0. Server cũng chặn (OrderService.LUONG_HOP_LE),
                                             đây chỉ là lớp giao diện cho admin khỏi chọn nhầm. --%>
                                        <form method="post" action="/admin/orders/${o.id}/status" style="display:flex;gap:4px;">
                                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                                            <select name="status" class="status-select">
                                                <option value="${o.status}" selected>${o.status == 'AWAITING_STOCK' ? 'Chờ hàng' : 'Chờ XN'}</option>
                                                <option value="SHIPPING">Duyệt &amp; xuất kho</option>
                                                <option value="CANCELLED">Hủy đơn</option>
                                            </select>
                                            <button type="submit" class="btn-edit" style="font-size:12px;padding:4px 8px;">Cập nhật</button>
                                        </form>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
            </tbody>
        </table>

        <!-- PHÂN TRANG -->
        <c:if test="${totalPages > 1}">
            <div class="pagination">
                <c:if test="${currentPage > 0}">
                    <a href="/admin/orders?page=${currentPage-1}&status=${param.status}&q=${param.q}" class="page-btn">← Trước</a>
                </c:if>
                <c:forEach begin="0" end="${totalPages-1}" var="i">
                    <a href="/admin/orders?page=${i}&status=${param.status}&q=${param.q}"
                       class="page-btn ${currentPage == i ? 'active' : ''}">${i+1}</a>
                </c:forEach>
                <c:if test="${currentPage < totalPages-1}">
                    <a href="/admin/orders?page=${currentPage+1}&status=${param.status}&q=${param.q}" class="page-btn">Tiếp →</a>
                </c:if>
            </div>
        </c:if>
    </div>
</div>
</body>
</html>

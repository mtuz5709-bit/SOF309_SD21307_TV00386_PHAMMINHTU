<%@ page contentType="text/html;charset=UTF-8" isELIgnored="false" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>Chi tiết đơn #${order.id} – TiTanGEAR</title>
    <link href="https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@400;500;600;700&family=IBM+Plex+Mono:wght@400;500;600&display=swap" rel="stylesheet"/>
    <link rel="stylesheet" href="/static/css/tokens.css"/>
    <link rel="stylesheet" href="/static/css/main.css"/>
    <link rel="stylesheet" href="/static/css/admin.css"/>
    <style>
        /* Phiếu xuất kho — thủ kho đọc trước khi ra kho lấy hàng */
        .pxk { border-left: 4px solid var(--accent); }
        .pxk-dong { padding: 11px 0; border-bottom: 1px dashed var(--line-new); }
        .pxk-dong:last-of-type { border-bottom: none; }
        .pxk-ten { font-size: 13.5px; font-weight: 600; color: var(--ink-1); margin-bottom: 7px; }
        .pxk-can { float: right; font-weight: 500; color: var(--ink-3); font-size: 12.5px; }
        .pxk-lo  { display: flex; align-items: center; gap: 10px; padding: 5px 0 5px 12px; }
        .pxk-sl  { min-width: 56px; font-size: 13px; font-weight: 700; color: var(--ink-1); }
        .pxk-ma  { font-family: 'IBM Plex Mono', monospace; font-size: 13px; font-weight: 700;
                   background: var(--accent-soft-2); color: var(--accent); padding: 2px 9px; }
        .pxk-vt  { font-size: 12.5px; font-weight: 600; color: var(--ok-deep); }
        .pxk-vt-trong { color: var(--warn-deep); }
        .pxk-thieu { margin: 6px 0 0 12px; font-size: 12.5px; color: var(--err);
                     background: var(--err-bg-2); border: 1px solid var(--err-line-2);
                     padding: 6px 10px; }
        .pxk-nhac { margin-top: 12px; font-size: 12px; color: var(--ink-3); line-height: 1.5; }
    </style>
</head>
<body>
<div class="admin-layout">
    <c:set var="adminActive" value="orders"/>
    <%@ include file="/WEB-INF/views/common/admin-sidebar.jsp" %>

    <div class="admin-main">
        <c:set var="adminTitle" value="Chi tiết đơn hàng"/>
        <c:set var="adminCrumb" value="ADMIN / ORDERS / DETAIL"/>
        <%@ include file="/WEB-INF/views/common/admin-topbar.jsp" %>

        <a href="/admin/orders" class="od-back">← Về danh sách đơn hàng</a>

        <%-- ===== biến suy diễn trạng thái (dữ liệu thật: status + isPaid) ===== --%>
        <c:set var="st"          value="${order.status}"/>
        <c:set var="paid"        value="${order.isPaid}"/>
        <c:set var="isCancelled" value="${st == 'CANCELLED'}"/>
        <c:set var="isCompleted" value="${st == 'COMPLETED'}"/>
        <c:set var="isShipping"  value="${st == 'SHIPPING'}"/>
        <c:set var="isPending"   value="${st == 'PENDING' || st == 'AWAITING_STOCK'}"/>
        <c:set var="pastPacking" value="${st == 'SHIPPING' || st == 'COMPLETED'}"/>

        <%-- tính tạm tính = tổng thành tiền các dòng --%>
        <c:set var="subtotal" value="0"/>
        <c:forEach var="it" items="${items}">
            <c:set var="subtotal" value="${subtotal + it.lineTotal}"/>
        </c:forEach>
        <c:set var="shipping" value="${order.totalAmount - subtotal}"/>

        <div class="order-detail-bp">
            <%-- ============ CỘT TRÁI ============ --%>
            <div class="od-col">
                <!-- Thẻ đơn -->
                <div class="od-card">
                    <div class="od-head">
                        <div>
                            <div class="od-head-id">#${order.id}</div>
                            <div class="od-head-time">Đặt lúc ${order.createdAt.toString().substring(0,16).replace("T"," ")}</div>
                        </div>
                        <div>
                            <c:choose>
                                <c:when test="${st == 'PENDING' && paid}"><span class="badge-blue">Đang đóng gói</span></c:when>
                                <c:when test="${st == 'PENDING'}"><span class="badge-yellow">Chờ xác nhận</span></c:when>
                                <c:when test="${st == 'SHIPPING'}"><span class="badge-blue">Đang giao</span></c:when>
                                <c:when test="${st == 'COMPLETED'}"><span class="badge-green">Hoàn thành</span></c:when>
                                <c:when test="${st == 'CANCELLED'}"><span class="badge-red">Đã hủy</span></c:when>
                                <c:when test="${st == 'AWAITING_STOCK'}"><span class="badge-yellow">Chờ hàng</span></c:when>
                                <c:otherwise><span class="badge-neutral">${st}</span></c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                </div>

                <!-- Bảng sản phẩm -->
                <div class="od-card">
                    <p class="od-card-title">Sản phẩm</p>
                    <table class="admin-table">
                        <thead>
                        <tr>
                            <th>Sản phẩm</th>
                            <th class="od-num">SL</th>
                            <th class="od-num">Đơn giá</th>
                            <th class="od-num">Thành tiền</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="it" items="${items}">
                            <tr>
                                <td>
                                    <div class="od-prod">
                                        <c:choose>
                                            <c:when test="${not empty it.variant && not empty it.variant.imageUrl}">
                                                <img class="od-prod-img" src="/static/images/${it.variant.imageUrl}" alt="${it.productName}" onerror="this.onerror=null;this.src='/static/images/banner.png';"/>
                                            </c:when>
                                            <c:when test="${not empty it.product && not empty it.product.imageUrl}">
                                                <img class="od-prod-img" src="/static/images/${it.product.imageUrl}" alt="${it.productName}" onerror="this.onerror=null;this.src='/static/images/banner.png';"/>
                                            </c:when>
                                            <c:otherwise>
                                                <img class="od-prod-img" src="/static/images/banner.png" alt="${it.productName}"/>
                                            </c:otherwise>
                                        </c:choose>
                                        <div class="od-prod-info">
                                            <div class="od-prod-name">${it.productName}</div>
                                            <c:if test="${not empty it.variant && (not empty it.variant.name || not empty it.variant.color)}">
                                                <div class="od-var">Biến thể:<c:if test="${not empty it.variant.name}"> ${it.variant.name}</c:if><c:if test="${not empty it.variant.name && not empty it.variant.color}">,</c:if><c:if test="${not empty it.variant.color}"> màu ${it.variant.color}</c:if></div>
                                            </c:if>
                                        </div>
                                    </div>
                                </td>
                                <td class="od-num">×${it.quantity}</td>
                                <td class="od-num"><fmt:formatNumber value="${it.unitPrice}" pattern="#,##0"/>đ</td>
                                <td class="od-num"><fmt:formatNumber value="${it.lineTotal}" pattern="#,##0"/>đ</td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty items}">
                            <tr><td colspan="4" style="text-align:center;color:var(--ink-3);padding:24px;">Không có sản phẩm</td></tr>
                        </c:if>
                        </tbody>
                    </table>

                    <!-- Tổng kết -->
                    <div class="od-summary">
                        <div class="od-sum-row">
                            <span>Tạm tính</span>
                            <span><fmt:formatNumber value="${subtotal}" pattern="#,##0"/>đ</span>
                        </div>
                        <div class="od-sum-row">
                            <span>Phí vận chuyển</span>
                            <c:choose>
                                <c:when test="${order.shippingFee > 0}"><span><fmt:formatNumber value="${order.shippingFee}" pattern="#,##0"/>đ</span></c:when>
                                <c:otherwise><span>Miễn phí</span></c:otherwise>
                            </c:choose>
                        </div>
                        <c:if test="${order.discountAmount != null && order.discountAmount > 0}">
                            <div class="od-sum-row">
                                <span>Giảm giá (mã)</span>
                                <span style="color:var(--ok-deep);">−<fmt:formatNumber value="${order.discountAmount}" pattern="#,##0"/>đ</span>
                            </div>
                        </c:if>
                        <div class="od-sum-row od-sum-total">
                            <span>Tổng cộng</span>
                            <span><fmt:formatNumber value="${order.totalAmount}" pattern="#,##0"/>đ</span>
                        </div>
                    </div>
                </div>

                <!-- Tiến trình đơn hàng -->
                <div class="od-card od-timeline-card">
                    <p class="od-card-title">Tiến trình đơn hàng</p>
                    <ul class="od-timeline">
                        <c:choose>
                            <%-- ===== nhánh ĐÃ HỦY ===== --%>
                            <c:when test="${isCancelled}">
                                <li class="od-step done">
                                    <span class="od-dot"></span>
                                    <div class="od-step-name">Đặt hàng</div>
                                    <div class="od-step-time">${order.createdAt.toString().substring(0,16).replace("T"," ")}</div>
                                </li>
                                <li class="od-step ${paid ? 'done' : 'todo'}">
                                    <span class="od-dot"></span>
                                    <div class="od-step-name">Thanh toán</div>
                                    <div class="od-step-time">${paid ? 'Đã thanh toán' : 'Chưa thanh toán'}</div>
                                </li>
                                <li class="od-step cancelled">
                                    <span class="od-dot"></span>
                                    <div class="od-step-name">Đã hủy</div>
                                    <c:if test="${not empty order.updatedAt}">
                                        <div class="od-step-time">${order.updatedAt.toString().substring(0,16).replace("T"," ")}</div>
                                    </c:if>
                                </li>
                            </c:when>
                            <%-- ===== luồng bình thường ===== --%>
                            <c:otherwise>
                                <li class="od-step done">
                                    <span class="od-dot"></span>
                                    <div class="od-step-name">Đặt hàng</div>
                                    <div class="od-step-time">${order.createdAt.toString().substring(0,16).replace("T"," ")}</div>
                                </li>
                                <li class="od-step ${paid ? 'done' : 'current'}">
                                    <span class="od-dot"></span>
                                    <div class="od-step-name">Thanh toán</div>
                                    <div class="od-step-time">${paid ? 'Đã thanh toán' : 'Chờ thanh toán'}</div>
                                </li>
                                <li class="od-step ${pastPacking ? 'done' : (isPending && paid ? 'current' : 'todo')}">
                                    <span class="od-dot"></span>
                                    <div class="od-step-name">Đóng gói</div>
                                    <div class="od-step-time">
                                        <c:choose>
                                            <c:when test="${pastPacking}">Đã đóng gói</c:when>
                                            <c:when test="${isPending && paid}">Đang chuẩn bị hàng</c:when>
                                            <c:otherwise>Chưa tới</c:otherwise>
                                        </c:choose>
                                    </div>
                                </li>
                                <li class="od-step ${isCompleted ? 'done' : (isShipping ? 'current' : 'todo')}">
                                    <span class="od-dot"></span>
                                    <div class="od-step-name">Giao hàng</div>
                                    <div class="od-step-time">
                                        <c:choose>
                                            <c:when test="${isCompleted}">Đã giao</c:when>
                                            <c:when test="${isShipping}">Đang trên đường</c:when>
                                            <c:otherwise>Chưa tới</c:otherwise>
                                        </c:choose>
                                    </div>
                                </li>
                                <li class="od-step ${isCompleted ? 'done' : 'todo'}">
                                    <span class="od-dot"></span>
                                    <div class="od-step-name">Hoàn thành</div>
                                    <div class="od-step-time">
                                        <c:choose>
                                            <c:when test="${isCompleted && not empty order.updatedAt}">${order.updatedAt.toString().substring(0,16).replace("T"," ")}</c:when>
                                            <c:when test="${isCompleted}">Đã hoàn thành</c:when>
                                            <c:otherwise>Chưa tới</c:otherwise>
                                        </c:choose>
                                    </div>
                                </li>
                            </c:otherwise>
                        </c:choose>
                    </ul>
                </div>
            </div>

            <%-- ============ CỘT PHẢI ============ --%>
            <div class="od-col">
                <!-- Khách hàng -->
                <div class="od-card">
                    <p class="od-card-title">Khách hàng</p>
                    <div class="od-field">
                        <div class="od-field-label">Người nhận</div>
                        <div class="od-field-val">${fn:escapeXml(order.receiverName)}</div>
                    </div>
                    <div class="od-field">
                        <div class="od-field-label">Điện thoại</div>
                        <div class="od-field-val">${fn:escapeXml(order.receiverPhone)}</div>
                    </div>
                    <div class="od-field">
                        <div class="od-field-label">Địa chỉ giao</div>
                        <div class="od-field-val">${fn:escapeXml(order.shippingAddress)}</div>
                    </div>
                    <c:if test="${not empty order.note}">
                        <div class="od-field">
                            <div class="od-field-label">Ghi chú</div>
                            <div class="od-field-val">${fn:escapeXml(order.note)}</div>
                        </div>
                    </c:if>
                </div>

                <!-- Thanh toán -->
                <div class="od-card">
                    <p class="od-card-title">Thanh toán</p>
                    <div class="od-field">
                        <div class="od-field-label">Phương thức</div>
                        <div class="od-field-val">${order.paymentMethod}</div>
                    </div>
                    <div class="od-field">
                        <div class="od-field-label">Trạng thái</div>
                        <div class="od-field-val">
                            <c:choose>
                                <c:when test="${paid}"><span class="badge-green">Đã thanh toán</span></c:when>
                                <c:otherwise><span class="badge-yellow">Chưa thanh toán</span></c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                    <c:if test="${!paid && !isCancelled}">
                        <form class="od-pay-form" action="/admin/orders/${order.id}/confirm-payment" method="post" style="margin-top:14px;">
                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                            <button type="submit" class="btn-primary" style="width:100%;">Xác nhận đã chuyển khoản</button>
                        </form>
                    </c:if>
                </div>

                <%-- PHIẾU XUẤT KHO.
                     Hệ thống tính FIFO trong máy thì đúng, nhưng nếu không NÓI RA
                     thì thủ kho ra kho lấy đại thùng nào gần tay — sổ sách là FIFO
                     còn ngoài kho là ngẫu nhiên. Khối này chỉ rõ: ra khu nào, kệ
                     nào, lấy thùng có số lô nào (số lô khớp với tem dán trên thùng). --%>
                <c:if test="${chuaXuat and not empty items}">
                    <div class="od-card pxk">
                        <p class="od-card-title">Hàng cần lấy trong kho</p>
                        <c:forEach var="it" items="${items}">
                            <div class="pxk-dong">
                                <div class="pxk-ten">
                                    ${it.productName}<c:if test="${not empty it.variant}"> — ${it.variant.color}</c:if>
                                    <span class="pxk-can">cần ${it.quantity}</span>
                                </div>
                                <c:set var="phan" value="${phieuXuat[it.id]}"/>
                                <c:choose>
                                    <c:when test="${empty phan}">
                                        <div class="pxk-thieu">Không còn lô nào trong kho — chưa xuất được.</div>
                                    </c:when>
                                    <c:otherwise>
                                        <c:set var="daChia" value="0"/>
                                        <c:forEach var="p" items="${phan}">
                                            <c:set var="daChia" value="${daChia + p.soLuong}"/>
                                            <div class="pxk-lo">
                                                <span class="pxk-sl">${p.soLuong} cái</span>
                                                <span class="pxk-ma">LÔ #${p.maLo}</span>
                                                <span class="pxk-vt ${p.coViTri ? '' : 'pxk-vt-trong'}">${p.viTri}</span>
                                            </div>
                                        </c:forEach>
                                        <c:if test="${daChia < it.quantity}">
                                            <div class="pxk-thieu">Kho chỉ còn ${daChia}/${it.quantity} — thiếu ${it.quantity - daChia} cái.</div>
                                        </c:if>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </c:forEach>
                        <p class="pxk-nhac">Tìm thùng có <strong>đúng số lô</strong> ghi trên tem. Lấy xong quay lại bấm duyệt.</p>
                    </div>
                </c:if>

                <%-- Đơn đã duyệt: phiếu thành BẰNG CHỨNG, đọc từ stock_out đã ghi.
                     Dùng khi nhà sản xuất thu hồi một lô lỗi và cần biết những đơn
                     nào đã nhận hàng từ lô đó. --%>
                <c:if test="${not chuaXuat and not empty daXuat}">
                    <div class="od-card pxk">
                        <p class="od-card-title">Đã xuất từ lô</p>
                        <c:forEach var="x" items="${daXuat}">
                            <div class="pxk-lo">
                                <span class="pxk-sl">${x.quantity} cái</span>
                                <span class="pxk-ma">LÔ #${x.stockIn.id}</span>
                                <span class="pxk-vt">${x.stockIn.viTri}</span>
                            </div>
                        </c:forEach>
                    </div>
                </c:if>

                <!-- Hành động -->
                <div class="od-card">
                    <p class="od-card-title">Hành động</p>
                    <div class="od-actions">
                        <c:if test="${isPending}">
                            <form action="/admin/orders/${order.id}/status" method="post">
                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                                <input type="hidden" name="status" value="SHIPPING"/>
                                <button type="submit" class="btn-primary">Duyệt &amp; chuyển giao hàng</button>
                            </form>
                        </c:if>
                        <c:if test="${isShipping}">
                            <form action="/admin/orders/${order.id}/status" method="post">
                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                                <input type="hidden" name="status" value="COMPLETED"/>
                                <button type="submit" class="btn-primary">Xác nhận hoàn thành</button>
                            </form>
                        </c:if>

                        <button type="button" class="btn-edit" onclick="window.print()">In phiếu giao hàng</button>

                        <c:if test="${!isCompleted && !isCancelled}">
                            <form action="/admin/orders/${order.id}/status" method="post"
                                  onsubmit="return confirm('Hủy đơn #${order.id}? Thao tác này không thể hoàn tác.');">
                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                                <input type="hidden" name="status" value="CANCELLED"/>
                                <button type="submit" class="btn-delete">Hủy đơn hàng</button>
                            </form>
                        </c:if>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
</html>

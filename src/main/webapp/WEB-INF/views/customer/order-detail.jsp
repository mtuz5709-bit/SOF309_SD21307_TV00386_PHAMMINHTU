<%@ page contentType="text/html;charset=UTF-8" isELIgnored="false" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Đơn #${order.id} – TiTanGEAR"/>
<c:set var="currentPage" value="my-orders"/>
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<div class="account-bp" style="max-width:1080px;margin:0 auto;padding:40px 24px 72px;">

    <a href="/my-orders" class="od-back">← Về danh sách đơn hàng</a>

    <%-- Suy diễn trạng thái từ dữ liệu thật (status + isPaid), giống trang admin --%>
    <c:set var="st"          value="${order.status}"/>
    <c:set var="paid"        value="${order.isPaid}"/>
    <c:set var="isCancelled" value="${st == 'CANCELLED'}"/>
    <c:set var="isCompleted" value="${st == 'COMPLETED'}"/>
    <c:set var="isShipping"  value="${st == 'SHIPPING'}"/>
    <c:set var="isPending"   value="${st == 'PENDING' || st == 'AWAITING_STOCK'}"/>
    <c:set var="pastPacking" value="${st == 'SHIPPING' || st == 'COMPLETED'}"/>

    <c:set var="subtotal" value="0"/>
    <c:forEach var="it" items="${items}">
        <c:set var="subtotal" value="${subtotal + it.lineTotal}"/>
    </c:forEach>
    <c:set var="shipping" value="${order.totalAmount - subtotal}"/>

    <div class="order-detail-bp">
        <div class="od-col">

            <!-- Thẻ đơn -->
            <div class="od-card">
                <div class="od-head">
                    <div>
                        <div class="od-head-id">#${order.id}</div>
                        <div class="od-head-time">Đặt lúc ${order.createdAt.toString().substring(0,16).replace("T"," ")}</div>
                    </div>
                    <span class="status-badge status-${st}${st == 'PENDING' && paid ? ' status-PACKING' : ''}">
                        <c:choose>
                            <c:when test="${st == 'PENDING' && paid}">Đang đóng gói</c:when>
                            <c:when test="${st == 'PENDING'}">Chờ xác nhận</c:when>
                            <c:when test="${st == 'SHIPPING'}">Đang giao</c:when>
                            <c:when test="${st == 'COMPLETED'}">Hoàn thành</c:when>
                            <c:when test="${st == 'CANCELLED'}">Đã hủy</c:when>
                            <c:when test="${st == 'AWAITING_STOCK'}">Chờ hàng</c:when>
                            <c:otherwise>${st}</c:otherwise>
                        </c:choose>
                    </span>
                </div>
            </div>

            <!-- Sản phẩm -->
            <div class="od-card">
                <p class="od-card-title">Sản phẩm</p>
                <%-- Danh sách dòng hàng: không có hàng tiêu đề cột, mỗi dòng gồm
                     ảnh 64px + tên + biến thể + serial bảo hành, rồi ×số lượng và thành tiền. --%>
                <c:forEach var="it" items="${items}">
                    <div class="od-line">
                        <div class="od-line-img">
                            <c:choose>
                                <c:when test="${not empty it.variant && not empty it.variant.imageUrl}">
                                    <img src="/static/images/${it.variant.imageUrl}" alt="${it.productName}" onerror="this.onerror=null;this.src='/static/images/banner.png';"/>
                                </c:when>
                                <c:when test="${not empty it.product && not empty it.product.imageUrl}">
                                    <img src="/static/images/${it.product.imageUrl}" alt="${it.productName}" onerror="this.onerror=null;this.src='/static/images/banner.png';"/>
                                </c:when>
                                <c:otherwise>
                                    <img src="/static/images/banner.png" alt="${it.productName}"/>
                                </c:otherwise>
                            </c:choose>
                        </div>
                        <div class="od-line-info">
                            <div class="od-line-name">${it.productName}</div>
                            <c:if test="${not empty it.variant && (not empty it.variant.name || not empty it.variant.color)}">
                                <div class="od-line-var"><c:if test="${not empty it.variant.name}">${it.variant.name}</c:if><c:if test="${not empty it.variant.name && not empty it.variant.color}"> · </c:if><c:if test="${not empty it.variant.color}">${it.variant.color}</c:if></div>
                            </c:if>
                            <%-- Serial bảo hành của chính mấy chiếc trong dòng này --%>
                            <c:if test="${not empty baoHanh[it.id]}">
                                <div class="od-serials">
                                    <c:forEach var="w" items="${baoHanh[it.id]}">
                                        <a href="/bao-hanh?serial=${w.serialNo}" class="od-serial"
                                           title="Bấm để tra cứu bảo hành">${w.serialNo}</a>
                                    </c:forEach>
                                </div>
                            </c:if>
                        </div>
                        <span class="od-line-qty">×${it.quantity}</span>
                        <span class="od-line-total"><fmt:formatNumber value="${it.lineTotal}" pattern="#,##0"/>đ</span>
                    </div>
                </c:forEach>
                <c:if test="${empty items}">
                    <div style="text-align:center;color:var(--ink-3);padding:24px;">Không có sản phẩm</div>
                </c:if>

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

            <!-- Tiến trình -->
            <div class="od-card od-timeline-card">
                <p class="od-card-title">Tiến trình đơn hàng</p>
                <ul class="od-timeline">
                    <c:choose>
                        <c:when test="${isCancelled}">
                            <li class="od-step done">
                                <span class="od-dot"></span>
                                <div class="od-step-name">Đặt hàng</div>
                                <div class="od-step-time">${order.createdAt.toString().substring(0,16).replace("T"," ")}</div>
                            </li>
                            <li class="od-step cancelled">
                                <span class="od-dot"></span>
                                <div class="od-step-name">Đã hủy</div>
                                <c:if test="${not empty order.updatedAt}">
                                    <div class="od-step-time">${order.updatedAt.toString().substring(0,16).replace("T"," ")}</div>
                                </c:if>
                            </li>
                        </c:when>
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
                        </c:otherwise>
                    </c:choose>
                </ul>
            </div>
        </div>

        <%-- ============ CỘT PHẢI ============ --%>
        <div class="od-col">
            <div class="od-card">
                <p class="od-card-title">Giao tới</p>
                <div class="od-field">
                    <div class="od-field-label">Người nhận</div>
                    <div class="od-field-val">${fn:escapeXml(order.receiverName)}</div>
                </div>
                <div class="od-field">
                    <div class="od-field-label">Điện thoại</div>
                    <div class="od-field-val">${fn:escapeXml(order.receiverPhone)}</div>
                </div>
                <div class="od-field">
                    <div class="od-field-label">Địa chỉ</div>
                    <div class="od-field-val">${fn:escapeXml(order.shippingAddress)}</div>
                </div>
                <c:if test="${not empty order.note}">
                    <div class="od-field">
                        <div class="od-field-label">Ghi chú</div>
                        <div class="od-field-val">${fn:escapeXml(order.note)}</div>
                    </div>
                </c:if>
            </div>

            <div class="od-card">
                <p class="od-card-title">Thanh toán</p>
                <div class="od-field">
                    <div class="od-field-label">Hình thức</div>
                    <div class="od-field-val">${order.paymentMethod == 'COD' ? 'Thanh toán khi nhận hàng (COD)' : 'Chuyển khoản (VNPAY)'}</div>
                </div>
                <div class="od-field">
                    <div class="od-field-label">Tình trạng</div>
                    <div class="od-field-val">${paid ? 'Đã thanh toán' : 'Chưa thanh toán'}</div>
                </div>
            </div>

            <%-- Khách chỉ có đúng 1 thao tác: hủy đơn khi còn chờ xác nhận.
                 Theo mẫu là nút chiếm trọn chiều ngang cột phải, không bọc trong thẻ. --%>
            <c:if test="${st == 'PENDING'}">
                <a href="/my-orders/${order.id}/cancel" data-post
                   data-confirm="Bạn có chắc muốn hủy đơn này?"
                   class="od-cancel-btn">Hủy đơn hàng</a>
            </c:if>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>

<%@ page contentType="text/html;charset=UTF-8" isELIgnored="false" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Đặt Hàng Thành Công – TiTanGEAR"/>
<c:set var="currentPage" value=""/>
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<div class="tg-page">
<div class="rs-wrap">
    <div class="tg-card tg-rise" style="padding:48px 44px;text-align:center;">

        <div class="rs-icon" style="background:var(--ok-bg-2);color:var(--ok-2);">
            <svg width="34" height="34" viewBox="0 0 24 24" fill="none"><path d="M4 12.5l5 5L20 6.5" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round"/></svg>
        </div>

        <c:choose>
            <c:when test="${order.paymentMethod == 'VNPAY' && order.isPaid}">
                <h1 class="rs-title">Thanh toán thành công</h1>
                <p class="rs-sub">Đơn hàng đã được thanh toán và xác nhận. Chúng tôi sẽ xử lý sớm nhất.</p>
            </c:when>
            <c:otherwise>
                <h1 class="rs-title">Đặt hàng thành công</h1>
                <p class="rs-sub">Cảm ơn bạn đã mua hàng tại TiTanGEAR. Chúng tôi sẽ xử lý đơn hàng sớm nhất.</p>
            </c:otherwise>
        </c:choose>

        <c:if test="${order != null}">
            <div class="rs-card">
                <div class="tg-mono" style="font-size:10px;letter-spacing:.14em;color:var(--ink-4);margin-bottom:16px;">
                    THÔNG TIN ĐƠN HÀNG #${order.id}
                </div>
                <div class="rs-row"><span>Người nhận</span><span>${fn:escapeXml(order.receiverName)}</span></div>
                <div class="rs-row"><span>Số điện thoại</span><span class="tg-mono">${fn:escapeXml(order.receiverPhone)}</span></div>
                <div class="rs-row"><span>Địa chỉ giao hàng</span><span>${fn:escapeXml(order.shippingAddress)}</span></div>
                <div class="rs-row">
                    <span>Phương thức thanh toán</span>
                    <span>${order.paymentMethod == 'COD' ? 'Thanh toán khi nhận hàng (COD)' : 'Chuyển khoản (VNPay)'}</span>
                </div>
                <div class="rs-row">
                    <span>Trạng thái</span>
                    <span class="badge-yellow" style="padding:3px 11px;border-radius:var(--r-pill);font-size:12px;font-weight:600;background:var(--warn-bg-2);color:var(--warn-deep);">Chờ xác nhận</span>
                </div>
                <%-- Tách riêng phí vận chuyển: khách vừa trả tiền nên phải thấy rõ
                     mình trả bao nhiêu cho hàng, bao nhiêu cho vận chuyển. --%>
                <div class="rs-row">
                    <span>Phí vận chuyển</span>
                    <c:choose>
                        <c:when test="${order.shippingFee > 0}"><span><fmt:formatNumber value="${order.shippingFee}" pattern="#,##0"/>đ</span></c:when>
                        <c:otherwise><span style="color:var(--ok-deep);">Miễn phí</span></c:otherwise>
                    </c:choose>
                </div>
                <div class="rs-row rs-total"><span>Tổng tiền</span><span><fmt:formatNumber value="${order.totalAmount}" pattern="#,##0"/>đ</span></div>
            </div>
        </c:if>

        <div style="display:flex;gap:12px;justify-content:center;flex-wrap:wrap;margin-top:28px;">
            <a href="/my-orders/${order.id}" class="tg-btn tg-primary">Theo dõi đơn hàng</a>
            <a href="/products" class="tg-btn tg-ghost">Tiếp tục mua sắm</a>
        </div>
    </div>
</div>
</div><!-- /.tg-page -->

<style>
    .rs-wrap  { max-width:660px; margin:0 auto; padding:56px 24px 90px; }
    .rs-icon  { width:74px; height:74px; border-radius:24px; margin:0 auto 22px;
                display:flex; align-items:center; justify-content:center; }
    .rs-title { font-size:30px; font-weight:700; margin:0 0 10px; letter-spacing:-.02em; }
    .rs-sub   { font-size:15px; color:var(--ink-2); line-height:1.7; margin:0 0 28px; }
    .rs-card  { background:var(--bg-page); border-radius:var(--r-lg); padding:24px 26px; text-align:left; }
    .rs-row   { display:flex; justify-content:space-between; align-items:baseline; gap:16px;
                padding:10px 0; font-size:14px; color:var(--ink-2); border-bottom:1px solid var(--line-soft); }
    .rs-row span:last-child { color:var(--ink); font-weight:500; text-align:right; }
    .rs-row:last-child { border-bottom:none; }
    .rs-total { border-top:1px solid var(--line-new); margin-top:6px; padding-top:14px;
                font-size:16px; font-weight:700; color:var(--ink); }
    .rs-total span:last-child { color:var(--accent); font-size:20px; font-weight:700; }
</style>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>

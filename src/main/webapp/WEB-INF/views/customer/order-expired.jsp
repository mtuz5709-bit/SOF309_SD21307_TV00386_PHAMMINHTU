<%@ page contentType="text/html;charset=UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Đơn Hàng Hết Hạn – TiTanGEAR"/>
<c:set var="currentPage" value=""/>
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<div class="tg-page">
<div class="rs-wrap">
    <div class="tg-card tg-rise" style="padding:48px 44px;text-align:center;">
        <div class="rs-icon" style="background:var(--err-bg-2);color:var(--err);">
            <svg width="34" height="34" viewBox="0 0 24 24" fill="none"><circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="2.2"/><path d="M12 7v5l3 2" stroke="currentColor" stroke-width="2.2" stroke-linecap="round"/></svg>
        </div>
        <h1 class="rs-title">Đơn hàng đã hết hạn</h1>
        <p class="rs-sub">
            Đơn hàng <span class="tg-mono" style="color:var(--ink);">#${orderId}</span> đã bị hủy do quá
            thời gian thanh toán 15 phút. Hàng đã được trả lại kho, bạn đặt lại giúp mình nhé.
        </p>
        <div style="display:flex;gap:12px;justify-content:center;flex-wrap:wrap;">
            <a href="/products" class="tg-btn tg-primary">Mua sắm tiếp</a>
            <a href="/my-orders" class="tg-btn tg-ghost">Xem đơn hàng</a>
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
</style>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>

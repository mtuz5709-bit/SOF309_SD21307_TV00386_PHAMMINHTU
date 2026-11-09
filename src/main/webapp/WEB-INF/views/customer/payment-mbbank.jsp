<%@ page contentType="text/html;charset=UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Thanh Toán Chuyển Khoản – TiTanGEAR"/>
<c:set var="currentPage" value=""/>
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<div class="payment-bp">
  <div class="payment-container">
    <div class="payment-eyebrow">CHUYỂN KHOẢN NGÂN HÀNG</div>
    <h1 class="payment-title">Thanh toán đơn hàng #${orderId}</h1>
    <div class="payment-sub">Quét mã QR bằng ứng dụng ngân hàng để chuyển khoản</div>

    <div class="qr-card">
      <div class="bank-header">
        <span class="bank-icon">
          <svg width="16" height="16" viewBox="0 0 16 16" fill="none"><rect x="2" y="4" width="12" height="9" stroke="#fff" stroke-width="1.4"></rect><path d="M2 7h12" stroke="#fff" stroke-width="1.4"></path></svg>
        </span>
        <div class="bank-name">MB Bank</div>
      </div>

      <div class="qr-body">
        <div class="qr-frame">
          <img class="qr-img" src="${qrUrl}" alt="QR Thanh toán MBBank"/>
        </div>

        <div class="account-info">
          <div class="account-row">
            <span>Ngân hàng</span>
            <span>MB Bank (MBBank)</span>
          </div>
          <div class="account-row">
            <span>Số tài khoản</span>
            <span class="mono">205072009</span>
          </div>
          <div class="account-row">
            <span>Chủ tài khoản</span>
            <span>PHAM MINH TU</span>
          </div>
          <div class="account-row">
            <span>Số tiền</span>
            <span class="amount-highlight"><fmt:formatNumber value="${amount}" pattern="#,##0"/>đ</span>
          </div>
          <div class="account-row">
            <span>Nội dung CK</span>
            <span class="mono">TiTanGEAR Don ${orderId}</span>
          </div>
        </div>
      </div>

      <div class="note-box">
        <strong>Lưu ý:</strong> Nhập đúng nội dung chuyển khoản
        <strong>"TiTanGEAR Don ${orderId}"</strong> để đơn hàng được xác nhận tự động.
      </div>

      <!-- Trạng thái chờ xác nhận tự động -->
      <div class="waiting-box" id="waitingBox">
        <div class="spinner"></div>
        Đang chờ xác nhận thanh toán tự động...
      </div>

      <div class="timer">
        Đơn hàng sẽ bị hủy sau: <span id="countdown">15:00</span>
      </div>
    </div>

    <a href="/products" class="btn-cancel">Hủy đơn hàng</a>
  </div>
</div>

<script>
  // Đếm ngược 15 phút
  let seconds = 15 * 60;
  const el = document.getElementById('countdown');
  const timer = setInterval(() => {
    seconds--;
    const m = Math.floor(seconds / 60).toString().padStart(2, '0');
    const s = (seconds % 60).toString().padStart(2, '0');
    el.textContent = m + ':' + s;
    if (seconds <= 0) {
      clearInterval(timer);
      clearInterval(checkTimer);
      // Hết giờ → hủy đơn và redirect
      window.location.href = '/checkout/expired?orderId=${orderId}';
    }
  }, 1000);

  // Tự động kiểm tra trạng thái thanh toán mỗi 5 giây
  const checkTimer = setInterval(() => {
    fetch('/api/payment/check?orderId=${orderId}')
            .then(res => res.json())
            .then(data => {
              if (data.paid) {
                clearInterval(checkTimer);
                clearInterval(timer);
                window.location.href = '/checkout/success?orderId=${orderId}';
              }
            })
            .catch(() => {});
  }, 5000);
</script>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>

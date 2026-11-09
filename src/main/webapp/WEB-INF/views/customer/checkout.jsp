<%@ page contentType="text/html;charset=UTF-8" isELIgnored="false" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Thanh Toán – TiTanGEAR"/>
<c:set var="currentPage" value=""/>
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<div class="tg-page">
<div class="co-wrap">

    <div class="tg-mono" style="font-size:11px;color:var(--ink-4);letter-spacing:.1em;margin-bottom:6px;">GIỎ HÀNG / ĐẶT HÀNG</div>
    <h1 style="font-size:32px;font-weight:700;margin:0 0 22px;letter-spacing:-.02em;">Đặt hàng</h1>

    <c:if test="${param.error != null}">
        <div class="co-err">${param.error}</div>
    </c:if>

    <form action="/checkout" method="post">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
        <div class="co-layout">

            <!-- ===== CỘT TRÁI ===== -->
            <div>
                <div class="tg-card tg-rise co-card">
                    <h3 class="co-h3"><span class="co-num">1</span>Thông tin giao hàng</h3>
                    <div class="co-grid2">
                        <div class="co-f">
                            <label>Họ và tên người nhận *</label>
                            <input type="text" name="receiverName" value="${fn:escapeXml(user.fullName)}" required
                                   minlength="2" maxlength="255" placeholder="Nhập họ tên người nhận"/>
                        </div>
                        <div class="co-f">
                            <label>Số điện thoại *</label>
                            <input type="text" name="receiverPhone" value="${fn:escapeXml(user.phone)}" required
                                   placeholder="09xxxxxxxx" style="font-family:var(--font-mono);"/>
                        </div>
                    </div>
                    <div class="co-f">
                        <label>Tỉnh / Thành phố *</label>
                        <%-- Chọn từ danh sách 34 tỉnh (sau sáp nhập 2025) thay vì gõ tay:
                             phí vận chuyển tính bằng cách so tỉnh này với tỉnh đặt kho,
                             gõ tự do thì không đối chiếu được. --%>
                        <select name="province" id="oTinh" required>
                            <option value="">— Chọn tỉnh/thành —</option>
                            <c:forEach var="t" items="${dsTinh}">
                                <option value="${t}" ${user.address != null && fn:contains(user.address, t) ? 'selected' : ''}>${t}</option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="co-f">
                        <label>Địa chỉ giao hàng *</label>
                        <%-- minlength/pattern chỉ là lớp nhắc cho khách biết TRƯỚC khi bấm.
                             Server vẫn kiểm lại đủ 3 chốt (xem CheckoutController) vì
                             người gửi thẳng request thì bỏ qua được hết phần này. --%>
                        <input type="text" name="shippingAddress" value="${fn:escapeXml(user.address)}" required
                               minlength="10" maxlength="500"
                               placeholder="VD: 42 Nguyễn Văn Tiết, Phường Phú Cường, Thủ Dầu Một"
                               oninvalid="this.setCustomValidity('Vui lòng ghi rõ số nhà, đường, phường/xã, quận/huyện')"
                               oninput="this.setCustomValidity('')"/>
                        <small style="display:block;margin-top:5px;font-size:11px;color:var(--ink-3);line-height:1.5;">
                            Ghi càng rõ càng giao nhanh — thiếu số nhà hoặc tên đường thì shipper phải gọi lại.
                        </small>
                    </div>
                    <div class="co-f" style="margin-bottom:0;">
                        <label>Ghi chú</label>
                        <textarea name="note" rows="3" placeholder="Ghi chú cho đơn hàng (không bắt buộc)…"></textarea>
                    </div>
                </div>

                <div class="tg-card tg-rise co-card" style="margin-bottom:0;">
                    <h3 class="co-h3"><span class="co-num">2</span>Phương thức thanh toán</h3>

                    <c:if test="${requireVnpay}">
                        <div class="co-warn">
                            Đơn hàng có số lượng hoặc giá trị lớn — vui lòng thanh toán trước qua chuyển khoản (VNPay) để đặt hàng.
                        </div>
                    </c:if>

                    <label class="co-pay ${requireVnpay ? 'is-off' : 'is-on'}" for="cod">
                        <input type="radio" name="paymentMethod" id="cod" value="COD" ${requireVnpay ? 'disabled' : 'checked'}/>
                        <span class="co-radio"></span>
                        <span style="flex:1;">
                            <span class="co-pay-t">Thanh toán khi nhận hàng (COD)</span>
                            <span class="co-pay-s">Trả tiền mặt khi nhận được hàng</span>
                        </span>
                    </label>

                    <label class="co-pay ${requireVnpay ? 'is-on' : ''}" for="vnpay">
                        <input type="radio" name="paymentMethod" id="vnpay" value="VNPAY" ${requireVnpay ? 'checked' : ''}/>
                        <span class="co-radio"></span>
                        <span style="flex:1;">
                            <span class="co-pay-t">Chuyển khoản ngân hàng (VNPay)</span>
                            <span class="co-pay-s">Thanh toán trước qua VNPay / chuyển khoản</span>
                        </span>
                    </label>
                </div>
            </div>

            <!-- ===== CỘT PHẢI ===== -->
            <div class="tg-card tg-rise co-sum">
                <div class="tg-mono" style="font-size:10px;letter-spacing:.14em;color:var(--ink-4);margin-bottom:16px;">ĐƠN HÀNG CỦA BẠN</div>

                <div style="display:flex;flex-direction:column;gap:14px;margin-bottom:18px;">
                    <c:forEach var="item" items="${cartItems}">
                        <div style="display:flex;gap:12px;align-items:center;">
                            <div class="co-thumb">
                                <c:choose>
                                    <c:when test="${not empty item.product.imageUrl}">
                                        <img src="/static/images/${item.product.imageUrl}" alt="${item.product.name}"
                                             onerror="this.onerror=null;this.src='/static/images/banner.png';"/>
                                    </c:when>
                                    <c:otherwise><img src="/static/images/banner.png" alt="${item.product.name}"/></c:otherwise>
                                </c:choose>
                                <span class="co-qty">${item.quantity}</span>
                            </div>
                            <div style="flex:1;min-width:0;">
                                <div style="font-size:13px;font-weight:600;line-height:1.35;">${item.product.name}</div>
                                <c:if test="${item.variant != null}">
                                    <div style="font-size:11px;color:var(--ink-3);margin-top:2px;">
                                        ${item.variant.name}<c:if test="${not empty item.variant.color}"> — ${item.variant.color}</c:if>
                                    </div>
                                </c:if>
                            </div>
                            <div style="font-size:13px;font-weight:700;white-space:nowrap;">
                                <%-- Lấy từ giaDong (đã trừ phần giảm của combo), không tự nhân lại:
                                     nhân lại là ra tổng khác với dòng "Tạm tính" bên dưới. --%>
                                <fmt:formatNumber value="${giaDong[item.id].thanhTien}" pattern="#,##0"/>đ
                            </div>
                        </div>
                    </c:forEach>
                </div>

                <%-- MÃ GIẢM GIÁ: kiểm qua AJAX cho khách thấy ngay; server vẫn tính lại lúc đặt --%>
                <div style="border-top:1px solid var(--line-soft);padding-top:16px;margin-bottom:6px;">
                    <div style="display:flex;gap:8px;">
                        <input type="text" id="voucherInput" placeholder="Nhập mã giảm giá" autocomplete="off"
                               style="flex:1;min-width:0;border:1px solid var(--line-new);border-radius:10px;padding:10px 13px;font-size:13px;font-family:var(--font-mono);text-transform:uppercase;outline:none;"/>
                        <button type="button" id="voucherBtn" class="tg-btn tg-ghost" style="padding:10px 18px;font-size:13px;">Áp dụng</button>
                    </div>
                    <div id="voucherMsg" style="font-size:12px;margin-top:8px;"></div>
                    <input type="hidden" name="voucherCode" id="voucherCode" value=""/>
                </div>

                <div class="co-srow"><span>Tạm tính</span><span><fmt:formatNumber value="${total}" pattern="#,##0"/>đ</span></div>
                <div class="co-srow" id="discountRow" style="display:none;">
                    <span>Giảm giá</span>
                    <span style="color:var(--ok-deep);">−<span id="discountValue">0</span>đ</span>
                </div>
                <div class="co-srow">
                    <span>Phí vận chuyển</span>
                    <span id="shipValue" style="color:var(--ok-deep);">Chọn tỉnh/thành</span>
                </div>
                <div id="shipNote" style="font-size:11px;color:var(--ink-3);margin:-4px 0 8px;line-height:1.5;"></div>
                <div class="co-srow co-stotal">
                    <span>Tổng cộng</span>
                    <span id="grandTotal" data-subtotal="${total}"><fmt:formatNumber value="${total}" pattern="#,##0"/>đ</span>
                </div>

                <button type="submit" class="tg-btn tg-primary" style="width:100%;margin-top:6px;">Xác nhận đặt hàng →</button>
                <a href="/cart" style="display:block;text-align:center;margin-top:12px;font-size:13px;color:var(--ink-3);text-decoration:none;">← Quay lại giỏ hàng</a>
            </div>
        </div>
    </form>
</div>
</div><!-- /.tg-page -->

<style>
    .co-wrap   { max-width:1300px; margin:0 auto; padding:24px 48px 80px; }
    .co-layout { display:grid; grid-template-columns:1fr 380px; gap:24px; align-items:start; }
    .co-card   { padding:28px 30px; margin-bottom:20px; }
    .co-h3     { display:flex; align-items:center; gap:11px; font-size:17px; font-weight:700; margin:0 0 20px; }
    .co-num    { width:26px; height:26px; flex-shrink:0; border-radius:50%; background:var(--accent); color:#fff;
                 font-family:var(--font-mono); font-size:12px; font-weight:600;
                 display:inline-flex; align-items:center; justify-content:center; }
    .co-grid2  { display:grid; grid-template-columns:1fr 1fr; gap:0 16px; }
    .co-f      { margin-bottom:16px; }
    .co-f label { display:block; font-size:13px; font-weight:600; color:var(--ink-2); margin-bottom:7px; }
    .co-f input, .co-f textarea {
        width:100%; box-sizing:border-box; border:1px solid var(--line-new); border-radius:var(--r-md);
        padding:12px 15px; font-size:14px; font-family:inherit; outline:none; resize:vertical;
        transition:border-color .2s ease, box-shadow .2s ease;
    }
    .co-f input:focus, .co-f textarea:focus { border-color:var(--accent); box-shadow:0 0 0 3px rgba(27,73,255,.12); }

    .co-pay    { display:flex; align-items:center; gap:13px; border:1px solid var(--line-new);
                 border-radius:var(--r-md); padding:16px 18px; margin-bottom:10px; cursor:pointer;
                 transition:border-color .2s ease, background .2s ease; }
    .co-pay:hover { border-color:var(--accent); }
    .co-pay.is-on { border-color:var(--accent); background:var(--accent-soft-2); }
    .co-pay.is-off { opacity:.5; cursor:not-allowed; }
    .co-pay input { position:absolute; opacity:0; pointer-events:none; }
    .co-radio  { width:18px; height:18px; flex-shrink:0; border-radius:50%; border:2px solid var(--line-new);
                 display:inline-block; position:relative; transition:border-color .2s ease; }
    .co-pay.is-on .co-radio { border-color:var(--accent); }
    .co-pay.is-on .co-radio::after { content:''; position:absolute; inset:3px; border-radius:50%; background:var(--accent); }

    /* Vẽ theo trạng thái THẬT của radio, không phụ thuộc lớp is-on do server gán.
       Nhờ vậy bấm phát là đổi ngay, kể cả khi JS chưa kịp chạy. */
    .co-pay:has(input:checked) { border-color:var(--accent); background:var(--accent-soft-2); }
    .co-pay input:checked + .co-radio { border-color:var(--accent); }
    .co-pay input:checked + .co-radio::after { content:''; position:absolute; inset:3px; border-radius:50%; background:var(--accent); }
    /* Bỏ chọn thì phải sạch hẳn — nếu không, lớp is-on cũ của server còn dính lại
       sẽ làm CẢ HAI ô cùng sáng xanh */
    .co-pay:has(input:not(:checked)) { border-color:var(--line-new); background:var(--surface); }
    .co-pay:has(input:not(:checked)) .co-radio { border-color:var(--line-new); }
    .co-pay:has(input:not(:checked)) .co-radio::after { content:none; }
    .co-pay-t  { display:block; font-size:14px; font-weight:600; }
    .co-pay-s  { display:block; font-size:12px; color:var(--ink-3); margin-top:2px; }

    .co-sum    { padding:24px 26px; position:sticky; top:100px; }
    .co-thumb  { position:relative; width:52px; height:52px; flex-shrink:0; border-radius:10px;
                 background:var(--bg-page); display:flex; align-items:center; justify-content:center; padding:5px;
                 box-sizing:border-box; }
    .co-thumb img { width:100%; height:100%; object-fit:contain; mix-blend-mode:multiply; }
    .co-qty    { position:absolute; top:-6px; right:-6px; min-width:18px; height:18px; padding:0 5px;
                 border-radius:var(--r-pill); background:var(--accent); color:#fff;
                 font-family:var(--font-mono); font-size:10px; font-weight:600;
                 display:flex; align-items:center; justify-content:center; }
    .co-srow   { display:flex; justify-content:space-between; align-items:baseline; padding:9px 0;
                 font-size:14px; color:var(--ink-2); }
    .co-stotal { border-top:1px solid var(--line-new); margin-top:8px; padding-top:14px;
                 font-size:16px; font-weight:700; color:var(--ink); }
    .co-stotal span:last-child { color:var(--accent); font-size:22px; }

    .co-err  { background:var(--err-bg-2);  border:1px solid var(--err-line-2);  color:var(--err);
               border-radius:var(--r-md); padding:12px 16px; font-size:13px; margin-bottom:16px; }
    .co-warn { background:var(--warn-bg-2); border:1px solid var(--warn-line-2); color:var(--warn-deep);
               border-radius:var(--r-md); padding:12px 16px; font-size:13px; margin-bottom:14px; }

    @media (max-width:1000px) {
        .co-wrap   { padding:24px 20px 60px; }
        .co-layout { grid-template-columns:1fr; }
        .co-sum    { position:static; }
        .co-grid2  { grid-template-columns:1fr; }
    }
</style>

<script>
/* Đồng bộ lớp is-on theo radio đang chọn.
   CSS :has() ở trên đã lo phần lớn, đây là lớp dự phòng cho trình duyệt cũ
   chưa hỗ trợ :has() — không có nó thì bấm VNPay giao diện đứng im. */
(function () {
    var oPay = document.querySelectorAll('input[name="paymentMethod"]');
    if (!oPay.length) return;

    function ve() {
        oPay.forEach(function (r) {
            var nhan = r.closest('.co-pay');
            if (nhan) nhan.classList.toggle('is-on', r.checked);
        });
    }
    oPay.forEach(function (r) { r.addEventListener('change', ve); });
    ve();
})();

(function () {
    var btn = document.getElementById('voucherBtn');
    if (!btn) return;
    var input   = document.getElementById('voucherInput');
    var msg     = document.getElementById('voucherMsg');
    var hidden  = document.getElementById('voucherCode');
    var row     = document.getElementById('discountRow');
    var val     = document.getElementById('discountValue');
    var totalEl = document.getElementById('grandTotal');
    var subtotal = parseFloat(totalEl.getAttribute('data-subtotal')) || 0;
    var csrfName = '${_csrf.parameterName}', csrfToken = '${_csrf.token}';

    function tien(n) { return new Intl.NumberFormat('vi-VN').format(n); }

    /* ===== PHÍ VẬN CHUYỂN =====
       Tính lại tại chỗ cho khách thấy ngay khi đổi tỉnh. Công thức phải GIỐNG HỆT
       PhiTheoTinh.tinhPhi() bên server — hai bên lệch nhau là khách thấy một giá,
       bị thu một giá khác. Số tiền THẬT vẫn do server tính lúc bấm đặt hàng. */
    var PHI_NOI    = ${phiNoiTinh};
    var PHI_NGOAI  = ${phiLienTinh};
    var NGUONG     = ${nguongMienPhi};
    var TINH_KHO   = '${tinhCuaKho}';
    var oTinh      = document.getElementById('oTinh');
    var shipEl     = document.getElementById('shipValue');
    var shipNote   = document.getElementById('shipNote');
    var giamGia    = 0;

    function tinhPhiShip() {
        var t = oTinh ? oTinh.value : '';
        if (!t) return null;                                  // chưa chọn tỉnh
        if (NGUONG > 0 && (subtotal - giamGia) >= NGUONG) return 0;
        return (t === TINH_KHO) ? PHI_NOI : PHI_NGOAI;
    }

    function veLaiTong() {
        var phi = tinhPhiShip();
        if (phi === null) {
            shipEl.textContent = 'Chọn tỉnh/thành';
            shipEl.style.color = 'var(--ink-3)';
            shipNote.textContent = '';
        } else if (phi === 0) {
            shipEl.textContent = 'Miễn phí';
            shipEl.style.color = 'var(--ok-deep)';
            shipNote.textContent = 'Đơn từ ' + tien(NGUONG) + 'đ được miễn phí vận chuyển.';
        } else {
            shipEl.textContent = tien(phi) + 'đ';
            shipEl.style.color = 'var(--ink)';
            shipNote.textContent = (oTinh.value === TINH_KHO ? 'Giao nội tỉnh.' : 'Giao liên tỉnh.')
                + ' Đơn từ ' + tien(NGUONG) + 'đ được miễn phí.';
        }
        totalEl.textContent = tien(Math.max(0, subtotal - giamGia) + (phi || 0)) + 'đ';
    }

    if (oTinh) oTinh.addEventListener('change', veLaiTong);
    veLaiTong();

    btn.addEventListener('click', function () {
        var code = (input.value || '').trim();
        if (!code) { msg.style.color = 'var(--err)'; msg.textContent = 'Vui lòng nhập mã.'; return; }

        var body = new URLSearchParams();
        body.append('code', code);
        body.append(csrfName, csrfToken);

        fetch('/checkout/apply-voucher', { method: 'POST', body: body })
            .then(function (r) { return r.json(); })
            .then(function (d) {
                if (d.ok) {
                    msg.style.color = 'var(--ok-deep)';
                    msg.textContent = d.message;
                    hidden.value = code;
                    val.textContent = tien(d.discount);
                    row.style.display = 'flex';
                    giamGia = d.discount;
                } else {
                    msg.style.color = 'var(--err)';
                    msg.textContent = d.message;
                    hidden.value = '';
                    row.style.display = 'none';
                    giamGia = 0;
                }
                // Giảm giá đổi -> có thể vượt/ tụt khỏi ngưỡng miễn phí ship,
                // nên phải tính lại cả phí lẫn tổng, không chỉ trừ tiền.
                veLaiTong();
            })
            .catch(function () {
                msg.style.color = 'var(--err)';
                msg.textContent = 'Không kiểm tra được mã, thử lại sau.';
            });
    });
})();
</script>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>

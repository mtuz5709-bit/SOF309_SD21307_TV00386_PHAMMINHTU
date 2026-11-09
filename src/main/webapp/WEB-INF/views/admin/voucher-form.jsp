<%@ page contentType="text/html;charset=UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>${voucher.id == null ? 'Tạo mã giảm giá' : 'Sửa mã giảm giá'} – TiTanGEAR</title>
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
        <c:set var="adminTitle" value="${voucher.id == null ? 'Tạo mã giảm giá' : 'Sửa mã giảm giá'}"/>
        <c:set var="adminCrumb" value="${voucher.id == null ? 'ADMIN / VOUCHERS / NEW' : 'ADMIN / VOUCHERS / EDIT'}"/>
        <%@ include file="/WEB-INF/views/common/admin-topbar.jsp" %>

        <div style="margin-bottom:16px;">
            <a href="/admin/vouchers" class="btn-edit">← Quay lại danh sách</a>
        </div>

        <c:if test="${not empty errorMsg}">
            <div class="admin-alert-err">${errorMsg}</div>
        </c:if>

        <div class="admin-form">
            <form method="post" action="/admin/vouchers/save">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                <c:if test="${voucher.id != null}">
                    <input type="hidden" name="id" value="${voucher.id}"/>
                </c:if>

                <div class="form-group">
                    <label>Mã giảm giá *</label>
                    <div style="display:flex;gap:8px;align-items:stretch;">
                        <input type="text" id="oMa" name="code" value="${voucher.code}" required
                               placeholder="VD: GIAM10" style="flex:1;text-transform:uppercase;"
                               ${voucher.id != null ? 'readonly' : ''}/>
                        <%-- Sửa voucher cũ thì không cho đổi mã: khách đã cầm mã đó rồi --%>
                        <c:if test="${voucher.id == null}">
                            <button type="button" id="nutSinhMa" class="btn-edit"
                                    style="white-space:nowrap;">Tự sinh mã</button>
                        </c:if>
                    </div>
                    <c:if test="${voucher.id == null}">
                        <div style="font-size:12px;color:var(--ink-3);margin-top:4px;">
                            Mã đã được điền sẵn, không cần gõ. Bỏ qua chữ dễ nhầm (O/0, I/1).
                        </div>
                    </c:if>
                </div>

                <div class="form-group">
                    <label>Mô tả</label>
                    <input type="text" name="description" value="${voucher.description}"
                           placeholder="VD: Giảm 10% cho đơn từ 500k"/>
                </div>

                <div class="form-group">
                    <label>Kiểu giảm *</label>
                    <select name="discountType" id="discountType" onchange="doiKieuGiam()">
                        <option value="PERCENT" ${voucher.discountType == 'PERCENT' ? 'selected' : ''}>Giảm theo phần trăm (%)</option>
                        <option value="AMOUNT"  ${voucher.discountType == 'AMOUNT'  ? 'selected' : ''}>Giảm số tiền cố định (đ)</option>
                    </select>
                </div>

                <div class="form-group">
                    <label>Giá trị giảm * <span id="donViGiam" style="font-weight:400;color:var(--ink-3);"></span></label>
                    <input type="number" name="discountValue" value="${voucher.discountValue}" required min="1" step="1"/>
                </div>

                <div class="form-group" id="nhomTranGiam">
                    <label>Giảm tối đa (đ)</label>
                    <input type="number" name="maxDiscountAmount" value="${voucher.maxDiscountAmount}" min="0" step="1000"
                           placeholder="Bỏ trống = không chặn"/>
                    <div style="font-size:12px;color:var(--ink-3);margin-top:4px;">
                        Chỉ áp dụng cho kiểu %. VD: giảm 10% nhưng tối đa 100.000đ.
                    </div>
                </div>

                <div class="form-group">
                    <label>Đơn tối thiểu (đ)</label>
                    <input type="number" name="minOrderAmount" value="${voucher.minOrderAmount}" min="0" step="1000"
                           placeholder="Bỏ trống = không yêu cầu"/>
                </div>

                <div class="form-group">
                    <label>Bắt đầu</label>
                    <input type="datetime-local" name="startAt"
                           value="${not empty voucher.startAt ? voucher.startAt.toString().substring(0,16) : ''}"/>
                </div>

                <div class="form-group">
                    <label>Kết thúc</label>
                    <input type="datetime-local" name="endAt"
                           value="${not empty voucher.endAt ? voucher.endAt.toString().substring(0,16) : ''}"/>
                </div>

                <div class="form-group">
                    <label>Tổng lượt dùng</label>
                    <input type="number" name="usageLimit" value="${voucher.usageLimit}" min="1" step="1"
                           placeholder="Bỏ trống = không giới hạn"/>
                </div>

                <div class="form-group">
                    <label style="display:flex;align-items:center;gap:8px;font-weight:600;">
                        <input type="checkbox" name="oncePerUser" style="width:auto;"
                               ${voucher.id == null || voucher.oncePerUser ? 'checked' : ''}/>
                        Mỗi khách chỉ dùng 1 lần
                    </label>
                </div>

                <div class="specs-section">
                    <h3>Ràng buộc combo</h3>

                    <div class="form-group">
                        <label>Chỉ áp dụng cho combo</label>
                        <select name="comboId" id="oCombo" onchange="doiRangBuoc()">
                            <option value="">— Không giới hạn theo combo —</option>
                            <c:forEach var="cb" items="${dsCombo}">
                                <option value="${cb.id}" ${voucher.combo != null && voucher.combo.id == cb.id ? 'selected' : ''}>
                                    ${cb.name}<c:if test="${not cb.isActive}"> (đã tắt)</c:if>
                                </option>
                            </c:forEach>
                        </select>
                        <small style="color:var(--ink-3);">Chọn một combo thì mã chỉ dùng được khi giỏ có đúng combo đó.</small>
                    </div>

                    <div class="form-group" id="oDonCoCombo">
                        <label style="display:flex;align-items:center;gap:8px;font-weight:600;">
                            <input type="checkbox" name="chiDonCoCombo" style="width:auto;"
                                   ${voucher.chiDonCoCombo ? 'checked' : ''}/>
                            Chỉ áp dụng cho đơn có combo bất kỳ
                        </label>
                        <small style="color:var(--ink-3);">Bỏ trống cả hai = mã dùng được cho mọi đơn như bình thường.</small>
                    </div>

                    <div style="background:var(--warn-bg-2);border:1px solid var(--warn-line-2);color:var(--warn-deep);border-radius:var(--r-md);padding:11px 14px;font-size:12px;line-height:1.7;">
                        Combo đã tự có mức giảm riêng. Mã này giảm <strong>thêm</strong> lên trên đó —
                        cân nhắc đặt trần giảm để không bán dưới giá vốn.
                    </div>
                </div>

                <button type="submit" class="btn-primary">
                    ${voucher.id == null ? 'Tạo mã' : 'Lưu thay đổi'}
                </button>
            </form>
        </div>
    </div>
</div>

<script>
    function doiKieuGiam() {
        var laPhanTram = document.getElementById('discountType').value === 'PERCENT';
        document.getElementById('nhomTranGiam').style.display = laPhanTram ? 'block' : 'none';
        document.getElementById('donViGiam').textContent = laPhanTram ? '(nhập số %, tối đa 100)' : '(nhập số tiền, VNĐ)';
    }
    doiKieuGiam();

    /* Chọn đúng một combo thì ô "đơn có combo bất kỳ" thành thừa -> mờ đi và bỏ tick,
       tránh admin bật cả hai rồi không hiểu điều kiện nào đang có hiệu lực.
       Server cũng tự bỏ cờ này khi đã chọn combo, đây chỉ là lớp giao diện. */
    function doiRangBuoc() {
        var coCombo = !!document.getElementById('oCombo').value;
        var o = document.getElementById('oDonCoCombo');
        var tick = o.querySelector('input');
        if (coCombo) { tick.checked = false; }
        tick.disabled = coCombo;
        o.style.opacity = coCombo ? '.45' : '';
    }
    doiRangBuoc();

    // Nút "Tự sinh mã": xin server 1 mã mới (server đã kiểm không trùng) rồi điền vào ô.
    var nutSinhMa = document.getElementById('nutSinhMa');
    if (nutSinhMa) {
        nutSinhMa.addEventListener('click', function () {
            nutSinhMa.disabled = true;
            fetch('/admin/vouchers/gen-code')
                .then(function (r) { return r.text(); })
                .then(function (ma) { document.getElementById('oMa').value = ma.trim(); })
                .catch(function () { alert('Không lấy được mã mới, thử lại giúp mình nhé.'); })
                .finally(function () { nutSinhMa.disabled = false; });
        });
    }
</script>
</body>
</html>

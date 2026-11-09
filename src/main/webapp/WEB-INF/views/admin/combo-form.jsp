<%@ page contentType="text/html;charset=UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>${combo.id == null ? 'Thêm' : 'Sửa'} combo – TiTanGEAR</title>
    <link href="https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@400;500;600;700&family=IBM+Plex+Mono:wght@400;500;600&display=swap" rel="stylesheet"/>
    <link rel="stylesheet" href="/static/css/tokens.css"/>
    <link rel="stylesheet" href="/static/css/main.css"/>
    <link rel="stylesheet" href="/static/css/admin.css"/>
    <style>
        .cf-grid  { display:grid; grid-template-columns:1fr 340px; gap:18px; align-items:start; }
        .cf-row   { display:grid; grid-template-columns:1fr 190px 90px 42px; gap:10px; margin-bottom:10px; }
        .cf-row select, .cf-row input { width:100%; box-sizing:border-box; }
        .cf-x     { border:1px solid var(--line-new); border-radius:var(--r-md); background:var(--surface);
                    color:var(--ink-3); font-size:16px; cursor:pointer; }
        .cf-x:hover { background:var(--err-bg-2); border-color:var(--err-line-2); color:var(--err); }
        .cf-head  { display:grid; grid-template-columns:1fr 190px 90px 42px; gap:10px;
                    font-family:var(--font-mono); font-size:10px; letter-spacing:.1em;
                    color:var(--ink-4); margin-bottom:8px; }
        .cf-tomtat{ background:var(--surface-3); border:1px solid var(--line-new);
                    border-radius:var(--r-md); padding:14px 16px; font-size:13px; line-height:1.9; }
        .cf-tomtat b { font-family:var(--font-mono); }
        .cf-canhbao { color:var(--err); font-weight:600; }
        .cf-mau   { display:flex; gap:8px; flex-wrap:wrap; }
        .cf-mau label { position:relative; cursor:pointer; }
        .cf-mau input { position:absolute; opacity:0; pointer-events:none; }
        .cf-mau span  { display:inline-block; width:30px; height:30px; border-radius:50%;
                        border:2px solid var(--line-new); }
        .cf-mau input:checked + span { border-color:var(--ink); transform:scale(1.12); }
    </style>
</head>
<body>
<div class="admin-layout">
    <c:set var="adminActive" value="combos"/>
    <%@ include file="/WEB-INF/views/common/admin-sidebar.jsp" %>

    <div class="admin-main">
        <c:set var="adminTitle" value="${combo.id == null ? 'Thêm combo' : 'Sửa combo'}"/>
        <c:set var="adminCrumb" value="ADMIN / COMBOS / ${combo.id == null ? 'NEW' : combo.id}"/>
        <%@ include file="/WEB-INF/views/common/admin-topbar.jsp" %>

        <a href="/admin/combos" class="btn-reset" style="display:inline-block;margin-bottom:16px;">← Quay lại danh sách</a>

        <c:if test="${not empty comboError}">
            <div class="admin-alert-err">${comboError}</div>
        </c:if>

        <form method="post" action="/admin/combos/save" id="cfForm">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
            <c:if test="${combo.id != null}">
                <input type="hidden" name="id" value="${combo.id}"/>
                <input type="hidden" name="createdAt" value="${combo.createdAt}"/>
            </c:if>

            <div class="cf-grid">
                <%-- ===== CỘT TRÁI ===== --%>
                <div class="admin-card" style="padding:24px;">

                    <div class="form-group">
                        <label>Tên combo *</label>
                        <input type="text" name="name" value="${combo.name}" required
                               placeholder="VD: Combo Aim Lab"/>
                    </div>

                    <div class="pf-2col">
                        <div class="form-group">
                            <label>Nhãn phong cách</label>
                            <input type="text" name="tag" value="${combo.tag}" placeholder="VD: FPS TRY-HARD"/>
                        </div>
                        <div class="form-group">
                            <label>Số tiền giảm (đ) *</label>
                            <input type="number" name="discountAmount" id="cfGiam" min="0" step="1000"
                                   value="${combo.discountAmount.intValue()}" required/>
                        </div>
                    </div>

                    <div class="form-group">
                        <label>Mô tả ngắn</label>
                        <textarea name="description" rows="2"
                                  placeholder="Một câu mô tả bộ này hợp với ai">${combo.description}</textarea>
                    </div>

                    <div class="specs-section">
                        <h3>Các món trong bộ</h3>
                        <div class="cf-head">
                            <span>SẢN PHẨM</span><span>MÀU MẶC ĐỊNH</span><span>SỐ LƯỢNG</span><span></span>
                        </div>
                        <div id="cfMon">
                            <c:forEach var="it" items="${combo.items}">
                                <div class="cf-row">
                                    <select name="sanPhamIds" onchange="napMau(this)" required>
                                        <c:forEach var="p" items="${sanPham}">
                                            <option value="${p.id}" ${p.id == it.product.id ? 'selected' : ''}>${p.name}</option>
                                        </c:forEach>
                                    </select>
                                    <select name="bienTheIds" data-chon="${it.variant == null ? '' : it.variant.id}">
                                        <option value="">— không có màu —</option>
                                    </select>
                                    <input type="number" name="soLuongs" min="1" value="${it.quantity}"/>
                                    <button type="button" class="cf-x" onclick="xoaDong(this)">×</button>
                                </div>
                            </c:forEach>
                        </div>
                        <button type="button" class="btn-add-spec" onclick="themDong()">+ Thêm món</button>
                    </div>
                </div>

                <%-- ===== CỘT PHẢI ===== --%>
                <div class="admin-card pf-side" style="padding:24px;">
                    <div class="form-group">
                        <label>Màu nhấn của thẻ</label>
                        <div class="cf-mau">
                            <c:forEach var="ma" items="#1B49FF,#7A5CC7,#10A268,#E08600,#C62828,#0E1013">
                                <label>
                                    <input type="radio" name="accentColor" value="${ma}"
                                           ${(empty combo.accentColor ? '#1B49FF' : combo.accentColor) == ma ? 'checked' : ''}/>
                                    <span style="background:${ma};"></span>
                                </label>
                            </c:forEach>
                        </div>
                    </div>

                    <div class="form-group">
                        <label>Trạng thái</label>
                        <select name="isActive">
                            <option value="true"  ${combo.isActive ? 'selected' : ''}>Đang bán</option>
                            <option value="false" ${!combo.isActive ? 'selected' : ''}>Tắt</option>
                        </select>
                    </div>

                    <div class="cf-tomtat" id="cfTomTat">Chọn sản phẩm để xem giá bộ.</div>

                    <button type="submit" class="btn-primary pf-submit">
                        ${combo.id == null ? 'Tạo combo' : 'Lưu thay đổi'}
                    </button>
                </div>
            </div>
        </form>
    </div>
</div>

<script>
    // Giá từng sản phẩm để tính thử giá bộ ngay trên form.
    // Chỉ để admin thấy trước — số tiền thật vẫn do server tính lại khi lưu.
    var GIA = {
        <c:forEach var="p" items="${sanPham}" varStatus="l">
        "${p.id}": ${p.price}<c:if test="${!l.last}">,</c:if>
        </c:forEach>
    };
    var TEN = {
        <c:forEach var="p" items="${sanPham}" varStatus="l">
        "${p.id}": "<c:out value='${p.name}'/>"<c:if test="${!l.last}">,</c:if>
        </c:forEach>
    };

    function tien(n) { return new Intl.NumberFormat('vi-VN').format(Math.round(n)) + 'đ'; }

    /** Nạp danh sách màu theo sản phẩm đang chọn ở dòng đó. */
    function napMau(oSanPham) {
        var dong  = oSanPham.closest('.cf-row');
        var oMau  = dong.querySelector('select[name="bienTheIds"]');
        var muon  = oMau.dataset.chon || '';
        oMau.innerHTML = '<option value="">— đang tải —</option>';

        fetch('/admin/products/api/variants-lite/' + oSanPham.value)
            .then(function (r) { return r.json(); })
            .then(function (ds) {
                if (!ds.length) {
                    oMau.innerHTML = '<option value="">— sản phẩm không có màu —</option>';
                    oMau.dataset.chon = '';
                    return;
                }
                oMau.innerHTML = ds.map(function (v) {
                    var chon = String(v.id) === String(muon) ? ' selected' : '';
                    return '<option value="' + v.id + '"' + chon + '>'
                         + (v.color || 'Mặc định') + ' — còn ' + v.stock + '</option>';
                }).join('');
                oMau.dataset.chon = '';
            })
            .catch(function () { oMau.innerHTML = '<option value="">— lỗi tải màu —</option>'; });
        tinhLai();
    }

    function themDong() {
        var mau = document.querySelector('.cf-row');
        var dong;
        if (mau) {
            dong = mau.cloneNode(true);
            dong.querySelector('input[name="soLuongs"]').value = 1;
            dong.querySelector('select[name="bienTheIds"]').dataset.chon = '';
        } else {
            dong = document.createElement('div');
            dong.className = 'cf-row';
            dong.innerHTML = document.getElementById('cfMauDong').innerHTML;
        }
        document.getElementById('cfMon').appendChild(dong);
        napMau(dong.querySelector('select[name="sanPhamIds"]'));
    }

    function xoaDong(nut) {
        var ds = document.querySelectorAll('.cf-row');
        if (ds.length <= 1) { alert('Combo phải có ít nhất một sản phẩm.'); return; }
        nut.closest('.cf-row').remove();
        tinhLai();
    }

    /** Tính thử giá bộ + cảnh báo NGAY nếu tiền giảm ≥ tổng giá (luật 1). */
    function tinhLai() {
        var tong = 0;
        document.querySelectorAll('.cf-row').forEach(function (d) {
            var sp = d.querySelector('select[name="sanPhamIds"]').value;
            var sl = Number(d.querySelector('input[name="soLuongs"]').value) || 1;
            tong += (GIA[sp] || 0) * sl;
        });
        var giam  = Number(document.getElementById('cfGiam').value) || 0;
        var giaBo = tong - giam;
        var loi   = tong > 0 && giam >= tong;

        document.getElementById('cfTomTat').innerHTML =
            'Tổng giá niêm yết: <b>' + tien(tong) + '</b><br>'
          + 'Tiền giảm: <b>−' + tien(giam) + '</b><br>'
          + 'Giá bộ bán ra: <b style="color:var(--accent);font-size:15px;">' + tien(Math.max(0, giaBo)) + '</b>'
          + (loi ? '<div class="cf-canhbao" style="margin-top:8px;">Tiền giảm phải NHỎ HƠN tổng giá bộ, '
                 + 'nếu không combo sẽ bán 0đ hoặc âm — server sẽ từ chối lưu.</div>' : '');
    }

    document.addEventListener('input', function (e) {
        if (e.target.name === 'soLuongs' || e.target.id === 'cfGiam') tinhLai();
    });

    // Nạp màu cho các dòng có sẵn (form sửa), hoặc tạo dòng đầu tiên (form thêm)
    var coSan = document.querySelectorAll('.cf-row');
    if (coSan.length) {
        coSan.forEach(function (d) { napMau(d.querySelector('select[name="sanPhamIds"]')); });
    } else {
        themDongDauTien();
    }

    function themDongDauTien() {
        var html = '<div class="cf-row">'
            + '<select name="sanPhamIds" onchange="napMau(this)" required>'
            + Object.keys(GIA).map(function (id) {
                  return '<option value="' + id + '">' + TEN[id] + '</option>'; }).join('')
            + '</select>'
            + '<select name="bienTheIds" data-chon=""><option value="">— không có màu —</option></select>'
            + '<input type="number" name="soLuongs" min="1" value="1"/>'
            + '<button type="button" class="cf-x" onclick="xoaDong(this)">×</button>'
            + '</div>';
        document.getElementById('cfMon').insertAdjacentHTML('beforeend', html);
        napMau(document.querySelector('.cf-row select[name="sanPhamIds"]'));
    }

    tinhLai();
</script>
</body>
</html>

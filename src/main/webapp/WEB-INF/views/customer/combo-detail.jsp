<%@ page contentType="text/html;charset=UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="${cb.combo.name} – TiTanGEAR"/>
<c:set var="currentPage" value="combo"/>
<%@ include file="/WEB-INF/views/common/header.jsp" %>
<c:set var="cbAccent" value="${empty cb.combo.accentColor ? '#1B49FF' : cb.combo.accentColor}"/>

<div class="tg-page">
<section class="tg-sect">
    <div class="tg-wrap">

        <div class="tg-mono" style="font-size:11px;letter-spacing:.1em;color:var(--ink-4);margin-bottom:18px;">
            <a href="/home" style="color:var(--ink-4);text-decoration:none;">TRANG CHỦ</a> /
            <a href="/combo" style="color:var(--ink-4);text-decoration:none;">COMBO</a> /
            <span style="color:var(--ink-2);">${cb.combo.name}</span>
        </div>

        <form method="post" action="/combo/${cb.combo.id}/mua" id="cbForm">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>

            <div class="cb-layout">

                <%-- ===== CỘT TRÁI: từng món, có chọn phiên bản + màu ===== --%>
                <div>
                    <div style="display:flex;align-items:center;gap:12px;margin-bottom:6px;">
                        <c:if test="${not empty cb.combo.tag}">
                            <span class="tg-combo-tag" style="color:${cbAccent};background:var(--surface-3);">${cb.combo.tag}</span>
                        </c:if>
                        <c:if test="${not cb.conHang}">
                            <span class="tg-combo-save" style="color:var(--ink-2);background:var(--surface-3);">TẠM HẾT</span>
                        </c:if>
                    </div>

                    <h1 style="font-size:32px;font-weight:700;margin:0 0 10px;letter-spacing:-.02em;">${cb.combo.name}</h1>
                    <p style="font-size:15px;color:var(--ink-2);line-height:1.75;margin:0 0 26px;max-width:620px;">${cb.combo.description}</p>

                    <div class="tg-mono" style="font-size:11px;letter-spacing:.12em;color:var(--ink-4);margin-bottom:14px;">
                        TRONG BỘ CÓ ${cb.cacMon.size()} MÓN
                    </div>

                    <div style="display:flex;flex-direction:column;gap:14px;">
                        <c:forEach var="m" items="${cb.cacMon}" varStatus="loop">
                            <c:set var="vt" value="${m.viTri.id}"/>
                            <div class="tg-card cb-slot tg-rise" data-slot="${vt}" style="animation-delay:${loop.index * 70}ms;">

                                <div class="cb-slot-top">
                                    <div class="cb-thumb">
                                        <img id="anh_${vt}"
                                             src="/static/images/${empty m.macDinh.imageUrl ? 'banner.png' : m.macDinh.imageUrl}"
                                             alt="${m.macDinh.name}" class="tg-zoom"
                                             onerror="this.onerror=null;this.src='/static/images/banner.png';"/>
                                    </div>
                                    <div style="min-width:0;flex:1;">
                                        <c:if test="${not empty m.macDinh.category}">
                                            <div class="tg-mono" style="font-size:10px;letter-spacing:.1em;color:var(--accent);font-weight:600;">${m.macDinh.category.name}</div>
                                        </c:if>
                                        <a id="ten_${vt}" href="/products/${m.macDinh.id}" class="cb-slot-name">${m.macDinh.name}</a>
                                        <div class="cb-slot-price" id="gia_${vt}"></div>
                                    </div>
                                    <c:if test="${m.soLuong > 1}">
                                        <span class="tg-chip">× ${m.soLuong}</span>
                                    </c:if>
                                </div>

                                <%-- Chọn phiên bản: chỉ hiện khi sản phẩm thuộc một dòng có nhiều bản --%>
                                <c:if test="${m.coTheDoiPhienBan}">
                                    <div class="cb-pick">
                                        <div class="cb-pick-lab">PHIÊN BẢN</div>
                                        <div class="cb-opts">
                                            <c:forEach var="pb" items="${m.phienBan}">
                                                <label class="cb-opt">
                                                    <input type="radio" name="sp_${vt}" value="${pb.id}"
                                                           data-slot="${vt}"
                                                           ${pb.id == m.macDinh.id ? 'checked' : ''}/>
                                                    <span>${pb.name}</span>
                                                </label>
                                            </c:forEach>
                                        </div>
                                    </div>
                                </c:if>

                                <%-- Chọn màu: danh sách phụ thuộc phiên bản đang chọn nên do JS dựng --%>
                                <div class="cb-pick" id="oMau_${vt}" style="display:none;">
                                    <div class="cb-pick-lab">MÀU</div>
                                    <div class="cb-opts" id="mau_${vt}"></div>
                                </div>

                            </div>
                        </c:forEach>
                    </div>
                </div>

                <%-- ===== CỘT PHẢI: bảng giá ===== --%>
                <aside>
                    <div class="tg-card cb-sum">
                        <div class="tg-combo-strip" style="background:${cbAccent};margin:-1px -1px 0;"></div>
                        <div style="padding:24px 26px 26px;">
                            <div class="tg-mono" style="font-size:11px;letter-spacing:.12em;color:var(--ink-4);margin-bottom:16px;">GIÁ TRỌN BỘ</div>

                            <div class="cb-srow">
                                <span>Mua lẻ từng món</span>
                                <span class="tg-combo-old" style="font-size:14px;" id="sumGoc"></span>
                            </div>
                            <div class="cb-srow" id="rowTietKiem">
                                <span>Tiết kiệm</span>
                                <span style="font-size:14px;font-weight:600;color:var(--ok-deep);" id="sumGiam"></span>
                            </div>

                            <div style="padding-top:16px;border-top:1px solid var(--line-soft);display:flex;align-items:baseline;justify-content:space-between;">
                                <span style="font-size:14px;font-weight:600;">Giá combo</span>
                                <span style="font-size:26px;font-weight:700;color:var(--accent);letter-spacing:-.02em;" id="sumBan"></span>
                            </div>

                            <c:if test="${not empty comboError}">
                                <div style="margin-top:18px;background:var(--err-bg-2);border:1px solid var(--err-line-2);color:var(--err);border-radius:var(--r-md);padding:12px 14px;font-size:13px;line-height:1.6;">
                                    ${comboError}
                                </div>
                            </c:if>

                            <c:choose>
                                <c:when test="${not cb.conHang}">
                                    <div style="margin-top:18px;background:var(--warn-bg-2);border:1px solid var(--warn-line-2);color:var(--warn-deep);border-radius:var(--r-md);padding:12px 14px;font-size:13px;line-height:1.6;">
                                        Một sản phẩm trong bộ đang hết hàng nên combo tạm ngừng bán. Bạn vẫn có thể mua lẻ các món còn lại.
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <%-- Form CHỈ gửi id phiên bản + màu, KHÔNG gửi giá.
                                         Server tự tính lại toàn bộ và tự kiểm id có hợp lệ
                                         với vị trí đó không (luật 6). --%>
                                    <button type="submit" class="tg-btn tg-primary" style="width:100%;margin-top:18px;">
                                        Mua trọn bộ
                                    </button>
                                    <div style="font-size:12px;color:var(--ink-3);margin-top:10px;line-height:1.6;text-align:center;">
                                        Cả bộ sẽ được thêm vào giỏ. Combo không tách lẻ từng món.
                                    </div>
                                </c:otherwise>
                            </c:choose>

                            <a href="/combo" class="tg-btn tg-ghost" style="display:block;text-align:center;margin-top:12px;">← Xem combo khác</a>
                        </div>
                    </div>
                </aside>

            </div>
        </form>
    </div>
</section>
</div>

<style>
    .cb-layout { display: grid; grid-template-columns: 1fr 360px; gap: 28px; align-items: start; }
    .cb-slot { padding: 16px 18px; }
    .cb-slot-top { display: flex; align-items: center; gap: 16px; }
    .cb-slot-name { font-size: 16px; font-weight: 600; margin: 5px 0 4px; line-height: 1.35;
                    display: block; color: var(--ink); text-decoration: none; }
    .cb-slot-name:hover { color: var(--accent); }
    .cb-slot-price { font-size: 15px; font-weight: 700; color: var(--accent); }
    .cb-slot-price .old { font-size: 12px; color: var(--ink-4); text-decoration: line-through;
                          font-weight: 400; margin-left: 6px; }
    .cb-thumb { width: 84px; height: 84px; flex: none; background: var(--surface-2);
                border-radius: var(--r-md); padding: 8px; display: flex;
                align-items: center; justify-content: center; }
    .cb-thumb img { width: 100%; height: 100%; object-fit: contain; mix-blend-mode: multiply; }

    .cb-pick { margin-top: 14px; padding-top: 13px; border-top: 1px solid var(--line-soft); }
    .cb-pick-lab { font-family: var(--font-mono); font-size: 10px; letter-spacing: .12em;
                   color: var(--ink-4); margin-bottom: 9px; }
    .cb-opts { display: flex; gap: 8px; flex-wrap: wrap; }
    .cb-opt { position: relative; }
    .cb-opt input { position: absolute; opacity: 0; pointer-events: none; }
    .cb-opt span { display: inline-flex; align-items: center; gap: 7px; cursor: pointer;
                   padding: 8px 14px; border: 1px solid var(--line-new); border-radius: var(--r-pill);
                   font-size: 13px; color: var(--ink-2); background: var(--surface);
                   transition: border-color .2s ease, color .2s ease, background .2s ease; }
    .cb-opt span:hover { border-color: var(--accent); color: var(--accent); }
    .cb-opt input:checked + span { border-color: var(--accent); background: var(--accent-soft-2);
                                   color: var(--accent); font-weight: 600; }
    .cb-opt input:disabled + span { opacity: .45; cursor: not-allowed; text-decoration: line-through; }
    .cb-dot { width: 13px; height: 13px; border-radius: 50%; border: 1px solid var(--line-new); }

    .cb-sum { position: sticky; top: 92px; overflow: hidden; }
    .cb-srow { display: flex; align-items: baseline; justify-content: space-between; margin-bottom: 8px;
               font-size: 13px; color: var(--ink-2); }
    @media (max-width: 1000px) {
        .cb-layout { grid-template-columns: 1fr; }
        .cb-sum { position: static; }
    }
</style>

<script>
    // Dữ liệu mọi phiên bản + màu. Chỉ dùng để hiện giá tại chỗ cho mượt —
    // số tiền THẬT luôn do server tính lại lúc bấm mua.
    var CB = {
        giam: ${cb.combo.discountAmount == null ? 0 : cb.combo.discountAmount},
        mon: [
            <c:forEach var="m" items="${cb.cacMon}" varStatus="lm">
            {
                slot: ${m.viTri.id},
                soLuong: ${m.soLuong},
                mauMacDinh: ${m.mauMacDinh == null ? 'null' : m.mauMacDinh.id},
                ban: [
                    <c:forEach var="pb" items="${m.phienBan}" varStatus="lp">
                    {
                        id: ${pb.id},
                        ten: "<c:out value='${pb.name}'/>",
                        anh: "<c:out value='${empty pb.imageUrl ? "banner.png" : pb.imageUrl}'/>",
                        giaGoc: ${pb.price},
                        giaBan: ${empty cb.giaHieuLuc[pb.id] ? pb.price : cb.giaHieuLuc[pb.id]},
                        ton: ${empty pb.stockQuantity ? 0 : pb.stockQuantity},
                        mau: [
                            <c:forEach var="v" items="${pb.variants}" varStatus="lv">
                            <c:if test="${v.isActive}">
                            { id: ${v.id}, ten: "<c:out value='${v.color}'/>",
                              ma: "<c:out value='${empty v.colorCode ? "#CCCCCC" : v.colorCode}'/>",
                              ton: ${empty v.stock ? 0 : v.stock} }<c:if test="${!lv.last}">,</c:if>
                            </c:if>
                            </c:forEach>
                        ]
                    }<c:if test="${!lp.last}">,</c:if>
                    </c:forEach>
                ]
            }<c:if test="${!lm.last}">,</c:if>
            </c:forEach>
        ]
    };

    function tien(n) { return new Intl.NumberFormat('vi-VN').format(Math.round(n)) + 'đ'; }

    function banDangChon(mon) {
        var o = document.querySelector('input[name="sp_' + mon.slot + '"]:checked');
        var id = o ? Number(o.value) : mon.ban[0].id;
        return mon.ban.find(function (b) { return b.id === id; }) || mon.ban[0];
    }

    /** Dựng lại danh sách màu theo phiên bản đang chọn. Màu hết hàng thì khóa lại. */
    function veMau(mon) {
        var ban = banDangChon(mon);
        var oMau = document.getElementById('oMau_' + mon.slot);
        var hop  = document.getElementById('mau_' + mon.slot);
        if (!ban.mau.length) { oMau.style.display = 'none'; hop.innerHTML = ''; return; }

        oMau.style.display = '';
        var conHang = ban.mau.filter(function (v) { return v.ton > 0; });
        var chon = conHang.length ? conHang[0].id : ban.mau[0].id;
        if (mon.mauMacDinh && ban.mau.some(function (v) { return v.id === mon.mauMacDinh && v.ton > 0; })) {
            chon = mon.mauMacDinh;
        }

        hop.innerHTML = ban.mau.map(function (v) {
            return '<label class="cb-opt">'
                 + '<input type="radio" name="mau_' + mon.slot + '" value="' + v.id + '"'
                 + (v.id === chon ? ' checked' : '') + (v.ton > 0 ? '' : ' disabled') + '/>'
                 + '<span><i class="cb-dot" style="background:' + v.ma + '"></i>' + v.ten + '</span>'
                 + '</label>';
        }).join('');
    }

    function capNhat() {
        var tongGoc = 0, tongLe = 0;
        CB.mon.forEach(function (mon) {
            var ban = banDangChon(mon);
            tongGoc += ban.giaGoc * mon.soLuong;
            tongLe  += ban.giaBan * mon.soLuong;

            document.getElementById('ten_' + mon.slot).textContent = ban.ten;
            document.getElementById('ten_' + mon.slot).href = '/products/' + ban.id;
            document.getElementById('anh_' + mon.slot).src = '/static/images/' + ban.anh;
            document.getElementById('gia_' + mon.slot).innerHTML =
                tien(ban.giaBan) + (ban.giaBan < ban.giaGoc
                    ? ' <span class="old">' + tien(ban.giaGoc) + '</span>' : '');
        });

        // Cùng công thức với server: MIN(tổng niêm yết − giảm, tổng đang bán lẻ).
        // Hai bên lệch công thức là khách thấy một giá, bị thu một giá khác.
        var giaBo = Math.min(tongGoc - CB.giam, tongLe);
        if (giaBo < 0) giaBo = 0;
        var tietKiem = tongGoc - giaBo;

        document.getElementById('sumGoc').textContent = tien(tongGoc);
        document.getElementById('sumBan').textContent = tien(giaBo);
        document.getElementById('sumGiam').textContent = '−' + tien(tietKiem);
        document.getElementById('rowTietKiem').style.display = tietKiem > 0 ? '' : 'none';
    }

    document.addEventListener('change', function (e) {
        if (!e.target.name) return;
        if (e.target.name.indexOf('sp_') === 0) {
            var slot = Number(e.target.dataset.slot);
            var mon = CB.mon.find(function (x) { return x.slot === slot; });
            if (mon) veMau(mon);          // đổi bản -> bộ màu khác
        }
        if (e.target.name.indexOf('sp_') === 0 || e.target.name.indexOf('mau_') === 0) capNhat();
    });

    CB.mon.forEach(veMau);
    capNhat();
</script>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>

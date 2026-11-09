<%@ page contentType="text/html;charset=UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Giỏ Hàng – TiTanGEAR"/>
<c:set var="currentPage" value=""/>
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<div class="tg-page">
<div class="ct-wrap">

    <div class="tg-mono" style="font-size:11px;color:var(--ink-4);letter-spacing:.1em;margin-bottom:6px;">TRANG CHỦ / GIỎ HÀNG</div>
    <h1 style="font-size:32px;font-weight:700;margin:0 0 22px;letter-spacing:-.02em;">Giỏ hàng</h1>

    <c:if test="${not empty param.qtyError}">
        <div class="ct-err">${param.qtyError}</div>
    </c:if>

    <c:choose>
        <c:when test="${empty cartItems}">
            <div class="tg-card" style="padding:80px 40px;text-align:center;">
                <div style="font-size:17px;font-weight:600;margin-bottom:8px;">Giỏ hàng đang trống</div>
                <p style="font-size:14px;color:var(--ink-3);margin:0 0 20px;">Chọn vài món ưng ý rồi quay lại nhé.</p>
                <a href="/products" class="tg-btn tg-primary">Tiếp tục mua sắm →</a>
            </div>
        </c:when>

        <c:otherwise>
            <div class="ct-layout">
                <div class="tg-card tg-rise" style="padding:8px 0;">
                    <c:set var="comboTruoc" value=""/>
                    <c:forEach var="item" items="${cartItems}" varStatus="l">
                        <%-- Dòng tiêu đề của bộ: chỉ in MỘT lần cho mỗi combo, để khách
                             thấy rõ mấy món này đi liền nhau chứ không phải mua rời. --%>
                        <c:if test="${item.combo != null && comboTruoc != item.combo.id}">
                            <c:set var="comboTruoc" value="${item.combo.id}"/>
                            <div class="ct-combo-head">
                                <span class="ct-combo-tag">COMBO</span>
                                <a href="/combo/${item.combo.id}">${item.combo.name}</a>
                                <span class="ct-combo-note">— mua trọn bộ, không tách lẻ</span>
                            </div>
                        </c:if>
                        <div class="ct-row ${l.last ? 'is-last' : ''} ${item.combo != null ? 'is-combo' : ''}">
                            <a href="/products/${item.product.id}" class="ct-thumb">
                                <c:choose>
                                    <c:when test="${not empty item.product.imageUrl}">
                                        <img src="/static/images/${item.product.imageUrl}" alt="${item.product.name}"
                                             onerror="this.onerror=null;this.src='/static/images/banner.png';"/>
                                    </c:when>
                                    <c:otherwise><img src="/static/images/banner.png" alt="${item.product.name}"/></c:otherwise>
                                </c:choose>
                            </a>

                            <div style="min-width:0;">
                                <a href="/products/${item.product.id}" class="ct-name">${item.product.name}</a>
                                <%-- Sản phẩm/màu bị ngừng bán SAU KHI khách đã bỏ vào giỏ.
                                     Báo ngay tại đây thay vì để khách bấm "Đặt hàng" rồi mới
                                     bị từ chối — lúc đó họ đã điền xong địa chỉ, rất khó chịu. --%>
                                <c:if test="${not item.product.isActive
                                              or (item.variant != null and not item.variant.isActive)}">
                                    <div class="ct-off">Sản phẩm này đã ngừng kinh doanh — vui lòng xóa khỏi giỏ</div>
                                </c:if>
                                <c:if test="${item.variant != null}">
                                    <div class="ct-var">
                                        ${item.variant.name}<c:if test="${not empty item.variant.color}"> — ${item.variant.color}</c:if>
                                    </div>
                                </c:if>
                                <div class="ct-unit">
                                    <c:choose>
                                        <%-- Dòng combo: hiện đơn giá ĐÃ chia phần giảm của bộ,
                                             gạch ngang giá niêm yết cho khách thấy được lợi bao nhiêu --%>
                                        <c:when test="${item.combo != null}">
                                            <fmt:formatNumber value="${giaDong[item.id].donGia}" pattern="#,##0"/>đ
                                            <span class="old"><fmt:formatNumber value="${item.product.price}" pattern="#,##0"/>đ</span>
                                        </c:when>
                                        <c:when test="${priceMap[item.product.id].hasSale}">
                                            <fmt:formatNumber value="${priceMap[item.product.id].salePrice}" pattern="#,##0"/>đ
                                            <span class="old"><fmt:formatNumber value="${item.product.price}" pattern="#,##0"/>đ</span>
                                        </c:when>
                                        <c:otherwise><fmt:formatNumber value="${item.product.price}" pattern="#,##0"/>đ</c:otherwise>
                                    </c:choose>
                                </div>
                            </div>

                            <c:choose>
                                <%-- LUẬT 3: dòng trong combo đi theo bộ nên KHÔNG có nút +/−.
                                     Server cũng chặn (CartService.updateQuantity), đây chỉ là
                                     lớp giao diện cho khách khỏi bấm rồi ăn lỗi. --%>
                                <c:when test="${item.combo != null}">
                                    <div class="ct-qty ct-qty-lock" title="Số lượng trong combo là cố định">
                                        <span class="ct-qnum">× ${item.quantity}</span>
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <div class="ct-qty">
                                        <form action="/cart/update" method="post" style="margin:0;">
                                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                                            <input type="hidden" name="cartItemId" value="${item.id}"/>
                                            <input type="hidden" name="quantity" value="${item.quantity - 1}"/>
                                            <button type="submit" class="ct-qbtn">−</button>
                                        </form>
                                        <span class="ct-qnum">${item.quantity}</span>
                                        <form action="/cart/update" method="post" style="margin:0;">
                                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                                            <input type="hidden" name="cartItemId" value="${item.id}"/>
                                            <input type="hidden" name="quantity" value="${item.quantity + 1}"/>
                                            <button type="submit" class="ct-qbtn" ${item.quantity >= item.product.stockQuantity ? 'disabled' : ''}>+</button>
                                        </form>
                                    </div>
                                </c:otherwise>
                            </c:choose>

                            <div class="ct-total">
                                <fmt:formatNumber value="${giaDong[item.id].thanhTien}" pattern="#,##0"/>đ
                            </div>

                            <a href="/cart/remove?cartItemId=${item.id}" class="ct-x" data-post
                               title="${item.combo != null ? 'Xóa cả combo khỏi giỏ' : 'Xóa khỏi giỏ'}">×</a>
                        </div>
                    </c:forEach>
                </div>

                <div class="tg-card tg-rise ct-sum">
                    <div class="tg-mono" style="font-size:10px;letter-spacing:.14em;color:var(--ink-4);margin-bottom:16px;">TÓM TẮT ĐƠN</div>
                    <div class="ct-srow"><span>Tạm tính</span><span><fmt:formatNumber value="${total}" pattern="#,##0"/>đ</span></div>
                    <%-- Giỏ hàng CHƯA biết khách ở tỉnh nào nên chưa tính được phí.
                         Ghi "Miễn phí" ở đây là hứa sai — sang bước đặt hàng khách chọn
                         tỉnh xong mới thấy con số thật, dễ tưởng bị tính thêm. --%>
                    <div class="ct-srow"><span>Phí vận chuyển</span><span style="color:var(--ink-3);">Tính ở bước đặt hàng</span></div>
                    <div class="ct-srow ct-stotal"><span>Tổng cộng</span><span><fmt:formatNumber value="${total}" pattern="#,##0"/>đ</span></div>
                    <a href="/checkout" class="tg-btn tg-primary" style="width:100%;margin-top:6px;">Tiến hành đặt hàng →</a>
                    <a href="/products" style="display:block;text-align:center;margin-top:12px;font-size:13px;color:var(--ink-3);text-decoration:none;">← Tiếp tục mua sắm</a>
                </div>
            </div>
        </c:otherwise>
    </c:choose>
</div>
</div><!-- /.tg-page -->

<style>
    .ct-wrap   { max-width:1300px; margin:0 auto; padding:24px 48px 80px; }
    .ct-layout { display:grid; grid-template-columns:1fr 340px; gap:24px; align-items:start; }
    .ct-row    { display:grid; grid-template-columns:88px 1fr auto auto 34px; gap:18px;
                 align-items:center; padding:18px 24px; border-bottom:1px solid var(--line-hair-2); }
    .ct-row.is-last { border-bottom:none; }
    .ct-thumb  { width:88px; height:88px; border-radius:var(--r-md); background:var(--bg-page);
                 display:flex; align-items:center; justify-content:center; padding:8px;
                 box-sizing:border-box; overflow:hidden; }
    .ct-thumb img { width:100%; height:100%; object-fit:contain; mix-blend-mode:multiply; }
    .ct-name   { font-size:15px; font-weight:600; color:var(--ink); text-decoration:none; display:block; }
    .ct-name:hover { color:var(--accent); }
    .ct-var    { font-size:12px; color:var(--ink-3); margin-top:3px; }
    .ct-unit   { font-size:13px; color:var(--accent); font-weight:600; margin-top:6px; }
    .ct-unit .old { color:var(--ink-4); text-decoration:line-through; font-weight:400; margin-left:6px; }
    .ct-qty    { display:flex; align-items:center; border:1px solid var(--line-new); border-radius:var(--r-md); overflow:hidden; }
    /* ---- Combo trong giỏ ---- */
    .ct-combo-head { display:flex; align-items:center; gap:9px; flex-wrap:wrap;
                     padding:14px 22px 4px; font-size:14px; font-weight:600; }
    .ct-combo-head a { color:var(--ink); text-decoration:none; }
    .ct-combo-head a:hover { color:var(--accent); }
    .ct-combo-tag { font-family:var(--font-mono); font-size:10px; letter-spacing:.12em;
                    background:var(--accent-soft-2); color:var(--accent); border:1px solid var(--accent-line);
                    border-radius:var(--r-pill); padding:3px 9px; }
    .ct-combo-note { font-size:12px; font-weight:400; color:var(--ink-3); }
    .ct-row.is-combo { background:linear-gradient(90deg, var(--accent-soft-2) 0 3px, transparent 3px); }
    .ct-qty-lock { border-color:transparent; color:var(--ink-3); padding:0 4px; }
    .ct-off { font-size:12px; color:var(--err); background:var(--err-bg-2); border:1px solid var(--err-line-2);
              border-radius:var(--r-sm); padding:4px 9px; margin-top:5px; display:inline-block; }
    .ct-qbtn   { width:36px; height:38px; border:none; background:var(--surface); color:var(--ink-2);
                 font-size:16px; cursor:pointer; transition:background .2s ease; }
    .ct-qbtn:hover:not(:disabled) { background:var(--surface-3); }
    .ct-qbtn:disabled { opacity:.35; cursor:not-allowed; }
    .ct-qnum   { width:42px; text-align:center; font-family:var(--font-mono); font-size:14px; font-weight:600;
                 border-left:1px solid var(--line-new); border-right:1px solid var(--line-new); line-height:38px; }
    .ct-total  { font-size:16px; font-weight:700; white-space:nowrap; min-width:120px; text-align:right; }
    .ct-x      { width:30px; height:30px; border-radius:50%; border:1px solid var(--line-new);
                 display:inline-flex; align-items:center; justify-content:center; color:var(--ink-3);
                 text-decoration:none; font-size:16px; transition:all .2s ease; }
    .ct-x:hover { background:var(--err-bg-2); border-color:var(--err-line-2); color:var(--err); }

    .ct-sum    { padding:24px 26px; position:sticky; top:100px; }
    .ct-srow   { display:flex; justify-content:space-between; align-items:baseline;
                 padding:9px 0; font-size:14px; color:var(--ink-2); }
    .ct-stotal { border-top:1px solid var(--line-new); margin-top:8px; padding-top:14px;
                 font-size:16px; font-weight:700; color:var(--ink); }
    .ct-stotal span:last-child { color:var(--accent); font-size:22px; }
    .ct-err    { background:var(--err-bg-2); border:1px solid var(--err-line-2); color:var(--err);
                 border-radius:var(--r-md); padding:12px 16px; font-size:13px; margin-bottom:16px; }

    @media (max-width:1000px) {
        .ct-wrap   { padding:24px 20px 60px; }
        .ct-layout { grid-template-columns:1fr; }
        .ct-sum    { position:static; }
        .ct-row    { grid-template-columns:70px 1fr 34px; row-gap:12px; }
        .ct-qty, .ct-total { grid-column:2; justify-self:start; }
        .ct-total  { text-align:left; }
    }
</style>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>

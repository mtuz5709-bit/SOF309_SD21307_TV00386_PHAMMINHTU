<%@ page contentType="text/html;charset=UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="So sánh sản phẩm – TiTanGEAR"/>
<c:set var="currentPage" value="products"/>
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<div class="tg-page">
<div class="cmp-wrap">

    <div style="display:flex;align-items:flex-end;justify-content:space-between;gap:16px;flex-wrap:wrap;margin-bottom:22px;">
        <div>
            <div class="tg-mono" style="font-size:11px;color:var(--ink-4);letter-spacing:.1em;margin-bottom:6px;">SO SÁNH THÔNG SỐ</div>
            <h1 style="font-size:32px;font-weight:700;margin:0;letter-spacing:-.02em;">
                So sánh ${sanPham.size()} sản phẩm
            </h1>
        </div>
        <div style="display:flex;gap:10px;flex-wrap:wrap;">
            <button type="button" class="tg-btn tg-ghost" onclick="cmpXoaHet()">Xóa tất cả</button>
            <a href="/products" class="tg-btn tg-primary">+ Thêm sản phẩm</a>
        </div>
    </div>

    <c:choose>
        <c:when test="${empty sanPham}">
            <div class="tg-card" style="padding:80px 40px;text-align:center;">
                <div style="font-size:17px;font-weight:600;margin-bottom:8px;">Chưa chọn sản phẩm nào để so sánh</div>
                <p style="font-size:14px;color:var(--ink-3);margin:0 0 20px;">
                    Vào danh sách sản phẩm, rê chuột vào một sản phẩm rồi bấm nút biểu đồ để thêm vào so sánh.
                </p>
                <a href="/products" class="tg-btn tg-primary">Xem sản phẩm</a>
            </div>
        </c:when>

        <c:otherwise>
            <div class="tg-card" style="overflow-x:auto;">
                <table class="cmp-table">
                    <%-- ===== HÀNG ĐẦU: ảnh + tên + giá ===== --%>
                    <thead>
                    <tr>
                        <th class="cmp-lab"></th>
                        <c:forEach var="p" items="${sanPham}">
                            <th class="cmp-col">
                                <button type="button" class="cmp-x" onclick="cmpBo(${p.id})" title="Bỏ khỏi so sánh">×</button>
                                <div style="background:var(--bg-page);border-radius:var(--r-lg);padding:24px;margin-bottom:14px;">
                                    <c:choose>
                                        <c:when test="${not empty p.imageUrl}">
                                            <img src="/static/images/${p.imageUrl}" alt="${p.name}"
                                                 onerror="this.onerror=null;this.src='/static/images/banner.png';"
                                                 style="width:100%;height:180px;object-fit:contain;mix-blend-mode:multiply;display:block;"/>
                                        </c:when>
                                        <c:otherwise>
                                            <img src="/static/images/banner.png" alt="${p.name}"
                                                 style="width:100%;height:180px;object-fit:contain;display:block;"/>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                                <div style="font-size:16px;font-weight:600;margin-bottom:4px;">${p.name}</div>
                                <div style="font-size:19px;font-weight:700;color:var(--accent);margin-bottom:12px;">
                                    <c:choose>
                                        <c:when test="${priceMap[p.id].hasSale}"><fmt:formatNumber value="${priceMap[p.id].salePrice}" pattern="#,##0"/>đ</c:when>
                                        <c:otherwise><fmt:formatNumber value="${p.price}" pattern="#,##0"/>đ</c:otherwise>
                                    </c:choose>
                                </div>
                                <a href="/products/${p.id}" class="tg-btn tg-ghost" style="width:100%;padding:10px;font-size:13px;">Xem chi tiết</a>
                            </th>
                        </c:forEach>
                    </tr>
                    </thead>

                    <tbody>
                    <tr class="cmp-head-row">
                        <td class="cmp-lab tg-mono" style="font-size:10px;letter-spacing:.14em;color:var(--ink-4);">THÔNG SỐ</td>
                        <c:forEach var="p" items="${sanPham}"><td></td></c:forEach>
                    </tr>

                    <%-- Giá bán — thấp nhất được gắn nhãn TỐT NHẤT --%>
                    <tr>
                        <td class="cmp-lab">Giá bán</td>
                        <c:forEach var="p" items="${sanPham}">
                            <c:set var="giaP" value="${priceMap[p.id].hasSale ? priceMap[p.id].salePrice : p.price}"/>
                            <td>
                                <c:choose>
                                    <c:when test="${giaP == giaThapNhat}">
                                        <span class="cmp-best-val"><fmt:formatNumber value="${giaP}" pattern="#,##0"/>đ</span>
                                        <span class="cmp-best">TỐT NHẤT</span>
                                    </c:when>
                                    <c:otherwise><fmt:formatNumber value="${giaP}" pattern="#,##0"/>đ</c:otherwise>
                                </c:choose>
                            </td>
                        </c:forEach>
                    </tr>

                    <tr>
                        <td class="cmp-lab">Danh mục</td>
                        <c:forEach var="p" items="${sanPham}">
                            <td>${not empty p.category ? p.category.name : '—'}</td>
                        </c:forEach>
                    </tr>

                    <tr>
                        <td class="cmp-lab">Kết nối</td>
                        <c:forEach var="p" items="${sanPham}">
                            <td>${not empty p.connectivity ? nhomKetNoi[p.connectivity] : '—'}</td>
                        </c:forEach>
                    </tr>

                    <tr>
                        <td class="cmp-lab">Loại switch</td>
                        <c:forEach var="p" items="${sanPham}">
                            <td>${not empty p.switchType ? nhomSwitch[p.switchType] : '—'}</td>
                        </c:forEach>
                    </tr>

                    <tr>
                        <td class="cmp-lab">Trọng lượng</td>
                        <c:forEach var="p" items="${sanPham}">
                            <td>${not empty p.weightGroup ? nhomTrongLuong[p.weightGroup] : '—'}</td>
                        </c:forEach>
                    </tr>

                    <%-- Các dòng thông số kỹ thuật do admin nhập cho từng sản phẩm --%>
                    <c:forEach var="ten" items="${dongThongSo}">
                        <tr>
                            <td class="cmp-lab">${ten}</td>
                            <c:forEach var="p" items="${sanPham}">
                                <td>
                                    <c:set var="giaTri" value="—"/>
                                    <c:forEach var="s" items="${p.specs}">
                                        <c:if test="${s.specName == ten}"><c:set var="giaTri" value="${s.specValue}"/></c:if>
                                    </c:forEach>
                                    ${giaTri}
                                </td>
                            </c:forEach>
                        </tr>
                    </c:forEach>

                    <tr>
                        <td class="cmp-lab">Bảo hành</td>
                        <c:forEach var="p" items="${sanPham}">
                            <td><c:choose><c:when test="${not empty p.warrantyMonths}">${p.warrantyMonths} tháng</c:when><c:otherwise>—</c:otherwise></c:choose></td>
                        </c:forEach>
                    </tr>

                    <tr>
                        <td class="cmp-lab">Đánh giá</td>
                        <c:forEach var="p" items="${sanPham}">
                            <td>
                                <c:choose>
                                    <c:when test="${not empty danhGiaMap[p.id]}">
                                        <span style="color:var(--warn-2);">
                                            <c:forEach begin="1" end="5" var="i">${i <= danhGiaMap[p.id].soSaoDac ? '★' : '☆'}</c:forEach>
                                        </span>
                                        <span class="tg-mono" style="font-size:12px;color:var(--ink-3);">
                                            ${danhGiaMap[p.id].diem}/5 (${danhGiaMap[p.id].soLuot})
                                        </span>
                                    </c:when>
                                    <c:otherwise><span style="color:var(--ink-4);">Chưa có đánh giá</span></c:otherwise>
                                </c:choose>
                            </td>
                        </c:forEach>
                    </tr>

                    <tr>
                        <td class="cmp-lab">Tồn kho</td>
                        <c:forEach var="p" items="${sanPham}">
                            <td>
                                <c:choose>
                                    <c:when test="${p.stockQuantity > 0}">${p.stockQuantity} sản phẩm</c:when>
                                    <c:otherwise><span style="color:var(--err);">Hết hàng</span></c:otherwise>
                                </c:choose>
                            </td>
                        </c:forEach>
                    </tr>
                    </tbody>
                </table>
            </div>
        </c:otherwise>
    </c:choose>
</div>
</div><!-- /.tg-page -->

<style>
    .cmp-wrap  { max-width:1760px; margin:0 auto; padding:24px 48px 80px; }
    .cmp-table { width:100%; border-collapse:collapse; min-width:760px; }
    .cmp-table th, .cmp-table td { padding:16px 20px; text-align:left; vertical-align:middle;
                                   border-bottom:1px solid var(--line-hair-2); font-size:14px; }
    .cmp-table thead th { vertical-align:top; border-bottom:1px solid var(--line-new); }
    .cmp-lab   { width:190px; color:var(--ink-2); font-size:13px; background:var(--surface-2); }
    .cmp-col   { position:relative; min-width:240px; }
    .cmp-x     { position:absolute; top:8px; right:8px; width:26px; height:26px; border-radius:50%;
                 border:1px solid var(--line-new); background:var(--surface); color:var(--ink-3);
                 font-size:15px; line-height:1; cursor:pointer; z-index:2;
                 transition:background .2s ease, color .2s ease, border-color .2s ease; }
    .cmp-x:hover { background:var(--err-bg-2); border-color:var(--err-line-2); color:var(--err); }
    .cmp-head-row td { background:var(--surface-2); padding:12px 20px; }
    .cmp-best-val { color:var(--ok-deep); font-weight:600; }
    .cmp-best  { display:inline-block; margin-left:8px; background:var(--ok-bg-2); color:var(--ok-deep);
                 border:1px solid var(--ok-line-2); border-radius:var(--r-pill);
                 font-family:var(--font-mono); font-size:10px; font-weight:600; padding:2px 8px; }
    @media (max-width:900px) { .cmp-wrap { padding:24px 20px 60px; } }
</style>

<script src="/static/js/compare.js"></script>
<script>
    /* Trang này mở ra là đồng bộ lại localStorage theo đúng danh sách server trả
       về — nếu id nào đã bị xoá/ngừng bán thì tự rụng khỏi thanh so sánh. */
    (function () {
        var conLai = [<c:forEach var="p" items="${sanPham}" varStatus="l">${p.id}<c:if test="${!l.last}">,</c:if></c:forEach>];
        TGCompare.dat(conLai);
    })();

    function cmpBo(id) {
        TGCompare.bo(id);
        location.href = '/so-sanh?ids=' + TGCompare.lay().join(',');
    }
    function cmpXoaHet() {
        TGCompare.dat([]);
        location.href = '/so-sanh';
    }
</script>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>

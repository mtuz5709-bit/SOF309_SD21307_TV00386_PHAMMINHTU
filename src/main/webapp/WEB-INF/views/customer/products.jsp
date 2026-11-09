<%@ page contentType="text/html;charset=UTF-8" isELIgnored="false" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%-- View này phục vụ CẢ /products lẫn /flash-sale — khác nhau ở ${duongDan} --%>
<c:set var="pageTitle" value="${laFlashSale ? 'Flash Sale – TiTanGEAR' : 'Sản Phẩm – TiTanGEAR'}"/>
<c:set var="currentPage" value="${laFlashSale ? 'flash-sale' : 'products'}"/>
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<div class="tg-page">
<div class="pl-layout">

    <!-- ================= SIDEBAR LỌC ================= -->
    <aside class="tg-card pl-side">
        <form action="${duongDan}" method="get" id="filterForm">
            <c:if test="${keyword != null && keyword != ''}">
                <input type="hidden" name="keyword" value="${fn:escapeXml(keyword)}"/>
            </c:if>

            <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:20px;">
                <span style="font-size:15px;font-weight:700;">Bộ lọc</span>
                <a href="${duongDan}" style="font-size:12px;color:var(--accent);font-weight:600;text-decoration:none;">Xóa tất cả</a>
            </div>

            <div class="pl-group">
                <div class="pl-gtitle"><span class="pl-dot" style="background:var(--accent);"></span>Danh mục</div>
                <label class="tg-filter ${selectedCategory == null ? 'is-on' : ''}">
                    <input type="radio" name="category" value="" ${selectedCategory == null ? 'checked' : ''} onchange="this.form.submit()"/>
                    <span class="tg-filter-box"><svg width="9" height="9" viewBox="0 0 10 10" fill="none"><path d="M1.5 5.2 4 7.5 8.5 2.5" stroke="#fff" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg></span>
                    <span style="flex:1;">Tất cả</span>
                    <span class="tg-mono" style="font-size:11px;color:var(--ink-4);">${demTatCa}</span>
                </label>
                <c:forEach var="cat" items="${categories}">
                    <label class="tg-filter ${selectedCategory == cat.id ? 'is-on' : ''}">
                        <input type="radio" name="category" value="${cat.id}" ${selectedCategory == cat.id ? 'checked' : ''} onchange="this.form.submit()"/>
                        <span class="tg-filter-box"><svg width="9" height="9" viewBox="0 0 10 10" fill="none"><path d="M1.5 5.2 4 7.5 8.5 2.5" stroke="#fff" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg></span>
                        <span style="flex:1;">${cat.name}</span>
                        <span class="tg-mono" style="font-size:11px;color:var(--ink-4);">${empty demDanhMuc[cat.id] ? 0 : demDanhMuc[cat.id]}</span>
                    </label>
                </c:forEach>
            </div>

            <div class="pl-group">
                <div class="pl-gtitle"><span class="pl-dot" style="background:var(--violet);"></span>Loại Switch</div>
                <c:forEach var="e" items="${nhomSwitch}">
                    <label class="tg-filter ${chonSwitch.contains(e.key) ? 'is-on' : ''}">
                        <input type="checkbox" name="switchType" value="${e.key}" ${chonSwitch.contains(e.key) ? 'checked' : ''} onchange="this.form.submit()"/>
                        <span class="tg-filter-box"><svg width="9" height="9" viewBox="0 0 10 10" fill="none"><path d="M1.5 5.2 4 7.5 8.5 2.5" stroke="#fff" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg></span>
                        <span style="flex:1;">${e.value}</span>
                        <span class="tg-mono" style="font-size:11px;color:var(--ink-4);">${empty demSwitch[e.key] ? 0 : demSwitch[e.key]}</span>
                    </label>
                </c:forEach>
            </div>

            <div class="pl-group">
                <div class="pl-gtitle"><span class="pl-dot" style="background:var(--warn-2);"></span>Trọng lượng chuột</div>
                <c:forEach var="e" items="${nhomTrongLuong}">
                    <label class="tg-filter ${chonTrongLuong.contains(e.key) ? 'is-on' : ''}">
                        <input type="checkbox" name="weight" value="${e.key}" ${chonTrongLuong.contains(e.key) ? 'checked' : ''} onchange="this.form.submit()"/>
                        <span class="tg-filter-box"><svg width="9" height="9" viewBox="0 0 10 10" fill="none"><path d="M1.5 5.2 4 7.5 8.5 2.5" stroke="#fff" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg></span>
                        <span style="flex:1;">${e.value}</span>
                        <span class="tg-mono" style="font-size:11px;color:var(--ink-4);">${empty demTrongLuong[e.key] ? 0 : demTrongLuong[e.key]}</span>
                    </label>
                </c:forEach>
            </div>

            <div class="pl-group">
                <div class="pl-gtitle"><span class="pl-dot" style="background:var(--accent-2);"></span>Kết nối</div>
                <c:forEach var="e" items="${nhomKetNoi}">
                    <label class="tg-filter ${chonKetNoi.contains(e.key) ? 'is-on' : ''}">
                        <input type="checkbox" name="conn" value="${e.key}" ${chonKetNoi.contains(e.key) ? 'checked' : ''} onchange="this.form.submit()"/>
                        <span class="tg-filter-box"><svg width="9" height="9" viewBox="0 0 10 10" fill="none"><path d="M1.5 5.2 4 7.5 8.5 2.5" stroke="#fff" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg></span>
                        <span style="flex:1;">${e.value}</span>
                        <span class="tg-mono" style="font-size:11px;color:var(--ink-4);">${empty demKetNoi[e.key] ? 0 : demKetNoi[e.key]}</span>
                    </label>
                </c:forEach>
            </div>

            <div class="pl-group">
                <div class="pl-gtitle"><span class="pl-dot" style="background:var(--err);"></span>Tình trạng</div>
                <label class="tg-filter ${chonStock.contains('instock') ? 'is-on' : ''}">
                    <input type="checkbox" name="stock" value="instock" ${chonStock.contains('instock') ? 'checked' : ''} onchange="this.form.submit()"/>
                    <span class="tg-filter-box"><svg width="9" height="9" viewBox="0 0 10 10" fill="none"><path d="M1.5 5.2 4 7.5 8.5 2.5" stroke="#fff" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg></span>
                    <span style="flex:1;">Còn hàng</span>
                    <span class="tg-mono" style="font-size:11px;color:var(--ink-4);">${demConHang}</span>
                </label>
                <label class="tg-filter ${chonStock.contains('outofstock') ? 'is-on' : ''}">
                    <input type="checkbox" name="stock" value="outofstock" ${chonStock.contains('outofstock') ? 'checked' : ''} onchange="this.form.submit()"/>
                    <span class="tg-filter-box"><svg width="9" height="9" viewBox="0 0 10 10" fill="none"><path d="M1.5 5.2 4 7.5 8.5 2.5" stroke="#fff" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg></span>
                    <span style="flex:1;">Hết hàng</span>
                    <span class="tg-mono" style="font-size:11px;color:var(--ink-4);">${demHetHang}</span>
                </label>
            </div>

            <div>
                <div class="pl-gtitle"><span class="pl-dot" style="background:var(--ok-2);"></span>Khoảng giá</div>
                <div style="display:flex;gap:8px;margin-bottom:10px;">
                    <input type="number" name="minPrice" class="pl-price" placeholder="Từ"  value="${minPrice}"/>
                    <input type="number" name="maxPrice" class="pl-price" placeholder="Đến" value="${maxPrice}"/>
                </div>
                <button type="submit" class="tg-btn tg-primary" style="width:100%;padding:11px;font-size:13px;">Áp dụng</button>
            </div>
        </form>
    </aside>

    <!-- ================= DANH SÁCH ================= -->
    <div>
        <c:if test="${laFlashSale}">
            <div class="flash-banner">
                <div>
                    <div class="flash-banner-tag">⚡ FLASH SALE</div>
                    <div class="flash-banner-sub">Giá đã giảm sẵn, không cần nhập mã.</div>
                </div>
                <c:if test="${not empty mocKetThuc}">
                    <div class="flash-countdown" data-ket-thuc="${mocKetThuc}">
                        <span class="flash-countdown-label">Kết thúc sau</span>
                        <span class="flash-countdown-time">--:--:--</span>
                    </div>
                </c:if>
            </div>
        </c:if>

        <div style="display:flex;align-items:flex-end;justify-content:space-between;margin-bottom:14px;gap:16px;flex-wrap:wrap;">
            <div>
                <div class="tg-mono" style="font-size:11px;color:var(--ink-4);letter-spacing:.1em;margin-bottom:6px;">
                    TRANG CHỦ / ${laFlashSale ? 'FLASH SALE' : 'SẢN PHẨM'}
                </div>
                <h1 style="font-size:32px;font-weight:700;margin:0;letter-spacing:-.02em;">
                    <c:choose>
                        <c:when test="${keyword != null && keyword != ''}">Kết quả: "${fn:escapeXml(keyword)}"</c:when>
                        <c:when test="${laFlashSale && selectedCategory == null}">Đang giảm giá</c:when>
                        <c:when test="${selectedCategory != null}">
                            <c:forEach var="cat" items="${categories}">
                                <c:if test="${cat.id == selectedCategory}">${cat.name}</c:if>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>Tất cả sản phẩm</c:otherwise>
                    </c:choose>
                </h1>
            </div>
            <div style="display:flex;align-items:center;gap:12px;flex-wrap:wrap;">
                <span class="tg-mono" style="font-size:12px;color:var(--ink-3);">${tongKetQua} SẢN PHẨM</span>
                <%-- Tab sắp xếp: giữ nguyên mọi tham số lọc đang bật, chỉ đổi sort
                     và luôn nhảy về trang 1 (đổi thứ tự mà giữ trang 3 thì vô nghĩa) --%>
                <div class="pl-sort">
                    <c:forEach var="s" items="${['noi-bat','gia-tang','gia-giam']}" varStatus="si">
                        <c:url var="uSort" value="${duongDan}">
                            <c:param name="keyword"  value="${fn:escapeXml(keyword)}"/>
                            <c:param name="category" value="${selectedCategory}"/>
                            <c:param name="minPrice" value="${minPrice}"/>
                            <c:param name="maxPrice" value="${maxPrice}"/>
                            <c:forEach var="k" items="${chonSwitch}"><c:param name="switchType" value="${k}"/></c:forEach>
                            <c:forEach var="k" items="${chonTrongLuong}"><c:param name="weight" value="${k}"/></c:forEach>
                            <c:forEach var="k" items="${chonKetNoi}"><c:param name="conn" value="${k}"/></c:forEach>
                            <c:forEach var="k" items="${chonStock}"><c:param name="stock" value="${k}"/></c:forEach>
                            <c:param name="sort" value="${s}"/>
                        </c:url>
                        <a href="${uSort}" class="pl-sort-btn ${(empty sort && si.first) || sort == s ? 'is-on' : ''}">
                            ${s == 'noi-bat' ? 'Nổi bật' : (s == 'gia-tang' ? 'Giá ↑' : 'Giá ↓')}
                        </a>
                    </c:forEach>
                </div>
            </div>
        </div>

        <%-- Chip lọc đang bật — bấm × là bỏ lọc đó. Dựng bằng <c:url> nên giữ
             nguyên các lọc còn lại, không phải viết JS. --%>
        <c:set var="coChip" value="${selectedCategory != null || not empty chonSwitch || not empty chonTrongLuong || not empty chonKetNoi || not empty chonStock || minPrice != null || maxPrice != null}"/>
        <c:if test="${coChip}">
            <div style="display:flex;gap:8px;flex-wrap:wrap;margin-bottom:18px;">
                <c:if test="${selectedCategory != null}">
                    <c:forEach var="cat" items="${categories}">
                        <c:if test="${cat.id == selectedCategory}"><span class="pl-chip">${cat.name} <span>×</span></span></c:if>
                    </c:forEach>
                </c:if>
                <c:forEach var="k" items="${chonSwitch}"><span class="pl-chip">${nhomSwitch[k]} <span>×</span></span></c:forEach>
                <c:forEach var="k" items="${chonTrongLuong}"><span class="pl-chip">${nhomTrongLuong[k]} <span>×</span></span></c:forEach>
                <c:forEach var="k" items="${chonKetNoi}"><span class="pl-chip">${nhomKetNoi[k]} <span>×</span></span></c:forEach>
                <c:forEach var="k" items="${chonStock}"><span class="pl-chip">${k == 'instock' ? 'Còn hàng' : 'Hết hàng'} <span>×</span></span></c:forEach>
                <c:if test="${minPrice != null || maxPrice != null}">
                    <span class="pl-chip">Giá <fmt:formatNumber value="${minPrice}" pattern="#,##0"/>–<fmt:formatNumber value="${maxPrice}" pattern="#,##0"/> <span>×</span></span>
                </c:if>
                <a href="${duongDan}" class="pl-chip pl-chip-clear">Xóa tất cả</a>
            </div>
        </c:if>

        <c:choose>
            <c:when test="${empty products}">
                <div class="tg-card" style="padding:80px 40px;text-align:center;">
                    <div style="font-size:17px;font-weight:600;margin-bottom:8px;">
                        ${laFlashSale ? 'Hiện chưa có sản phẩm nào đang giảm giá' : 'Không tìm thấy sản phẩm nào'}
                    </div>
                    <p style="font-size:14px;color:var(--ink-3);margin:0 0 20px;">Thử bỏ một vài bộ lọc hoặc tìm bằng từ khóa khác.</p>
                    <a href="${duongDan}" class="tg-btn tg-primary">Xóa bộ lọc</a>
                </div>
            </c:when>
            <c:otherwise>
                <div class="pl-grid">
                    <c:forEach var="p" items="${products}" varStatus="loop">
                        <%-- data-cmp-* để compare.js dựng được ảnh nhỏ trên thanh so sánh --%>
                        <div class="tg-prod tg-rise" style="animation-delay:${(loop.index % 9) * 60}ms;"
                             data-cmp-id="${p.id}" data-cmp-ten="${p.name}"
                             data-cmp-img="/static/images/${not empty p.imageUrl ? p.imageUrl : 'banner.png'}">
                            <div style="position:relative;background:var(--bg-page);aspect-ratio:4/3;padding:22px;overflow:hidden;">
                                <a href="/products/${p.id}" style="display:block;height:100%;">
                                    <c:choose>
                                        <c:when test="${not empty p.imageUrl}">
                                            <img class="tg-thumb" src="/static/images/${p.imageUrl}" alt="${p.name}"
                                                 onerror="this.onerror=null;this.src='/static/images/banner.png';"
                                                 style="width:100%;height:100%;object-fit:contain;mix-blend-mode:multiply;"/>
                                        </c:when>
                                        <c:otherwise>
                                            <img class="tg-thumb" src="/static/images/banner.png" alt="${p.name}"
                                                 style="width:100%;height:100%;object-fit:contain;"/>
                                        </c:otherwise>
                                    </c:choose>
                                </a>
                                <span class="tg-mono" style="position:absolute;top:12px;left:14px;font-size:10px;color:var(--ink-4);letter-spacing:.06em;">SKU.${p.id}</span>

                                <c:choose>
                                    <c:when test="${p.stockQuantity == 0}">
                                        <span class="pl-badge" style="background:var(--ink-2);">HẾT HÀNG</span>
                                    </c:when>
                                    <c:when test="${priceMap[p.id].hasSale}">
                                        <span class="pl-badge" style="background:var(--err);">−${priceMap[p.id].percentOff}%</span>
                                    </c:when>
                                </c:choose>

                                <%-- Mua nhanh: sản phẩm CÓ biến thể thì bắt buộc vào trang chi tiết
                                     để chọn phiên bản. Cho thêm thẳng vào giỏ mà không kèm biến thể
                                     sẽ tạo ra đơn không xuất kho được. --%>
                                <div class="tg-quick" style="position:absolute;bottom:12px;left:14px;right:14px;display:flex;gap:8px;">
                                    <c:choose>
                                        <c:when test="${p.stockQuantity == 0}">
                                            <a href="/products/${p.id}" class="tg-btn tg-ghost" style="flex:1;padding:10px;font-size:12px;border-radius:10px;">Báo khi có hàng</a>
                                        </c:when>
                                        <c:when test="${not empty p.variants}">
                                            <a href="/products/${p.id}" class="tg-btn tg-primary" style="flex:1;padding:10px;font-size:12px;border-radius:10px;">Chọn phiên bản</a>
                                        </c:when>
                                        <c:otherwise>
                                            <a href="/cart/add?productId=${p.id}&quantity=1" class="tg-btn tg-primary" style="flex:1;padding:10px;font-size:12px;border-radius:10px;">Thêm vào giỏ</a>
                                        </c:otherwise>
                                    </c:choose>
                                    <button type="button" class="tg-cmp-btn" title="Thêm vào so sánh"
                                            onclick="TGCompare.bat(${p.id})">
                                        <svg width="15" height="15" viewBox="0 0 16 16" fill="none"><path d="M3 13V7M8 13V3M13 13V9.5" stroke="currentColor" stroke-width="1.7" stroke-linecap="round"/></svg>
                                    </button>
                                </div>
                            </div>

                            <div style="padding:16px 18px;display:flex;flex-direction:column;gap:8px;">
                                <c:if test="${not empty p.category}">
                                    <span class="tg-mono" style="font-size:10px;letter-spacing:.1em;font-weight:600;color:var(--accent);">${p.category.name}</span>
                                </c:if>
                                <a href="/products/${p.id}" style="font-size:16px;font-weight:600;line-height:1.25;text-decoration:none;color:inherit;">${p.name}</a>

                                <%-- Sao đánh giá — sản phẩm chưa ai đánh giá thì nói thẳng
                                     là "Chưa có đánh giá", không vẽ 5 sao rỗng gây hiểu nhầm --%>
                                <div style="display:flex;align-items:center;gap:6px;min-height:18px;">
                                    <c:choose>
                                        <c:when test="${not empty danhGiaMap[p.id]}">
                                            <span class="tg-stars"><c:forEach begin="1" end="5" var="i">${i <= danhGiaMap[p.id].soSaoDac ? '★' : '☆'}</c:forEach></span>
                                            <span class="tg-rate">${danhGiaMap[p.id].diem} (${danhGiaMap[p.id].soLuot})</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="tg-rate">Chưa có đánh giá</span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>

                                <div style="display:flex;gap:6px;flex-wrap:wrap;min-height:22px;">
                                    <c:if test="${not empty p.switchType}"><span class="tg-chip">${nhomSwitch[p.switchType]}</span></c:if>
                                    <c:if test="${not empty p.weightGroup}"><span class="tg-chip">${nhomTrongLuong[p.weightGroup]}</span></c:if>
                                    <c:if test="${not empty p.connectivity}"><span class="tg-chip">${nhomKetNoi[p.connectivity]}</span></c:if>
                                </div>

                                <div style="display:flex;align-items:baseline;gap:8px;margin-top:2px;flex-wrap:wrap;">
                                    <span style="font-size:19px;font-weight:700;color:var(--accent);">
                                        <c:choose>
                                            <c:when test="${priceMap[p.id].hasSale}"><fmt:formatNumber value="${priceMap[p.id].salePrice}" pattern="#,##0"/>đ</c:when>
                                            <c:otherwise><fmt:formatNumber value="${p.price}" pattern="#,##0"/>đ</c:otherwise>
                                        </c:choose>
                                    </span>
                                    <c:if test="${priceMap[p.id].hasSale}">
                                        <span style="font-size:13px;color:var(--ink-4);text-decoration:line-through;"><fmt:formatNumber value="${p.price}" pattern="#,##0"/>đ</span>
                                    </c:if>
                                </div>

                                <c:if test="${p.stockQuantity > 0 && p.stockQuantity <= 5}">
                                    <div class="tg-mono" style="font-size:11px;color:var(--warn-deep);">Chỉ còn ${p.stockQuantity}</div>
                                </c:if>
                            </div>
                        </div>
                    </c:forEach>
                </div>

                <%-- ===== PHÂN TRANG ===== --%>
                <c:if test="${tongTrang > 1}">
                    <%-- Hàm dựng link giữ nguyên mọi bộ lọc + sắp xếp, chỉ đổi số trang --%>
                    <c:set var="pgFrom" value="${trang - 2 < 1 ? 1 : trang - 2}"/>
                    <c:set var="pgTo"   value="${pgFrom + 4 > tongTrang ? tongTrang : pgFrom + 4}"/>
                    <c:set var="pgFrom" value="${pgTo - 4 < 1 ? 1 : pgTo - 4}"/>

                    <div class="tg-pager">
                        <c:url var="uPrev" value="${duongDan}">
                            <c:param name="keyword"  value="${fn:escapeXml(keyword)}"/>
                            <c:param name="category" value="${selectedCategory}"/>
                            <c:param name="minPrice" value="${minPrice}"/>
                            <c:param name="maxPrice" value="${maxPrice}"/>
                            <c:forEach var="k" items="${chonSwitch}"><c:param name="switchType" value="${k}"/></c:forEach>
                            <c:forEach var="k" items="${chonTrongLuong}"><c:param name="weight" value="${k}"/></c:forEach>
                            <c:forEach var="k" items="${chonKetNoi}"><c:param name="conn" value="${k}"/></c:forEach>
                            <c:forEach var="k" items="${chonStock}"><c:param name="stock" value="${k}"/></c:forEach>
                            <c:param name="sort" value="${sort}"/>
                            <c:param name="page" value="${trang - 1}"/>
                        </c:url>
                        <a href="${uPrev}" class="${trang == 1 ? 'is-off' : ''}">←</a>

                        <c:forEach var="i" begin="${pgFrom}" end="${pgTo}">
                            <c:url var="uPage" value="${duongDan}">
                                <c:param name="keyword"  value="${fn:escapeXml(keyword)}"/>
                                <c:param name="category" value="${selectedCategory}"/>
                                <c:param name="minPrice" value="${minPrice}"/>
                                <c:param name="maxPrice" value="${maxPrice}"/>
                                <c:forEach var="k" items="${chonSwitch}"><c:param name="switchType" value="${k}"/></c:forEach>
                                <c:forEach var="k" items="${chonTrongLuong}"><c:param name="weight" value="${k}"/></c:forEach>
                                <c:forEach var="k" items="${chonKetNoi}"><c:param name="conn" value="${k}"/></c:forEach>
                                <c:forEach var="k" items="${chonStock}"><c:param name="stock" value="${k}"/></c:forEach>
                                <c:param name="sort" value="${sort}"/>
                                <c:param name="page" value="${i}"/>
                            </c:url>
                            <a href="${uPage}" class="${i == trang ? 'is-on' : ''}">${i}</a>
                        </c:forEach>

                        <c:url var="uNext" value="${duongDan}">
                            <c:param name="keyword"  value="${fn:escapeXml(keyword)}"/>
                            <c:param name="category" value="${selectedCategory}"/>
                            <c:param name="minPrice" value="${minPrice}"/>
                            <c:param name="maxPrice" value="${maxPrice}"/>
                            <c:forEach var="k" items="${chonSwitch}"><c:param name="switchType" value="${k}"/></c:forEach>
                            <c:forEach var="k" items="${chonTrongLuong}"><c:param name="weight" value="${k}"/></c:forEach>
                            <c:forEach var="k" items="${chonKetNoi}"><c:param name="conn" value="${k}"/></c:forEach>
                            <c:forEach var="k" items="${chonStock}"><c:param name="stock" value="${k}"/></c:forEach>
                            <c:param name="sort" value="${sort}"/>
                            <c:param name="page" value="${trang + 1}"/>
                        </c:url>
                        <a href="${uNext}" class="${trang == tongTrang ? 'is-off' : ''}">→</a>

                        <span style="border:none;background:none;color:var(--ink-4);font-weight:400;">
                            Trang ${trang}/${tongTrang}
                        </span>
                    </div>
                </c:if>
            </c:otherwise>
        </c:choose>
    </div>
</div>
</div><!-- /.tg-page -->

<script src="/static/js/compare.js"></script>

<style>
    /* Khung rộng gần hết màn hình — max-width 1440 để lại 2 dải trắng rất to ở
       màn 1900px. 1760 + đệm 48 là vừa: vẫn có lề thở, mà thẻ sản phẩm to hẳn. */
    .pl-layout { display:grid; grid-template-columns:320px 1fr; gap:28px; padding:24px 48px 80px;
                 align-items:start; max-width:1760px; margin:0 auto; }
    .pl-side   { padding:26px 24px; position:sticky; top:100px; }
    .pl-group  { margin-bottom:24px; }
    .pl-gtitle { font-size:13px; font-weight:600; margin-bottom:11px; display:flex; align-items:center; gap:8px; }
    .pl-dot    { width:5px; height:5px; border-radius:50%; display:inline-block; flex-shrink:0; }
    .pl-price  { width:50%; min-width:0; border:1px solid var(--line-new); border-radius:10px;
                 padding:9px 12px; font-family:var(--font-mono); font-size:12px; outline:none; }
    .pl-price:focus { border-color:var(--accent); }
    .pl-grid   { display:grid; grid-template-columns:repeat(3,1fr); gap:20px; }
    .pl-badge  { position:absolute; top:12px; right:14px; color:#fff; font-family:var(--font-mono);
                 font-size:10px; font-weight:600; letter-spacing:.06em; padding:4px 11px;
                 border-radius:var(--r-pill); z-index:2; }
    .pl-chip   { display:inline-flex; align-items:center; gap:7px; background:var(--accent-soft-2);
                 border:1px solid var(--accent-line); color:var(--accent); border-radius:var(--r-pill);
                 padding:6px 14px; font-size:12px; font-weight:600; text-decoration:none; }
    .pl-chip span { font-size:14px; line-height:1; }
    .pl-chip-clear { background:var(--surface); border-color:var(--line-new); color:var(--ink-2); }
    .pl-chip-clear:hover { border-color:var(--accent); color:var(--accent); }

    .pl-sort { display:flex; gap:4px; background:#EEF0F4; border-radius:var(--r-pill); padding:3px; }
    .pl-sort-btn { padding:6px 14px; border-radius:var(--r-pill); font-size:12px; color:var(--ink-3);
                   text-decoration:none; white-space:nowrap; transition:background .2s ease, color .2s ease; }
    .pl-sort-btn:hover { color:var(--accent); }
    .pl-sort-btn.is-on { background:var(--surface); color:var(--accent); font-weight:600;
                         box-shadow:0 1px 3px rgba(20,23,26,.12); }

    /* Màn rất rộng: chuyển 4 cột cho đỡ bị kéo giãn quá khổ */
    @media (min-width:1700px) { .pl-grid { grid-template-columns:repeat(4,1fr); } }
    @media (max-width:1200px) { .pl-layout { grid-template-columns:280px 1fr; padding:24px 28px 70px; }
                                .pl-grid { grid-template-columns:repeat(2,1fr); } }
    @media (max-width:900px)  { .pl-layout { grid-template-columns:1fr; padding:24px 20px 60px; }
                                .pl-side { position:static; }
                                .pl-grid { grid-template-columns:1fr; } }
</style>

<c:if test="${laFlashSale && not empty mocKetThuc}">
<script>
    /* Đếm ngược tới lúc đợt giảm gần nhất kết thúc. Mốc do server đưa xuống dạng
       ISO không kèm múi giờ nên trình duyệt hiểu là giờ máy khách — đúng ý ở đây
       vì cả shop lẫn khách đều ở Việt Nam. */
    (function () {
        var oDongHo = document.querySelector('.flash-countdown');
        if (!oDongHo) return;
        var moc = new Date(oDongHo.dataset.ketThuc).getTime();
        var oSo = oDongHo.querySelector('.flash-countdown-time');

        function hai(n) { return (n < 10 ? '0' : '') + n; }
        function ve() {
            var conLai = moc - Date.now();
            if (isNaN(moc)) { oDongHo.style.display = 'none'; return; }
            if (conLai <= 0) { oSo.textContent = 'Đã kết thúc'; clearInterval(dem); return; }
            var giay = Math.floor(conLai / 1000);
            var ngay = Math.floor(giay / 86400);
            var gio  = Math.floor((giay % 86400) / 3600);
            var phut = Math.floor((giay % 3600) / 60);
            oSo.textContent = (ngay > 0 ? ngay + ' ngày ' : '') + hai(gio) + ':' + hai(phut) + ':' + hai(giay % 60);
        }
        ve();
        var dem = setInterval(ve, 1000);
    })();
</script>
</c:if>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>

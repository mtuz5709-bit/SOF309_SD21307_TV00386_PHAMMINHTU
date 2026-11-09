<%@ page contentType="text/html;charset=UTF-8" isELIgnored="false" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="${product.name} – TiTanGEAR"/>
<c:set var="currentPage" value="products"/>
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<div class="tg-page">
<div class="dp-wrap">

    <div class="tg-mono dp-crumb">
        <a href="/home">TRANG CHỦ</a> / <a href="/products">SẢN PHẨM</a> /
        <span style="color:var(--ink);">${product.name}</span>
    </div>

    <c:if test="${param.added == 'true'}">
        <div class="dp-ok">✓ Đã thêm vào giỏ hàng. <a href="/cart" style="color:inherit;text-decoration:underline;">Xem giỏ hàng →</a></div>
    </c:if>
    <c:if test="${not empty param.qtyError}">
        <div class="dp-err">${param.qtyError}</div>
    </c:if>

    <!-- ================= ẢNH + THÔNG TIN MUA ================= -->
    <div class="dp-top">
        <div class="tg-card tg-rise" style="padding:28px;">
            <div style="position:relative;background:var(--bg-page);border-radius:14px;aspect-ratio:1/1;overflow:hidden;">
                <c:choose>
                    <c:when test="${not empty product.imageUrl}">
                        <img id="dpMainImg" src="/static/images/${product.imageUrl}" alt="${product.name}"
                             onerror="this.onerror=null;this.src='/static/images/banner.png';"
                             style="width:100%;height:100%;object-fit:contain;padding:34px;box-sizing:border-box;mix-blend-mode:multiply;"/>
                    </c:when>
                    <c:otherwise>
                        <img id="dpMainImg" src="/static/images/banner.png" alt="${product.name}"
                             style="width:100%;height:100%;object-fit:contain;padding:34px;box-sizing:border-box;"/>
                    </c:otherwise>
                </c:choose>
                <div class="tg-mono" style="position:absolute;top:16px;left:18px;font-size:10px;color:var(--ink-4);letter-spacing:.1em;">
                    SKU.${product.id}
                </div>
                <c:if test="${effectivePrice.hasSale}">
                    <span class="tg-mono" style="position:absolute;top:16px;right:18px;background:var(--err);color:#fff;border-radius:var(--r-pill);font-size:11px;font-weight:600;padding:5px 13px;">
                        −${effectivePrice.percentOff}%
                    </span>
                </c:if>
            </div>
            <%-- Ảnh nhỏ theo biến thể: bấm là đổi ảnh chính + chọn luôn biến thể đó --%>
            <div id="dpThumbs" style="display:flex;gap:10px;margin-top:14px;flex-wrap:wrap;"></div>
        </div>

        <div class="tg-card tg-rise" style="padding:36px 38px;">
            <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:12px;gap:12px;flex-wrap:wrap;">
                <c:if test="${not empty product.category}">
                    <span class="tg-mono" style="background:var(--accent-soft-2);color:var(--accent);border:1px solid var(--accent-line);border-radius:var(--r-pill);font-size:11px;font-weight:600;letter-spacing:.1em;padding:5px 13px;">
                        ${product.category.name}
                    </span>
                </c:if>
                <button type="button" class="tg-btn tg-ghost dp-cmp" style="padding:8px 16px;font-size:12px;"
                        onclick="TGCompare.bat(${product.id})">So sánh</button>
            </div>

            <h1 style="font-size:36px;font-weight:700;margin:0 0 12px;letter-spacing:-.02em;line-height:1.08;">${product.name}</h1>

            <div style="display:flex;align-items:center;gap:10px;margin-bottom:20px;flex-wrap:wrap;">
                <c:choose>
                    <c:when test="${reviewSummary.total > 0}">
                        <span class="tg-stars" style="font-size:15px;letter-spacing:2px;">
                            <c:forEach begin="1" end="5" var="i">${i <= reviewSummary.roundedAverage ? '★' : '☆'}</c:forEach>
                        </span>
                        <span class="tg-mono" style="font-size:12px;color:var(--ink-2);">
                            <fmt:formatNumber value="${reviewSummary.average}" pattern="0.0"/>/5
                        </span>
                        <a href="#reviews" style="font-size:13px;color:var(--accent);text-decoration:none;">${reviewSummary.total} đánh giá</a>
                    </c:when>
                    <c:otherwise>
                        <span class="tg-rate">Chưa có đánh giá</span>
                    </c:otherwise>
                </c:choose>
            </div>

            <div style="display:flex;align-items:baseline;gap:12px;margin-bottom:10px;flex-wrap:wrap;">
                <span id="dpPrice" style="font-size:34px;font-weight:700;color:var(--accent);letter-spacing:-.02em;">
                    <c:choose>
                        <c:when test="${effectivePrice.hasSale}"><fmt:formatNumber value="${effectivePrice.salePrice}" pattern="#,##0"/>đ</c:when>
                        <c:otherwise><fmt:formatNumber value="${product.price}" pattern="#,##0"/>đ</c:otherwise>
                    </c:choose>
                </span>
                <c:if test="${effectivePrice.hasSale}">
                    <span style="font-size:16px;color:var(--ink-4);text-decoration:line-through;"><fmt:formatNumber value="${product.price}" pattern="#,##0"/>đ</span>
                </c:if>
            </div>

            <div id="dpStock" class="dp-pill"></div>

            <c:if test="${not empty product.description}">
                <p style="font-size:14px;line-height:1.75;color:var(--ink-2);margin:18px 0 24px;">${product.description}</p>
            </c:if>

            <%-- ===== CHỌN BIẾN THỂ ===== --%>
            <%-- ===== CHUYỂN NHANH GIỮA CÁC PHIÊN BẢN CÙNG DÒNG =====
                 Mỗi phiên bản là một SẢN PHẨM riêng (vì thông số kỹ thuật khác nhau
                 mà thông số lại gắn theo sản phẩm), nên nút ở đây là LINK sang trang
                 khác chứ không đổi tại chỗ. Đổi lại mỗi bản có URL riêng — gửi link
                 cho bạn bè là ra đúng bản đó. --%>
            <c:if test="${cungDong.size() > 1}">
                <div style="margin-bottom:20px;">
                    <div class="tg-mono" style="font-size:11px;color:var(--ink-4);letter-spacing:.1em;margin-bottom:9px;">
                        DÒNG SẢN PHẨM — ${product.line.name}
                    </div>
                    <div style="display:flex;gap:8px;flex-wrap:wrap;">
                        <c:forEach var="ban" items="${cungDong}">
                            <c:choose>
                                <c:when test="${ban.id == product.id}">
                                    <span class="dp-vbtn is-on">${ban.name}</span>
                                </c:when>
                                <c:otherwise>
                                    <a href="/products/${ban.id}" class="dp-vbtn">${ban.name}</a>
                                </c:otherwise>
                            </c:choose>
                        </c:forEach>
                    </div>
                </div>
            </c:if>

            <c:if test="${not empty product.variants}">
                <%-- Khối tên gọi thêm — JS tự ẩn khi biến thể chỉ khác màu (thường gặp) --%>
                <div id="dpNameBox" style="margin-bottom:20px;display:none;">
                    <div class="tg-mono" style="font-size:11px;color:var(--ink-4);letter-spacing:.1em;margin-bottom:9px;">TÊN GỌI THÊM</div>
                    <div id="dpNames" style="display:flex;gap:8px;flex-wrap:wrap;"></div>
                </div>
                <div style="margin-bottom:26px;">
                    <div class="tg-mono" style="font-size:11px;color:var(--ink-4);letter-spacing:.1em;margin-bottom:9px;">
                        MÀU SẮC — <span id="dpColorName" style="color:var(--ink);">—</span>
                    </div>
                    <div id="dpColors" style="display:flex;gap:10px;flex-wrap:wrap;"></div>
                </div>
            </c:if>
            <input type="hidden" id="selectedVariantId" value=""/>

            <div style="display:flex;align-items:center;gap:14px;margin-bottom:18px;flex-wrap:wrap;">
                <div style="display:flex;border:1px solid var(--line-new);border-radius:12px;overflow:hidden;">
                    <button type="button" class="dp-qbtn" onclick="dpQty(-1)">−</button>
                    <input type="number" id="qty" class="dp-qinp" value="1" min="1" max="${product.stockQuantity}"/>
                    <button type="button" class="dp-qbtn" onclick="dpQty(1)">+</button>
                </div>
                <span id="dpStockNote" class="tg-mono" style="font-size:12px;color:var(--ink-4);"></span>
            </div>

            <div style="display:flex;gap:12px;flex-wrap:wrap;">
                <c:choose>
                    <c:when test="${product.stockQuantity > 0}">
                        <button type="button" class="tg-btn dp-btn-dark" onclick="addToCart(${product.id})">Thêm vào giỏ</button>
                        <button type="button" class="tg-btn tg-primary" style="flex:1;padding:16px;border-radius:12px;" onclick="buyNow(${product.id})">Mua ngay</button>
                    </c:when>
                    <c:otherwise>
                        <button type="button" class="tg-btn tg-ghost" style="flex:1;padding:16px;border-radius:12px;" disabled>HẾT HÀNG</button>
                    </c:otherwise>
                </c:choose>
            </div>

            <%-- ===== BÁO HÀNG VỀ (hiện khi biến thể đang chọn hết hàng) ===== --%>
            <div id="dpRestock" style="display:none;margin-top:16px;">
                <c:choose>
                    <c:when test="${param.notifyOk == 'true'}">
                        <div class="dp-ok" style="margin:0;">✓ Đã đăng ký! Chúng tôi sẽ gửi email khi có hàng.</div>
                    </c:when>
                    <c:otherwise>
                        <c:if test="${param.notifyErr == 'true'}">
                            <div class="dp-err">Email không hợp lệ, vui lòng kiểm tra lại.</div>
                        </c:if>
                        <div style="background:var(--surface-2);border:1px solid var(--line-new);border-radius:var(--r-md);padding:16px 18px;">
                            <div style="font-size:13px;font-weight:600;margin-bottom:10px;">Hết hàng — nhận email khi có lại?</div>
                            <form method="post" action="/products/${product.id}/notify-restock" style="display:flex;gap:8px;flex-wrap:wrap;">
                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                                <input type="hidden" name="variantId" id="restockVariantId" value=""/>
                                <input type="email" name="email" required placeholder="Email của bạn"
                                       value="<c:out value='${currentEmail}'/>"
                                       style="flex:1;min-width:180px;border:1px solid var(--line-new);border-radius:10px;padding:10px 13px;font-size:13px;font-family:inherit;outline:none;"/>
                                <button type="submit" class="tg-btn tg-primary" style="padding:10px 20px;font-size:13px;">Đăng ký</button>
                            </form>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>

            <%-- ===== 3 CAM KẾT ===== --%>
            <div class="dp-trust">
                <div>
                    <span class="dp-tico" style="background:var(--ok-bg-2);">
                        <svg width="15" height="15" viewBox="0 0 16 16" fill="none" stroke="#10A268" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M8 1.8 3 3.4v4c0 3 2.1 5.3 5 6.6 2.9-1.3 5-3.6 5-6.6v-4L8 1.8Z"/><path d="M5.9 7.7 7.4 9.2l2.9-2.9"/></svg>
                    </span>
                    <span>Bảo hành ${not empty product.warrantyMonths ? product.warrantyMonths : 12} tháng</span>
                </div>
                <div>
                    <span class="dp-tico" style="background:var(--accent-soft-2);">
                        <svg width="15" height="15" viewBox="0 0 16 16" fill="none" stroke="#1B49FF" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M2 11.5h9V4H2zM11 7h2.2L15 9.4v2.1h-4z"/><circle cx="4.5" cy="13" r="1.1"/><circle cx="12" cy="13" r="1.1"/></svg>
                    </span>
                    <span>Freeship đơn từ 500k</span>
                </div>
                <div>
                    <span class="dp-tico" style="background:var(--violet-bg);">
                        <svg width="15" height="15" viewBox="0 0 16 16" fill="none" stroke="#7A5CC7" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="8" cy="8" r="6"/><path d="M8 4.8V8l2.2 1.3"/></svg>
                    </span>
                    <span>Đổi mới 7 ngày</span>
                </div>
            </div>
        </div>
    </div>

    <!-- ================= THÔNG SỐ KỸ THUẬT ================= -->
    <c:if test="${not empty product.specs}">
        <div class="tg-card tg-rise dp-sect">
            <div style="display:flex;align-items:baseline;justify-content:space-between;margin-bottom:26px;gap:12px;flex-wrap:wrap;">
                <h2 class="dp-h2">Thông số kỹ thuật</h2>
                <span class="tg-mono" style="font-size:11px;color:var(--ink-4);letter-spacing:.1em;">SPEC GRID / SKU.${product.id}</span>
            </div>
            <div class="dp-specs">
                <c:forEach var="s" items="${product.specs}">
                    <div class="dp-spec">
                        <div class="tg-mono" style="font-size:10px;letter-spacing:.1em;color:var(--ink-4);">${s.specName}</div>
                        <div style="margin-top:6px;font-size:20px;font-weight:700;letter-spacing:-.01em;">${s.specValue}</div>
                    </div>
                </c:forEach>
            </div>
        </div>
    </c:if>

    <!-- ================= ĐÁNH GIÁ ================= -->
    <div class="tg-card tg-rise dp-sect" id="reviews">
        <h2 class="dp-h2" style="margin-bottom:26px;">Đánh giá từ khách hàng</h2>

        <c:if test="${param.reviewed == 'true'}">
            <div class="dp-ok">✓ Cảm ơn bạn đã đánh giá!</div>
        </c:if>
        <c:if test="${not empty param.reviewError}">
            <div class="dp-err">${param.reviewError}</div>
        </c:if>

        <div class="dp-rv">
            <div style="background:var(--bg-page);border-radius:14px;padding:26px;text-align:center;">
                <div style="font-size:46px;font-weight:700;letter-spacing:-.03em;line-height:1;">
                    <fmt:formatNumber value="${reviewSummary.average}" pattern="0.0"/>
                </div>
                <div class="tg-stars" style="font-size:17px;letter-spacing:3px;margin:8px 0;">
                    <c:forEach begin="1" end="5" var="i">${i <= reviewSummary.roundedAverage ? '★' : '☆'}</c:forEach>
                </div>
                <div class="tg-mono" style="font-size:11px;color:var(--ink-3);">${reviewSummary.total} ĐÁNH GIÁ</div>
            </div>

            <div>
                <div style="display:flex;flex-direction:column;gap:8px;margin-bottom:26px;">
                    <c:forEach begin="1" end="5" var="star">
                        <c:set var="sao" value="${6 - star}"/>
                        <div style="display:flex;align-items:center;gap:12px;">
                            <span class="tg-mono" style="font-size:12px;width:26px;color:var(--ink-2);">${sao}★</span>
                            <div style="flex:1;height:8px;background:var(--line-soft);border-radius:var(--r-pill);overflow:hidden;">
                                <span class="tg-bar" style="display:block;height:100%;border-radius:var(--r-pill);background:var(--warn-2);width:${reviewSummary.getPercent(sao)}%;"></span>
                            </div>
                            <span class="tg-mono" style="font-size:12px;width:30px;text-align:right;color:var(--ink-4);">${reviewSummary.breakdown[sao]}</span>
                        </div>
                    </c:forEach>
                </div>

                <%-- Form viết đánh giá: chỉ mở cho người ĐÃ MUA (kiểm lại ở server) --%>
                <c:choose>
                    <c:when test="${not empty myReview}">
                        <div style="background:var(--surface-2);border-radius:14px;padding:18px 22px;margin-bottom:22px;font-size:13px;color:var(--ink-2);">
                            Bạn đã đánh giá sản phẩm này <span class="tg-stars">${myReview.rating}★</span>. Gửi lại để cập nhật nội dung.
                        </div>
                    </c:when>
                    <c:when test="${not canReview}">
                        <div style="background:var(--surface-2);border-radius:14px;padding:18px 22px;margin-bottom:22px;font-size:13px;color:var(--ink-3);">
                            Chỉ khách đã mua và nhận hàng thành công mới đánh giá được sản phẩm này.
                        </div>
                    </c:when>
                </c:choose>

                <c:if test="${canReview || not empty myReview}">
                    <div style="background:var(--bg-page);border-radius:14px;padding:20px 22px;margin-bottom:22px;">
                        <div style="font-size:14px;font-weight:600;margin-bottom:12px;">Viết đánh giá của bạn</div>
                        <form method="post" action="/products/${product.id}/reviews">
                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                            <div style="display:flex;align-items:center;gap:12px;margin-bottom:12px;flex-wrap:wrap;">
                                <span style="font-size:13px;color:var(--ink-2);">Chấm điểm:</span>
                                <div id="dpStarPick" style="display:flex;gap:4px;">
                                    <c:forEach begin="1" end="5" var="i">
                                        <button type="button" class="dp-star" data-sao="${i}" onclick="dpChonSao(${i})">★</button>
                                    </c:forEach>
                                </div>
                                <span id="dpStarText" class="tg-mono" style="font-size:12px;color:var(--ink-4);"></span>
                            </div>
                            <input type="hidden" name="rating" id="ratingInput" value="${not empty myReview ? myReview.rating : 5}" required/>
                            <textarea name="comment" rows="3" placeholder="Chia sẻ cảm nhận về sản phẩm — độ hoàn thiện, cảm giác dùng, độ trễ…"
                                      style="width:100%;box-sizing:border-box;border:1px solid var(--line-new);border-radius:10px;padding:12px 14px;font-size:13px;font-family:inherit;outline:none;resize:vertical;margin-bottom:12px;">${not empty myReview ? myReview.comment : ''}</textarea>
                            <button type="submit" class="tg-btn tg-primary" style="padding:11px 24px;font-size:13px;border-radius:10px;">Gửi đánh giá</button>
                        </form>
                    </div>
                </c:if>

                <div style="display:flex;flex-direction:column;">
                    <c:forEach var="r" items="${reviews}">
                        <div style="border-top:1px solid var(--line-soft);padding:18px 0;">
                            <div style="display:flex;align-items:center;gap:12px;margin-bottom:7px;flex-wrap:wrap;">
                                <span style="font-weight:600;font-size:14px;">${r.user.fullName}</span>
                                <span class="tg-stars" style="font-size:13px;letter-spacing:1px;">
                                    <c:forEach begin="1" end="5" var="i">${i <= r.rating ? '★' : '☆'}</c:forEach>
                                </span>
                                <span style="background:var(--ok-bg-2);color:var(--ok-deep);border-radius:var(--r-pill);font-size:11px;font-weight:600;padding:2px 9px;">Đã mua hàng</span>
                                <c:if test="${not empty r.createdAt}">
                                    <span class="tg-mono" style="margin-left:auto;font-size:11px;color:var(--ink-4);">
                                        ${r.createdAt.toString().substring(0,10)}
                                    </span>
                                </c:if>
                            </div>
                            <c:if test="${not empty r.comment}">
                                <p style="font-size:14px;line-height:1.65;color:var(--ink-2);margin:0;">${fn:escapeXml(r.comment)}</p>
                            </c:if>
                        </div>
                    </c:forEach>
                    <c:if test="${empty reviews}">
                        <p style="font-size:13px;color:var(--ink-3);margin:0;">Chưa có đánh giá nào. Hãy là người đầu tiên!</p>
                    </c:if>
                </div>
            </div>
        </div>
    </div>

    <!-- ================= SẢN PHẨM LIÊN QUAN ================= -->
    <c:if test="${not empty lienQuan}">
        <div style="display:flex;align-items:baseline;justify-content:space-between;margin-bottom:20px;gap:12px;flex-wrap:wrap;">
            <h2 class="dp-h2">Sản phẩm liên quan</h2>
            <a href="/products?category=${product.category.id}" class="tg-link">Xem tất cả →</a>
        </div>
        <div class="dp-rel">
            <c:forEach var="r" items="${lienQuan}" varStatus="l">
                <a href="/products/${r.id}" class="tg-prod tg-rise" style="animation-delay:${l.index * 70}ms;">
                    <div style="background:var(--bg-page);aspect-ratio:4/3;padding:20px;overflow:hidden;position:relative;">
                        <c:if test="${giaLienQuan[r.id].hasSale}">
                            <span class="tg-mono" style="position:absolute;top:12px;right:12px;background:var(--err);color:#fff;font-size:10px;font-weight:600;padding:3px 9px;border-radius:var(--r-pill);z-index:2;">−${giaLienQuan[r.id].percentOff}%</span>
                        </c:if>
                        <c:choose>
                            <c:when test="${not empty r.imageUrl}">
                                <img class="tg-thumb" src="/static/images/${r.imageUrl}" alt="${r.name}"
                                     onerror="this.onerror=null;this.src='/static/images/banner.png';"
                                     style="width:100%;height:100%;object-fit:contain;mix-blend-mode:multiply;"/>
                            </c:when>
                            <c:otherwise>
                                <img class="tg-thumb" src="/static/images/banner.png" alt="${r.name}"
                                     style="width:100%;height:100%;object-fit:contain;"/>
                            </c:otherwise>
                        </c:choose>
                    </div>
                    <div style="padding:15px 17px;">
                        <div style="font-size:14px;font-weight:600;margin-bottom:5px;line-height:1.35;">${r.name}</div>
                        <div style="font-size:15px;font-weight:700;color:var(--accent);">
                            <c:choose>
                                <c:when test="${giaLienQuan[r.id].hasSale}"><fmt:formatNumber value="${giaLienQuan[r.id].salePrice}" pattern="#,##0"/>đ</c:when>
                                <c:otherwise><fmt:formatNumber value="${r.price}" pattern="#,##0"/>đ</c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                </a>
            </c:forEach>
        </div>
    </c:if>
</div>
</div><!-- /.tg-page -->

<style>
    .dp-wrap  { max-width:1760px; margin:0 auto; padding:0 48px 60px; }
    .dp-crumb { padding:20px 0; font-size:11px; color:var(--ink-4); letter-spacing:.08em; }
    .dp-crumb a { color:inherit; text-decoration:none; }
    .dp-crumb a:hover { color:var(--accent); }
    .dp-top   { display:grid; grid-template-columns:1fr 1fr; gap:24px; margin-bottom:24px; }
    .dp-sect  { padding:32px 36px; margin-bottom:24px; }
    .dp-h2    { font-size:24px; font-weight:700; margin:0; letter-spacing:-.01em; }

    .dp-ok  { background:var(--ok-bg-2);  border:1px solid var(--ok-line-2);  color:var(--ok-deep);
              border-radius:var(--r-md); padding:12px 16px; font-size:13px; margin-bottom:16px; }
    .dp-err { background:var(--err-bg-2); border:1px solid var(--err-line-2); color:var(--err);
              border-radius:var(--r-md); padding:12px 16px; font-size:13px; margin-bottom:16px; }

    .dp-pill { display:inline-block; border-radius:var(--r-pill); font-family:var(--font-mono);
               font-size:12px; font-weight:600; padding:6px 14px; }

    .dp-qbtn { width:44px; height:48px; border:none; background:var(--surface); font-size:18px;
               cursor:pointer; color:var(--ink-2); transition:background .2s ease; }
    .dp-qbtn:hover { background:var(--surface-3); }
    .dp-qinp { width:60px; height:48px; border:none; border-left:1px solid var(--line-new);
               border-right:1px solid var(--line-new); text-align:center;
               font-family:var(--font-mono); font-size:15px; font-weight:600; outline:none; }
    .dp-qinp::-webkit-outer-spin-button, .dp-qinp::-webkit-inner-spin-button { -webkit-appearance:none; margin:0; }

    .dp-btn-dark { flex:1; background:var(--surface); border:1px solid var(--ink); color:var(--ink);
                   border-radius:12px; padding:16px; font-size:14px; font-weight:600; cursor:pointer; }
    .dp-btn-dark:hover { background:var(--ink); color:#fff; }

    .dp-trust { display:grid; grid-template-columns:repeat(3,1fr); gap:12px; margin-top:24px;
                padding-top:24px; border-top:1px solid var(--line-soft); }
    .dp-trust > div { display:flex; align-items:center; gap:9px; font-size:12px; color:var(--ink-2); line-height:1.3; }
    .dp-tico { width:30px; height:30px; flex-shrink:0; border-radius:var(--r-sm);
               display:inline-flex; align-items:center; justify-content:center; }

    .dp-specs { display:grid; grid-template-columns:repeat(4,1fr); gap:12px; }
    .dp-spec  { background:var(--bg-page); border-radius:14px; padding:16px 18px; }

    .dp-rv    { display:grid; grid-template-columns:230px 1fr; gap:36px; align-items:start; }
    .dp-rel   { display:grid; grid-template-columns:repeat(4,1fr); gap:20px; }

    /* Nút chọn biến thể + ô màu */
    .dp-vbtn  { display:inline-block; text-decoration:none; padding:10px 18px; border-radius:var(--r-md); border:1px solid var(--line-new);
                background:var(--surface); color:var(--ink-2); font:inherit; font-size:13px;
                font-weight:600; cursor:pointer; transition:all .2s ease; }
    .dp-vbtn:hover  { border-color:var(--accent); color:var(--accent); }
    .dp-vbtn.is-on  { background:var(--accent); border-color:var(--accent); color:#fff; }
    .dp-sw    { width:38px; height:38px; border-radius:50%; border:2px solid var(--line-new);
                background:var(--surface); cursor:pointer; padding:3px; transition:all .2s ease; }
    .dp-sw:hover  { border-color:var(--accent); }
    .dp-sw.is-on  { border-color:var(--accent); box-shadow:0 0 0 3px var(--accent-soft-2); }
    .dp-sw span   { display:block; width:100%; height:100%; border-radius:50%; }
    .dp-thumb { width:64px; height:64px; border-radius:12px; border:1px solid var(--line-new);
                background:var(--bg-page); cursor:pointer; padding:6px; transition:all .2s ease; }
    .dp-thumb:hover { border-color:var(--accent); }
    .dp-thumb.is-on { border-color:var(--accent); box-shadow:0 0 0 3px var(--accent-soft-2); }
    .dp-thumb img { width:100%; height:100%; object-fit:contain; mix-blend-mode:multiply; }

    .dp-star  { background:none; border:none; font-size:22px; line-height:1; cursor:pointer;
                color:var(--line-new); padding:0; transition:color .15s ease; }
    .dp-star.is-on { color:var(--warn-2); }

    @media (max-width:1100px) {
        .dp-top   { grid-template-columns:1fr; }
        .dp-specs { grid-template-columns:repeat(2,1fr); }
        .dp-rv    { grid-template-columns:1fr; gap:24px; }
        .dp-rel   { grid-template-columns:repeat(2,1fr); }
        .dp-wrap  { padding:0 20px 50px; }
    }
</style>

<script src="/static/js/compare.js"></script>
<script>
    /* ===== DỮ LIỆU BIẾN THỂ (đưa từ server xuống) ===== */
    var dpVariants = [
        <c:forEach var="v" items="${product.variants}" varStatus="l">
        <c:if test="${v.isActive}">
        {
            id: ${v.id},
            name: '<c:out value="${v.name}"/>',
            color: '<c:out value="${v.color}"/>',
            colorCode: '<c:out value="${v.colorCode}"/>',
            /* Bỏ price: giá lấy theo sản phẩm, không theo màu. */
            stock: ${empty v.stock ? 0 : v.stock},
            reserved: ${empty reservedMap[v.id] ? 0 : reservedMap[v.id]},
            imageUrl: '<c:out value="${v.imageUrl}"/>'
        }<c:if test="${!l.last}">,</c:if>
        </c:if>
        </c:forEach>
    ];

    var dpTonSanPham = ${empty product.stockQuantity ? 0 : product.stockQuantity};
    var dpTenDangChon = null;
    var dpBienTheDangChon = null;
    var dpDinhDang = new Intl.NumberFormat('vi-VN');

    /* ===== HIỆN TÌNH TRẠNG KHO ===== */
    function dpVeKho(banDuoc) {
        var pill = document.getElementById('dpStock');
        var note = document.getElementById('dpStockNote');
        var qty  = document.getElementById('qty');
        var box  = document.getElementById('dpRestock');

        if (banDuoc <= 0) {
            pill.textContent = 'Hết hàng';
            pill.style.background = 'var(--err-bg-2)'; pill.style.color = 'var(--err)';
            note.textContent = '';
            qty.max = 0; qty.value = 1;
            if (box) box.style.display = 'block';
        } else {
            pill.textContent = banDuoc <= 5 ? ('Sắp hết — còn ' + banDuoc) : ('Còn hàng — ' + banDuoc + ' sản phẩm');
            pill.style.background = banDuoc <= 5 ? 'var(--warn-bg-2)' : 'var(--ok-bg-2)';
            pill.style.color      = banDuoc <= 5 ? 'var(--warn-deep)' : 'var(--ok-deep)';
            note.textContent = 'Tối đa ' + banDuoc + ' sản phẩm';
            qty.max = banDuoc;
            if (Number(qty.value) > banDuoc) qty.value = banDuoc;
            if (box) box.style.display = 'none';
        }
    }

    /* ===== CHỌN BIẾN THỂ ===== */
    function dpChonBienThe(v) {
        dpBienTheDangChon = v;
        document.getElementById('selectedVariantId').value = v.id;

        var rv = document.getElementById('restockVariantId');
        if (rv) rv.value = v.id;

        /* Đổi màu chỉ đổi tên màu và ảnh, KHÔNG đổi giá hiển thị.
           Mọi màu dùng chung giá của sản phẩm; chỉ khác dòng sản phẩm mới khác giá.
           Lấy giá theo v.price (giá riêng của biến thể) sẽ làm trang chi tiết hiện
           một số, còn giỏ hàng và đơn hàng hiện số khác — sai tiền mà không báo lỗi. */
        var ten = document.getElementById('dpColorName');
        if (ten) ten.textContent = v.color || '—';

        if (v.imageUrl) document.getElementById('dpMainImg').src = '/static/images/' + v.imageUrl;

        /* Model 2 — "bán được" = tồn trừ phần đã nằm trong đơn chưa xuất */
        dpVeKho(Math.max(0, v.stock - (v.reserved || 0)));

        document.querySelectorAll('#dpColors .dp-sw').forEach(function (b) {
            b.classList.toggle('is-on', Number(b.dataset.id) === v.id);
        });
        document.querySelectorAll('#dpThumbs .dp-thumb').forEach(function (b) {
            b.classList.toggle('is-on', Number(b.dataset.id) === v.id);
        });
    }

    function dpChonTen(ten) {
        dpTenDangChon = ten;
        document.querySelectorAll('#dpNames .dp-vbtn').forEach(function (b) {
            b.classList.toggle('is-on', b.dataset.ten === ten);
        });

        var mau = dpVariants.filter(function (v) { return v.name === ten; });
        var oMau = document.getElementById('dpColors');
        oMau.innerHTML = '';
        mau.forEach(function (v) {
            var b = document.createElement('button');
            b.type = 'button'; b.className = 'dp-sw'; b.dataset.id = v.id;
            b.title = v.color || v.name;
            b.innerHTML = '<span style="background:' + (v.colorCode || '#CFD4DC') + ';"></span>';
            b.onclick = function () { dpChonBienThe(v); };
            oMau.appendChild(b);
        });
        if (mau.length) dpChonBienThe(mau[0]);
    }

    (function dpKhoiTao() {
        if (!dpVariants.length) {
            /* Sản phẩm không có biến thể — dùng tồn ở mức sản phẩm */
            dpVeKho(dpTonSanPham);
            return;
        }

        /* Nút dòng sản phẩm (bỏ trùng tên). Chỉ hiện khi CÓ TỪ 2 TÊN TRỞ LÊN —
           biến thể giờ chủ yếu chỉ khác màu nên hầu hết sản phẩm sẽ không thấy
           khối này, khách đi thẳng vào chọn màu. */
        var tenDs = [];
        dpVariants.forEach(function (v) { if (tenDs.indexOf(v.name) === -1) tenDs.push(v.name); });

        if (tenDs.length > 1) {
            var oTen = document.getElementById('dpNames');
            tenDs.forEach(function (t) {
                var b = document.createElement('button');
                b.type = 'button'; b.className = 'dp-vbtn'; b.dataset.ten = t;
                b.textContent = t || 'Bản tiêu chuẩn';
                b.onclick = function () { dpChonTen(t); };
                oTen.appendChild(b);
            });
            document.getElementById('dpNameBox').style.display = 'block';
        }

        /* Ảnh nhỏ: mỗi biến thể có ảnh riêng thì hiện một ô */
        var oThumb = document.getElementById('dpThumbs');
        dpVariants.forEach(function (v) {
            if (!v.imageUrl) return;
            var b = document.createElement('button');
            b.type = 'button'; b.className = 'dp-thumb'; b.dataset.id = v.id;
            b.title = v.name + (v.color ? ' — ' + v.color : '');
            b.innerHTML = '<img src="/static/images/' + v.imageUrl + '" alt="">';
            b.onclick = function () { dpChonTen(v.name); dpChonBienThe(v); };
            oThumb.appendChild(b);
        });

        dpChonTen(tenDs[0]);
    })();

    /* ===== SỐ LƯỢNG ===== */
    function dpQty(b) {
        var o = document.getElementById('qty');
        var max = Number(o.max) || 1;
        var v = Math.min(max, Math.max(1, (Number(o.value) || 1) + b));
        o.value = v;
    }

    /* ===== THÊM GIỎ / MUA NGAY ===== */
    function dpDuongDan(id, muaNgay) {
        var qty = document.getElementById('qty').value;
        var vid = document.getElementById('selectedVariantId').value;
        return '/cart/add?productId=' + id + '&quantity=' + qty
             + (muaNgay ? '&buyNow=true' : '')
             + (vid ? '&variantId=' + vid : '');
    }
    function addToCart(id) { window.location.href = dpDuongDan(id, false); }
    function buyNow(id)    { window.location.href = dpDuongDan(id, true); }

    /* ===== CHẤM SAO ===== */
    function dpChonSao(n) {
        document.getElementById('ratingInput').value = n;
        document.querySelectorAll('#dpStarPick .dp-star').forEach(function (b) {
            b.classList.toggle('is-on', Number(b.dataset.sao) <= n);
        });
        var nhan = ['', 'Rất tệ', 'Tệ', 'Bình thường', 'Tốt', 'Rất tốt'];
        document.getElementById('dpStarText').textContent = nhan[n] || '';
    }
    (function () {
        var o = document.getElementById('ratingInput');
        if (o) dpChonSao(Number(o.value) || 5);
    })();
</script>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>

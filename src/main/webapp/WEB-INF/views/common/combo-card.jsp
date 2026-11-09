<%@ page contentType="text/html;charset=UTF-8" %>
<%-- Thẻ combo dùng chung cho trang chủ + trang /combo.
     Cần sẵn biến ${cv} (ComboService.ComboView); ${cvDelay} là độ trễ hiệu ứng, có thể bỏ trống. --%>
<c:set var="cbAccent" value="${empty cv.combo.accentColor ? '#1B49FF' : cv.combo.accentColor}"/>
<a href="/combo/${cv.combo.id}" class="tg-card tg-lift tg-rise"
   style="display:block;text-decoration:none;color:inherit;overflow:hidden;animation-delay:${empty cvDelay ? 0 : cvDelay}ms;">

    <div class="tg-combo-strip" style="background:${cbAccent};"></div>

    <div style="padding:24px 26px 26px;">
        <div style="display:flex;align-items:center;justify-content:space-between;gap:10px;margin-bottom:14px;min-height:24px;">
            <c:if test="${not empty cv.combo.tag}">
                <span class="tg-combo-tag" style="color:${cbAccent};background:var(--surface-3);">${cv.combo.tag}</span>
            </c:if>
            <c:choose>
                <c:when test="${not cv.conHang}">
                    <span class="tg-combo-save" style="color:var(--ink-2);background:var(--surface-3);">TẠM HẾT</span>
                </c:when>
                <c:when test="${cv.coGiam}">
                    <span class="tg-combo-save">Tiết kiệm <fmt:formatNumber value="${cv.tietKiem}" pattern="#,##0"/>đ</span>
                </c:when>
            </c:choose>
        </div>

        <div style="font-size:19px;font-weight:700;letter-spacing:-.01em;margin-bottom:6px;line-height:1.3;">${cv.combo.name}</div>
        <p style="font-size:13px;color:var(--ink-3);line-height:1.65;margin:0 0 16px;min-height:42px;">${cv.combo.description}</p>

        <div class="tg-combo-thumbs">
            <c:forEach var="m" items="${cv.cacMon}">
                <div class="tg-combo-thumb">
                    <c:choose>
                        <c:when test="${not empty m.macDinh.imageUrl}">
                            <img src="/static/images/${m.macDinh.imageUrl}" alt="${m.macDinh.name}" class="tg-zoom"
                                 onerror="this.onerror=null;this.src='/static/images/banner.png';"/>
                        </c:when>
                        <c:otherwise>
                            <img src="/static/images/banner.png" alt="${m.macDinh.name}" class="tg-zoom"/>
                        </c:otherwise>
                    </c:choose>
                </div>
            </c:forEach>
        </div>

        <div style="display:flex;align-items:baseline;justify-content:space-between;gap:10px;padding-top:14px;border-top:1px solid var(--line-soft);">
            <div>
                <div style="font-size:20px;font-weight:700;color:var(--accent);">
                    <fmt:formatNumber value="${cv.giaBan}" pattern="#,##0"/>đ
                </div>
                <c:if test="${cv.coGiam}">
                    <div class="tg-combo-old"><fmt:formatNumber value="${cv.giaGoc}" pattern="#,##0"/>đ</div>
                </c:if>
            </div>
            <span class="tg-cta" style="border:1px solid var(--line-new);border-radius:11px;font-size:12px;font-weight:600;padding:9px 17px;white-space:nowrap;">Xem combo</span>
        </div>
    </div>
</a>

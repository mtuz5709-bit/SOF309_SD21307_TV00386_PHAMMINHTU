<%@ page contentType="text/html;charset=UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Combo – TiTanGEAR"/>
<c:set var="currentPage" value="combo"/>
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<div class="tg-page">
<section class="tg-sect">
    <div class="tg-wrap">

        <div style="margin-bottom:26px;">
            <div class="tg-mono" style="font-size:11px;letter-spacing:.16em;color:var(--violet);margin-bottom:8px;">SETUP INSPIRATION</div>
            <h1 style="font-size:32px;font-weight:700;margin:0 0 10px;letter-spacing:-.02em;">Góc máy đồng bộ</h1>
            <p style="font-size:14px;color:var(--ink-3);margin:0;max-width:560px;line-height:1.7;">
                Những bộ ghép sẵn theo phong cách chơi — mua trọn bộ tiết kiệm hơn mua lẻ.
            </p>
        </div>

        <c:choose>
            <c:when test="${empty combos}">
                <div class="tg-card" style="padding:80px 40px;text-align:center;">
                    <div style="font-size:17px;font-weight:600;margin-bottom:8px;">Chưa có combo nào đang bán</div>
                    <p style="font-size:14px;color:var(--ink-3);margin:0 0 20px;">
                        Ghé lại sau nhé, hoặc xem toàn bộ sản phẩm đang có.
                    </p>
                    <a href="/products" class="tg-btn tg-primary">Xem sản phẩm</a>
                </div>
            </c:when>
            <c:otherwise>
                <div class="tg-combo-grid">
                    <c:forEach var="cv" items="${combos}" varStatus="loop">
                        <c:set var="cvDelay" value="${loop.index * 80}"/>
                        <%@ include file="/WEB-INF/views/common/combo-card.jsp" %>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>

    </div>
</section>
</div>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>

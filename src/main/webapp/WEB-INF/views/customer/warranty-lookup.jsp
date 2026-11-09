<%@ page contentType="text/html;charset=UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Tra Cứu Bảo Hành – TiTanGEAR"/>
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<div class="tg-page"><div style="max-width:760px;margin:0 auto;padding:48px 24px 90px;">
    <h1 style="font-size:30px;font-weight:700;letter-spacing:-0.02em;margin:0 0 8px;">Tra cứu bảo hành</h1>
    <p style="color:var(--ink-2);font-size:14px;margin:0 0 28px;">
        Nhập mã bảo hành in trên phiếu (dạng <strong>SR000001</strong>) để xem sản phẩm còn hạn bảo hành không.
    </p>

    <form method="get" action="/bao-hanh" style="display:flex;gap:10px;margin-bottom:28px;">
        <input type="text" name="serial" value="${serial}" placeholder="VD: SR000001" required
               style="flex:1;padding:12px 14px;border:1px solid var(--line-new);border-radius:var(--r-md);font-size:14px;font-family:var(--font-mono);outline:none;text-transform:uppercase;background:var(--surface);"/>
        <button type="submit" class="tg-btn tg-primary" style="padding:12px 26px;">Tra cứu</button>
    </form>

    <%-- Khách đăng nhập: liệt kê sẵn phiếu của họ, khỏi phải nhớ serial --%>
    <c:if test="${not empty cuaToi}">
        <div style="background:var(--surface);border:1px solid var(--line-new);border-radius:var(--r-xl);box-shadow:var(--sh-card);padding:22px 26px;margin-bottom:28px;">
            <div style="font-family:var(--font-mono);font-size:10px;letter-spacing:0.12em;color:var(--ink-3);text-transform:uppercase;margin-bottom:14px;">
                Phiếu bảo hành của bạn
            </div>
            <c:forEach var="p" items="${cuaToi}">
                <div style="display:flex;justify-content:space-between;align-items:center;gap:12px;flex-wrap:wrap;padding:10px 0;border-bottom:1px solid var(--line-soft);">
                    <div>
                        <a href="/bao-hanh?serial=${p.serialNo}"
                           style="font-family:var(--font-mono);font-weight:600;color:var(--accent);text-decoration:none;">${p.serialNo}</a>
                        <span style="font-size:13px;color:var(--ink-2);margin-left:10px;">${tenSanPham[p.productId]}</span>
                    </div>
                    <div style="font-size:12px;font-family:var(--font-mono);color:var(--ink-3);">
                        <c:choose>
                            <c:when test="${p.conHan}">Còn ${p.soNgayConLai} ngày · hết hạn ${p.endDate}</c:when>
                            <c:otherwise>Đã hết hạn ${p.endDate}</c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </c:forEach>
        </div>
    </c:if>

    <c:if test="${daTim}">
        <c:choose>
            <c:when test="${empty w}">
                <div style="border:1px solid var(--err-line-2);background:var(--err-bg-2);color:var(--err);border-radius:var(--r-md);padding:16px 18px;font-size:14px;">
                    Không tìm thấy phiếu bảo hành với mã <strong>${serial}</strong>.
                    Kiểm tra lại mã trên phiếu giúp mình nhé.
                </div>
            </c:when>
            <c:otherwise>
                <div style="background:var(--surface);border:1px solid var(--line-new);border-radius:var(--r-xl);box-shadow:var(--sh-card);padding:26px 28px;">
                    <div style="display:flex;justify-content:space-between;align-items:center;gap:16px;flex-wrap:wrap;margin-bottom:20px;">
                        <div>
                            <div style="font-size:24px;font-weight:700;font-family:var(--font-mono);">${w.serialNo}</div>
                            <div style="font-size:14px;color:var(--ink-2);margin-top:4px;">${tenSp}</div>
                        </div>
                        <c:choose>
                            <c:when test="${w.conHan}"><span class="badge-green">Còn bảo hành</span></c:when>
                            <c:otherwise><span class="badge-red">Hết bảo hành</span></c:otherwise>
                        </c:choose>
                    </div>

                    <div style="display:flex;gap:40px;flex-wrap:wrap;border-top:1px solid var(--line-soft);padding-top:16px;">
                        <div>
                            <div style="font-family:var(--font-mono);font-size:10px;letter-spacing:0.12em;color:var(--ink-3);text-transform:uppercase;">Bắt đầu</div>
                            <div style="font-size:15px;font-weight:600;margin-top:2px;">${w.startDate}</div>
                        </div>
                        <div>
                            <div style="font-family:var(--font-mono);font-size:10px;letter-spacing:0.12em;color:var(--ink-3);text-transform:uppercase;">Hết hạn</div>
                            <div style="font-size:15px;font-weight:600;margin-top:2px;">${w.endDate}</div>
                        </div>
                        <div>
                            <div style="font-family:var(--font-mono);font-size:10px;letter-spacing:0.12em;color:var(--ink-3);text-transform:uppercase;">Còn lại</div>
                            <div style="font-size:15px;font-weight:600;margin-top:2px;">
                                <c:choose>
                                    <c:when test="${w.conHan}">${w.soNgayConLai} ngày</c:when>
                                    <c:otherwise>Đã hết hạn</c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                    </div>

                    <c:if test="${not empty lichSu}">
                        <div style="border-top:1px solid var(--line-soft);margin-top:16px;padding-top:16px;">
                            <div style="font-family:var(--font-mono);font-size:10px;letter-spacing:0.12em;color:var(--ink-3);text-transform:uppercase;margin-bottom:10px;">Lịch sử sửa chữa</div>
                            <c:forEach var="r" items="${lichSu}">
                                <div style="font-size:13px;color:var(--ink-2);padding:6px 0;border-bottom:1px solid var(--line-soft);">
                                    <c:choose>
                                        <c:when test="${r.trangThai == 'TIEP_NHAN'}">Tiếp nhận</c:when>
                                        <c:when test="${r.trangThai == 'DANG_SUA'}">Đang sửa</c:when>
                                        <c:when test="${r.trangThai == 'XONG'}">Đã xong</c:when>
                                        <c:when test="${r.trangThai == 'TU_CHOI'}">Từ chối bảo hành</c:when>
                                        <c:otherwise>${r.trangThai}</c:otherwise>
                                    </c:choose>
                                    — ${r.noiDung}
                                </div>
                            </c:forEach>
                        </div>
                    </c:if>
                </div>
            </c:otherwise>
        </c:choose>
    </c:if>
</div>

</div>
<%@ include file="/WEB-INF/views/common/footer.jsp" %>

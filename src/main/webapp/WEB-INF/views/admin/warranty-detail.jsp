<%@ page contentType="text/html;charset=UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>Phiếu ${w.serialNo} – TiTanGEAR</title>
    <link href="https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@400;500;600;700&family=IBM+Plex+Mono:wght@400;500;600&display=swap" rel="stylesheet"/>
    <link rel="stylesheet" href="/static/css/tokens.css"/>
    <link rel="stylesheet" href="/static/css/main.css"/>
    <link rel="stylesheet" href="/static/css/admin.css"/>
</head>
<body>
<div class="admin-layout">
    <c:set var="adminActive" value="warranties"/>
    <%@ include file="/WEB-INF/views/common/admin-sidebar.jsp" %>

    <div class="admin-main">
        <c:set var="adminTitle" value="Phiếu bảo hành"/>
        <c:set var="adminCrumb" value="ADMIN / WARRANTIES / DETAIL"/>
        <%@ include file="/WEB-INF/views/common/admin-topbar.jsp" %>

        <div style="margin-bottom:16px;">
            <a href="/admin/warranties" class="btn-edit">← Quay lại danh sách</a>
        </div>

        <c:if test="${not empty loi}">
            <div class="admin-alert-err">${loi}</div>
        </c:if>

        <div class="order-detail-bp">
            <div class="od-col">
                <div class="od-card">
                    <div class="od-head">
                        <div>
                            <div class="od-head-id">${w.serialNo}</div>
                            <div class="od-head-time">
                                Mua ngày ${w.startDate} · Hết hạn ${w.endDate} ·
                                <c:choose>
                                    <c:when test="${w.conHan}">còn ${w.soNgayConLai} ngày</c:when>
                                    <c:otherwise>đã quá hạn ${-w.soNgayConLai} ngày</c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                        <div>
                            <c:choose>
                                <c:when test="${w.sapHetHan}"><span class="badge-yellow">Sắp hết hạn</span></c:when>
                                <c:when test="${w.conHan}"><span class="badge-green">Còn bảo hành</span></c:when>
                                <c:otherwise><span class="badge-red">Hết bảo hành</span></c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                </div>

                <div class="od-card">
                    <p class="od-card-title">Lịch sử sửa chữa</p>
                    <c:choose>
                        <c:when test="${empty lichSu}">
                            <p style="font-size:13px;color:var(--ink-3);margin:0;">Chưa có lần sửa chữa nào.</p>
                        </c:when>
                        <c:otherwise>
                            <ul class="od-timeline">
                                <c:forEach var="r" items="${lichSu}">
                                    <li class="od-step done">
                                        <span class="od-dot"></span>
                                        <div class="od-step-name">
                                            <c:choose>
                                                <c:when test="${r.trangThai == 'TIEP_NHAN'}">Tiếp nhận</c:when>
                                                <c:when test="${r.trangThai == 'DANG_SUA'}">Đang sửa</c:when>
                                                <c:when test="${r.trangThai == 'XONG'}">Đã xong</c:when>
                                                <c:when test="${r.trangThai == 'TU_CHOI'}">Từ chối bảo hành</c:when>
                                                <c:otherwise>${r.trangThai}</c:otherwise>
                                            </c:choose>
                                        </div>
                                        <div class="od-step-time">
                                            ${r.noiDung}
                                            <c:if test="${not empty r.createdAt}">
                                                · ${r.createdAt.toString().substring(0,16).replace("T"," ")}
                                            </c:if>
                                        </div>
                                    </li>
                                </c:forEach>
                            </ul>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <div class="od-col">
                <div class="od-card">
                    <p class="od-card-title">Thông tin</p>
                    <div class="od-field">
                        <div class="od-field-label">Sản phẩm</div>
                        <div class="od-field-val">${tenSp}</div>
                    </div>
                    <div class="od-field">
                        <div class="od-field-label">Khách hàng</div>
                        <div class="od-field-val">${tenKh}</div>
                    </div>
                    <div class="od-field">
                        <div class="od-field-label">Thuộc dòng đơn</div>
                        <div class="od-field-val">#${w.orderItemId}</div>
                    </div>
                </div>

                <div class="od-card">
                    <p class="od-card-title">Ghi nhận sửa chữa</p>
                    <c:choose>
                        <%-- HẾT HẠN: hệ thống tự khoá, admin không phải tự kiểm tra ngày.
                             Server cũng chặn (WarrantyService.themLanSua) nên ẩn form ở đây
                             chỉ là để nhìn cho rõ, không phải lớp bảo vệ duy nhất. --%>
                        <c:when test="${not w.conHan}">
                            <div class="admin-alert-err" style="margin:0;">
                                Phiếu đã <strong>hết hạn bảo hành</strong> từ ngày ${w.endDate}
                                (${-w.soNgayConLai} ngày trước).<br/>
                                Hệ thống đã tự khoá — không nhận sửa chữa bảo hành cho phiếu này.
                            </div>
                        </c:when>
                        <c:otherwise>
                            <c:if test="${w.sapHetHan}">
                                <div class="admin-alert-warn">
                                    Chỉ còn <strong>${w.soNgayConLai} ngày</strong> là hết hạn — xử lý sớm giúp khách.
                                </div>
                            </c:if>
                            <form method="post" action="/admin/warranties/${w.id}/repairs">
                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                                <div class="form-group">
                                    <label>Nội dung</label>
                                    <textarea name="noiDung" required placeholder="VD: Khách báo lỗi nút chuột trái"></textarea>
                                </div>
                                <div class="form-group">
                                    <label>Trạng thái</label>
                                    <select name="trangThai">
                                        <option value="TIEP_NHAN">Tiếp nhận</option>
                                        <option value="DANG_SUA">Đang sửa</option>
                                        <option value="XONG">Đã xong</option>
                                        <option value="TU_CHOI">Từ chối bảo hành</option>
                                    </select>
                                </div>
                                <button type="submit" class="btn-primary" style="width:100%;">Lưu ghi nhận</button>
                            </form>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
</html>

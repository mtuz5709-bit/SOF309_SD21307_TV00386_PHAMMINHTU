<%@ page contentType="text/html;charset=UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>Cài Đặt Hệ Thống – TiTanGEAR</title>
    <link href="https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@400;500;600;700&family=IBM+Plex+Mono:wght@400;500;600&display=swap" rel="stylesheet"/>
    <link rel="stylesheet" href="/static/css/tokens.css"/>
    <link rel="stylesheet" href="/static/css/main.css"/>
    <link rel="stylesheet" href="/static/css/admin.css"/>
</head>
<body>

<div class="admin-layout">
    <c:set var="adminActive" value="settings"/>
    <%@ include file="/WEB-INF/views/common/admin-sidebar.jsp" %>

    <div class="admin-main">
        <c:set var="adminTitle" value="Cài Đặt Hệ Thống"/>
        <c:set var="adminCrumb" value="ADMIN / SETTINGS"/>
        <%@ include file="/WEB-INF/views/common/admin-topbar.jsp" %>

        <c:if test="${not empty successMsg}">
            <div class="admin-alert-ok">${successMsg}</div>
        </c:if>
        <c:if test="${not empty errorMsg}">
            <div class="admin-alert-err">${errorMsg}</div>
        </c:if>

        <c:choose>
            <c:when test="${empty settings}">
                <div class="setting-card" style="text-align:center;color:var(--ink-3);">
                    Chưa có cài đặt nào trong hệ thống.
                </div>
            </c:when>
            <c:otherwise>
                <h2 class="settings-section-title">Giới Hạn Đơn Hàng</h2>
                <p class="settings-section-desc">
                    Các ngưỡng chống mua sỉ và chống bom hàng. Sửa số rồi bấm <strong>Lưu</strong>.
                </p>

                <c:forEach var="s" items="${settings}">
                    <c:if test="${!s.settingKey.startsWith('WAREHOUSE_')}">
                        <div class="setting-card">
                            <h3>
                                <c:choose>
                                    <c:when test="${s.settingKey == 'MAX_QTY_PER_PRODUCT_PER_ORDER'}">
                                        Giới hạn số lượng mua 1 sản phẩm / đơn hàng
                                    </c:when>
                                    <c:when test="${s.settingKey == 'COD_MAX_QTY_PER_PRODUCT'}">
                                        Số lượng tối đa được chọn COD (vượt quá bắt buộc chuyển khoản)
                                    </c:when>
                                    <c:when test="${s.settingKey == 'COD_MAX_ORDER_AMOUNT'}">
                                        Tổng tiền đơn tối đa được chọn COD (vượt quá bắt buộc chuyển khoản)
                                    </c:when>
                                    <c:when test="${s.settingKey == 'COD_MAX_ACTIVE_ORDERS'}">
                                        Số đơn COD tối đa đang chờ giao / khách
                                    </c:when>
                                    <c:otherwise>${s.settingKey}</c:otherwise>
                                </c:choose>
                            </h3>
                            <p class="setting-desc">${s.description}</p>

                            <form method="post" action="/admin/settings/update" class="setting-form">
                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                                <input type="hidden" name="settingKey" value="${s.settingKey}"/>
                                <input type="text" name="settingValue" value="${s.settingValue}" required/>
                                <button type="submit" class="btn-primary">Lưu</button>
                            </form>

                            <c:if test="${s.settingKey == 'MAX_QTY_PER_PRODUCT_PER_ORDER'}">
                                <div class="setting-note">
                                    Áp dụng ngay lập tức cho đơn hàng mới — khách không thể mua quá số lượng này
                                    cho 1 sản phẩm trong 1 đơn (chặn cả ở giao diện lẫn ở tầng CSDL).
                                </div>
                            </c:if>
                            <c:if test="${s.settingKey == 'COD_MAX_QTY_PER_PRODUCT'}">
                                <div class="setting-note">
                                    Đặt số lượng 1 sản phẩm nhiều hơn số này thì khách bắt buộc phải thanh toán trước
                                    qua chuyển khoản (VNPAY), không được chọn COD nữa — vì rủi ro bom hàng tỉ lệ
                                    thuận với giá trị đơn.
                                </div>
                            </c:if>
                            <c:if test="${s.settingKey == 'COD_MAX_ORDER_AMOUNT'}">
                                <div class="setting-note">
                                    Đơn hàng có tổng giá trị vượt quá số tiền này (dù chỉ 1 sản phẩm giá cao) cũng
                                    bắt buộc chuyển khoản trước — không quan tâm số lượng, chỉ quan tâm giá trị.
                                </div>
                            </c:if>
                            <c:if test="${s.settingKey == 'COD_MAX_ACTIVE_ORDERS'}">
                                <div class="setting-note">
                                    Mỗi khách chỉ được có tối đa bấy nhiêu đơn COD chưa giao xong cùng lúc — chống
                                    bom hàng mà không cấm mua lại. Đơn đã <strong>thanh toán trước qua chuyển khoản
                                    (VNPAY) KHÔNG bị giới hạn</strong>, vì tiền đã thu nên không còn rủi ro.
                                </div>
                            </c:if>
                        </div>
                    </c:if>
                </c:forEach>

                <!-- PHÂN QUYỀN THỦ KHO -->
                <h2 class="settings-section-title">Phân Quyền Tài Khoản Thủ Kho</h2>
                <p class="settings-section-desc">
                    Mặc định thủ kho chỉ vào được trang "Kho hàng". Bật quyền để cho phép xem thêm khu vực khác
                    — chỉ xem/thao tác cơ bản, không thêm/sửa/xóa. <strong>Gạt công tắc là lưu ngay.</strong>
                </p>

                <c:forEach var="s" items="${settings}">
                    <c:if test="${s.settingKey.startsWith('WAREHOUSE_')}">
                        <div class="setting-card setting-perm">
                            <div>
                                <h3>
                                    <c:choose>
                                        <c:when test="${s.settingKey == 'WAREHOUSE_CAN_VIEW_PRODUCTS'}">Xem danh sách sản phẩm</c:when>
                                        <c:when test="${s.settingKey == 'WAREHOUSE_CAN_VIEW_REVENUE'}">Xem báo cáo doanh thu</c:when>
                                        <c:when test="${s.settingKey == 'WAREHOUSE_CAN_MANAGE_ORDERS'}">Xem và cập nhật trạng thái đơn hàng</c:when>
                                        <c:otherwise>${s.settingKey}</c:otherwise>
                                    </c:choose>
                                </h3>
                                <p class="setting-desc" style="margin-bottom:0;">${s.description}</p>
                            </div>
                            <%-- Công tắc: gạt là lưu ngay (submit form ẩn), không cần bấm Lưu.
                                 Giá trị gửi đi là giá trị ĐẢO của hiện tại. --%>
                            <form method="post" action="/admin/settings/update" class="setting-toggle-form">
                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                                <input type="hidden" name="settingKey" value="${s.settingKey}"/>
                                <input type="hidden" name="settingValue" value="${s.settingValue == 'true' ? 'false' : 'true'}"/>
                                <label class="toggle-switch" title="${s.settingValue == 'true' ? 'Đang bật — bấm để tắt' : 'Đang tắt — bấm để bật'}">
                                    <input type="checkbox" ${s.settingValue == 'true' ? 'checked' : ''}
                                           onchange="this.form.submit()"/>
                                    <span class="toggle-track"><span class="toggle-thumb"></span></span>
                                </label>
                            </form>
                        </div>
                    </c:if>
                </c:forEach>
            </c:otherwise>
        </c:choose>
    </div>
</div>

</body>
</html>

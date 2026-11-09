<%@ page contentType="text/html;charset=UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Đơn Hàng – TiTanGEAR"/>
<c:set var="currentPage" value=""/>
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<div class="account-bp">
  <div class="profile-container">

    <!-- SIDEBAR TÀI KHOẢN -->
    <aside class="acc-sidebar">
      <c:if test="${not empty me}">
        <div class="acc-me">
          <span class="acc-me-av">${me.chuCai}</span>
          <div style="min-width:0;">
            <div class="acc-me-name">${me.ten}</div>
            <div class="acc-me-role">${user.role == 'ADMIN' ? 'Quản trị viên' : (user.role == 'WAREHOUSE' ? 'Thủ kho' : 'Khách hàng')}</div>
          </div>
        </div>
      </c:if>
      <ul class="acc-nav">
        <li><a href="/profile">Thông tin tài khoản</a></li>
        <li><a href="/my-orders" class="active">Đơn hàng của tôi</a></li>
        <li><a href="/bao-hanh">Tra cứu bảo hành</a></li>
        <li><a href="/logout">Đăng xuất</a></li>
      </ul>
    </aside>

    <!-- NỘI DUNG -->
    <div class="profile-content">
      <h1 class="acc-title">Đơn hàng của tôi</h1>

      <c:if test="${not empty thanhCong}">
        <div class="alert-success">${thanhCong}</div>
      </c:if>
      <c:if test="${not empty loi}">
        <div class="alert-error">${loi}</div>
      </c:if>

      <!-- FILTER TABS -->
      <div class="filter-tabs">
        <a href="/my-orders" class="filter-tab ${param.status == null ? 'active' : ''}">Tất cả</a>
        <a href="/my-orders?status=PENDING" class="filter-tab ${param.status == 'PENDING' ? 'active' : ''}">Chờ xác nhận</a>
        <a href="/my-orders?status=SHIPPING" class="filter-tab ${param.status == 'SHIPPING' ? 'active' : ''}">Đang giao</a>
        <a href="/my-orders?status=COMPLETED" class="filter-tab ${param.status == 'COMPLETED' ? 'active' : ''}">Hoàn thành</a>
        <a href="/my-orders?status=CANCELLED" class="filter-tab ${param.status == 'CANCELLED' ? 'active' : ''}">Đã hủy</a>
      </div>

      <c:choose>
        <c:when test="${empty orders}">
          <div class="empty-orders">
            <div class="empty-orders-label">KHÔNG CÓ ĐƠN HÀNG NÀO</div>
            <a href="/products" class="empty-orders-link">Mua sắm ngay →</a>
          </div>
        </c:when>
        <c:otherwise>
          <div class="order-table-wrap">
            <table class="order-table">
              <thead>
              <tr>
                <th>Mã đơn</th>
                <th>Ngày đặt</th>
                <th>Tổng tiền</th>
                <th>Thanh toán</th>
                <th>Trạng thái</th>
                <th></th>
              </tr>
              </thead>
              <tbody>
              <c:forEach var="order" items="${orders}">
                <tr>
                  <td class="col-id">#${order.id}</td>
                  <td class="col-date">${order.createdAt.toString().substring(0,16).replace("T"," ")}</td>
                  <td class="col-total"><fmt:formatNumber value="${order.totalAmount}" pattern="#,##0"/>đ</td>
                  <td class="col-pay">${order.paymentMethod}</td>
                  <td>
                    <span class="status-badge status-${order.status}${order.status == 'PENDING' && order.isPaid ? ' status-PACKING' : ''}">
                      <c:choose>
                        <c:when test="${order.status == 'PENDING' && order.isPaid}">Đang đóng gói</c:when>
                        <c:when test="${order.status == 'PENDING'}">Chờ xác nhận</c:when>
                        <c:when test="${order.status == 'SHIPPING'}">Đang giao</c:when>
                        <c:when test="${order.status == 'COMPLETED'}">Hoàn thành</c:when>
                        <c:when test="${order.status == 'CANCELLED'}">Đã hủy</c:when>
                        <c:when test="${order.status == 'AWAITING_STOCK'}">Chờ hàng</c:when>
                        <c:otherwise>${order.status}</c:otherwise>
                      </c:choose>
                    </span>
                  </td>
                  <td class="col-action">
                    <a href="/my-orders/${order.id}" class="order-detail-link">Chi tiết</a>
                    <c:if test="${order.status == 'PENDING'}">
                      <a href="/my-orders/${order.id}/cancel" data-post
                         data-confirm="Bạn có chắc muốn hủy đơn này?"
                         class="order-cancel-link">Hủy đơn</a>
                    </c:if>
                    <c:if test="${order.status == 'COMPLETED' && not empty firstProductByOrder[order.id]}">
                      <a href="/products/${firstProductByOrder[order.id]}#reviews"
                         class="order-review-link">Đánh giá</a>
                    </c:if>
                    <c:if test="${coBaoHanh[order.id]}">
                      <a href="/bao-hanh" class="order-warranty-link">Bảo hành</a>
                    </c:if>
                  </td>
                </tr>
              </c:forEach>
              </tbody>
            </table>
          </div>
        </c:otherwise>
      </c:choose>
    </div>
  </div>
</div>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>

<%@ page contentType="text/html;charset=UTF-8" isELIgnored="false" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Tài Khoản - TiTanGEAR"/>
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
        <li><a href="/profile" class="active">Thông tin tài khoản</a></li>
        <li><a href="/my-orders">Đơn hàng của tôi</a></li>
        <li><a href="/bao-hanh">Tra cứu bảo hành</a></li>
        <li><a href="/logout">Đăng xuất</a></li>
      </ul>
    </aside>

    <!-- NỘI DUNG -->
    <div class="profile-content">
      <h1 class="acc-title">Thông tin tài khoản</h1>

      <!-- THÔNG TIN CÁ NHÂN -->
      <div class="acc-card">
        <div class="acc-card-header">
          <h3>Thông tin liên hệ</h3>
          <button type="button" class="btn-edit-outline" onclick="toggleEdit('contact')">Sửa</button>
        </div>

        <c:if test="${param.updated == 'true'}">
          <div class="alert-success">Cập nhật thông tin thành công!</div>
        </c:if>

        <!-- CHẾ ĐỘ XEM -->
        <div id="view-contact">
          <div class="acc-row"><span class="acc-row-label">Họ và tên</span><span class="acc-row-value">${fn:escapeXml(user.fullName)}</span></div>
          <div class="acc-row"><span class="acc-row-label">Email</span><span class="acc-row-value">${fn:escapeXml(user.email)}</span></div>
          <div class="acc-row">
            <span class="acc-row-label">Số điện thoại</span>
            <span class="acc-row-value ${empty user.phone ? 'muted' : ''}">${not empty user.phone ? user.phone : 'Chưa cập nhật'}</span>
          </div>
          <div class="acc-row">
            <span class="acc-row-label">Địa chỉ</span>
            <span class="acc-row-value ${empty user.address ? 'muted' : ''}">${not empty user.address ? user.address : 'Chưa cập nhật'}</span>
          </div>
        </div>

        <!-- CHẾ ĐỘ SỬA (ẩn mặc định) -->
        <div id="edit-contact" style="display:none;">
          <form action="/profile/update" method="post">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
            <div class="form-row">
              <div class="form-group">
                <label>Họ và tên</label>
                <input type="text" name="fullName" value="${fn:escapeXml(user.fullName)}" required/>
              </div>
              <div class="form-group">
                <label>Email</label>
                <input type="email" value="${fn:escapeXml(user.email)}" readonly/>
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>Số điện thoại</label>
                <input type="text" name="phone" value="${fn:escapeXml(user.phone)}" placeholder="Nhập số điện thoại"/>
              </div>
              <div class="form-group">
                <label>Vai trò</label>
                <input type="text" value="${user.role}" readonly/>
              </div>
            </div>
            <div class="form-group">
              <label>Địa chỉ</label>
              <input type="text" name="address" value="${fn:escapeXml(user.address)}" placeholder="Nhập địa chỉ"/>
            </div>
            <div class="acc-edit-actions">
              <button type="submit" class="btn-save">Lưu thay đổi</button>
              <button type="button" class="btn-cancel-outline" onclick="cancelEdit('contact')">Hủy</button>
            </div>
          </form>
        </div>
      </div>

      <!-- ĐỔI MẬT KHẨU -->
      <div class="acc-card" id="password">
        <div class="acc-card-header">
          <h3>Bảo mật</h3>
          <button type="button" class="btn-edit-outline" onclick="toggleEdit('password')">Đổi mật khẩu</button>
        </div>

        <c:if test="${param.passwordChanged == 'true'}">
          <div class="alert-success">Đổi mật khẩu thành công!</div>
        </c:if>
        <c:if test="${not empty passwordError}">
          <div class="alert-error">${passwordError}</div>
        </c:if>

        <!-- CHẾ ĐỘ XEM -->
        <div id="view-password">
          <div class="acc-row"><span class="acc-row-label">Mật khẩu</span><span class="acc-row-value muted">••••••••</span></div>
        </div>

        <!-- CHẾ ĐỘ SỬA -->
        <div id="edit-password" style="display:${not empty passwordError ? 'block' : 'none'};">
          <form action="/profile/change-password" method="post">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
            <div class="form-group">
              <label>Mật khẩu hiện tại</label>
              <input type="password" name="oldPassword" required placeholder="Nhập mật khẩu hiện tại"/>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>Mật khẩu mới</label>
                <input type="password" name="newPassword" required placeholder="Tối thiểu 6 ký tự" minlength="6"/>
              </div>
              <div class="form-group">
                <label>Xác nhận mật khẩu mới</label>
                <input type="password" name="confirmPassword" required placeholder="Nhập lại mật khẩu mới"/>
              </div>
            </div>
            <div class="acc-edit-actions">
              <button type="submit" class="btn-save">Đổi mật khẩu</button>
              <button type="button" class="btn-cancel-outline" onclick="cancelEdit('password')">Hủy</button>
            </div>
          </form>
        </div>
      </div>

    </div>
  </div>
</div>

<script>
  function toggleEdit(section) {
    document.getElementById('view-' + section).style.display = 'none';
    document.getElementById('edit-' + section).style.display = 'block';
  }
  function cancelEdit(section) {
    document.getElementById('edit-' + section).style.display = 'none';
    document.getElementById('view-' + section).style.display = 'block';
  }
</script>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>

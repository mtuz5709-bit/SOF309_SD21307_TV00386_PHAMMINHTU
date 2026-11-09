<%@ page contentType="text/html;charset=UTF-8" %>
<!-- Sidebar admin dùng chung — set ${adminActive} trước khi include -->
<aside class="sidebar">
    <a href="/home" class="sidebar-brand"><img src="/static/images/logo-lockup.svg" alt="TiTanGEAR"/></a>
    <div class="sidebar-label">QUẢN TRỊ</div>
    <nav class="sidebar-nav">
        <a href="/admin/dashboard" class="${adminActive == 'dashboard' ? 'active' : ''}">
            <svg viewBox="0 0 16 16" fill="none"><rect x="2" y="2" width="5.5" height="5.5" stroke="currentColor" stroke-width="1.4"></rect><rect x="8.5" y="2" width="5.5" height="3.5" stroke="currentColor" stroke-width="1.4"></rect><rect x="2" y="8.5" width="5.5" height="5.5" stroke="currentColor" stroke-width="1.4"></rect><rect x="8.5" y="6.5" width="5.5" height="7.5" stroke="currentColor" stroke-width="1.4"></rect></svg>
            <span>Tổng quan</span>
        </a>
        <a href="/admin/products" class="${adminActive == 'products' ? 'active' : ''}">
            <svg viewBox="0 0 16 16" fill="none"><path d="M8 1.5L14 4.5V11.5L8 14.5L2 11.5V4.5L8 1.5Z" stroke="currentColor" stroke-width="1.4" stroke-linejoin="round"></path><path d="M2 4.5L8 7.5L14 4.5M8 7.5V14.5" stroke="currentColor" stroke-width="1.4" stroke-linejoin="round"></path></svg>
            <span>Sản phẩm</span>
        </a>
        <a href="/admin/orders" class="${adminActive == 'orders' ? 'active' : ''}">
            <svg viewBox="0 0 16 16" fill="none"><rect x="3" y="2" width="10" height="12" stroke="currentColor" stroke-width="1.4"></rect><path d="M5.5 5.5H10.5M5.5 8H10.5M5.5 10.5H8.5" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"></path></svg>
            <span>Đơn hàng</span>
            <c:if test="${canhBao.donHang > 0}"><span class="nav-badge">${canhBao.donHang}</span></c:if>
        </a>
        <a href="/admin/report" class="${adminActive == 'report' ? 'active' : ''}">
            <svg viewBox="0 0 16 16" fill="none"><path d="M2 14H14" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"></path><rect x="3" y="8" width="2.6" height="4.5" stroke="currentColor" stroke-width="1.4"></rect><rect x="6.7" y="5" width="2.6" height="7.5" stroke="currentColor" stroke-width="1.4"></rect><rect x="10.4" y="9.5" width="2.6" height="3" stroke="currentColor" stroke-width="1.4"></rect></svg>
            <span>Doanh thu</span>
        </a>
        <a href="/admin/flash-sales" class="${adminActive == 'flashsale' ? 'active' : ''}">
            <svg viewBox="0 0 16 16" fill="none"><path d="M9 1.5L3.5 8.5H7L6.5 14.5L12.5 7H8.5L9 1.5Z" stroke="currentColor" stroke-width="1.4" stroke-linejoin="round"></path></svg>
            <span>Flash Sale</span>
        </a>
        <a href="/admin/combos" class="${adminActive == 'combos' ? 'active' : ''}">
            <svg viewBox="0 0 16 16" fill="none"><path d="M2 5.5L8 2.5L14 5.5L8 8.5L2 5.5Z" stroke="currentColor" stroke-width="1.4" stroke-linejoin="round"></path><path d="M2 8L8 11L14 8" stroke="currentColor" stroke-width="1.4" stroke-linejoin="round"></path><path d="M2 10.5L8 13.5L14 10.5" stroke="currentColor" stroke-width="1.4" stroke-linejoin="round"></path></svg>
            <span>Combo</span>
        </a>
        <a href="/admin/vouchers" class="${adminActive == 'vouchers' ? 'active' : ''}">
            <svg viewBox="0 0 16 16" fill="none"><path d="M2 5.5H14V7a1.5 1.5 0 0 0 0 2v1.5H2V9a1.5 1.5 0 0 0 0-2V5.5Z" stroke="currentColor" stroke-width="1.4" stroke-linejoin="round"></path><path d="M9.5 5.5v5" stroke="currentColor" stroke-width="1.4" stroke-dasharray="1.6 1.6"></path></svg>
            <span>Mã giảm giá</span>
        </a>
        <a href="/admin/warranties" class="${adminActive == 'warranties' ? 'active' : ''}">
            <svg viewBox="0 0 16 16" fill="none"><path d="M8 1.5L13.5 3.5V8C13.5 11.2 11.2 13.7 8 14.5C4.8 13.7 2.5 11.2 2.5 8V3.5L8 1.5Z" stroke="currentColor" stroke-width="1.4" stroke-linejoin="round"></path><path d="M5.8 7.9L7.3 9.4L10.2 6.5" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round"></path></svg>
            <span>Bảo hành</span>
            <c:if test="${canhBao.baoHanh > 0}"><span class="nav-badge">${canhBao.baoHanh}</span></c:if>
        </a>
        <a href="/warehouse/stock" class="${adminActive == 'warehouse' ? 'active' : ''}">
            <svg viewBox="0 0 16 16" fill="none"><path d="M2 6.5L8 2.5L14 6.5V14H2V6.5Z" stroke="currentColor" stroke-width="1.4" stroke-linejoin="round"></path><path d="M5 14V9H11V14" stroke="currentColor" stroke-width="1.4"></path></svg>
            <span>Kho hàng</span>
            <c:if test="${canhBao.khoHang > 0}"><span class="nav-badge">${canhBao.khoHang}</span></c:if>
        </a>
        <a href="/admin/users" class="${adminActive == 'users' ? 'active' : ''}">
            <svg viewBox="0 0 16 16" fill="none"><circle cx="6" cy="5" r="2.5" stroke="currentColor" stroke-width="1.4"></circle><path d="M1.5 14C1.5 11.5 3.5 10 6 10C8.5 10 10.5 11.5 10.5 14" stroke="currentColor" stroke-width="1.4"></path><path d="M11 10C12.9 10 14.5 11.4 14.5 13.5" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"></path></svg>
            <span>Người dùng</span>
        </a>
        <a href="/admin/nhat-ky" class="${adminActive == 'nhatky' ? 'active' : ''}">
            <svg viewBox="0 0 16 16" fill="none"><path d="M3 2.5h10v11H3z" stroke="currentColor" stroke-width="1.4" stroke-linejoin="round"></path><path d="M5.5 5.5h5M5.5 8h5M5.5 10.5h3" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"></path></svg>
            <span>Nhật ký thao tác</span>
        </a>
        <a href="/admin/settings" class="${adminActive == 'settings' ? 'active' : ''}">
            <svg viewBox="0 0 16 16" fill="none"><circle cx="8" cy="8" r="2.4" stroke="currentColor" stroke-width="1.4"></circle><path d="M8 1V3M8 13V15M15 8H13M3 8H1M12.9 3.1L11.5 4.5M4.5 11.5L3.1 12.9M12.9 12.9L11.5 11.5M4.5 4.5L3.1 3.1" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"></path></svg>
            <span>Cài đặt</span>
        </a>
    </nav>
    <div class="sidebar-foot">
        <a href="/logout" class="sidebar-logout">
            <svg viewBox="0 0 16 16" fill="none"><path d="M6 2H3V14H6" stroke="currentColor" stroke-width="1.4"></path><path d="M10 5L13 8L10 11M13 8H6" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round"></path></svg>
            <span>Đăng xuất</span>
        </a>
    </div>
</aside>

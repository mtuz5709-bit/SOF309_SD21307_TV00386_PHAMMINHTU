<%@ page contentType="text/html;charset=UTF-8" %>
<!-- Header admin dùng chung — set ${adminTitle} + ${adminCrumb} trước khi include -->
<div class="admin-topbar">
    <div class="admin-topbar-left">
        <h1 class="admin-topbar-title">${adminTitle}</h1>
        <c:if test="${not empty adminCrumb}">
            <span class="admin-topbar-crumb">${adminCrumb}</span>
        </c:if>
    </div>
    <div class="admin-topbar-right">
        <%-- Ô tìm kiếm CHỈ hiện ở trang thật sự có tìm kiếm, tức trang có set
             ${adminSearchUrl}. Trang không có thì bỏ hẳn ô đi, không hiện ô mờ:
             ô mờ vẫn mời người dùng bấm vào rồi phát hiện gõ không được. --%>
        <c:if test="${not empty adminSearchUrl}">
            <form method="get" action="${adminSearchUrl}" class="admin-search">
                <svg width="13" height="13" viewBox="0 0 16 16" fill="none"><circle cx="7" cy="7" r="5" stroke="currentColor" stroke-width="1.6"></circle><path d="M11 11L15 15" stroke="currentColor" stroke-width="1.6"></path></svg>
                <input type="text" name="q" value="<c:out value='${param.q}'/>"
                       placeholder="${not empty adminSearchPlaceholder ? adminSearchPlaceholder : 'Tìm kiếm…'}"/>
            </form>
        </c:if>
        <c:if test="${not empty adminAction && not empty adminActionUrl}">
            <a href="${adminActionUrl}" class="btn-primary">${adminAction}</a>
        </c:if>
        <%-- Ô avatar hiện chữ cái đầu tên người đang đăng nhập --%>
        <c:choose>
            <c:when test="${not empty me}">
                <div class="admin-avatar" title="${me.ten}"
                     style="background:${me.mau};border-color:${me.mau};">${me.chuCai}</div>
            </c:when>
            <c:otherwise><div class="admin-avatar">AD</div></c:otherwise>
        </c:choose>
    </div>
</div>

<%-- Nạp ở topbar vì topbar được include ở MỌI trang quản trị -> chặn gửi trùng
     cho toàn bộ form admin mà không phải sửa từng trang một. --%>
<script src="/static/js/form-guard.js"></script>

<%-- Token CSRF cho post-link.js đọc. Đặt cạnh script luôn để trang nào nạp
     script thì chắc chắn có token, khỏi phải sửa <head> của từng trang. --%>
<meta name="_csrf" content="${_csrf.token}">
<meta name="_csrf_param" content="${_csrf.parameterName}">
<script src="/static/js/post-link.js"></script>

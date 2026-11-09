package com.titangear.config;

import com.titangear.model.User;
import com.titangear.service.UserService;
import com.titangear.service.WarehousePermissionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

// ĐIỂM KIỂM TRA QUYỀN DUY NHẤT cho các trang admin.
//
// Chạy TRƯỚC mọi controller method có gắn @RequirePermission:
//  1. Đọc annotation trên method (ưu tiên) hoặc trên class.
//  2. Không có annotation → cho qua (Spring Security vẫn chặn URL thô bên ngoài).
//  3. Có annotation → tra role của user đang đăng nhập, hỏi
//     WarehousePermissionService (ADMIN luôn qua; WAREHOUSE tùy cài đặt).
//  4. Không đủ quyền → redirect /403, method controller KHÔNG BAO GIỜ chạy.
//
// Đây là lớp thứ 2 sau Spring Security: SecurityConfig chặn theo URL + ROLE
// (thô, cứng), lớp này chặn theo quyền chi tiết admin bật/tắt được trong
// trang Cài đặt mà không cần build lại code.
@Component
public class PermissionInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;

    @Autowired
    private WarehousePermissionService warehousePermissionService;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        // Chỉ quan tâm request đi vào controller method (bỏ qua static resource...)
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;

        // Annotation trên method ưu tiên hơn trên class
        RequirePermission required = handlerMethod.getMethodAnnotation(RequirePermission.class);
        if (required == null) {
            required = handlerMethod.getBeanType().getAnnotation(RequirePermission.class);
        }
        if (required == null) {
            return true; // method không yêu cầu quyền chi tiết
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String role = null;
        if (auth != null && auth.isAuthenticated()) {
            role = userService.findByEmail(auth.getName())
                    .map(User::getRole)
                    .orElse(null);
        }

        if (role == null || !warehousePermissionService.hasPermission(role, required.value())) {
            response.sendRedirect(request.getContextPath() + "/403");
            return false; // chặn — controller không chạy
        }
        return true;
    }
}

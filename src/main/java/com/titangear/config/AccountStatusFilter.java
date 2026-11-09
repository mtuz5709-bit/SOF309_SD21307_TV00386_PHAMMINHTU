package com.titangear.config;

import com.titangear.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// Kiểm lại trạng thái tài khoản ở MỖI request. Tài khoản đã bị khóa hoặc đã bị xóa
// thì hủy phiên ngay và đá về trang đăng nhập.
// Cần filter này vì Spring Security chỉ kiểm trạng thái một lần lúc đăng nhập, sau
// đó chỉ dựa vào phiên — không có nó thì khóa tài khoản chỉ có hiệu lực sau khi
// người đó tự đăng xuất.
@Component
public class AccountStatusFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(AccountStatusFilter.class);

    @Autowired private UserService userService;

    // Bỏ qua tài nguyên tĩnh và chính trang đăng nhập/đăng xuất để khỏi lặp vô hạn.
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String duongDan = request.getServletPath();
        return duongDan.startsWith("/static/")
                || duongDan.equals("/login")
                || duongDan.equals("/logout");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean daDangNhap = auth != null
                && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken);

        if (daDangNhap) {
            // Không tìm thấy user (đã bị xóa) cũng coi như hết hiệu lực
            boolean conHieuLuc = userService.findByEmail(auth.getName())
                    .map(u -> Boolean.TRUE.equals(u.getIsActive()))
                    .orElse(false);

            if (!conHieuLuc) {
                log.info("Đá phiên đang mở của tài khoản đã bị khóa/xóa: {}", auth.getName());
                new SecurityContextLogoutHandler().logout(request, response, auth);
                response.sendRedirect(request.getContextPath() + "/login?locked=true");
                return;
            }
        }

        chain.doFilter(request, response);
    }
}

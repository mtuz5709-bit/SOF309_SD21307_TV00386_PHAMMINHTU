package com.titangear.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired private AccountStatusFilter accountStatusFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.securityMatcher(new AntPathRequestMatcher("/**"));
        // Đặt SAU bộ lọc đăng nhập để lúc chạy đã có sẵn thông tin người đang đăng nhập
        http.addFilterAfter(accountStatusFilter, UsernamePasswordAuthenticationFilter.class);
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                new AntPathRequestMatcher("/"),
                                new AntPathRequestMatcher("/home"),
                                new AntPathRequestMatcher("/login"),
                                new AntPathRequestMatcher("/register"),
                                new AntPathRequestMatcher("/verify-otp"),
                                new AntPathRequestMatcher("/resend-otp"),
                                new AntPathRequestMatcher("/quen-mat-khau"),
                                new AntPathRequestMatcher("/quen-mat-khau/**"),
                                new AntPathRequestMatcher("/products/**"),
                                new AntPathRequestMatcher("/flash-sale"),
                                new AntPathRequestMatcher("/so-sanh"),
                                // CHỈ mở công khai phần XEM. "/combo/**" trần sẽ mở
                                // luôn POST /combo/{id}/mua cho người chưa đăng nhập
                                // -> controller nhận Authentication = null -> lỗi 500.
                                // Giới hạn theo GET để nút "Mua trọn bộ" rơi vào nhánh
                                // bắt đăng nhập như các thao tác mua khác.
                                new AntPathRequestMatcher("/combo", "GET"),
                                new AntPathRequestMatcher("/combo/*", "GET"),
                                new AntPathRequestMatcher("/bao-hanh"),
                                // Ba trang pháp lý PHẢI mở công khai: khách cần đọc
                                // được điều khoản TRƯỚC khi quyết định đăng ký tài khoản.
                                new AntPathRequestMatcher("/dieu-khoan"),
                                new AntPathRequestMatcher("/chinh-sach-bao-mat"),
                                new AntPathRequestMatcher("/chinh-sach-bao-hanh"),
                                new AntPathRequestMatcher("/static/**"),
                                // Webhook do SePay gọi từ ngoài -> phải mở, đã tự xác thực
                                // bằng API key + đối chiếu số tiền trong controller.
                                new AntPathRequestMatcher("/api/payment/webhook"),
                                // Danh sách này KHÔNG có /api/payment/check: đường đó bắt buộc
                                // đăng nhập và chỉ trả lời cho chủ đơn. Mở công khai thì ai đổi
                                // số trên URL cũng dò được đơn nào đã thanh toán.
                                new AntPathRequestMatcher("/api/search")
                        ).permitAll()
                        .requestMatchers(
                                new AntPathRequestMatcher("/admin/products")
                        ).hasAnyRole("ADMIN", "WAREHOUSE")
                        .requestMatchers(
                                new AntPathRequestMatcher("/admin/report")
                        ).hasAnyRole("ADMIN", "WAREHOUSE")
                        .requestMatchers(
                                new AntPathRequestMatcher("/admin/orders/**")
                        ).hasAnyRole("ADMIN", "WAREHOUSE")
                        .requestMatchers(
                                new AntPathRequestMatcher("/admin/**")
                        ).hasRole("ADMIN")
                        .requestMatchers(
                                new AntPathRequestMatcher("/warehouse/**")
                        ).hasAnyRole("ADMIN", "WAREHOUSE")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/dashboard", false)
                        // Tách riêng hai loại lỗi đăng nhập: tài khoản bị khóa đưa về
                        // ?locked=true, sai mật khẩu đưa về ?error=true — để không báo
                        // nhầm "sai mật khẩu" cho người đang bị khóa.
                        .failureHandler((req, res, ex) -> {
                            String dich = (ex instanceof DisabledException)
                                    ? "/login?locked=true"
                                    : "/login?error=true";
                            res.sendRedirect(req.getContextPath() + dich);
                        })
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        // Khi CSRF bật, Spring Security mặc định chỉ nhận logout qua POST.
                        // Toàn bộ giao diện đang dùng link GET /logout (10+ trang), nên
                        // khai báo rõ matcher chấp nhận GET. Đánh đổi: kẻ xấu có thể ép
                        // người dùng bị đăng xuất (phiền, không mất dữ liệu) — chấp nhận
                        // được so với việc sửa 10+ trang thành form POST.
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                        .logoutSuccessUrl("/login?logout=true")
                        .permitAll()
                )
                .exceptionHandling(ex -> ex
                        .accessDeniedPage("/403")
                )
                // Bật chống CSRF cho toàn site: mọi form POST phải kèm token, đã nhúng
                // sẵn trong các JSP. Chỉ miễn trừ webhook SePay vì server bên ngoài gọi
                // tới không thể có token của mình — endpoint đó tự bảo vệ bằng API key.
                .csrf(csrf -> csrf.ignoringRequestMatchers(
                        new AntPathRequestMatcher("/api/payment/webhook")
                ));
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            HttpSecurity http,
            UserDetailsService userDetailsService) throws Exception {
        AuthenticationManagerBuilder builder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        builder.userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
        return builder.build();
    }
}
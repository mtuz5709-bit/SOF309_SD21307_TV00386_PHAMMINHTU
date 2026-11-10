package com.poly.sof309.controllers;

import com.titangear.controller.AuthController;
import com.titangear.exception.LoiNghiepVu;
import com.titangear.service.OtpService;
import com.titangear.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.ui.Model;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * =========================================================================
 * [Y3] BÀI TEST KIỂM THỬ ĐƠN VỊ CHO CHỨC NĂNG XÁC THỰC (AUTHCONTROLLER)
 * - @BeforeEach: Khởi tạo môi trường độc lập trước mỗi ca test.
 * - @ParameterizedTest + @CsvSource: Kiểm tra các giá trị biên Form đăng ký.
 * - Assertions: assertEquals, assertTrue, assertFalse, assertThrows, assertNotNull.
 * =========================================================================
 */
@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private OtpService otpService;

    @Mock
    private HttpSession session;

    @Mock
    private Model model;

    @Mock
    private Authentication auth;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    public void setUp() {
        // Nguyên tắc Slide 1: Giữ mỗi ca test độc lập (Isolated)
    }

    @Test
    @DisplayName("TC_AUTH_01: Đăng nhập thành công với Admin chuyển hướng về /admin/dashboard")
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void testLoginSuccessWithAdminRole() {
        Collection authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        when(auth.getAuthorities()).thenReturn(authorities);

        String redirectUrl = authController.dashboard(auth);

        assertNotNull(redirectUrl);
        assertEquals("redirect:/admin/dashboard", redirectUrl, "Admin đăng nhập phải chuyển về /admin/dashboard");
    }

    @Test
    @DisplayName("TC_AUTH_02: Mở trang đăng nhập trả về đúng view common/login")
    public void testLoginViewPage() {
        String viewName = authController.login();
        assertEquals("common/login", viewName, "View đăng nhập phải là common/login");
    }

    @ParameterizedTest
    @DisplayName("TC_AUTH_03: Kiểm tra biên validation form đăng ký bằng @ParameterizedTest & @CsvSource")
    @CsvSource({
            "'Phạm Minh Tu', 'email_sai_dinh_dang', '0912345678', '123456', 'Email không hợp lệ!'",
            "'Phạm Minh Tu', 'minhtu@gmail.com', '09123', '123456', 'Số điện thoại phải đúng 10 chữ số!'",
            "'Phạm Minh Tu', 'minhtu@gmail.com', '0912345678', '123', 'Mật khẩu phải có ít nhất 6 ký tự!'",
            "'A', 'minhtu@gmail.com', '0912345678', '123456', 'Họ tên phải có ít nhất 2 ký tự!'"
    })
    public void testRegisterFormBoundaryValidation(
            String fullName,
            String email,
            String phone,
            String password,
            String expectedError) {

        String view = authController.register(fullName, email, phone, password, session, model);

        assertEquals("customer/register", view, "Khi dữ liệu sai biên phải giữ lại trang customer/register");
        verify(model).addAttribute("error", expectedError);
    }

    @Test
    @DisplayName("TC_AUTH_04: Đăng ký người dùng mới hợp lệ thành công và gửi mã OTP")
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    public void testRegisterNewUserSuccess() {
        when(userService.emailExists("minhtu@gmail.com")).thenReturn(false);
        when(otpService.generateOtp()).thenReturn("123456");

        String view = authController.register(
                "Phạm Minh Tu",
                "minhtu@gmail.com",
                "0912345678",
                "123456",
                session,
                model
        );

        assertEquals("redirect:/verify-otp", view, "Đăng ký hợp lệ phải chuyển sang trang xác thực OTP");
        verify(session).setAttribute(eq("pending_email"), eq("minhtu@gmail.com"));
        verify(session).setAttribute(eq("pending_otp"), eq("123456"));
    }

    @Test
    @DisplayName("TC_AUTH_05: Đăng ký thất bại khi trùng email đã có trong hệ thống")
    public void testRegisterDuplicateEmailFailed() {
        when(userService.emailExists("admin@titangear.vn")).thenReturn(true);

        String view = authController.register(
                "Phạm Minh Tu",
                "admin@titangear.vn",
                "0912345678",
                "123456",
                session,
                model
        );

        assertEquals("customer/register", view);
        verify(model).addAttribute("error", "Email đã được sử dụng!");
    }

    @Test
    @DisplayName("TC_CART_03: Xác thực mã OTP 6 số hợp lệ thành công")
    public void testVerifyOtpSuccess() {
        when(session.getAttribute("pending_otp")).thenReturn("123456");
        when(session.getAttribute("pending_otp_time")).thenReturn(LocalDateTime.now());
        when(session.getAttribute("pending_email")).thenReturn("minhtu@gmail.com");
        when(session.getAttribute("pending_fullName")).thenReturn("Phạm Minh Tu");
        when(session.getAttribute("pending_phone")).thenReturn("0912345678");
        when(session.getAttribute("pending_password")).thenReturn("123456");

        String view = authController.verifyOtp("123456", session, model);

        assertEquals("redirect:/login?registered=true", view);
        verify(userService).register(any());
    }

    @Test
    @DisplayName("TC_AUTH_EX: Kiểm thử ngoại lệ assertThrows khi gửi OTP thất bại hoặc vi phạm nghiệp vụ")
    public void testSendOtpExceptionThrows() {
        doThrow(new LoiNghiepVu("Không thể gửi mã OTP tới email này"))
                .when(otpService).sendOtp(eq("error@gmail.com"), eq("123456"));

        LoiNghiepVu ex = assertThrows(LoiNghiepVu.class, () -> {
            otpService.sendOtp("error@gmail.com", "123456");
        });

        assertTrue(ex.getMessage().contains("OTP"), "Thông điệp ngoại lệ phải chứa nguyên nhân lỗi OTP");
    }
}

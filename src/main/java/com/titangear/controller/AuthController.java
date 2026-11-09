package com.titangear.controller;

import com.titangear.model.User;
import com.titangear.service.OtpService;
import com.titangear.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@Controller
public class AuthController {

    // Số lần gõ sai OTP tối đa cho mỗi mã. Vượt là huỷ mã, phải xin mã mới.
    private static final int MAX_OTP_SAI = 5;

    @Autowired private UserService userService;
    @Autowired private OtpService otpService;

    @GetMapping("/login")
    public String login() {
        return "common/login";
    }

    // ── BƯỚC 1: Hiển thị form đăng ký ──
    @GetMapping("/register")
    public String registerPage() {
        return "customer/register";
    }

    // ── BƯỚC 2: Submit form → validate → gửi OTP → chuyển sang trang verify ──
    @PostMapping("/register")
    public String register(
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam String password,
            HttpSession session,
            Model model) {

        // Validate
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            model.addAttribute("error", "Email không hợp lệ!");
            return "customer/register";
        }
        if (!phone.isEmpty() && !phone.matches("[0-9]{10}")) {
            model.addAttribute("error", "Số điện thoại phải đúng 10 chữ số!");
            return "customer/register";
        }
        if (password.length() < 6) {
            model.addAttribute("error", "Mật khẩu phải có ít nhất 6 ký tự!");
            return "customer/register";
        }
        if (fullName.trim().length() < 2) {
            model.addAttribute("error", "Họ tên phải có ít nhất 2 ký tự!");
            return "customer/register";
        }
        if (userService.emailExists(email)) {
            model.addAttribute("error", "Email đã được sử dụng!");
            return "customer/register";
        }

        // Tạo OTP và gửi email
        String otp = otpService.generateOtp();
        try {
            otpService.sendOtp(email.toLowerCase().trim(), otp);
        } catch (Exception e) {
            model.addAttribute("error", "Không thể gửi email xác nhận. Kiểm tra lại email hoặc thử lại sau.");
            return "customer/register";
        }

        // Lưu thông tin tạm vào session (chưa lưu DB)
        session.setAttribute("pending_fullName", fullName.trim());
        session.setAttribute("pending_email",    email.toLowerCase().trim());
        session.setAttribute("pending_phone",    phone.trim());
        session.setAttribute("pending_password", password);
        session.setAttribute("pending_otp",      otp);
        session.setAttribute("pending_otp_time", LocalDateTime.now());
        session.setAttribute("pending_otp_sai",  0);   // mã mới -> đếm lại từ đầu

        return "redirect:/verify-otp";
    }

    // ── BƯỚC 3: Hiển thị trang nhập OTP ──
    @GetMapping("/verify-otp")
    public String verifyOtpPage(HttpSession session, Model model) {
        // Nếu không có session pending → về trang đăng ký
        if (session.getAttribute("pending_email") == null) {
            return "redirect:/register";
        }
        model.addAttribute("email", session.getAttribute("pending_email"));
        return "customer/verify-otp";
    }

    // ── BƯỚC 4: Submit OTP → kiểm tra → lưu user ──
    @PostMapping("/verify-otp")
    public String verifyOtp(
            @RequestParam String otp,
            HttpSession session,
            Model model) {

        String savedOtp    = (String)        session.getAttribute("pending_otp");
        LocalDateTime sentAt = (LocalDateTime) session.getAttribute("pending_otp_time");
        String email       = (String)        session.getAttribute("pending_email");

        if (savedOtp == null || email == null) {
            return "redirect:/register";
        }

        // Kiểm tra hết hạn 5 phút
        if (LocalDateTime.now().isAfter(sentAt.plusMinutes(5))) {
            session.removeAttribute("pending_otp");
            model.addAttribute("email", email);
            model.addAttribute("error", "Mã OTP đã hết hạn. Vui lòng đăng ký lại.");
            model.addAttribute("expired", true);
            return "customer/verify-otp";
        }

        // Kiểm tra OTP sai — CÓ GIỚI HẠN SỐ LẦN THỬ.
        // Mã 6 số, hạn 5 phút: không đếm lần sai thì viết script gõ vài nghìn lần
        // là dò ra. Cùng luật với luồng quên mật khẩu.
        if (!savedOtp.equals(otp.trim())) {
            Object daSai = session.getAttribute("pending_otp_sai");
            int soLanSai = (daSai instanceof Integer ? (Integer) daSai : 0) + 1;
            session.setAttribute("pending_otp_sai", soLanSai);
            model.addAttribute("email", email);

            if (soLanSai >= MAX_OTP_SAI) {
                session.removeAttribute("pending_otp");
                session.removeAttribute("pending_otp_time");
                session.removeAttribute("pending_otp_sai");
                model.addAttribute("error", "Bạn đã nhập sai quá " + MAX_OTP_SAI
                        + " lần. Vui lòng đăng ký lại để nhận mã mới.");
                model.addAttribute("expired", true);
                return "customer/verify-otp";
            }
            model.addAttribute("error", "Mã xác nhận không đúng (còn "
                    + (MAX_OTP_SAI - soLanSai) + " lần thử).");
            return "customer/verify-otp";
        }
        session.removeAttribute("pending_otp_sai");

        // OTP đúng → lưu user vào DB
        User user = new User();
        user.setFullName((String) session.getAttribute("pending_fullName"));
        user.setEmail(email);
        user.setPhone((String)    session.getAttribute("pending_phone"));
        user.setPasswordHash((String) session.getAttribute("pending_password"));
        userService.register(user);

        // Xóa session pending
        session.removeAttribute("pending_fullName");
        session.removeAttribute("pending_email");
        session.removeAttribute("pending_phone");
        session.removeAttribute("pending_password");
        session.removeAttribute("pending_otp");
        session.removeAttribute("pending_otp_time");

        return "redirect:/login?registered=true";
    }

    // ── Gửi lại OTP ──
    @PostMapping("/resend-otp")
    public String resendOtp(HttpSession session, Model model) {
        String email = (String) session.getAttribute("pending_email");
        if (email == null) return "redirect:/register";

        String newOtp = otpService.generateOtp();
        try {
            otpService.sendOtp(email, newOtp);
        } catch (Exception e) {
            model.addAttribute("email", email);
            model.addAttribute("error", "Không thể gửi lại email. Vui lòng thử lại sau.");
            return "customer/verify-otp";
        }

        session.setAttribute("pending_otp",      newOtp);
        session.setAttribute("pending_otp_time", LocalDateTime.now());

        return "redirect:/verify-otp?resent=true";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth) {
        if (auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            // Admin vào thẳng trang Tổng quan (dashboard) — nhìn được toàn cảnh
            // trước khi đi vào từng khu vực quản lý.
            return "redirect:/admin/dashboard";
        }
        if (auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_WAREHOUSE"))) {
            return "redirect:/warehouse/stock";
        }
        return "redirect:/home";
    }
}
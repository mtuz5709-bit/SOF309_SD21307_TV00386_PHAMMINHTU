package com.titangear.controller;

import com.titangear.model.User;
import com.titangear.service.OtpService;
import com.titangear.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

// QUÊN MẬT KHẨU — 3 bước: nhập email → nhập mã OTP gửi về mail → đặt mật khẩu mới.
//
// Dùng lại đúng cơ chế OTP của đăng ký (lưu tạm trong session, hết hạn 5 phút)
// nên KHÔNG phải thêm bảng nào vào CSDL.
//
// Nguyên tắc bảo mật áp dụng ở đây:
//  - Không tiết lộ email có tồn tại hay không: nhập email lạ vẫn sang bước nhập
//    mã như thường. Nếu báo "email không tồn tại" thì kẻ xấu dò được ai đã có
//    tài khoản ở shop.
//  - Tài khoản bị khóa cũng không gửi mã (đằng nào cũng không đăng nhập được).
//  - Phải nhập đúng OTP mới mở được bước đặt mật khẩu (cờ trong session), không
//    thể gõ thẳng URL bước 3 để nhảy cóc.
@Controller
public class ForgotPasswordController {

    private static final Logger log = LoggerFactory.getLogger(ForgotPasswordController.class);

    private static final String S_EMAIL    = "reset_email";
    private static final String S_OTP      = "reset_otp";
    private static final String S_OTP_TIME = "reset_otp_time";
    private static final String S_DA_XAC   = "reset_da_xac_thuc";
    private static final String S_SO_LAN_SAI = "reset_so_lan_sai";
    // Số lần gõ sai tối đa cho MỖI mã. Vượt là huỷ mã, phải xin mã mới.
    private static final int MAX_LAN_SAI = 5;
    private static final int    HAN_PHUT   = 5;

    @Autowired private UserService userService;
    @Autowired private OtpService otpService;

    // ── BƯỚC 1: nhập email ──
    @GetMapping("/quen-mat-khau")
    public String nhapEmail() {
        return "customer/forgot-password";
    }

    @PostMapping("/quen-mat-khau")
    public String guiMa(@RequestParam String email, HttpSession session, Model model) {
        String mail = email == null ? "" : email.toLowerCase().trim();
        if (mail.isEmpty()) {
            model.addAttribute("error", "Vui lòng nhập email.");
            return "customer/forgot-password";
        }

        User u = userService.findByEmail(mail).orElse(null);
        boolean guiDuoc = u != null && Boolean.TRUE.equals(u.getIsActive());

        session.setAttribute(S_EMAIL, mail);
        session.removeAttribute(S_OTP);
        session.removeAttribute(S_DA_XAC);

        if (guiDuoc) {
            String otp = otpService.generateOtp();
            try {
                otpService.sendOtpDatLaiMatKhau(mail, otp);
                session.setAttribute(S_SO_LAN_SAI, 0);   // mã mới -> đếm lại từ đầu
                session.setAttribute(S_OTP, otp);
                session.setAttribute(S_OTP_TIME, LocalDateTime.now());
            } catch (Exception e) {
                log.error("Không gửi được mã đặt lại mật khẩu tới {}", mail, e);
                model.addAttribute("error", "Không gửi được email lúc này. Vui lòng thử lại sau.");
                return "customer/forgot-password";
            }
        } else {
            // Email lạ hoặc tài khoản bị khóa: vẫn đi tiếp như bình thường để
            // người ngoài không đoán được email nào có tài khoản
            log.info("Yêu cầu đặt lại mật khẩu cho email không hợp lệ/bị khóa: {}", mail);
        }

        return "redirect:/quen-mat-khau/xac-nhan";
    }

    // ── BƯỚC 2: nhập mã OTP ──
    @GetMapping("/quen-mat-khau/xac-nhan")
    public String nhapMa(HttpSession session, Model model) {
        String mail = (String) session.getAttribute(S_EMAIL);
        if (mail == null) return "redirect:/quen-mat-khau";
        model.addAttribute("email", mail);
        return "customer/forgot-otp";
    }

    @PostMapping("/quen-mat-khau/xac-nhan")
    public String kiemTraMa(@RequestParam String otp, HttpSession session, Model model) {
        String mail    = (String) session.getAttribute(S_EMAIL);
        String maDung  = (String) session.getAttribute(S_OTP);
        LocalDateTime luc = (LocalDateTime) session.getAttribute(S_OTP_TIME);
        if (mail == null) return "redirect:/quen-mat-khau";

        model.addAttribute("email", mail);

        if (maDung == null || luc == null) {
            // Email lạ (không hề gửi mã) rơi vào đây — báo y hệt trường hợp gõ sai
            model.addAttribute("error", "Mã xác nhận không đúng. Vui lòng kiểm tra lại email.");
            return "customer/forgot-otp";
        }
        if (LocalDateTime.now().isAfter(luc.plusMinutes(HAN_PHUT))) {
            session.removeAttribute(S_OTP);
            model.addAttribute("error", "Mã đã hết hạn. Vui lòng yêu cầu mã mới.");
            return "customer/forgot-otp";
        }
        if (!maDung.equals(otp == null ? "" : otp.trim())) {
            // GIỚI HẠN SỐ LẦN THỬ. Mã 6 số có 1 triệu tổ hợp nhưng hạn tới 5 phút:
            // không đếm lần sai thì viết script gõ vài nghìn lần là dò ra, mà dò
            // trúng là ĐỔI ĐƯỢC MẬT KHẨU của người khác.
            int soLanSai = (int) session.getAttribute(S_SO_LAN_SAI) + 1;
            session.setAttribute(S_SO_LAN_SAI, soLanSai);
            if (soLanSai >= MAX_LAN_SAI) {
                // Huỷ mã luôn, bắt yêu cầu mã mới — mỗi mã chỉ chịu được 5 lần đoán
                session.removeAttribute(S_OTP);
                session.removeAttribute(S_OTP_TIME);
                session.removeAttribute(S_SO_LAN_SAI);
                model.addAttribute("error",
                        "Bạn đã nhập sai quá " + MAX_LAN_SAI + " lần. Mã này đã bị huỷ, vui lòng yêu cầu mã mới.");
                return "customer/forgot-otp";
            }
            model.addAttribute("error", "Mã xác nhận không đúng (còn "
                    + (MAX_LAN_SAI - soLanSai) + " lần thử). Vui lòng kiểm tra lại email.");
            return "customer/forgot-otp";
        }
        session.removeAttribute(S_SO_LAN_SAI);

        // Đúng mã: mở khoá bước đặt mật khẩu, đồng thời huỷ mã để không dùng lại
        session.setAttribute(S_DA_XAC, Boolean.TRUE);
        session.removeAttribute(S_OTP);
        return "redirect:/quen-mat-khau/dat-lai";
    }

    // ── BƯỚC 3: đặt mật khẩu mới ──
    @GetMapping("/quen-mat-khau/dat-lai")
    public String formDatLai(HttpSession session, Model model) {
        if (!Boolean.TRUE.equals(session.getAttribute(S_DA_XAC))) {
            return "redirect:/quen-mat-khau";
        }
        model.addAttribute("email", session.getAttribute(S_EMAIL));
        return "customer/reset-password";
    }

    @PostMapping("/quen-mat-khau/dat-lai")
    public String datLai(@RequestParam String matKhau,
                         @RequestParam String matKhauLai,
                         HttpSession session, Model model) {

        if (!Boolean.TRUE.equals(session.getAttribute(S_DA_XAC))) {
            return "redirect:/quen-mat-khau";
        }
        String mail = (String) session.getAttribute(S_EMAIL);
        model.addAttribute("email", mail);

        if (matKhau == null || matKhau.length() < 6) {
            model.addAttribute("error", "Mật khẩu phải có ít nhất 6 ký tự.");
            return "customer/reset-password";
        }
        if (!matKhau.equals(matKhauLai)) {
            model.addAttribute("error", "Hai lần nhập mật khẩu không giống nhau.");
            return "customer/reset-password";
        }
        if (!userService.datLaiMatKhau(mail, matKhau)) {
            model.addAttribute("error", "Không đặt lại được mật khẩu. Vui lòng thử lại.");
            return "customer/reset-password";
        }

        log.info("Đã đặt lại mật khẩu cho {}", mail);
        session.removeAttribute(S_EMAIL);
        session.removeAttribute(S_OTP);
        session.removeAttribute(S_OTP_TIME);
        session.removeAttribute(S_DA_XAC);
        return "redirect:/login?reset=true";
    }
}

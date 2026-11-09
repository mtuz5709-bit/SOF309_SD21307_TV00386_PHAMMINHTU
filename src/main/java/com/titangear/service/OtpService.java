package com.titangear.service;

import com.titangear.exception.LoiNghiepVu;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpService.class);

    @Autowired
    private JavaMailSender mailSender;

    // Tạo OTP 6 số ngẫu nhiên
    public String generateOtp() {
        return String.format("%06d", new Random().nextInt(1000000));
    }

    // Gửi OTP qua email
    public void sendOtp(String toEmail, String otp) {
        gui(toEmail, otp, "TiTanGEAR – Mã xác nhận đăng ký", "Mã xác nhận đăng ký tài khoản của bạn:");
    }

    // OTP cho luồng quên mật khẩu — tách tiêu đề/nội dung riêng, vì email báo
    // "xác nhận đăng ký" khi người ta đang đặt lại mật khẩu sẽ làm khách hoang mang
    // (tưởng có ai đó đăng ký tài khoản bằng email của mình).
    public void sendOtpDatLaiMatKhau(String toEmail, String otp) {
        gui(toEmail, otp, "TiTanGEAR – Mã đặt lại mật khẩu",
                "Mã xác nhận để đặt lại mật khẩu của bạn:");
    }

    private void gui(String toEmail, String otp, String tieuDe, String loiDan) {
        try {
            log.info("Đang gửi OTP tới: {}", toEmail);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject(tieuDe);
            helper.setText(buildEmailBody(otp, loiDan), true);

            mailSender.send(message);
            log.info("Gửi OTP thành công tới: {}", toEmail);
        } catch (Exception e) {
            log.error("Lỗi gửi OTP tới {}", toEmail, e);
            throw new LoiNghiepVu("Không thể gửi email OTP: " + e.getMessage());
        }
    }

    private String buildEmailBody(String otp, String loiDan) {
        return """
            <div style="font-family:Arial,sans-serif;max-width:480px;margin:0 auto;padding:32px;border:1px solid #eee;border-radius:8px;">
                <h2 style="color:#DB4444;margin-bottom:8px;">TiTanGEAR</h2>
                <p style="color:#333;font-size:15px;">%s</p>
                <div style="font-size:36px;font-weight:700;letter-spacing:8px;color:#DB4444;text-align:center;padding:20px 0;">
                    %s
                </div>
                <p style="color:#777;font-size:13px;">Mã có hiệu lực trong <strong>5 phút</strong>. Không chia sẻ mã này cho bất kỳ ai.</p>
                <p style="color:#aaa;font-size:12px;margin-top:24px;">Nếu bạn không thực hiện yêu cầu này, hãy bỏ qua email.</p>
            </div>
            """.formatted(loiDan, otp);
    }
}
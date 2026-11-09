package com.titangear.service;

import com.titangear.model.Product;
import com.titangear.model.RestockNotification;
import com.titangear.repository.RestockNotificationRepository;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class RestockNotificationService {

    private static final Logger log = LoggerFactory.getLogger(RestockNotificationService.class);

    @Autowired private RestockNotificationRepository repo;
    @Autowired private JavaMailSender mailSender;
    @Autowired private ProductService productService;
    @Autowired private ProductVariantService variantService;

    /** Địa chỉ gốc của site dùng cho link trong email. Có mặc định để chạy được
        ngay khi dev; khi deploy thật khai app.base-url trong application.properties. */
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    // Dạng email tối thiểu. Không kiểm thì khách gõ "abc" vẫn lưu được, rồi CỨ
    // MỖI LẦN nhập kho hệ thống lại thử gửi cho địa chỉ đó và thất bại — vì cờ
    // notified chỉ bật khi gửi thành công nên bản ghi hỏng nằm lại vĩnh viễn,
    // mỗi lần nhập kho lại tốn thêm một lượt bắt tay SMTP vô ích.
    private static final java.util.regex.Pattern DANG_EMAIL =
            java.util.regex.Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");

    // Khách đăng ký nhận email khi hàng về (bỏ qua nếu trống hoặc đã đăng ký chưa gửi).
    public void subscribe(Integer productId, Integer variantId, String email) {
        if (email == null || email.trim().isEmpty()) return;
        String e = email.trim();
        if (!DANG_EMAIL.matcher(e).matches()) {
            throw new com.titangear.exception.LoiNghiepVu("Email \"" + e + "\" không đúng định dạng.");
        }
        // Chống đăng ký trùng — xét đúng nhóm (chờ 1 biến thể vs chờ sản phẩm chung)
        boolean daCo = (variantId != null)
                ? repo.existsByProductIdAndVariantIdAndEmailAndNotifiedFalse(productId, variantId, e)
                : repo.existsByProductIdAndVariantIdIsNullAndEmailAndNotifiedFalse(productId, e);
        if (daCo) return;

        RestockNotification n = new RestockNotification();
        n.setProductId(productId);
        n.setVariantId(variantId);
        n.setEmail(e);
        n.setNotified(false);
        n.setCreatedAt(LocalDateTime.now());
        repo.save(n);
    }

    // Hàng về -> gửi email cho người đang chờ, rồi đánh dấu đã gửi.
    //   - Nhập kho CÓ biến thể: báo cho người chờ ĐÚNG biến thể đó + người chờ
    //     sản phẩm nói chung (variant NULL).
    //   - Nhập kho mức sản phẩm: chỉ báo nhóm chờ chung.
    // Lỗi mail của 1 người KHÔNG chặn người khác và KHÔNG làm hỏng luồng nhập kho
    // (đánh dấu notified chỉ khi gửi thành công -> lần sau còn thử lại).
    @org.springframework.scheduling.annotation.Async
    @Transactional
    public void notifyRestock(Integer productId, Integer variantId) {
        List<RestockNotification> subs = new ArrayList<>(
                repo.findByProductIdAndVariantIdIsNullAndNotifiedFalse(productId));
        if (variantId != null) {
            subs.addAll(repo.findByProductIdAndVariantIdAndNotifiedFalse(productId, variantId));
        }
        if (subs.isEmpty()) return;

        String productName = productService.findById(productId)
                .map(Product::getName).orElse("sản phẩm bạn quan tâm");

        // Tên hiển thị kèm biến thể, vd: "Chuột ATK F1 Ultimate – Đỏ"
        String tenHienThi = productName;
        if (variantId != null) {
            tenHienThi = variantService.findById(variantId)
                    .map(v -> productName + " – " + v.getName()
                            + (v.getColor() != null ? " " + v.getColor() : ""))
                    .orElse(productName);
        }

        for (RestockNotification n : subs) {
            try {
                sendEmail(n.getEmail(), productId, tenHienThi);
                n.setNotified(true);
                repo.save(n);
            } catch (Exception ex) {
                log.error("Lỗi gửi mail báo hàng về tới {}", n.getEmail(), ex);
            }
        }
    }

    private void sendEmail(String to, Integer productId, String tenHienThi) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(to);
        helper.setSubject("TiTanGEAR – \"" + tenHienThi + "\" đã có hàng trở lại!");
        helper.setText(buildBody(productId, tenHienThi), true);
        mailSender.send(message);
    }

    private String buildBody(Integer productId, String productName) {
        // Link tuyệt đối cho email — lấy từ cấu hình app.base-url (không hardcode)
        String url = baseUrl + "/products/" + productId;
        return """
            <div style="font-family:Arial,sans-serif;max-width:480px;margin:0 auto;padding:32px;border:1px solid #eee;border-radius:8px;">
                <h2 style="color:#1B49FF;margin-bottom:8px;">TiTanGEAR</h2>
                <p style="color:#333;font-size:15px;">Tin vui! Sản phẩm bạn quan tâm đã có hàng trở lại:</p>
                <p style="font-size:18px;font-weight:700;color:#14171A;margin:12px 0;">%s</p>
                <a href="%s" style="display:inline-block;background:#1B49FF;color:#fff;text-decoration:none;padding:12px 24px;font-weight:600;">Xem &amp; đặt ngay</a>
                <p style="color:#aaa;font-size:12px;margin-top:24px;">Bạn nhận email này vì đã đăng ký thông báo khi hàng về. Hãy nhanh tay kẻo hết!</p>
            </div>
            """.formatted(productName, url);
    }
}

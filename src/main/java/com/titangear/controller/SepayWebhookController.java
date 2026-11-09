package com.titangear.controller;

import com.titangear.model.User;
import com.titangear.service.OrderService;
import com.titangear.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
public class SepayWebhookController {

    private static final Logger log = LoggerFactory.getLogger(SepayWebhookController.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    // Đọc từ application.properties (không hardcode secret vào Git nữa)
    @org.springframework.beans.factory.annotation.Value("${sepay.api-key}")
    private String apiKey;

    // Sepay gọi endpoint này khi có tiền vào
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> body) {

        // Xác thực API Key
        if (authHeader == null || !authHeader.equals("Apikey " + apiKey)) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        try {
            String content = body.getOrDefault("content", "").toString();
            String transferType = body.getOrDefault("transferType", "").toString();

            // Chỉ xử lý tiền vào
            if (!"in".equals(transferType)) {
                return ResponseEntity.ok("OK");
            }

            // Tìm mã đơn hàng trong nội dung CK: "TiTanGEAR Don 15"
            String contentLower = content.toLowerCase();
            if (contentLower.contains("titangear don")) {
                String[] parts = content.split("(?i)titangear don");
                if (parts.length > 1) {
                    String orderIdStr = parts[1].trim().split("[\\s.]+")[0];
                    try {
                        Integer orderId = Integer.parseInt(orderIdStr);

                        // Đối chiếu số tiền thật đã chuyển với tổng đơn. Thiếu bước này thì
                        // chuyển 10.000đ cho đơn 3.270.000đ cũng được đánh dấu đã thanh toán,
                        // mà cú pháp nội dung chuyển khoản in ngay trên trang QR ai cũng thấy.
                        java.math.BigDecimal daChuyen = docSoTien(body.get("transferAmount"));
                        java.math.BigDecimal phaiTra = orderService.findById(orderId)
                                .map(o -> o.getTotalAmount()).orElse(null);

                        if (phaiTra == null) {
                            log.warn("Sepay webhook: không tìm thấy đơn #{} nêu trong nội dung CK", orderId);
                        } else if (daChuyen == null || daChuyen.compareTo(phaiTra) < 0) {
                            // KHÔNG đánh dấu đã trả. Ghi log để admin đối soát tay —
                            // có thể khách chuyển thiếu, hoặc chuyển làm nhiều lần.
                            log.warn("Sepay webhook: đơn #{} nhận {}đ nhưng cần {}đ -> CHƯA xác nhận, cần đối soát tay",
                                    orderId, daChuyen, phaiTra);
                        } else {
                            orderService.confirmPayment(orderId);
                            log.info("Sepay webhook: đã xác nhận thanh toán đơn #{} ({}đ)", orderId, daChuyen);
                        }
                    } catch (NumberFormatException e) {
                        log.warn("Sepay webhook: không đọc được mã đơn từ nội dung CK: {}", content);
                    }
                }
            }

            return ResponseEntity.ok("OK");

        } catch (Exception e) {
            log.error("Sepay webhook: lỗi xử lý dữ liệu nhận về", e);
            return ResponseEntity.status(500).body("Error");
        }
    }

    // Số tiền SePay gửi về có thể là số hoặc chuỗi tùy phiên bản webhook, nên
    // đọc qua chuỗi cho chắc. Không đọc được -> trả null và coi như CHƯA đủ tiền
    private java.math.BigDecimal docSoTien(Object gtri) {
        if (gtri == null) return null;
        try {
            return new java.math.BigDecimal(gtri.toString().trim().replace(",", ""));
        } catch (NumberFormatException e) {
            log.warn("Sepay webhook: không đọc được transferAmount = {}", gtri);
            return null;
        }
    }

    // Trang thanh toán QR gọi mỗi 5 giây để biết tiền đã vào chưa.
    // Chỉ trả lời cho chủ đơn: phải đăng nhập và đơn phải thuộc về người đó, để đổi
    // số trên URL không dò được đơn của người khác đã thanh toán hay chưa.
    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> checkPayment(@RequestParam Integer orderId,
                                                            Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();
        Integer userId = userService.findByEmail(auth.getName()).map(User::getId).orElse(null);
        if (userId == null) return ResponseEntity.status(401).build();

        boolean paid = orderService.findById(orderId)
                .filter(o -> o.getUser() != null && userId.equals(o.getUser().getId()))
                .map(order -> Boolean.TRUE.equals(order.getIsPaid()))
                .orElse(false);
        return ResponseEntity.ok(Map.of("paid", paid));
    }
}
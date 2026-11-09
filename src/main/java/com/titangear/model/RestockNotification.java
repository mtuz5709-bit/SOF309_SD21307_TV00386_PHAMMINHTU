package com.titangear.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

// Đăng ký "báo hàng về": khách để lại email khi sản phẩm HẾT HÀNG, hệ thống
// gửi email khi kho nhập hàng lại. Lưu product_id + email dạng cột đơn giản.
@Data
@Entity
@Table(name = "restock_notifications")
public class RestockNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "product_id", nullable = false)
    private Integer productId;

    // Biến thể (màu/dòng) mà khách đang chờ. NULL = chờ sản phẩm nói chung
    // (sản phẩm không có biến thể, hoặc các đăng ký cũ trước khi có tính năng này).
    @Column(name = "variant_id")
    private Integer variantId;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "is_notified", nullable = false)
    private Boolean notified = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}

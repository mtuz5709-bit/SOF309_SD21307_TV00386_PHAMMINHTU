package com.titangear.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

// Lịch sử dùng voucher — phục vụ 2 việc: chặn "mỗi khách 1 lần" và lưu vết
// đơn nào đã giảm bao nhiêu (đối chiếu doanh thu).
@Data
@Entity
@Table(name = "voucher_usages")
public class VoucherUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "voucher_id", nullable = false)
    private Integer voucherId;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "order_id")
    private Integer orderId;

    @Column(name = "discount_amount", nullable = false)
    private BigDecimal discountAmount;

    @Column(name = "used_at")
    private LocalDateTime usedAt;
}

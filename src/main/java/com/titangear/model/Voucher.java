package com.titangear.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

// Mã giảm giá. Hỗ trợ 2 kiểu:
//   PERCENT — giảm theo % (có thể chặn trần bằng maxDiscountAmount)
//   AMOUNT  — giảm thẳng số tiền
// Kèm các điều kiện chống lạm dụng: đơn tối thiểu, hạn dùng, tổng lượt, mỗi khách 1 lần.
@Data
@Entity
@Table(name = "vouchers")
public class Voucher {

    public static final String TYPE_PERCENT = "PERCENT";
    public static final String TYPE_AMOUNT  = "AMOUNT";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "description")
    private String description;

    @Column(name = "discount_type", nullable = false)
    private String discountType;

    @Column(name = "discount_value", nullable = false)
    private BigDecimal discountValue;

    // Đơn phải đạt tối thiểu bao nhiêu mới dùng được (NULL = không yêu cầu)
    @Column(name = "min_order_amount")
    private BigDecimal minOrderAmount;

    // Trần số tiền được giảm — chỉ có ý nghĩa với PERCENT (NULL = không chặn)
    @Column(name = "max_discount_amount")
    private BigDecimal maxDiscountAmount;

    @Column(name = "start_at")
    private LocalDateTime startAt;

    @Column(name = "end_at")
    private LocalDateTime endAt;

    // Tổng số lượt được dùng (NULL = không giới hạn)
    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Column(name = "used_count", nullable = false)
    private Integer usedCount = 0;

    // Mỗi khách chỉ dùng được 1 lần
    @Column(name = "once_per_user", nullable = false)
    private Boolean oncePerUser = true;

    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    // Mã chỉ dùng được cho ĐÚNG combo này. NULL = không giới hạn theo combo.
    //
    // LAZY bắt buộc: @ManyToOne mặc định EAGER sẽ kéo Combo vào mọi truy vấn
    // voucher (kể cả danh sách admin) — đúng cái bẫy đã làm treo server ở CartItem.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "combo_id")
    private Combo combo;

    // Mã chỉ dùng được khi giỏ có combo BẤT KỲ (không chỉ định combo nào).
    @Column(name = "chi_don_co_combo", nullable = false)
    private Boolean chiDonCoCombo = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}

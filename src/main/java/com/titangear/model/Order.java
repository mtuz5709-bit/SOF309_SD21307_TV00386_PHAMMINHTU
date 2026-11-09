package com.titangear.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "receiver_name", nullable = false)
    @jakarta.validation.constraints.Size(max = 255, message = "Tên người nhận tối đa 255 ký tự")
    private String receiverName;

    @Column(name = "receiver_phone", nullable = false)
    private String receiverPhone;

    @Column(name = "shipping_address", nullable = false)
    @jakarta.validation.constraints.Size(max = 500, message = "Địa chỉ giao hàng tối đa 500 ký tự")
    private String shippingAddress;

    // Tỉnh/thành giao hàng — dùng để tính phí ship. NULL với các đơn cũ trước khi có chức năng này.
    @Column(name = "province")
    private String province;

    // Phí vận chuyển ĐÃ TÍNH lúc đặt, không tính lại về sau.
    // Mai admin đổi mức phí thì đơn cũ vẫn phải giữ đúng số khách đã trả,
    // nếu không hóa đơn và sổ sách lệch nhau.
    @Column(name = "shipping_fee", nullable = false)
    private BigDecimal shippingFee = BigDecimal.ZERO;

    // Tổng tiền KHÁCH PHẢI TRẢ = tiền hàng − giảm giá + phí vận chuyển.
    //
    // Cột này ĐÃ BAO GỒM phí ship, mà phí ship là tiền trả cho nhà vận chuyển chứ
    // không phải doanh thu bán hàng. Mọi truy vấn doanh thu đều phải trừ
    // shipping_fee ra, nếu không lãi bị đội lên.
    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "status")
    private String status;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "is_paid")
    private Boolean isPaid = false;

    @Column(name = "note", columnDefinition = "NVARCHAR(MAX)")
    @jakarta.validation.constraints.Size(max = 500, message = "Ghi chú tối đa 500 ký tự")
    private String note;

    // Voucher đã áp dụng (NULL = không dùng mã)
    @Column(name = "voucher_id")
    private Integer voucherId;

    // Số tiền đã giảm nhờ voucher — total_amount là số SAU giảm
    @Column(name = "discount_amount", nullable = false)
    private java.math.BigDecimal discountAmount = java.math.BigDecimal.ZERO;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
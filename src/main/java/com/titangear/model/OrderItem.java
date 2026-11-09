package com.titangear.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // Biến thể (màu/phiên bản) khách chọn khi đặt hàng — NULL nếu sản phẩm không có biến thể
    @ManyToOne
    @JoinColumn(name = "variant_id")
    private ProductVariant variant;

    // Dòng này thuộc combo nào lúc đặt — NULL là mua lẻ.
    // Giữ lại để trang chi tiết đơn gom được các món cùng bộ và để báo cáo
    // tách được doanh thu combo với doanh thu bán lẻ.
    // LAZY vì lý do như CartItem.comboItem: @ManyToOne mặc định EAGER sẽ kéo
    // theo cả cây combo -> items -> product -> specs + variants vào mọi truy vấn
    // đơn hàng, kể cả trang danh sách đơn vốn không hiện gì về combo.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "combo_id")
    private Combo combo;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "line_total", nullable = false)
    private BigDecimal lineTotal;
}
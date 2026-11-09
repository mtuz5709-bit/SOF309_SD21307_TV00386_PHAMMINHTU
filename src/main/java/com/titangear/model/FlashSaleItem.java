package com.titangear.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;

// 1 sản phẩm được giảm giá trong 1 chương trình Flash Sale cụ thể.
// discountType = PERCENT  -> discountValue là % (0-100]
// discountType = AMOUNT   -> discountValue là số tiền giảm cụ thể (VNĐ)
@Data
@Entity
@Table(name = "flash_sale_items")
public class FlashSaleItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Loại quan hệ ngược khỏi equals/hashCode/toString để tránh đệ quy vô hạn
    // (FlashSale.hashCode gồm items -> FlashSaleItem.hashCode gồm flashSale -> ...)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "flash_sale_id", nullable = false)
    private FlashSale flashSale;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 10)
    private DiscountType discountType;

    @Column(name = "discount_value", nullable = false)
    private BigDecimal discountValue;

    public enum DiscountType { PERCENT, AMOUNT }
}

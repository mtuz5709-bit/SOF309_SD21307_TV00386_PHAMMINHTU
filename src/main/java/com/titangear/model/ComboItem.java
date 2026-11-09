package com.titangear.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

// Một sản phẩm nằm trong combo.
@Data
@Entity
@Table(name = "combo_items")
public class ComboItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Loại quan hệ ngược khỏi equals/hashCode/toString để tránh đệ quy vô hạn
    // (Combo.hashCode gồm items -> ComboItem.hashCode gồm combo -> ...).
    // Cùng cách xử lý với ProductVariant.product.
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "combo_id", nullable = false)
    private Combo combo;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // Biến thể (màu) được chốt sẵn cho món này. NULL khi sản phẩm không có biến thể.
    //
    // BẮT BUỘC với sản phẩm CÓ biến thể: Model 2 coi biến thể là đơn vị tồn kho,
    // đưa vào giỏ mà thiếu biến thể thì đơn tạo được nhưng không xuất kho được —
    // đúng lỗi đã gặp ở nút "Thêm vào giỏ" trang danh sách sản phẩm.
    // ComboService coi combo thiếu biến thể là dữ liệu sai và ẩn combo đó đi.
    @ManyToOne
    @JoinColumn(name = "variant_id")
    private ProductVariant variant;

    // Số lượng món này trong bộ (thường là 1).
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
}

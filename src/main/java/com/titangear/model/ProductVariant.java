package com.titangear.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "product_variants")
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Loại quan hệ ngược khỏi equals/hashCode/toString để tránh đệ quy vô hạn
    // (Product.hashCode gồm variants -> ProductVariant.hashCode gồm product -> ...)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // Tên gọi thêm — KHÔNG bắt buộc.
    //
    // Biến thể chỉ mang khác biệt KHÔNG làm đổi thông số kỹ thuật, tức là màu sắc.
    // Phiên bản khác nhau (F1 Ultimate, F1 Extreme) phải là các sản phẩm riêng, vì
    // mỗi phiên bản có bảng thông số riêng gắn theo product_specs.product_id.
    //
    // Quy ước từ nay: mỗi phiên bản là MỘT SẢN PHẨM riêng, biến thể chỉ mang khác
    // biệt KHÔNG làm đổi thông số — tức là màu sắc. Cột này giữ lại cho trường hợp
    // cần ghi chú thêm (vd "bản kèm lót chuột"), để trống là bình thường.
    @Column(name = "name")
    private String name;

    @Column(name = "color")
    private String color;       // "Đen", "Trắng", "Đỏ"

    @Column(name = "rgb_code")
    private String colorCode;   // "#000000" — cột DB đã đổi tên thành rgb_code (xem 12.sql), giữ tên field Java để không phải sửa form/JS/JSP đang dùng "colorCode"

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    // Model 2: tồn biến thể do TRIGGER tính từ các lô stock_in của biến thể đó.
    // insertable/updatable = false: Java CHỈ ĐỌC (giống Product.stockQuantity),
    // hàng vào kho qua nhập kho theo biến thể, không còn phân bổ tay.
    @Column(name = "stock", insertable = false, updatable = false)
    private Integer stock = 0;

    @Column(name = "is_active")
    private Boolean isActive = true;
}
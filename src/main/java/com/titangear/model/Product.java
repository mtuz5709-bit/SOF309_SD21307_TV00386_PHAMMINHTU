package com.titangear.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    // Dòng sản phẩm gom các phiên bản cùng họ, ví dụ F1 Pro và F1 Ultimate cùng
    // thuộc dòng ATK F1. Để NULL nếu hàng lẻ không thuộc dòng nào.
    @ManyToOne
    @JoinColumn(name = "line_id")
    private ProductLine line;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Column(name = "name", nullable = false)
    @jakarta.validation.constraints.Size(max = 255, message = "Tên sản phẩm tối đa 255 ký tự")
    private String name;

    @Column(name = "description", columnDefinition = "NVARCHAR(MAX)")
    @jakarta.validation.constraints.Size(max = 4000, message = "Mô tả sản phẩm tối đa 4000 ký tự")
    private String description;

    @NotNull(message = "Giá không được để trống")
    @Positive(message = "Giá phải lớn hơn 0")
    @Column(name = "price", nullable = false)
    private BigDecimal price;

    // Tồn kho sản phẩm, bằng tổng số còn lại của các lô nhập. Do trigger trong
    // SQL Server tính lại. Java chỉ đọc, không ghi cột này.
    @Column(name = "stock_quantity", insertable = false, updatable = false)
    private Integer stockQuantity;

    @Column(name = "image_url")
    private String imageUrl;

    /* Thuộc tính đã chuẩn hoá, dùng cho bộ lọc ở trang danh sách (xem constant/ProductAttr).
       Để NULL nếu không áp dụng: vd tai nghe không có "loại switch". --- */

    @Column(name = "switch_type")
    private String switchType;      // LINEAR | TACTILE | CLICKY | MAGNETIC

    @Column(name = "weight_group")
    private String weightGroup;     // ULTRA | MID | HEAVY

    @Column(name = "connectivity")
    private String connectivity;    // WIRED | WIRELESS | DUAL

    // Số tháng bảo hành. Để NULL hoặc 0 nghĩa là sản phẩm không có bảo hành.
    @Column(name = "warranty_months")
    private Integer warrantyMonths;

    @Column(name = "is_active")
    private Boolean isActive;

    // Dùng Set chứ không dùng List: Hibernate không cho nạp cùng lúc hai collection
    // kiểu List.  giữ thứ tự hiển thị ổn định theo id.
    // Thông số kỹ thuật và biến thể của sản phẩm, để EAGER có chủ ý.
    // Không đổi sang LAZY: JSP dựng sau khi controller trả về, lúc đó phiên Hibernate
    // đã đóng nên đọc collection sẽ ném LazyInitializationException ở 18 màn hình.
    // Muốn đổi phải khai interceptor mở phiên tới hết vòng đời request trước.
    @OneToMany(mappedBy = "product", fetch = FetchType.EAGER)
    @OrderBy("id ASC")
    private Set<ProductSpec> specs;

    @OneToMany(mappedBy = "product", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @OrderBy("id ASC")
    private Set<ProductVariant> variants;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
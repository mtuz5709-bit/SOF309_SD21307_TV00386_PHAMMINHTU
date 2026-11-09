package com.titangear.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

// Combo: bộ sản phẩm ghép sẵn, mua trọn bộ rẻ hơn mua lẻ.
// Bảng này lưu SỐ TIỀN GIẢM chứ không lưu giá bán trọn bộ, để giá combo tự trôi
// theo giá sản phẩm khi admin đổi giá. Cách tính giá bán xem ComboService.
@Data
@Entity
@Table(name = "combos")
public class Combo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", nullable = false)
    @jakarta.validation.constraints.Size(max = 150, message = "Tên combo tối đa 150 ký tự")
    private String name;

    // Nhãn phong cách hiện trên thẻ combo, ví dụ "FPS TRY-HARD", "VĂN PHÒNG".
    @Column(name = "tag")
    @jakarta.validation.constraints.Size(max = 50, message = "Nhãn combo tối đa 50 ký tự")
    private String tag;

    @Column(name = "description", columnDefinition = "NVARCHAR(500)")
    @jakarta.validation.constraints.Size(max = 500, message = "Mô tả combo tối đa 500 ký tự")
    private String description;

    // Số tiền giảm khi mua trọn bộ, đơn vị đồng.
    @Column(name = "discount_amount", nullable = false)
    private BigDecimal discountAmount;

    // Màu vạch và màu nhãn trên thẻ combo, ví dụ "#7A5CC7". Để trống thì dùng màu
    // xanh mặc định.
    @Column(name = "accent_color")
    private String accentColor;

    // Cờ bật/tắt combo. Ngừng bán thì tắt cờ này chứ không xóa cứng, vì order_items
    // trỏ tới bảng combos nên xóa là gãy khóa ngoại của đơn đã bán.
    @Column(name = "is_active")
    private Boolean isActive;

    // Danh sách món trong bộ. Để LAZY, không được đổi sang EAGER: mỗi dòng giỏ hàng
    // sẽ kéo theo items -> product -> specs + variants và nhân nhau thành hàng nghìn
    // dòng kết quả. Chỗ nào cần items thì nạp bằng truy vấn JOIN FETCH trong ComboRepository.
    @OneToMany(mappedBy = "combo", fetch = FetchType.LAZY)
    @OrderBy("id ASC")
    private Set<ComboItem> items;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}

package com.titangear.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "cart_items")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // Biến thể (màu/phiên bản) khách chọn trong giỏ — NULL nếu sản phẩm không có biến thể
    @ManyToOne
    @JoinColumn(name = "variant_id")
    private ProductVariant variant;

    // Dòng này thuộc combo nào — NULL nghĩa là mua lẻ.
    // Các dòng cùng combo_id đi theo BỘ: không sửa lẻ số lượng, xóa 1 là xóa cả bộ
    // (xem CartService.updateQuantity / removeItem).
    @ManyToOne
    @JoinColumn(name = "combo_id")
    private Combo combo;

    // Dòng này ứng với VỊ TRÍ nào trong bộ (món thứ mấy).
    //
    // Cần vì khách được đổi phiên bản: dòng giỏ có thể là F1 Extreme trong khi
    // admin đặt sẵn F1 Pro. Dò theo product_id sẽ ghép nhầm khi combo có 2 món
    // cùng một dòng sản phẩm — nhầm vị trí là tính tiền giảm sai.
    // LAZY: @ManyToOne mặc định là EAGER, mà ComboItem lại kéo theo Combo -> items
    // -> product -> specs + variants. Chỉ cần lấy id nên proxy là đủ, không nạp gì thêm.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "combo_item_id")
    private ComboItem comboItem;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "added_at")
    private LocalDateTime addedAt;
}

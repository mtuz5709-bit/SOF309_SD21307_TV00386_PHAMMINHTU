package com.titangear.repository;

import com.titangear.model.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Integer> {

    List<ProductVariant> findByProductIdAndIsActiveTrue(Integer productId);

    // Giao diện này CỐ Ý không khai truy vấn "DELETE FROM product_variants" nào.
    //
    // Biến thể KHÔNG BAO GIỜ được xóa cứng:
    //   - order_items.variant_id và stock_in.variant_id có khóa ngoại trỏ tới đây
    //     -> xóa là vi phạm ràng buộc, hoặc mất lịch sử đơn đã bán.
    //   - cart_items.variant_id khai ON DELETE SET NULL -> dòng giỏ hàng của khách
    //     bỗng dưng MẤT MÀU. Đơn tạo được nhưng không xuất kho nổi, vì Model 2
    //     tính tồn theo biến thể.
    //
    // Ngừng bán một biến thể = đặt is_active = false, và phải hết tồn kho trước.
    // Xem ProductVariantController.deleteVariant.
}

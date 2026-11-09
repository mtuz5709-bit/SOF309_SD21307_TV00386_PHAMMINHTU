package com.titangear.service;

import com.titangear.model.ProductVariant;
import com.titangear.repository.ProductVariantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class ProductVariantService {

    @Autowired
    private ProductVariantRepository variantRepository;

    public List<ProductVariant> getByProductId(Integer productId) {
        return variantRepository.findByProductIdAndIsActiveTrue(productId);
    }

    public Optional<ProductVariant> findById(Integer id) {
        return variantRepository.findById(id);
    }

    public ProductVariant save(ProductVariant variant) {
        // Chuẩn hoá "để trống": form gửi lên chuỗi rỗng, lưu thẳng thì DB có cả
        // NULL lẫn "" cùng nghĩa — mọi chỗ hiển thị phải kiểm tra 2 kiểu.
        // Quy về NULL hết cho dữ liệu sạch, JSP chỉ cần `empty` là đủ.
        variant.setName(rongThanhNull(variant.getName()));
        variant.setColor(rongThanhNull(variant.getColor()));
        variant.setColorCode(rongThanhNull(variant.getColorCode()));
        return variantRepository.save(variant);
    }

    private String rongThanhNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    // Lớp này CỐ Ý không có hàm xóa cứng biến thể (kiểu deleteByProductId, delete).
    //
    // Hàm chạy thẳng "DELETE FROM product_variants" rất nguy hiểm — ai gọi nhầm
    // là hỏng dữ liệu thật:
    //
    //   - order_items.variant_id và stock_in.variant_id đều có khóa ngoại trỏ tới
    //     đây -> xóa biến thể ĐÃ BÁN là vi phạm khóa ngoại, hoặc mất lịch sử đơn.
    //   - cart_items.variant_id khai ON DELETE SET NULL -> dòng giỏ hàng của khách
    //     BỖNG DƯNG MẤT MÀU. Đơn tạo được nhưng không bao giờ xuất kho được, vì
    //     tồn kho tính theo biến thể.
    //
    // Cách đúng đang dùng ở ProductVariantController.deleteVariant: đặt
    // is_active = false, và CHẶN nếu biến thể còn tồn kho (ẩn khi còn hàng thì
    // số tồn đó vẫn cộng vào tồn sản phẩm nhưng không bán được -> kẹt vô hình).
}
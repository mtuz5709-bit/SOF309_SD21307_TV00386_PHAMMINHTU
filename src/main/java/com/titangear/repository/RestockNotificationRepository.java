package com.titangear.repository;

import com.titangear.model.RestockNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RestockNotificationRepository extends JpaRepository<RestockNotification, Integer> {

    // Các đăng ký CHƯA gửi của 1 sản phẩm (mọi biến thể) — dùng cho thống kê
    List<RestockNotification> findByProductIdAndNotifiedFalse(Integer productId);

    // Người chờ ĐÚNG 1 biến thể (vd chỉ chờ màu Đỏ)
    List<RestockNotification> findByProductIdAndVariantIdAndNotifiedFalse(Integer productId, Integer variantId);

    // Người chờ SẢN PHẨM nói chung (variant_id NULL): SP không biến thể + đăng ký cũ
    List<RestockNotification> findByProductIdAndVariantIdIsNullAndNotifiedFalse(Integer productId);

    // Chống đăng ký trùng — theo biến thể và theo sản phẩm chung
    boolean existsByProductIdAndVariantIdAndEmailAndNotifiedFalse(Integer productId, Integer variantId, String email);

    boolean existsByProductIdAndVariantIdIsNullAndEmailAndNotifiedFalse(Integer productId, String email);
}

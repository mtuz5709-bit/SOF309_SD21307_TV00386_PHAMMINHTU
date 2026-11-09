package com.titangear.repository;

import com.titangear.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {

    // Lấy tất cả sản phẩm trong 1 đơn hàng
    List<OrderItem> findByOrderId(Integer orderId);

    // Kiểm tra sản phẩm đã từng được đặt chưa
    List<OrderItem> findByProductId(Integer productId);

    // Tổng số lượng sản phẩm đang chờ duyệt
    @Query("SELECT COALESCE(SUM(oi.quantity), 0) FROM OrderItem oi " +
            "JOIN oi.order o " +
            "WHERE oi.product.id = :productId " +
            "AND o.status IN ('PENDING', 'AWAITING_STOCK')")
    Integer getTotalPendingQuantity(Integer productId);

    // Model 2 — số lượng ĐANG GIỮ (reserved) của 1 BIẾN THỂ: tổng SL trong các
    // đơn còn hoạt động, chưa xuất kho (PENDING/AWAITING_STOCK). available của
    // biến thể = variant.stock − reserved.
    @Query("SELECT COALESCE(SUM(oi.quantity), 0) FROM OrderItem oi " +
            "JOIN oi.order o " +
            "WHERE oi.variant.id = :variantId " +
            "AND o.status IN ('PENDING', 'AWAITING_STOCK')")
    Integer getReservedQtyByVariant(@Param("variantId") Integer variantId);

}
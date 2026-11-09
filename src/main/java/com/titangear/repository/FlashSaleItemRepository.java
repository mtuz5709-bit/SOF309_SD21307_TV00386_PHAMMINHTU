package com.titangear.repository;

import com.titangear.model.FlashSaleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FlashSaleItemRepository extends JpaRepository<FlashSaleItem, Integer> {

    List<FlashSaleItem> findByFlashSaleId(Integer flashSaleId);

    // Đếm số sản phẩm áp dụng theo từng flash sale (cho trang danh sách admin)
    @Query("SELECT fi.flashSale.id, COUNT(fi) FROM FlashSaleItem fi GROUP BY fi.flashSale.id")
    List<Object[]> countItemsGroupedBySale();

    @Modifying
    @Transactional
    void deleteByFlashSaleId(Integer flashSaleId);

    // Các flash sale item đang active áp dụng cho 1 sản phẩm cụ thể
    // (dùng để tính giá sau giảm khi hiển thị sản phẩm).
    @Query("SELECT fi FROM FlashSaleItem fi " +
           "WHERE fi.product.id = :productId " +
           "AND fi.flashSale.isActive = true " +
           "AND :now BETWEEN fi.flashSale.startAt AND fi.flashSale.endAt " +
           "ORDER BY fi.flashSale.startAt ASC")
    List<FlashSaleItem> findActiveItemsForProduct(@Param("productId") Integer productId,
                                                    @Param("now") LocalDateTime now);

    // Kiểm tra 1 sản phẩm có đang bị áp flash sale khác chồng khoảng thời gian không
    // (excludeSaleId = id của chính flash sale đang sửa, để không tự chặn chính nó;
    //  truyền -1 khi tạo mới).
    @Query("SELECT fi FROM FlashSaleItem fi " +
           "WHERE fi.product.id = :productId " +
           "AND fi.flashSale.id <> :excludeSaleId " +
           "AND fi.flashSale.isActive = true " +
           "AND fi.flashSale.startAt < :endAt AND fi.flashSale.endAt > :startAt")
    List<FlashSaleItem> findOverlapping(@Param("productId") Integer productId,
                                          @Param("excludeSaleId") Integer excludeSaleId,
                                          @Param("startAt") LocalDateTime startAt,
                                          @Param("endAt") LocalDateTime endAt);

    // Toàn bộ flash sale item đang active tại thời điểm hiện tại (dùng để tính giá hàng loạt)
    @Query("SELECT fi FROM FlashSaleItem fi " +
           "WHERE fi.flashSale.isActive = true " +
           "AND :now BETWEEN fi.flashSale.startAt AND fi.flashSale.endAt")
    List<FlashSaleItem> findAllActiveItems(@Param("now") LocalDateTime now);
}

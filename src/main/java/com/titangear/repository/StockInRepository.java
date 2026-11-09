package com.titangear.repository;

import com.titangear.model.StockIn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StockInRepository extends JpaRepository<StockIn, Integer> {

    // Xem lịch sử nhập kho theo sản phẩm.
    List<StockIn> findByProductId(Integer productId);

    // Xem lịch sử nhập kho theo nhân viên.
    List<StockIn> findByImportedById(Integer userId);

    // Model 2 — FIFO theo BIẾN THỂ: các lô còn hàng của đúng 1 biến thể,
    // cũ nhất trước. Dùng khi xuất kho cho order_item có biến thể.
    @Query("SELECT si FROM StockIn si " +
            "WHERE si.variant.id = :variantId AND si.remainingQuantity > 0 " +
            "ORDER BY si.importedAt ASC")
    List<StockIn> findVariantLotsFIFO(@Param("variantId") Integer variantId);

    // Model 2 — FIFO mức SẢN PHẨM cho SP KHÔNG có biến thể: chỉ lấy lô
    // variant_id IS NULL (tránh nuốt nhầm lô của biến thể).
    @Query("SELECT si FROM StockIn si " +
            "WHERE si.product.id = :productId AND si.variant IS NULL AND si.remainingQuantity > 0 " +
            "ORDER BY si.importedAt ASC")
    List<StockIn> findProductLotsFIFO(@Param("productId") Integer productId);

    // Model 2 (hướng A) — GÁN 1 lô "mức sản phẩm" (variant_id NULL) vào 1 biến thể.
    // Dùng khi lỡ nhập kho TRƯỚC khi tạo biến thể: đổi nhãn variant_id của lô,
    // trigger DB tự tính lại tồn biến thể. Chốt an toàn: chỉ gán lô CÒN TRỐNG
    // (variant_id IS NULL) và ĐÚNG sản phẩm. Trả về số dòng đổi (0 = không hợp lệ).
    @Modifying
    @Query(value = "UPDATE stock_in SET variant_id = :variantId " +
            "WHERE id = :lotId AND product_id = :productId AND variant_id IS NULL",
            nativeQuery = true)
    int assignLotToVariant(@Param("lotId") Integer lotId,
                           @Param("productId") Integer productId,
                           @Param("variantId") Integer variantId);

    // HOÀN KHO: cộng trả số lượng về đúng lô đã xuất (khi hủy đơn đã giao).
    // Dùng native vì remaining_quantity đánh dấu updatable=false ở entity —
    // DB là nguồn sự thật. UPDATE này kích hoạt TRG_StockIn_SyncProductStock,
    // tự tính lại tồn CẢ sản phẩm LẪN biến thể.
    @Modifying
    @Query(value = "UPDATE stock_in SET remaining_quantity = remaining_quantity + :qty " +
            "WHERE id = :lotId", nativeQuery = true)
    int restoreLot(@Param("lotId") Integer lotId, @Param("qty") Integer qty);
}

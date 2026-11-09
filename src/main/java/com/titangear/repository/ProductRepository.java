package com.titangear.repository;

import com.titangear.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    List<Product> findByCategoryId(Integer categoryId);

    List<Product> findByNameContainingIgnoreCase(String name);

    // Bản dùng cho phía khách — bỏ qua hàng đã ngừng kinh doanh.
    List<Product> findByNameContainingIgnoreCaseAndIsActiveTrue(String name);

    List<Product> findByStockQuantityGreaterThan(Integer quantity);

    // Đếm hàng đang bán mà tồn kho ≤5 (gồm cả hết sạch) — con số cảnh báo trên
    // menu "Kho hàng", nhắc admin nhập thêm trước khi đứt hàng.
    @Query("SELECT COUNT(p) FROM Product p WHERE p.isActive = true AND p.stockQuantity <= 5")
    long demSapHetHang();

    List<Product> findByStockQuantityEquals(Integer quantity);

    List<Product> findByPriceBetween(BigDecimal min, BigDecimal max);

    List<Product> findByCategoryIdAndNameContainingIgnoreCase(
            Integer categoryId, String name);

    List<Product> findByIsActiveTrue();

    // Lấy sản phẩm đang bán, mới nhất trước, đúng số lượng cần. Sắp xếp và giới hạn
    // ngay ở tầng CSDL để trang chủ không phải nạp cả kho rồi cắt trong Java.
    // Thứ tự: createdAt giảm dần, sản phẩm chưa có ngày tạo xuống cuối, rồi tới id
    // giảm dần cho ổn định.
    @Query("SELECT p FROM Product p WHERE p.isActive = true "
         + "ORDER BY CASE WHEN p.createdAt IS NULL THEN 1 ELSE 0 END, p.createdAt DESC, p.id DESC")
    List<Product> findMoiNhat(org.springframework.data.domain.Pageable pageable);

    // Các phiên bản cùng dòng (F1 Pro / F1 Ultimate / F1 Extreme), đang bán.
    // Trang combo cần danh sách này cho mỗi món để khách đổi phiên bản.
    // Hỏi thẳng theo line_id thay vì nạp toàn bộ sản phẩm rồi lọc trong Java
    // như ProductService.cungDong() — combo 3 món là 3 lần nạp cả bảng.
    List<Product> findByLineIdAndIsActiveTrueOrderByIdAsc(Integer lineId);

    // Lọc theo tháng + năm (dùng cho admin)
    @Query("SELECT p FROM Product p WHERE p.isActive = true " +
            "AND (:month IS NULL OR MONTH(p.createdAt) = :month) " +
            "AND (:year  IS NULL OR YEAR(p.createdAt)  = :year)")
    List<Product> findByMonthYear(@Param("month") Integer month,
                                  @Param("year")  Integer year);
}
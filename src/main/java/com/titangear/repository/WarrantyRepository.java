package com.titangear.repository;

import com.titangear.model.Warranty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface WarrantyRepository extends JpaRepository<Warranty, Integer> {

    // Tra cứu bằng mã bảo hành (khách nhập trên phiếu)
    Optional<Warranty> findBySerialNoIgnoreCase(String serialNo);

    // Phiếu của 1 khách — hiện trong "Đơn hàng của tôi"
    List<Warranty> findByUserIdOrderByIdDesc(Integer userId);

    // Phiếu sinh ra từ 1 dòng đơn — dùng để CHỐNG TẠO TRÙNG khi đơn bị
    // chuyển sang Hoàn thành nhiều lần
    List<Warranty> findByOrderItemId(Integer orderItemId);

    boolean existsByOrderItemId(Integer orderItemId);

    List<Warranty> findAllByOrderByIdDesc();

    // Đếm phiếu SẮP hết hạn — cho con số cảnh báo trên menu "Bảo hành".
    // Đếm thẳng trên CSDL thay vì nạp hết phiếu rồi lọc trong Java: menu hiện ở
    // MỌI trang admin nên truy vấn này chạy rất thường xuyên.
    long countByEndDateBetween(java.time.LocalDate tuNgay, java.time.LocalDate denNgay);

    // Tìm cho admin: theo serial hoặc tên khách/sản phẩm không cần chính xác.
    @Query("SELECT w FROM Warranty w WHERE LOWER(w.serialNo) LIKE LOWER(CONCAT('%', :q, '%'))")
    List<Warranty> timTheoSerial(@Param("q") String q);
}

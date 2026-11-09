package com.titangear.repository;

import com.titangear.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    // Xem lịch sử đơn hàng của 1 khách
    List<Order> findByUserId(Integer userId);

    // Đơn chuyển khoản quá hạn thanh toán — chờ dọn tự động.
    //
    // CHỈ lấy VNPAY: đơn COD không có hạn thanh toán (thu tiền lúc giao), hủy
    // nhầm là mất đơn thật của khách.
    // Đã trả tiền rồi thì tuyệt đối không đụng, kể cả khi quá giờ — tiền đã vào
    // tài khoản shop, hủy đơn là khách mất tiền lẫn hàng.
    @Query("SELECT o FROM Order o "
         + "WHERE o.status = 'PENDING' AND o.paymentMethod = 'VNPAY' "
         + "AND (o.isPaid IS NULL OR o.isPaid = false) "
         + "AND o.createdAt < :truoc")
    List<Order> findDonChuyenKhoanQuaHan(@Param("truoc") java.time.LocalDateTime truoc);

    // Lọc đơn theo trạng thái — admin quản lý (có phân trang)
    Page<Order> findByStatus(String status, Pageable pageable);

    // Lọc đơn theo trạng thái — không phân trang
    List<Order> findByStatus(String status);

    // Đếm đơn theo trạng thái (cho thẻ thống kê + số đếm tab admin)
    long countByStatus(String status);

    // Đơn CẦN ADMIN XỬ LÝ — con số cảnh báo trên menu "Đơn hàng".
    // Gộp 2 trạng thái vào 1 truy vấn thay vì gọi countByStatus hai lần, vì menu
    // hiện ở mọi trang admin.
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status IN ('PENDING', 'AWAITING_STOCK')")
    long demDonChoXuLy();

    // Lịch sử đơn của khách theo trạng thái
    List<Order> findByUserIdAndStatus(Integer userId, String status);

    // Sắp xếp mới nhất lên đầu
    List<Order> findByUserIdOrderByCreatedAtDesc(Integer userId);

    // Đếm đơn COD ĐANG CHỜ XỬ LÝ của 1 khách (chưa giao xong, chưa hủy).
    // Dùng chống bom hàng: chỉ siết COD vì tiền chưa thu; đơn VNPAY đã trả
    // trước nên không tính vào đây.
    @Query("SELECT COUNT(o) FROM Order o WHERE o.user.id = :userId " +
            "AND o.paymentMethod = 'COD' AND o.status NOT IN ('COMPLETED','CANCELLED')")
    long countActiveCodOrders(@Param("userId") Integer userId);

    // Tìm kiếm admin: theo tên/SĐT người nhận hoặc mã đơn (nếu q là số)
    @Query("SELECT o FROM Order o WHERE " +
            "LOWER(o.receiverName) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR o.receiverPhone LIKE CONCAT('%', :q, '%') " +
            "OR CAST(o.id AS string) = :q")
    Page<Order> searchOrders(@Param("q") String q, Pageable pageable);
}
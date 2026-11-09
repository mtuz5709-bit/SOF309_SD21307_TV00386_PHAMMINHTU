package com.titangear.repository;

import com.titangear.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {

    List<Review> findByProductIdOrderByCreatedAtDesc(Integer productId);

    List<Review> findByProductIdOrderByCreatedAtAsc(Integer productId);

    List<Review> findByProductIdOrderByRatingDescCreatedAtDesc(Integer productId);

    List<Review> findByProductIdOrderByRatingAscCreatedAtDesc(Integer productId);

    Optional<Review> findByUserIdAndProductId(Integer userId, Integer productId);

    // Tài khoản này đã viết bao nhiêu đánh giá — dùng để chặn xóa cứng tài khoản.
    long countByUserId(Integer userId);

    boolean existsByUserIdAndProductId(Integer userId, Integer productId);

    // Trung bình sao + tổng số review của 1 sản phẩm — luôn trả về đúng 1 dòng (không GROUP BY)
    /* CAST sang double TRƯỚC khi AVG: SQL Server tính AVG trên INT sẽ cắt phần
       thập phân (AVG(4,5) = 4 chứ không phải 4.5) — cast để ra số thực đúng */
    @Query("SELECT AVG(CAST(r.rating AS double)), COUNT(r) FROM Review r WHERE r.product.id = :productId")
    List<Object[]> getSummary(@Param("productId") Integer productId);

    // Điểm trung bình + số lượt của NHIỀU sản phẩm trong MỘT truy vấn.
    // Trang danh sách hiện 9-12 sản phẩm; nếu gọi getSummary() cho từng cái thì
    // thành 12 lượt đi lại CSDL cho mỗi lần tải trang (lỗi N+1 kinh điển).
    // Trả về [productId, avg, count].
    @Query("SELECT r.product.id, AVG(CAST(r.rating AS double)), COUNT(r) FROM Review r " +
           "WHERE r.product.id IN :ids GROUP BY r.product.id")
    List<Object[]> tomTatTheoDanhSach(@Param("ids") List<Integer> ids);

    // Số lượng review theo từng mức sao (1-5) của 1 sản phẩm, dùng vẽ thanh phân bố
    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.product.id = :productId GROUP BY r.rating")
    List<Object[]> countByRatingGrouped(@Param("productId") Integer productId);

    // Kiểm tra user có đơn hàng COMPLETED chứa sản phẩm này không
    // (điều kiện bắt buộc để được đánh giá).
    @Query("SELECT COUNT(oi) > 0 FROM OrderItem oi " +
           "WHERE oi.order.user.id = :userId AND oi.product.id = :productId " +
           "AND oi.order.status = 'COMPLETED'")
    boolean hasPurchased(@Param("userId") Integer userId, @Param("productId") Integer productId);
}

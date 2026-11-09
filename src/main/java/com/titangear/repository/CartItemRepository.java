package com.titangear.repository;

import com.titangear.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Integer> {

    // Lấy toàn bộ giỏ hàng của 1 khách
    List<CartItem> findByUserId(Integer userId);

    // Tổng SỐ LƯỢNG hàng trong giỏ (mua 3 con chuột = 3), cho con số trên icon giỏ.
    // Dùng SUM thay vì nạp cả giỏ rồi đếm: header hiện ở MỌI trang nên truy vấn này
    // chạy liên tục, nạp nguyên danh sách kèm sản phẩm/biến thể là quá phí.
    // Giỏ trống -> SUM trả NULL nên nhận qua Long rồi tự quy về 0.
    @Query("SELECT SUM(c.quantity) FROM CartItem c WHERE c.user.id = :userId")
    Long tongSoLuong(@Param("userId") Integer userId);

    // Tìm 1 sản phẩm cụ thể trong giỏ của khách
    Optional<CartItem> findByUserIdAndProductId(Integer userId, Integer productId);

    // Tìm dòng giỏ hàng theo đúng bộ ba (khách, sản phẩm, biến thể), để chuột đen và
    // chuột đỏ nằm hai dòng riêng chứ không gộp làm một. Xử lý cả trường hợp sản phẩm
    // không có biến thể.
    // Điều kiện c.combo IS NULL bỏ qua các dòng thuộc combo, để khách mua lẻ đúng món
    // đang có trong combo thì không bị cộng dồn vào dòng của bộ.
    @Query("SELECT c FROM CartItem c " +
           "WHERE c.user.id = :userId AND c.product.id = :productId " +
           "AND c.combo IS NULL " +
           "AND ((:variantId IS NULL AND c.variant IS NULL) " +
           "     OR c.variant.id = :variantId)")
    Optional<CartItem> findByUserProductAndVariant(@Param("userId") Integer userId,
                                                   @Param("productId") Integer productId,
                                                   @Param("variantId") Integer variantId);

    // CHỐNG IDOR: chỉ trả về cart item nếu nó THUỘC VỀ đúng user này.
    // Mọi thao tác sửa/xóa giỏ hàng phải đi qua đây thay vì findById trần.
    Optional<CartItem> findByIdAndUserId(Integer id, Integer userId);

    // Xóa toàn bộ giỏ hàng sau khi đặt hàng xong
    void deleteByUserId(Integer userId);

    // Các dòng thuộc một combo trong giỏ của khách.
    // Dùng khi xóa combo (xóa 1 món = xóa cả bộ) và khi tính lại giá bộ.
    // Luôn kèm userId để không đụng được vào giỏ người khác (chống IDOR).
    List<CartItem> findByUserIdAndComboId(Integer userId, Integer comboId);

    // Khách đã có combo này trong giỏ chưa — chặn thêm trùng bộ.
    boolean existsByUserIdAndComboId(Integer userId, Integer comboId);
}
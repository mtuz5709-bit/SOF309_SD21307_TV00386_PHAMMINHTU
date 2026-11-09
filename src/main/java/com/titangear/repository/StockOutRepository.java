package com.titangear.repository;

import com.titangear.model.StockOut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StockOutRepository extends JpaRepository<StockOut, Integer> {

    // StockOut không có field "product"/"order" trực tiếp (chỉ có orderItem)
    // → phải đi qua orderItem để tham chiếu đúng entity thật sự.

    // Xem lịch sử xuất kho theo sản phẩm (đi qua orderItem.product.id).
    List<StockOut> findByOrderItem_Product_Id(Integer productId);

    // Xem lịch sử xuất kho theo đơn hàng (đi qua orderItem.order.id).
    List<StockOut> findByOrderItem_Order_Id(Integer orderId);

    // Các dòng xuất CHƯA hoàn kho của một đơn — dùng khi hủy đơn đã xuất.
    List<StockOut> findByOrderItem_Order_IdAndReturnedFalse(Integer orderId);

    // Xem lịch sử xuất kho theo nhân viên.
    List<StockOut> findByExportedById(Integer userId);

    // Nhân viên này đã từng xuất kho chưa — dùng để chặn xóa cứng tài khoản.
    long countByExportedById(Integer userId);

    // Xem lịch sử xuất kho theo lô nhập cụ thể (truy vết FIFO).
    List<StockOut> findByStockInId(Integer stockInId);
}

package com.titangear.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "stock_out")
public class StockOut {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Dòng đơn hàng mà lần xuất kho này phục vụ. Muốn biết xuất sản phẩm gì, số
    // lượng bao nhiêu thì đọc qua orderItem, không lưu lại ở đây cho khỏi trùng.
    @ManyToOne
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    // Lô nhập mà số hàng này được lấy ra. Một dòng đơn có thể phải lấy từ nhiều lô
    // nếu một lô không đủ hàng, nên mỗi lô là một dòng stock_out riêng.
    @ManyToOne
    @JoinColumn(name = "stock_in_id", nullable = false)
    private StockIn stockIn;

    // Số lượng lấy từ riêng lô này, không phải tổng số của cả dòng đơn hàng.
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    // Giá vốn thực tế của lô, chốt lại ngay lúc xuất. Dùng để tính lãi:
    //     lãi = doanh thu − import_cost × quantity
    @Column(name = "import_cost")
    private BigDecimal importCost;

    @ManyToOne
    @JoinColumn(name = "exported_by", nullable = false)
    private User exportedBy;

    @Column(name = "exported_at")
    private LocalDateTime exportedAt;

    @Column(name = "note", columnDefinition = "NVARCHAR(MAX)")
    private String note;

    // Đánh dấu dòng xuất này đã hoàn kho chưa, dùng khi đơn bị hủy sau khi đã xuất.
    // Giữ lại bản ghi thay vì xóa, để còn dấu vết và để báo cáo tháng loại phần đã
    // hoàn ra khỏi tổng xuất.
    @Column(name = "is_returned", nullable = false)
    private Boolean returned = false;

    @Column(name = "returned_at")
    private LocalDateTime returnedAt;
}

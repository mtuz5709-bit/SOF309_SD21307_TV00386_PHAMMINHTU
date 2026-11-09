package com.titangear.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "stock_in")
public class StockIn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // Model 2: mỗi lô nhập gắn theo BIẾN THỂ. NULL = sản phẩm không có biến thể
    // (lô ở mức sản phẩm, giữ nguyên hành vi cũ).
    @ManyToOne
    @JoinColumn(name = "variant_id")
    private ProductVariant variant;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "import_price", nullable = false)
    private BigDecimal importPrice;

    @Column(name = "supplier")
    private String supplier;

    // Vị trí cất hàng của lô: khu (A/B/C) và số kệ. Đặt ở lô chứ không đặt ở sản
    // phẩm, vì hai lô của cùng một sản phẩm có thể nằm hai chỗ khác nhau.
    // Vị trí đổi được khi dọn kho nên không in lên tem dán thùng.
    @Column(name = "khu", length = 1)
    private String khu;

    @Column(name = "ke")
    private Integer ke;

    // Ghép nhãn vị trí để hiện ra màn hình, dạng "KHU A · KỆ 3". Lô cũ chưa gắn
    // vị trí thì trả về "— chưa gán —".
    public String getViTri() {
        if (khu == null || khu.isBlank() || ke == null) return "— chưa gán —";
        return "KHU " + khu + " · KỆ " + ke;
    }

    // Kiểm lô đã gắn vị trí chưa, để JSP tô màu cảnh báo cho lô chưa gắn.
    public boolean isCoViTri() {
        return khu != null && !khu.isBlank() && ke != null;
    }

    // Định dạng ngày nhập thành dd/MM/yyyy cho JSP đọc. Thử tag fmt:formatDate của
    // JSTL thì không nhận LocalDateTime, còn in thẳng thì ra dạng 2026-08-02T15:30.
    public String getNgayNhapVN() {
        return importedAt == null ? "" :
                importedAt.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    @Column(name = "note", columnDefinition = "NVARCHAR(MAX)")
    private String note;

    @ManyToOne
    @JoinColumn(name = "imported_by", nullable = false)
    private User importedBy;

    @Column(name = "imported_at")
    private LocalDateTime importedAt;

    // Ngày nhận hàng thực tế từ nhà cung cấp, tách riêng với ngày kiểm hàng.
    @Column(name = "received_at")
    private LocalDateTime receivedAt;

    // Ngày kiểm hàng, đối chiếu số lượng thực nhận với số trên hóa đơn.
    @Column(name = "checked_at")
    private LocalDateTime checkedAt;

    // Số lượng còn lại trong lô này, FIFO trừ dần mỗi lần xuất. Java chỉ đặt giá trị
    // ban đầu lúc nhập lô; việc trừ dần do trigger TRG_StockOut_ApplyToStockIn làm.
    @Column(name = "remaining_quantity", nullable = false, updatable = false)
    private Integer remainingQuantity;
}

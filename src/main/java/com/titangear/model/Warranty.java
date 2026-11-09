package com.titangear.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

// PHIẾU BẢO HÀNH — mỗi CHIẾC hàng bán ra là 1 phiếu (mua 3 con = 3 phiếu,
// 3 serial khác nhau). Phiếu được tạo TỰ ĐỘNG khi đơn chuyển sang Hoàn thành.
//
// serial_no do hệ thống tự sinh dạng SR000001 — admin không phải gõ.
// Lưu ý: đây là mã bảo hành CỦA CỬA HÀNG, không phải serial in trên vỏ máy.
@Data
@Entity
@Table(name = "warranties")
public class Warranty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "serial_no", nullable = false, unique = true)
    private String serialNo;

    @Column(name = "order_item_id", nullable = false)
    private Integer orderItemId;

    @Column(name = "product_id", nullable = false)
    private Integer productId;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    // Ngày giao hàng (đơn chuyển Hoàn thành)
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    // = startDate + số tháng bảo hành của sản phẩm
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "note")
    private String note;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Còn bao nhiêu ngày nữa hết hạn thì coi là "sắp hết" (để nhắc admin trước).
    public static final int NGUONG_SAP_HET = 30;

    // Còn hạn bảo hành không — TÍNH SỐNG tại thời điểm gọi, không lưu vào CSDL.
    // Nhờ vậy phiếu tự chuyển sang "Hết hạn" đúng ngày mà không cần job chạy nền;
    // job có chết một hôm thì dữ liệu vẫn không bị sai.
    @Transient
    public boolean isConHan() {
        return endDate != null && !LocalDate.now().isAfter(endDate);
    }

    // Số ngày còn lại; ÂM nghĩa là đã hết hạn bấy nhiêu ngày.
    @Transient
    public long getSoNgayConLai() {
        return endDate == null ? 0 : ChronoUnit.DAYS.between(LocalDate.now(), endDate);
    }

    // Còn hạn nhưng dưới 30 ngày — hiện badge vàng để admin chăm khách trước khi rớt hạn.
    @Transient
    public boolean isSapHetHan() {
        return isConHan() && getSoNgayConLai() <= NGUONG_SAP_HET;
    }
}

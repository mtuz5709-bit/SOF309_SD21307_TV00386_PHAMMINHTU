package com.titangear.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

// Một lần tiếp nhận sửa chữa/bảo hành của 1 phiếu. Nhiều lần sửa cùng 1 phiếu
// thì nhiều dòng — xem được lịch sử máy đó đã sửa mấy lần.
@Data
@Entity
@Table(name = "warranty_repairs")
public class WarrantyRepair {

    public static final String TIEP_NHAN = "TIEP_NHAN";
    public static final String DANG_SUA  = "DANG_SUA";
    public static final String XONG      = "XONG";
    public static final String TU_CHOI   = "TU_CHOI";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "warranty_id", nullable = false)
    private Integer warrantyId;

    @Column(name = "noi_dung", nullable = false)
    private String noiDung;

    @Column(name = "trang_thai", nullable = false)
    private String trangThai;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}

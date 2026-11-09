package com.titangear.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

// Một dòng nhật ký thao tác quản trị: AI làm GÌ, trên CÁI GÌ, LÚC NÀO.
//
// Bảng này CHỈ GHI THÊM, không bao giờ sửa hay xóa — nhật ký mà sửa được thì
// không còn giá trị làm bằng chứng.
@Data
@Entity
@Table(name = "audit_log")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id")
    private Integer userId;

    // Email chép lại TẠI THỜI ĐIỂM thao tác.
    // Không nối khóa ngoại để lấy email hiện tại: người đó có thể đổi email hoặc
    // bị xóa, khi đó nhật ký sẽ không còn đọc được là ai đã làm.
    @Column(name = "user_email")
    private String userEmail;

    // Mã hành động, xem hằng số trong AuditLogService.
    @Column(name = "hanh_dong", nullable = false)
    private String hanhDong;

    // Tên tiếng Việt của hành động để JSP hiện ra màn hình, thay cho mã in hoa.
    public String getTenHanhDong() {
        return com.titangear.service.AuditLogService.tenTiengViet(hanhDong);
    }

    // Tên bảng bị tác động: "orders", "products", "system_settings"…
    @Column(name = "doi_tuong")
    private String doiTuong;

    @Column(name = "doi_tuong_id")
    private Integer doiTuongId;

    // Chỉ ghi phần THAY ĐỔI, không chép cả bản ghi — bảng phình nhanh mà khó đọc.
    @Column(name = "gia_tri_cu")
    private String giaTriCu;

    @Column(name = "gia_tri_moi")
    private String giaTriMoi;

    // Câu tiếng Việt để admin đọc hiểu ngay, khỏi phải suy từ mã hành động.
    @Column(name = "mo_ta")
    private String moTa;

    @Column(name = "dia_chi_ip")
    private String diaChiIp;

    @Column(name = "thoi_diem", nullable = false)
    private LocalDateTime thoiDiem;
}

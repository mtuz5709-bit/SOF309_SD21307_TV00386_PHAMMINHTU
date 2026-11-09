package com.titangear.repository;

import com.titangear.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Integer> {

    // Trang nhật ký — mới nhất trước.
    Page<AuditLog> findAllByOrderByThoiDiemDesc(Pageable pageable);

    // Lọc theo loại hành động.
    Page<AuditLog> findByHanhDongOrderByThoiDiemDesc(String hanhDong, Pageable pageable);

    // Lịch sử của MỘT đối tượng — vd toàn bộ thao tác trên đơn #1039.
    List<AuditLog> findByDoiTuongAndDoiTuongIdOrderByThoiDiemDesc(String doiTuong, Integer doiTuongId);

    // Các loại hành động đã từng ghi — dựng ô lọc mà không cần khai cứng.
    @org.springframework.data.jpa.repository.Query(
            "SELECT DISTINCT a.hanhDong FROM AuditLog a ORDER BY a.hanhDong")
    List<String> cacLoaiHanhDong();
}

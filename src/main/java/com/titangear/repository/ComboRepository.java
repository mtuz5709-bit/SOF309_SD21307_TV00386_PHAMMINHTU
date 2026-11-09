package com.titangear.repository;

import com.titangear.model.Combo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComboRepository extends JpaRepository<Combo, Integer> {

    // Nạp combo kèm luôn sản phẩm bên trong bằng JOIN FETCH, gói tất cả vào một câu
    // truy vấn thay vì để Hibernate nạp dần thành hàng chục lượt đi lại CSDL.
    // Có DISTINCT vì JOIN với bảng con làm nhân bản dòng combo cha.
    @Query("SELECT DISTINCT c FROM Combo c "
            + "LEFT JOIN FETCH c.items i "
            + "LEFT JOIN FETCH i.product "
            + "LEFT JOIN FETCH i.variant "
            + "WHERE c.isActive = true "
            + "ORDER BY c.id DESC")
    List<Combo> findDangBan();

    @Query("SELECT DISTINCT c FROM Combo c "
            + "LEFT JOIN FETCH c.items i "
            + "LEFT JOIN FETCH i.product "
            + "LEFT JOIN FETCH i.variant "
            + "WHERE c.id = :id AND c.isActive = true")
    Optional<Combo> findDangBanTheoId(@Param("id") Integer id);

    // Trang quản trị: lấy CẢ combo đã tắt, kèm sản phẩm bên trong.
    @Query("SELECT DISTINCT c FROM Combo c "
            + "LEFT JOIN FETCH c.items i "
            + "LEFT JOIN FETCH i.product "
            + "LEFT JOIN FETCH i.variant "
            + "ORDER BY c.id DESC")
    List<Combo> findTatCaKemMon();

    // Một combo bất kể bật/tắt — dùng cho form sửa.
    @Query("SELECT DISTINCT c FROM Combo c "
            + "LEFT JOIN FETCH c.items i "
            + "LEFT JOIN FETCH i.product "
            + "LEFT JOIN FETCH i.variant "
            + "WHERE c.id = :id")
    Optional<Combo> findKemMonTheoId(@Param("id") Integer id);
}

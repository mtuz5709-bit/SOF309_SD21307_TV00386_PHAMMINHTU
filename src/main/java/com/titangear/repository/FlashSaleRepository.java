package com.titangear.repository;

import com.titangear.model.FlashSale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FlashSaleRepository extends JpaRepository<FlashSale, Integer> {

    List<FlashSale> findAllByOrderByStartAtDesc();

    // Các flash sale đang chạy của một kiểu hiển thị (BANNER hoặc TEXT_BAR),
    // sắp theo display_order để làm carousel/danh sách trên trang khách.
    // Nơi gọi truyền `now` cho cả hai tham số thời gian (start <= now <= end).
    List<FlashSale> findByDisplayTypeAndIsActiveTrueAndStartAtLessThanEqualAndEndAtGreaterThanEqualOrderByDisplayOrderAscStartAtAsc(
            FlashSale.DisplayType displayType, LocalDateTime nowForStart, LocalDateTime nowForEnd);
}

package com.titangear.repository;

import com.titangear.model.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Integer> {

    // Khách gõ mã không phân biệt hoa/thường
    Optional<Voucher> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);

    List<Voucher> findAllByOrderByIdDesc();

    // Tăng lượt dùng của mã bằng một câu UPDATE duy nhất, kèm luôn điều kiện chưa
    // vượt giới hạn. Để CSDL tự cộng thì hai khách đặt cùng lúc không đua nhau ghi đè
    // và mã giới hạn 100 lượt không thể bị dùng quá 100.
    // Trả về số dòng đổi: 0 nghĩa là mã vừa hết lượt do người khác dùng trước.
    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE vouchers SET used_count = ISNULL(used_count, 0) + 1 "
                 + "WHERE id = :id AND (usage_limit IS NULL OR ISNULL(used_count, 0) < usage_limit)",
           nativeQuery = true)
    int tangLuotDung(@Param("id") Integer id);
}

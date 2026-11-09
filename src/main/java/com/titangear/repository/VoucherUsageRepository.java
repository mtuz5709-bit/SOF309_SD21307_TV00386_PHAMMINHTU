package com.titangear.repository;

import com.titangear.model.VoucherUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VoucherUsageRepository extends JpaRepository<VoucherUsage, Integer> {

    // Khách này đã dùng mã này chưa (cho điều kiện "mỗi khách 1 lần")
    boolean existsByVoucherIdAndUserId(Integer voucherId, Integer userId);
}

package com.titangear.repository;

import com.titangear.model.WarrantyRepair;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WarrantyRepairRepository extends JpaRepository<WarrantyRepair, Integer> {

    // Lịch sử sửa chữa của 1 phiếu, mới nhất trước
    List<WarrantyRepair> findByWarrantyIdOrderByIdDesc(Integer warrantyId);
}

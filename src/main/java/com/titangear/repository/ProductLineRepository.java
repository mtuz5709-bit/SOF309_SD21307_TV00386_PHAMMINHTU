package com.titangear.repository;

import com.titangear.model.ProductLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductLineRepository extends JpaRepository<ProductLine, Integer> {

    List<ProductLine> findAllByOrderByNameAsc();

    /** Tìm theo tên KHÔNG phân biệt hoa thường — để admin gõ "atk f1" không tạo
        thêm dòng trùng với "ATK F1" đã có. */
    Optional<ProductLine> findByNameIgnoreCase(String name);
}

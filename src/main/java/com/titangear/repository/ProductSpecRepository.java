package com.titangear.repository;

import com.titangear.model.ProductSpec;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface ProductSpecRepository extends JpaRepository<ProductSpec, Integer> {
    @Modifying
    @Transactional
    @Query("DELETE FROM ProductSpec s WHERE s.product.id = :productId")
    void deleteByProductId(Integer productId);
}
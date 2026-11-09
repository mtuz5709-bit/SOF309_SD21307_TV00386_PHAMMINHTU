package com.titangear.repository;

import com.titangear.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    // Tìm danh mục theo tên
    Category findByName(String name);
}
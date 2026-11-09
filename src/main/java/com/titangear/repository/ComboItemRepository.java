package com.titangear.repository;

import com.titangear.model.ComboItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComboItemRepository extends JpaRepository<ComboItem, Integer> {

    List<ComboItem> findByComboId(Integer comboId);

    void deleteByComboId(Integer comboId);
}

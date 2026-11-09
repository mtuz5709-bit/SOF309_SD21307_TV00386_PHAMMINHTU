package com.titangear.model;

import jakarta.persistence.*;
import lombok.Data;

// Dòng sản phẩm: nhóm các phiên bản cùng họ lại với nhau. Ví dụ dòng "ATK Blazing
// Sky F1" gồm 4 sản phẩm F1 Ultimate, F1 Pro, F1 Pro Max, F1 Extreme.
// Mỗi phiên bản là một sản phẩm riêng vì có bộ thông số kỹ thuật riêng; bảng này
// nối chúng lại để trang chi tiết dựng được hàng nút chuyển nhanh giữa các bản.
// Cột products.line_id để NULL nếu hàng lẻ không thuộc dòng nào.
@Data
@Entity
@Table(name = "product_lines")
public class ProductLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", nullable = false)
    private String name;
}

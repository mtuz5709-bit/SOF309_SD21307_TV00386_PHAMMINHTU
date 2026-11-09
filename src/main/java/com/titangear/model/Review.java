package com.titangear.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

// Đánh giá sản phẩm — chỉ tạo được qua ReviewService.submitReview() sau khi
// đã kiểm tra user thực sự có đơn hàng COMPLETED chứa sản phẩm này, nên mọi
// review trong hệ thống đều là "đã mua hàng thật" (hiển thị badge
// "Verified by shop" cho tất cả, không cần cột is_verified riêng).
@Data
@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Column(name = "comment")
    @jakarta.validation.constraints.Size(max = 2000, message = "Nội dung đánh giá tối đa 2000 ký tự")
    private String comment;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

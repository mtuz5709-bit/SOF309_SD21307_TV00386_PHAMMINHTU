package com.titangear.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

// Một chương trình Flash Sale, hiển thị theo một trong hai kiểu:
//   BANNER   - ảnh, tiêu đề và nút bấm. Nhiều banner cùng chạy thì thành slide xoay
//              vòng ở trang chủ.
//   TEXT_BAR - một dòng chữ chạy trên đầu trang, không có ảnh.
// Trường displayType quyết định dùng nhóm cột nào. DB có ràng buộc CHECK để hai
// kiểu không lẫn vào nhau.
@Data
@Entity
@Table(name = "flash_sales")
public class FlashSale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "display_type", nullable = false, length = 20)
    private DisplayType displayType;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    // ---- Chỉ dùng khi displayType = BANNER ----
    @Column(name = "banner_image_url")
    private String bannerImageUrl;

    @Column(name = "banner_title")
    private String bannerTitle;

    @Column(name = "banner_subtitle")
    private String bannerSubtitle;

    @Column(name = "banner_cta_text")
    private String bannerCtaText;

    @Column(name = "banner_cta_link")
    private String bannerCtaLink;

    // ---- Chỉ dùng khi displayType = TEXT_BAR ----
    @Column(name = "text_bar_content")
    private String textBarContent;

    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Danh sách sản phẩm được giảm giá, chỉ để đọc. Việc thêm/xóa item làm qua
    // FlashSaleItemRepository trong FlashSaleService, không bật cascade ở đây.
    @OneToMany(mappedBy = "flashSale", fetch = FetchType.LAZY)
    private List<FlashSaleItem> items;

    public enum DisplayType { BANNER, TEXT_BAR }
}

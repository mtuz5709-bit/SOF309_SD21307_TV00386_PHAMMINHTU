package com.titangear.service;

import com.titangear.model.Product;
import com.titangear.model.ProductLine;
import com.titangear.model.ProductSpec;
import com.titangear.repository.ProductLineRepository;
import com.titangear.repository.ProductRepository;
import com.titangear.repository.ProductSpecRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired private ProductRepository productRepository;
    @Autowired private ProductSpecRepository productSpecRepository;
    @Autowired private ProductLineRepository productLineRepository;
    @Autowired private AuditLogService auditLogService;

    // N sản phẩm đang bán mới nhất — trang chủ dùng, không nạp cả kho.
    public List<Product> layMoiNhat(int soLuong) {
        return productRepository.findMoiNhat(
                org.springframework.data.domain.PageRequest.of(0, Math.max(1, soLuong)));
    }

    public List<Product> getAllProducts() {
        return productRepository.findByIsActiveTrue();
    }

    public Optional<Product> findById(Integer id) {
        return productRepository.findById(id);
    }

    public List<Product> getByCategory(Integer categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }

    // Tìm theo tên — TRẢ CẢ hàng đã ngừng bán. Dành cho trang quản trị: admin
    // cần tìm lại sản phẩm đã ẩn để bật lại hoặc xem lịch sử.
    // Phía khách hàng KHÔNG được dùng hàm này, xem timKiemChoKhach().
    public List<Product> search(String keyword) {
        return productRepository.findByNameContainingIgnoreCase(keyword);
    }

    // Tìm theo tên cho phía khách, chỉ trả hàng đang bán. Thanh gợi ý tìm kiếm dùng
    // hàm này chứ không dùng search(), để không gợi ý ra hàng khách bấm vào không mua được.
    public List<Product> timKiemChoKhach(String keyword) {
        return productRepository.findByNameContainingIgnoreCaseAndIsActiveTrue(keyword);
    }

    public List<Product> filterByPrice(BigDecimal min, BigDecimal max) {
        return productRepository.findByPriceBetween(min, max);
    }

    public List<Product> getInStock() {
        return productRepository.findByStockQuantityGreaterThan(0);
    }

    public List<Product> getOutOfStock() {
        return productRepository.findByStockQuantityEquals(0);
    }

    public List<Product> filterByMonthYear(Integer month, Integer year) {
        return productRepository.findByMonthYear(month, year);
    }

    public Product addProduct(Product product) {
        product.setIsActive(true);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        return productRepository.save(product);
    }

    // Cập nhật sản phẩm: nạp bản ghi gốc từ CSDL rồi chỉ chép các trường mà form được
    // phép sửa. Lưu thẳng đối tượng từ form sẽ ghi đè cả những cột không có trên form
    // thành NULL.
    @Transactional
    public void updateProduct(Product product) {
        Product existing = productRepository.findById(product.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm #" + product.getId()));

        // Ghi nhật ký ĐỔI GIÁ — thao tác nhạy cảm nhất, phải biết ai đổi và đổi từ bao nhiêu
        if (existing.getPrice() != null && product.getPrice() != null
                && existing.getPrice().compareTo(product.getPrice()) != 0) {
            auditLogService.ghi(AuditLogService.SUA_GIA, "products", existing.getId(),
                    existing.getPrice().toPlainString(), product.getPrice().toPlainString(),
                    "Đổi giá \"" + existing.getName() + "\"");
        }

        existing.setName(product.getName());
        existing.setCategory(product.getCategory());
        existing.setLine(product.getLine());   // để trống = gỡ khỏi dòng, nên gán thẳng
        existing.setDescription(product.getDescription());
        existing.setPrice(product.getPrice());

        // Thuộc tính lọc + bảo hành: form gửi rỗng nghĩa là "không áp dụng" nên
        // gán thẳng (kể cả null) — KHÔNG bọc if, nếu không admin sẽ không bao giờ
        // xoá được giá trị đã chọn.
        existing.setSwitchType(rongThanhNull(product.getSwitchType()));
        existing.setWeightGroup(rongThanhNull(product.getWeightGroup()));
        existing.setConnectivity(rongThanhNull(product.getConnectivity()));
        existing.setWarrantyMonths(product.getWarrantyMonths());
        if (product.getImageUrl() != null) {
            existing.setImageUrl(product.getImageUrl());
        }
        if (product.getIsActive() != null) {
            existing.setIsActive(product.getIsActive());
        }
        existing.setUpdatedAt(LocalDateTime.now());
        productRepository.save(existing);
    }

    // Form gửi ô "— Không áp dụng —" thành chuỗi rỗng; lưu NULL cho sạch dữ liệu.
    private String rongThanhNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    // ================= DÒNG SẢN PHẨM =================

    public List<ProductLine> tatCaDongSanPham() {
        return productLineRepository.findAllByOrderByNameAsc();
    }

    // Lấy dòng sản phẩm cho form: ưu tiên tên admin vừa gõ, không có thì lấy dòng đã
    // chọn trong danh sách. Gõ trùng tên dòng đã có thì dùng lại dòng đó, so sánh không
    // phân biệt hoa thường để "ATK F1" và "atk f1" không thành hai dòng khác nhau.
    @Transactional
    public ProductLine layHoacTaoDong(Integer lineId, String tenMoi) {
        if (tenMoi != null && !tenMoi.isBlank()) {
            String ten = tenMoi.trim();
            return productLineRepository.findByNameIgnoreCase(ten)
                    .orElseGet(() -> {
                        ProductLine d = new ProductLine();
                        d.setName(ten);
                        return productLineRepository.save(d);
                    });
        }
        return lineId != null ? productLineRepository.findById(lineId).orElse(null) : null;
    }

    // Các sản phẩm CÙNG DÒNG với sản phẩm đang xem (gồm cả chính nó, để trang chi
    // tiết tô sáng nút đang đứng). Chỉ lấy hàng còn bán.
    public List<Product> cungDong(Product sp) {
        if (sp == null || sp.getLine() == null) return List.of();
        return productRepository.findByIsActiveTrue().stream()
                .filter(p -> p.getLine() != null && p.getLine().getId().equals(sp.getLine().getId()))
                .sorted(java.util.Comparator.comparing(Product::getId))
                .toList();
    }

    public void deleteProduct(Integer id) {
        productRepository.findById(id).ifPresent(p -> {
            p.setIsActive(false);
            productRepository.save(p);
        });
    }


    public void deleteSpecsByProductId(Integer productId) {
        productSpecRepository.deleteByProductId(productId);
    }

    public void saveSpec(ProductSpec spec) {
        productSpecRepository.save(spec);
    }
}
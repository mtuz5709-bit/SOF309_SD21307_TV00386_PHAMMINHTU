package com.titangear.controller;

import com.titangear.model.Product;
import com.titangear.model.ProductVariant;
import com.titangear.model.StockIn;
import com.titangear.repository.OrderItemRepository;
import com.titangear.service.ProductService;
import com.titangear.service.ProductVariantService;
import com.titangear.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin/products")
public class ProductVariantController {

    @Autowired private ProductVariantService variantService;
    @Autowired private ProductService productService;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private StockService stockService;
    @Autowired private ServletContext servletContext;

    // Trang quản lý biến thể
    @GetMapping("/{productId}/variants")
    public String variantPage(@PathVariable Integer productId, Model model) {
        // Sản phẩm không tồn tại thì về danh sách, không mở màn hình biến thể
        // mồ côi — thêm biến thể vào một productId không có thật sẽ vỡ khoá ngoại.
        Optional<Product> sanPham = productService.findById(productId);
        if (sanPham.isEmpty()) {
            return "redirect:/admin/products";
        }
        model.addAttribute("product", sanPham.get());
        List<ProductVariant> variants = variantService.getByProductId(productId);
        model.addAttribute("variants", variants);

        // Model 2 — tồn do trigger tính từ lô; ở đây tính thêm ĐANG GIỮ (reserved)
        // để JSP hiện: tồn / đang giữ / bán được (available = tồn − đang giữ).
        Map<Integer, Integer> reservedMap = new HashMap<>();
        int totalStock = 0;
        for (ProductVariant v : variants) {
            Integer r = orderItemRepository.getReservedQtyByVariant(v.getId());
            reservedMap.put(v.getId(), r != null ? r : 0);
            totalStock += (v.getStock() != null ? v.getStock() : 0);
        }
        model.addAttribute("reservedMap", reservedMap);
        model.addAttribute("totalStock", totalStock);

        // Hướng A — các lô "mức sản phẩm" (variant_id NULL) còn hàng: chỉ là hàng
        // BỊ KẸT khi SP đã có biến thể (nhập trước khi tạo biến thể). Truyền xuống
        // để hiện ô "gán vào biến thể". SP chưa có biến thể thì không coi là kẹt.
        List<StockIn> strandedLots = variants.isEmpty()
                ? java.util.Collections.emptyList()
                : stockService.getStrandedLots(productId);
        int strandedTotal = strandedLots.stream()
                .mapToInt(si -> si.getRemainingQuantity() != null ? si.getRemainingQuantity() : 0)
                .sum();
        model.addAttribute("strandedLots", strandedLots);
        model.addAttribute("strandedTotal", strandedTotal);

        return "admin/product-variants";
    }

    // Hướng A — GÁN 1 lô tồn "mức sản phẩm" vào 1 biến thể (đổi variant_id của lô).
    @PostMapping("/{productId}/variants/assign-stock")
    public String assignStock(@PathVariable Integer productId,
                              @RequestParam Integer lotId,
                              @RequestParam Integer variantId) {
        // Chốt an toàn: biến thể phải thuộc đúng sản phẩm này
        boolean variantOfThisProduct = variantService.findById(variantId)
                .map(v -> v.getProduct() != null && productId.equals(v.getProduct().getId()))
                .orElse(false);
        if (variantOfThisProduct) {
            stockService.assignLotToVariant(lotId, productId, variantId);
            return "redirect:/admin/products/" + productId + "/variants?assigned=true";
        }
        return "redirect:/admin/products/" + productId + "/variants?error=assign";
    }

    // Thêm biến thể
    @PostMapping("/{productId}/variants/add")
    public String addVariant(
            @PathVariable Integer productId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String colorCode,
            @RequestParam(value = "existingImage", required = false) String existingImage,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) {

        // Model 2 — KHÔNG còn nhập/phân bổ stock tay. Biến thể mới tồn = 0,
        // hàng vào qua "Nhập kho theo biến thể" (cột stock do trigger quản lý).
        ProductVariant v = new ProductVariant();
        Product p = new Product();
        p.setId(productId);
        v.setProduct(p);
        v.setName(name);
        v.setColor(color);
        v.setColorCode(colorCode);
        // Giá của biến thể luôn lấy theo giá sản phẩm, mọi màu bán chung một giá.
        // Cho nhập giá riêng từng màu thì khách xem một giá mà giỏ hàng tính một
        // giá khác, vì giỏ hàng đọc products.price.
        productService.findById(productId).ifPresent(sp -> v.setPrice(sp.getPrice()));
        v.setIsActive(true);

        // Ưu tiên: nếu nhập tên ảnh có sẵn thì dùng, không thì upload mới
        if (existingImage != null && !existingImage.trim().isEmpty()) {
            v.setImageUrl(existingImage.trim());
        } else if (imageFile != null && !imageFile.isEmpty()) {
            String fileName = handleUpload(imageFile);
            if (fileName != null) v.setImageUrl(fileName);
        }

        variantService.save(v);
        return "redirect:/admin/products/" + productId + "/variants?added=true";
    }

    // Sửa biến thể — POST từ form inline edit
    @PostMapping("/{productId}/variants/edit/{variantId}")
    public String editVariant(
            @PathVariable Integer productId,
            @PathVariable Integer variantId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String colorCode,
            @RequestParam(value = "existingImage", required = false) String existingImage,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) {

        // Model 2 — chỉ sửa thông tin biến thể; tồn kho KHÔNG sửa tay ở đây
        // (đến từ nhập kho, do trigger quản lý).
        variantService.findById(variantId).ifPresent(v -> {
            v.setName(name);
            v.setColor(color);
            v.setColorCode(colorCode);
            productService.findById(productId).ifPresent(sp -> v.setPrice(sp.getPrice()));

            // Ưu tiên: nếu nhập tên ảnh có sẵn thì dùng, không thì upload mới
            if (existingImage != null && !existingImage.trim().isEmpty()) {
                v.setImageUrl(existingImage.trim());
            } else if (imageFile != null && !imageFile.isEmpty()) {
                String fileName = handleUpload(imageFile);
                if (fileName != null) v.setImageUrl(fileName);
            }

            variantService.save(v);
        });

        return "redirect:/admin/products/" + productId + "/variants?updated=true";
    }

    // Ngừng bán biến thể — Model 2: XOÁ MỀM (is_active=false) thay vì DELETE.
    // Giữ nguyên lịch sử đơn (order_items.variant_id) + lô kho, chỉ ẩn khỏi bán.
    @PostMapping("/{productId}/variants/delete/{variantId}")
    public String deleteVariant(@PathVariable Integer productId,
                                @PathVariable Integer variantId) {
        ProductVariant v = variantService.findById(variantId).orElse(null);
        if (v == null) {
            return "redirect:/admin/products/" + productId + "/variants";
        }
        // CHẶN ẩn khi còn tồn: nếu ẩn biến thể còn hàng, số tồn đó vẫn cộng vào
        // tồn sản phẩm nhưng KHÔNG bán được (biến thể đã ẩn) -> "kẹt" vô hình,
        // mà ô "gán" (hướng A) cũng không bắt được vì lô đã có variant_id.
        int stock = v.getStock() != null ? v.getStock() : 0;
        if (stock > 0) {
            return "redirect:/admin/products/" + productId + "/variants?error=hasStock&stock=" + stock;
        }
        v.setIsActive(false);
        variantService.save(v);
        return "redirect:/admin/products/" + productId + "/variants?deleted=true";
    }

    // JSON GỌN cho form nhập kho — chỉ trả id/name/color/stock, tránh serialize
    // cả entity (Product <-> variants gây đệ quy Jackson).
    @GetMapping("/api/variants-lite/{productId}")
    @ResponseBody
    public List<Map<String, Object>> getVariantsLite(@PathVariable Integer productId) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (ProductVariant v : variantService.getByProductId(productId)) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", v.getId());
            m.put("name", v.getName());
            m.put("color", v.getColor());
            // Mã màu để form nhập kho vẽ chấm tròn đúng màu biến thể.
            // Biến thể chưa đặt mã màu -> trả xám nhạt, tránh chấm trong suốt.
            m.put("colorCode", v.getColorCode() != null && !v.getColorCode().isBlank()
                    ? v.getColorCode() : "#CFD4DC");
            m.put("stock", v.getStock() != null ? v.getStock() : 0);
            out.add(m);
        }
        return out;
    }

    /** Thư mục upload đọc từ application.properties (upload.dir) —
        nằm NGOÀI target/ nên không mất khi mvn clean, không cứng ổ đĩa */
    @org.springframework.beans.factory.annotation.Value("${upload.dir}")
    private String uploadDir;

    private String handleUpload(MultipartFile file) {
        try {
            String originalName = file.getOriginalFilename();
            if (originalName == null) return null;
            String ext = originalName.substring(originalName.lastIndexOf(".")).toLowerCase();
            if (!ext.matches("\\.(jpg|jpeg|png|gif|webp)")) return null;
            String fileName = System.currentTimeMillis() + "_" + originalName.replaceAll("\\s+", "_");
            File uploadFolder = new File(uploadDir);
            if (!uploadFolder.exists()) uploadFolder.mkdirs();
            file.transferTo(new File(uploadDir, fileName));
            return fileName;
        } catch (IOException e) {
            return null;
        }
    }
}
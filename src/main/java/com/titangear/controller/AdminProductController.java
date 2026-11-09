package com.titangear.controller;

import com.titangear.config.RequirePermission;

import com.titangear.model.Product;
import com.titangear.model.ProductSpec;
import com.titangear.model.User;
import com.titangear.service.CategoryService;
import com.titangear.service.ProductService;
import com.titangear.service.UserService;
import com.titangear.service.WarehousePermissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.ServletContext;
import jakarta.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminProductController {

    private static final Logger log = LoggerFactory.getLogger(AdminProductController.class);

    @Autowired private ProductService productService;
    @Autowired private CategoryService categoryService;
    @Autowired private ServletContext servletContext;

    @RequirePermission(WarehousePermissionService.CAN_VIEW_PRODUCTS)
    @GetMapping("/products")
    public String listProducts(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer category,
            @RequestParam(required = false) Integer page,
            Authentication auth,
            Model model) {

        List<Product> products;
        if (q != null && !q.trim().isEmpty()) {
            products = productService.search(q.trim());          // tìm kiếm theo tên (ưu tiên)
        } else if (month != null || year != null) {
            products = productService.filterByMonthYear(month, year);
        } else {
            products = productService.getAllProducts();
        }

        // Lọc theo danh mục (tab trên trang). Lọc SAU các nhánh trên nên kết hợp
        // được với tìm kiếm/lọc tháng, không phải viết thêm nhánh if chồng chéo.
        if (category != null) {
            final Integer dm = category;
            products = products.stream()
                    .filter(p -> p.getCategory() != null && dm.equals(p.getCategory().getId()))
                    .toList();
        }

        int currentYear = LocalDate.now().getYear();
        List<Integer> years = new java.util.ArrayList<>();
        for (int y = currentYear; y >= currentYear - 4; y--) years.add(y);

        // ---- Số liệu cho 4 thẻ chỉ số + tab lọc theo danh mục ----
        // Tính Ở ĐÂY chứ không tính trong JSP: JSP chỉ nên lo hiển thị, đưa vòng lặp
        // cộng dồn vào đó là phá nguyên tắc tách tầng Controller–View.
        // Đếm trên TOÀN BỘ hàng đang bán (không phải danh sách sau khi lọc/tìm kiếm)
        // để mấy con số này đứng yên, không nhảy mỗi lần admin gõ tìm kiếm.
        List<Product> tatCa = productService.getAllProducts();

        long soConHang = tatCa.stream().filter(p -> p.getStockQuantity() != null && p.getStockQuantity() > 0).count();
        long soSapHet  = tatCa.stream().filter(p -> p.getStockQuantity() != null
                                                 && p.getStockQuantity() > 0 && p.getStockQuantity() <= 5).count();

        // Giá trị tồn kho = tổng (giá bán × số lượng còn) — cho admin biết đang
        // "chôn" bao nhiêu tiền trong kho
        java.math.BigDecimal giaTriKho = java.math.BigDecimal.ZERO;
        for (Product p : tatCa) {
            if (p.getPrice() != null && p.getStockQuantity() != null) {
                giaTriKho = giaTriKho.add(p.getPrice().multiply(java.math.BigDecimal.valueOf(p.getStockQuantity())));
            }
        }

        // Số sản phẩm của từng danh mục — hiện trên tab
        java.util.Map<Integer, Long> demDanhMuc = new java.util.HashMap<>();
        for (Product p : tatCa) {
            if (p.getCategory() != null) demDanhMuc.merge(p.getCategory().getId(), 1L, Long::sum);
        }

        model.addAttribute("tongSanPham", tatCa.size());
        model.addAttribute("soConHang",   soConHang);
        model.addAttribute("soHetHang",   tatCa.size() - soConHang);
        model.addAttribute("soSapHet",    soSapHet);
        model.addAttribute("giaTriKho",   giaTriKho);
        model.addAttribute("demDanhMuc",  demDanhMuc);

        // ---- Phân trang: 10 sản phẩm mỗi trang ----
        // Cắt trang SAU khi đã lọc và tìm kiếm, để số trang tính trên đúng tập
        // kết quả đang xem chứ không phải trên toàn bộ kho.
        // Kẹp số trang về khoảng hợp lệ: gõ ?page=99999 hay ?page=-5 trên URL
        // cũng không làm subList ném IndexOutOfBoundsException.
        final int MOI_TRANG = 10;
        int tongKetQua = products.size();
        int tongTrang  = Math.max(1, (int) Math.ceil(tongKetQua / (double) MOI_TRANG));
        int trang      = (page == null || page < 1) ? 1 : Math.min(page, tongTrang);
        int tu         = (trang - 1) * MOI_TRANG;
        int den        = Math.min(tu + MOI_TRANG, tongKetQua);
        List<Product> sanPhamTrangNay = products.subList(tu, den);

        model.addAttribute("trang",      trang);
        model.addAttribute("tongTrang",  tongTrang);
        model.addAttribute("tongKetQua", tongKetQua);

        model.addAttribute("products",      sanPhamTrangNay);
        model.addAttribute("categories",    categoryService.getAll());
        model.addAttribute("selectedMonth", month);
        model.addAttribute("selectedYear",  year);
        model.addAttribute("years",         years);
        return "admin/products";
    }

    @GetMapping("/products/add")
    public String addForm(Model model) {
        model.addAttribute("product",    new Product());
        model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("productLines", productService.tatCaDongSanPham());
        return "admin/product-form";
    }

    @PostMapping("/products/add")
    public String addProduct(@Valid @ModelAttribute Product product,
                             BindingResult result,
                             @RequestParam(required = false) Integer lineId,
                             @RequestParam(required = false) String lineNameMoi,
                             @RequestParam(required = false) List<String> specNames,
                             @RequestParam(required = false) List<String> specValues,
                             @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                             Model model) {
        // Validate phía server — dữ liệu bậy (tên rỗng, giá <= 0) bị chặn dù client lách
        if (result.hasErrors()) {
            model.addAttribute("validationErrors", messagesOf(result));
            model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("productLines", productService.tatCaDongSanPham());
            return "admin/product-form";
        }
        if (imageFile != null && !imageFile.isEmpty()) {
            String fileName = handleImageUpload(imageFile);
            if (fileName != null) product.setImageUrl(fileName);
        }
        // Dòng sản phẩm: admin gõ tên mới thì tạo dòng, không thì lấy dòng đã chọn
        product.setLine(productService.layHoacTaoDong(lineId, lineNameMoi));

        Product saved = productService.addProduct(product);
        saveSpecs(saved, specNames, specValues);
        return "redirect:/admin/products";
    }

    @GetMapping("/products/edit/{id}")
    public String editForm(@PathVariable("id") Integer id, Model model) {
        // Id không có thật thì về danh sách, không mở form sửa rỗng — form rỗng
        // mà bấm Lưu sẽ tạo ra bản ghi rác hoặc ghi đè nhầm.
        Optional<Product> sanPham = productService.findById(id);
        if (sanPham.isEmpty()) {
            return "redirect:/admin/products";
        }
        model.addAttribute("product", sanPham.get());
        model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("productLines", productService.tatCaDongSanPham());
        return "admin/product-form";
    }

    @PostMapping("/products/edit/{id}")
    public String editProduct(@PathVariable("id") Integer id,
                              @Valid @ModelAttribute Product product,
                              BindingResult result,
                              @RequestParam(required = false) Integer lineId,
                             @RequestParam(required = false) String lineNameMoi,
                             @RequestParam(required = false) List<String> specNames,
                              @RequestParam(required = false) List<String> specValues,
                              @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                              Model model) {
        if (result.hasErrors()) {
            model.addAttribute("validationErrors", messagesOf(result));
            model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("productLines", productService.tatCaDongSanPham());
            return "admin/product-form";
        }
        product.setId(id);
        productService.findById(id).ifPresent(existing -> {
            product.setIsActive(existing.getIsActive());
            product.setCreatedAt(existing.getCreatedAt());
            if (imageFile == null || imageFile.isEmpty()) {
                product.setImageUrl(existing.getImageUrl());
            }
        });
        product.setLine(productService.layHoacTaoDong(lineId, lineNameMoi));
        if (imageFile != null && !imageFile.isEmpty()) {
            String fileName = handleImageUpload(imageFile);
            if (fileName != null) product.setImageUrl(fileName);
        }
        productService.updateProduct(product);
        productService.deleteSpecsByProductId(id);
        Product saved = productService.findById(id).orElse(product);
        saveSpecs(saved, specNames, specValues);
        return "redirect:/admin/products";
    }

    @PostMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable("id") Integer id) {
        productService.deleteProduct(id);
        return "redirect:/admin/products";
    }

    // Gom các message lỗi validate để hiện ở đầu form.
    private List<String> messagesOf(BindingResult result) {
        return result.getAllErrors().stream()
                .map(e -> e.getDefaultMessage())
                .collect(Collectors.toList());
    }

    @org.springframework.beans.factory.annotation.Value("${upload.dir}")
    private String uploadDir;

    private String handleImageUpload(MultipartFile file) {
        try {
            String originalName = file.getOriginalFilename();
            if (originalName == null || originalName.isEmpty()) return null;
            String ext = originalName.substring(originalName.lastIndexOf(".")).toLowerCase();
            if (!ext.equals(".jpg") && !ext.equals(".jpeg")
                    && !ext.equals(".png") && !ext.equals(".gif") && !ext.equals(".webp")) {
                return null;
            }
            String fileName = System.currentTimeMillis() + "_" + originalName.replaceAll("\\s+", "_");
            File uploadFolder = new File(uploadDir);
            if (!uploadFolder.exists()) uploadFolder.mkdirs();
            file.transferTo(new File(uploadDir, fileName));
            return fileName;
        } catch (IOException e) {
            log.error("Lỗi lưu ảnh sản phẩm", e);
            return null;
        }
    }

    private void saveSpecs(Product product, List<String> specNames, List<String> specValues) {
        if (specNames == null || specValues == null) return;
        for (int i = 0; i < specNames.size(); i++) {
            String name = specNames.get(i).trim();
            String value = i < specValues.size() ? specValues.get(i).trim() : "";
            if (!name.isEmpty() && !value.isEmpty()) {
                ProductSpec spec = new ProductSpec();
                spec.setProduct(product);
                spec.setSpecName(name);
                spec.setSpecValue(value);
                productService.saveSpec(spec);
            }
        }
    }
}
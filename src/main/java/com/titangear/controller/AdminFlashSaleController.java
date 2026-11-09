package com.titangear.controller;

import com.titangear.exception.LoiNghiepVu;
import com.titangear.model.FlashSale;
import com.titangear.model.User;
import com.titangear.service.FlashSaleService;
import com.titangear.service.ProductService;
import com.titangear.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin/flash-sales")
public class AdminFlashSaleController {

    private static final Logger log = LoggerFactory.getLogger(AdminFlashSaleController.class);

    @Autowired private FlashSaleService flashSaleService;
    @Autowired private ProductService productService;
    @Autowired private UserService userService;

    // Cùng thư mục với ảnh sản phẩm — xem AdminProductController
    /** Thư mục upload đọc từ application.properties (upload.dir) —
        nằm NGOÀI target/ nên không mất khi mvn clean, không cứng ổ đĩa */
    @org.springframework.beans.factory.annotation.Value("${upload.dir}")
    private String uploadDir;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("flashSales", flashSaleService.getAllFlashSales());
        model.addAttribute("itemCounts", flashSaleService.getItemCountsBySale());
        model.addAttribute("now", LocalDateTime.now());
        return "admin/flash-sales";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("flashSale", new FlashSale());
        model.addAttribute("products", productService.getAllProducts());
        return "admin/flash-sale-form";
    }

    @PostMapping("/add")
    public String add(@ModelAttribute FlashSale flashSale,
                       @RequestParam(value = "bannerImageFile", required = false) MultipartFile bannerImageFile,
                       @RequestParam(value = "productIds", required = false) List<Integer> productIds,
                       @RequestParam(value = "discountTypes", required = false) List<String> discountTypes,
                       @RequestParam(value = "discountValues", required = false) List<BigDecimal> discountValues,
                       Authentication auth,
                       Model model) {
        try {
            if (bannerImageFile != null && !bannerImageFile.isEmpty()) {
                String fileName = handleImageUpload(bannerImageFile);
                if (fileName != null) flashSale.setBannerImageUrl(fileName);
            }

            Integer userId = userService.findByEmail(auth.getName()).map(User::getId).orElse(1);
            User creator = new User();
            creator.setId(userId);
            flashSale.setCreatedBy(creator);
            flashSale.setCreatedAt(LocalDateTime.now());

            flashSaleService.createFlashSale(flashSale, productIds, discountTypes, discountValues);
            return "redirect:/admin/flash-sales?created=true";

        } catch (LoiNghiepVu e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("flashSale", flashSale);
            model.addAttribute("products", productService.getAllProducts());
            return "admin/flash-sale-form";
        }
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model) {
        FlashSale flashSale = flashSaleService.findById(id);
        if (flashSale == null) return "redirect:/admin/flash-sales";
        model.addAttribute("flashSale", flashSale);
        model.addAttribute("existingItems", flashSaleService.getItemsBySaleId(id));
        model.addAttribute("products", productService.getAllProducts());
        return "admin/flash-sale-form";
    }

    @PostMapping("/edit/{id}")
    public String edit(@PathVariable Integer id,
                        @ModelAttribute FlashSale flashSale,
                        @RequestParam(value = "bannerImageFile", required = false) MultipartFile bannerImageFile,
                        @RequestParam(value = "productIds", required = false) List<Integer> productIds,
                        @RequestParam(value = "discountTypes", required = false) List<String> discountTypes,
                        @RequestParam(value = "discountValues", required = false) List<BigDecimal> discountValues,
                        Model model) {
        FlashSale existing = flashSaleService.findById(id);
        if (existing == null) return "redirect:/admin/flash-sales";

        try {
            if (bannerImageFile != null && !bannerImageFile.isEmpty()) {
                String fileName = handleImageUpload(bannerImageFile);
                if (fileName != null) flashSale.setBannerImageUrl(fileName);
            } else if (flashSale.getDisplayType() == FlashSale.DisplayType.BANNER) {
                // Không upload ảnh mới -> giữ ảnh cũ
                flashSale.setBannerImageUrl(existing.getBannerImageUrl());
            }

            flashSale.setCreatedBy(existing.getCreatedBy());
            flashSale.setCreatedAt(existing.getCreatedAt());

            flashSaleService.updateFlashSale(id, flashSale, productIds, discountTypes, discountValues);
            return "redirect:/admin/flash-sales?updated=true";

        } catch (LoiNghiepVu e) {
            model.addAttribute("error", e.getMessage());
            flashSale.setId(id);
            model.addAttribute("flashSale", flashSale);
            model.addAttribute("products", productService.getAllProducts());
            return "admin/flash-sale-form";
        }
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        flashSaleService.deleteFlashSale(id);
        return "redirect:/admin/flash-sales";
    }

    @PostMapping("/toggle/{id}")
    public String toggle(@PathVariable Integer id,
                         org.springframework.web.servlet.mvc.support.RedirectAttributes ra) {
        try {
            flashSaleService.toggleActive(id);
        } catch (LoiNghiepVu e) {
            // Bật lại một đợt có thể đụng đợt khác đang chạy trùng thời gian —
            // phải hiện lý do cho admin thay vì trang lỗi 500.
            ra.addFlashAttribute("loi", e.getMessage());
        }
        return "redirect:/admin/flash-sales";
    }

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
            log.error("Lỗi lưu ảnh banner flash sale", e);
            return null;
        }
    }
}

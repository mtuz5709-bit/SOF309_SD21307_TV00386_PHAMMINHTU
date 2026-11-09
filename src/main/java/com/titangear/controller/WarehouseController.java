package com.titangear.controller;

import com.titangear.constant.KhoViTri;
import com.titangear.exception.LoiNghiepVu;
import com.titangear.model.Product;
import com.titangear.model.ProductVariant;
import com.titangear.model.StockIn;
import com.titangear.model.User;
import com.titangear.service.AuditLogService;
import com.titangear.service.ProductService;
import com.titangear.service.ProductVariantService;
import com.titangear.service.RestockNotificationService;
import com.titangear.service.StockService;
import com.titangear.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/warehouse")
public class WarehouseController {

    private static final Logger log = LoggerFactory.getLogger(WarehouseController.class);

    @Autowired private StockService stockService;
    @Autowired private ProductService productService;
    @Autowired private AuditLogService auditLogService;
    @Autowired private ProductVariantService variantService;
    @Autowired private UserService userService;
    @Autowired private RestockNotificationService restockNotificationService;

    @GetMapping("/stock")
    public String stock(@RequestParam(required = false) Integer year,
                         @RequestParam(required = false) Integer month,
                         @RequestParam(required = false, name = "lo") Integer loId,
                         @RequestParam(required = false, name = "timLo") String timLo,
                         Model model) {
        LocalDate now = LocalDate.now();
        int reportYear  = (year  != null) ? year  : now.getYear();
        int reportMonth = (month != null) ? month : now.getMonthValue();

        model.addAttribute("stockReport",     stockService.getStockReport(reportYear, reportMonth));
        model.addAttribute("reportYear",      reportYear);
        model.addAttribute("reportMonth",     reportMonth);
        model.addAttribute("isCurrentMonth",  reportYear == now.getYear() && reportMonth == now.getMonthValue());
        model.addAttribute("lowStock",        stockService.getLowStockProducts());
        model.addAttribute("stockInHistory",  stockService.getAllStockInHistory(timLo));
        model.addAttribute("stockOutHistory", stockService.getAllStockOutHistory());
        model.addAttribute("products",        productService.getAllProducts());
        model.addAttribute("timLo",           timLo);

        // Đếm TỔNG số lô cho thẻ chỉ số, tách khỏi danh sách đã lọc ở tab lịch sử.
        // Dùng chung một con số thì hễ thủ kho gõ tìm kiếm là thẻ "Lượt nhập kho"
        // tụt xuống theo, nhìn tưởng kho chỉ còn mấy lô — sai mà không báo gì.
        model.addAttribute("tongSoLo", stockService.getAllStockInHistory(null).size());

        // Sơ đồ kho cho ô chọn khu và danh sách gợi ý số kệ ở form nhập kho.
        model.addAttribute("cacKhu",     KhoViTri.CAC_KHU);
        model.addAttribute("danhSachKe", KhoViTri.DANH_SACH_KE);
        model.addAttribute("soKeMoiKhu", KhoViTri.SO_KE_MOI_KHU);

        // TEM DÁN THÙNG — hiện sau khi nhập kho xong, hoặc khi bấm "Xem tem"
        // ở bảng lịch sử lô (lỡ tem trên thùng bị mờ, rách).
        // Tem CHỈ ghi số lô, sản phẩm, số lượng, ngày nhập — KHÔNG ghi vị trí,
        // vì vị trí đổi khi dọn kho mà tem đã dán lên thùng thì không bóc ra
        // sửa được. Vị trí tra trên màn hình.
        if (loId != null) {
            stockService.timLo(loId).ifPresent(lo -> model.addAttribute("tem", lo));
        }
        return "warehouse/stock";
    }

    // Đổi vị trí cất hàng của một lô — dùng khi dọn kho, sắp xếp lại kệ, hoặc
    // gõ nhầm lúc nhập. Số lô KHÔNG đổi nên tem đã dán trên thùng vẫn đúng.
    @PostMapping("/lo/{id}/vi-tri")
    public String doiViTri(@PathVariable Integer id,
                            @RequestParam String khu,
                            @RequestParam Integer ke,
                            org.springframework.web.servlet.mvc.support.RedirectAttributes ra) {
        try {
            // Đọc vị trí cũ TRƯỚC khi đổi — sau lệnh doiViTri thì dữ liệu đã bị
            // ghi đè, không lấy lại được nữa. Nhật ký cần cả cũ lẫn mới thì mới
            // truy ngược được lô đã nằm ở đâu, chứ chỉ có vị trí mới thì vô dụng.
            String viTriCu = stockService.timLo(id).map(StockIn::getViTri).orElse(null);
            stockService.doiViTri(id, khu, ke);
            auditLogService.ghi(AuditLogService.DOI_VI_TRI, "stock_in", id, viTriCu,
                    "KHU " + khu.toUpperCase() + " · KỆ " + ke,
                    "Đổi vị trí cất hàng của lô #" + id);
            ra.addFlashAttribute("thanhCong", "Đã chuyển lô #" + id
                    + " sang KHU " + khu.toUpperCase() + " · KỆ " + ke + ".");
        } catch (LoiNghiepVu e) {
            ra.addFlashAttribute("loi", e.getMessage());
        }
        // Quay lại đúng tab lịch sử lô, không đá người dùng về tab Tồn kho.
        return "redirect:/warehouse/stock?tab=import-history";
    }

    @PostMapping("/stock/in")
    public String stockIn(
            @RequestParam("productId")   Integer productId,
            @RequestParam(value = "variantId", required = false) Integer variantId,
            @RequestParam("quantity")    Integer quantity,
            @RequestParam("importPrice") BigDecimal importPrice,
            @RequestParam(value = "supplier", required = false) String supplier,
            @RequestParam(value = "note",     required = false) String note,
            @RequestParam(value = "khu",      required = false) String khu,
            @RequestParam(value = "ke",       required = false) Integer ke,
            Authentication auth) {

        // Model 2 — nếu sản phẩm CÓ biến thể thì bắt buộc nhập theo biến thể,
        // không cho nhập ở mức sản phẩm (sẽ phá bất biến tổng biến thể = tồn SP).
        boolean hasVariants = variantService.getByProductId(productId).stream()
                .anyMatch(v -> Boolean.TRUE.equals(v.getIsActive()));
        if (hasVariants && variantId == null) {
            return "redirect:/warehouse/stock?error=needVariant";
        }

        // Vị trí bắt buộc và phải nằm trong sơ đồ kho: khu có thật, số kệ từ 1 tới
        // SO_KE_MOI_KHU. Kiểm ở server chứ không chỉ dựa vào thuộc tính required và
        // min/max của ô nhập, vì sửa HTML là bỏ qua được hết.
        if (!KhoViTri.hopLe(khu, ke)) {
            return "redirect:/warehouse/stock?error=needViTri";
        }

        StockIn stockIn = new StockIn();

        Product product = new Product();
        product.setId(productId);
        stockIn.setProduct(product);

        // Gắn lô nhập theo biến thể nếu có chọn
        if (variantId != null) {
            ProductVariant variant = new ProductVariant();
            variant.setId(variantId);
            stockIn.setVariant(variant);
        }

        // Lấy id user đang đăng nhập
        Integer userId = userService.findByEmail(auth.getName())
                .map(User::getId).orElse(1);
        User importedBy = new User();
        importedBy.setId(userId);
        stockIn.setImportedBy(importedBy);

        stockIn.setQuantity(quantity);
        stockIn.setImportPrice(importPrice);
        stockIn.setSupplier(supplier);
        stockIn.setNote(note);
        stockIn.setKhu(khu.trim().toUpperCase());
        stockIn.setKe(ke);
        stockIn.setImportedAt(LocalDateTime.now());

        stockService.stockIn(stockIn);

        // NHẬT KÝ — việc chính của thủ kho, và là chỗ nhập GIÁ VỐN từng lô.
        // Giá vốn quyết định toàn bộ báo cáo lãi lỗ: gõ nhầm một con số là lãi
        // sai cả tháng, không ghi lại thì không truy được ai nhập.
        String tenSp = productService.findById(productId).map(Product::getName).orElse("#" + productId);
        auditLogService.ghi(AuditLogService.NHAP_KHO, "stock_in", stockIn.getId(),
                null,
                quantity + " cái × " + importPrice.toPlainString() + "đ",
                "Nhập kho \"" + tenSp + "\""
                        + (variantId != null ? " (biến thể #" + variantId + ")" : "")
                        + " — " + stockIn.getViTri()
                        + (supplier != null && !supplier.isBlank() ? " — NCC: " + supplier : ""));

        // Hàng về -> gửi email cho khách đã đăng ký "báo hàng về" của SP này.
        // Hàm này gắn @Async: trả về NGAY, việc gửi mail chạy ở luồng nền. Nếu
        // gửi đồng bộ thì thủ kho bấm "Nhập kho" phải ngồi chờ hệ thống gửi xong
        // từng email cho từng người đăng ký mới thấy trang phản hồi.
        // try/catch giữ lại phòng trường hợp Spring chưa kịp dựng proxy async.
        try {
            restockNotificationService.notifyRestock(productId, variantId);
        } catch (Exception e) {
            log.error("Lỗi gửi thông báo hàng về cho sản phẩm {}", productId, e);
        }

        // Kèm số lô vừa tạo để màn hình hiện TEM cho thủ kho chép tay lên thùng.
        return "redirect:/warehouse/stock?imported=true&lo=" + stockIn.getId();
    }
}

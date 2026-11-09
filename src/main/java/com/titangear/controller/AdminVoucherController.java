package com.titangear.controller;

import com.titangear.model.Combo;
import com.titangear.model.Voucher;
import com.titangear.service.ComboService;
import com.titangear.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// Quản trị mã giảm giá (chỉ ADMIN — cấu hình quyền ở SecurityConfig: /admin/**).
@Controller
@RequestMapping("/admin/vouchers")
public class AdminVoucherController {

    @Autowired private VoucherService voucherService;
    @Autowired private ComboService   comboService;

    // Danh sách combo cho ô chọn trên form — chạy trước mọi trang của controller này.
    @ModelAttribute("dsCombo")
    public java.util.List<Combo> dsCombo() {
        return comboService.layTatCaChoAdmin();
    }

    @GetMapping
    public String list(Model model) {
        List<Voucher> ds = voucherService.getAll();

        // ---- Số liệu cho 4 thẻ chỉ số ----
        // Tính ở controller, JSP chỉ hiển thị (tách tầng Controller–View).
        LocalDateTime bayGio = LocalDateTime.now();
        long dangChay = ds.stream().filter(v ->
                Boolean.TRUE.equals(v.getActive())
                && (v.getStartAt() == null || !v.getStartAt().isAfter(bayGio))
                && (v.getEndAt()   == null || !v.getEndAt().isBefore(bayGio))).count();
        long hetHan = ds.stream().filter(v -> v.getEndAt() != null && v.getEndAt().isBefore(bayGio)).count();
        long tongLuotDung = ds.stream().mapToLong(v -> v.getUsedCount() != null ? v.getUsedCount() : 0).sum();

        model.addAttribute("tongMa",       ds.size());
        model.addAttribute("maDangChay",   dangChay);
        model.addAttribute("maHetHan",     hetHan);
        model.addAttribute("tongLuotDung", tongLuotDung);

        model.addAttribute("vouchers", ds);
        return "admin/vouchers";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        Voucher v = new Voucher();
        // Điền sẵn mã ngay khi mở form -> admin không phải gõ, cũng không phải bấm
        // thêm nút nào. Vẫn sửa tay hoặc bấm "Tự sinh mã" để lấy mã khác được.
        v.setCode(voucherService.sinhMa());
        model.addAttribute("voucher", v);
        return "admin/voucher-form";
    }

    // Trả 1 mã mới cho nút "Tự sinh mã" (gọi bằng AJAX, không tải lại trang).
    // PHẢI khai produces=text/plain: không khai thì Jackson đóng gói String thành
    // JSON, trả về "TTGABC123" KÈM dấu nháy kép và ô nhập sẽ dính luôn dấu nháy.
    @GetMapping(value = "/gen-code", produces = MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8")
    @ResponseBody
    public String genCode() {
        return voucherService.sinhMa();
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model) {
        Voucher v = voucherService.findById(id).orElse(null);
        if (v == null) return "redirect:/admin/vouchers";
        model.addAttribute("voucher", v);
        return "admin/voucher-form";
    }

    @PostMapping("/save")
    public String save(@RequestParam(required = false) Integer id,
                       @RequestParam String code,
                       @RequestParam(required = false) String description,
                       @RequestParam String discountType,
                       @RequestParam BigDecimal discountValue,
                       @RequestParam(required = false) BigDecimal minOrderAmount,
                       @RequestParam(required = false) BigDecimal maxDiscountAmount,
                       @RequestParam(required = false) String startAt,
                       @RequestParam(required = false) String endAt,
                       @RequestParam(required = false) Integer usageLimit,
                       @RequestParam(required = false) String oncePerUser,
                       @RequestParam(required = false) Integer comboId,
                       @RequestParam(required = false) String chiDonCoCombo,
                       Model model) {

        String ma = code == null ? "" : code.trim().toUpperCase();

        // Validate phía server (client lách được)
        String loi = null;
        if (ma.isEmpty()) {
            loi = "Mã không được để trống.";
        } else if (discountValue == null || discountValue.compareTo(BigDecimal.ZERO) <= 0) {
            loi = "Giá trị giảm phải lớn hơn 0.";
        } else if (Voucher.TYPE_PERCENT.equals(discountType)
                && discountValue.compareTo(new BigDecimal("100")) > 0) {
            loi = "Giảm theo % không được vượt quá 100.";
        } else if (id == null && voucherService.codeDaTonTai(ma)) {
            loi = "Mã \"" + ma + "\" đã tồn tại.";
        }

        Voucher v = (id != null) ? voucherService.findById(id).orElse(new Voucher()) : new Voucher();

        if (loi != null) {
            // Trả lại form kèm dữ liệu vừa nhập để khỏi gõ lại
            v.setCode(ma);
            v.setDescription(description);
            v.setDiscountType(discountType);
            v.setDiscountValue(discountValue);
            model.addAttribute("voucher", v);
            model.addAttribute("errorMsg", loi);
            return "admin/voucher-form";
        }

        v.setCode(ma);
        v.setDescription(description);
        v.setDiscountType(discountType);
        v.setDiscountValue(discountValue);
        v.setMinOrderAmount(minOrderAmount);
        // Trần giảm chỉ có ý nghĩa với loại %
        v.setMaxDiscountAmount(Voucher.TYPE_PERCENT.equals(discountType) ? maxDiscountAmount : null);
        v.setStartAt(parseNgay(startAt));
        v.setEndAt(parseNgay(endAt));
        v.setUsageLimit(usageLimit);
        v.setOncePerUser("on".equals(oncePerUser) || "true".equals(oncePerUser));

        // Ràng buộc combo. Chọn ĐÚNG một combo thì cờ "đơn có combo bất kỳ" thành
        // thừa — bỏ luôn cho khỏi hai điều kiện chồng nhau gây khó hiểu về sau.
        Combo combo = (comboId == null) ? null : comboService.layDeSua(comboId);
        v.setCombo(combo);
        v.setChiDonCoCombo(combo == null
                && ("on".equals(chiDonCoCombo) || "true".equals(chiDonCoCombo)));

        if (id == null) {
            v.setUsedCount(0);
            v.setActive(true);
            v.setCreatedAt(LocalDateTime.now());
        }
        voucherService.save(v);
        return "redirect:/admin/vouchers?saved=true";
    }

    // Bật/tắt nhanh từ danh sách.
    @PostMapping("/toggle/{id}")
    public String toggle(@PathVariable Integer id) {
        voucherService.findById(id).ifPresent(v -> {
            v.setActive(!Boolean.TRUE.equals(v.getActive()));
            voucherService.save(v);
        });
        return "redirect:/admin/vouchers";
    }

    // Input datetime-local gửi dạng "yyyy-MM-ddTHH:mm".
    private LocalDateTime parseNgay(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try {
            return LocalDateTime.parse(s.trim());
        } catch (Exception e) {
            return null;
        }
    }
}

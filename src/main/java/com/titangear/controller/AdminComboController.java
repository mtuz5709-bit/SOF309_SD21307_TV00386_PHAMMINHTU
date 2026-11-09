package com.titangear.controller;

import com.titangear.exception.LoiNghiepVu;
import com.titangear.model.Combo;
import com.titangear.service.ComboService;
import com.titangear.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

// Quản trị combo (chỉ ADMIN — cấu hình quyền ở SecurityConfig: /admin/**).
@Controller
@RequestMapping("/admin/combos")
public class    AdminComboController {

    @Autowired private ComboService   comboService;
    @Autowired private ProductService productService;

    @GetMapping
    public String list(Model model) {
        List<Combo> ds = comboService.layTatCaChoAdmin();

        // Số liệu cho thẻ chỉ số — tính ở controller, JSP chỉ hiển thị.
        long dangBan = ds.stream().filter(c -> Boolean.TRUE.equals(c.getIsActive())).count();
        long tongMon = ds.stream().mapToLong(c -> c.getItems() == null ? 0 : c.getItems().size()).sum();
        BigDecimal giamCaoNhat = ds.stream()
                .map(c -> c.getDiscountAmount() == null ? BigDecimal.ZERO : c.getDiscountAmount())
                .max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);

        model.addAttribute("combos",      ds);
        model.addAttribute("tongCombo",   ds.size());
        model.addAttribute("comboDangBan", dangBan);
        model.addAttribute("comboDaTat",  ds.size() - dangBan);
        model.addAttribute("tongMon",     tongMon);
        model.addAttribute("giamCaoNhat", giamCaoNhat);
        return "admin/combos";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        Combo c = new Combo();
        c.setIsActive(true);
        c.setDiscountAmount(BigDecimal.ZERO);
        c.setAccentColor("#1B49FF");
        model.addAttribute("combo", c);
        model.addAttribute("sanPham", productService.getAllProducts());   // chỉ hàng đang bán
        return "admin/combo-form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model) {
        Combo c = comboService.layDeSua(id);
        if (c == null) return "redirect:/admin/combos";
        model.addAttribute("combo", c);
        model.addAttribute("sanPham", productService.getAllProducts());   // chỉ hàng đang bán
        return "admin/combo-form";
    }

    // Lưu combo. Danh sách món gửi lên dạng 3 mảng song song
    // (sanPhamIds[i], bienTheIds[i], soLuongs[i]) — JSP dựng động từng dòng nên
    // đây là cách gọn nhất, không phải tạo DTO riêng cho một form.
    @PostMapping("/save")
    public String save(@ModelAttribute Combo combo,
                       @RequestParam(required = false) List<Integer> sanPhamIds,
                       @RequestParam(required = false) List<Integer> bienTheIds,
                       @RequestParam(required = false) List<Integer> soLuongs,
                       RedirectAttributes ra) {
        try {
            comboService.luuCombo(combo, sanPhamIds, bienTheIds, soLuongs);
        } catch (LoiNghiepVu e) {
            ra.addFlashAttribute("comboError", e.getMessage());
            return combo.getId() == null
                    ? "redirect:/admin/combos/add"
                    : "redirect:/admin/combos/edit/" + combo.getId();
        }
        return "redirect:/admin/combos";
    }

    // Bật/tắt — KHÔNG xóa cứng vì order_items trỏ tới combos.
    @PostMapping("/toggle/{id}")
    public String toggle(@PathVariable Integer id) {
        comboService.doiTrangThai(id);
        return "redirect:/admin/combos";
    }
}

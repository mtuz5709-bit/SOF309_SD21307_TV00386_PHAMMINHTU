package com.titangear.controller;

import com.titangear.exception.LoiNghiepVu;
import com.titangear.model.Warranty;
import com.titangear.service.ProductService;
import com.titangear.service.UserService;
import com.titangear.service.WarrantyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Quản lý bảo hành: danh sách phiếu, tra theo mã serial, xem/ghi lịch sử sửa chữa.
@Controller
@RequestMapping("/admin/warranties")
public class AdminWarrantyController {

    @Autowired private WarrantyService warrantyService;
    @Autowired private ProductService productService;
    @Autowired private UserService userService;

    @GetMapping
    public String list(@RequestParam(required = false) String q,
                       @RequestParam(required = false) String trangThai,
                       Model model) {
        List<Warranty> tatCa = (q != null && !q.trim().isEmpty())
                ? warrantyService.tim(q.trim())
                : warrantyService.tatCa();

        List<Warranty> ds = warrantyService.loc(tatCa, trangThai);

        // Số đếm trên tab tính từ TOÀN BỘ kết quả tìm kiếm (chưa lọc), để khi bấm
        // qua lại giữa các tab thì con số không nhảy lung tung.
        model.addAttribute("tong",      tatCa.size());
        model.addAttribute("soConHan",  warrantyService.loc(tatCa, WarrantyService.CON_HAN).size());
        model.addAttribute("soSapHet",  warrantyService.loc(tatCa, WarrantyService.SAP_HET).size());
        model.addAttribute("soHetHan",  warrantyService.loc(tatCa, WarrantyService.HET_HAN).size());

        model.addAttribute("warranties", ds);
        model.addAttribute("tenSanPham", mapTenSanPham(ds));
        model.addAttribute("tenKhach",   mapTenKhach(ds));
        model.addAttribute("adminActive", "warranties");
        return "admin/warranties";
    }

    @GetMapping("/{id}")
    public String chiTiet(@PathVariable Integer id, Model model) {
        Warranty w = warrantyService.findById(id).orElse(null);
        if (w == null) return "redirect:/admin/warranties";

        model.addAttribute("w", w);
        model.addAttribute("tenSp", productService.findById(w.getProductId())
                .map(p -> p.getName()).orElse("—"));
        model.addAttribute("tenKh", userService.findById(w.getUserId())
                .map(u -> u.getFullName()).orElse("—"));
        model.addAttribute("lichSu", warrantyService.lichSuSua(id));
        model.addAttribute("adminActive", "warranties");
        return "admin/warranty-detail";
    }

    @PostMapping("/{id}/repairs")
    public String themLanSua(@PathVariable Integer id,
                             @RequestParam String noiDung,
                             @RequestParam String trangThai,
                             RedirectAttributes ra) {
        try {
            if (noiDung != null && !noiDung.trim().isEmpty()) {
                warrantyService.themLanSua(id, noiDung.trim(), trangThai);
            }
        } catch (LoiNghiepVu e) {
            // Phiếu hết hạn (hoặc không tồn tại): báo đỏ ngay trên trang phiếu,
            // KHÔNG để văng ra trang lỗi 500 vì đây là lỗi nghiệp vụ bình thường.
            ra.addFlashAttribute("loi", e.getMessage());
        }
        return "redirect:/admin/warranties/" + id;
    }

    /* Nạp sẵn tên sản phẩm / tên khách để JSP khỏi phải tra từng dòng */

    private Map<Integer, String> mapTenSanPham(List<Warranty> ds) {
        Map<Integer, String> m = new HashMap<>();
        for (Warranty w : ds) {
            m.computeIfAbsent(w.getProductId(),
                    pid -> productService.findById(pid).map(p -> p.getName()).orElse("—"));
        }
        return m;
    }

    private Map<Integer, String> mapTenKhach(List<Warranty> ds) {
        Map<Integer, String> m = new HashMap<>();
        for (Warranty w : ds) {
            m.computeIfAbsent(w.getUserId(),
                    uid -> userService.findById(uid).map(u -> u.getFullName()).orElse("—"));
        }
        return m;
    }
}

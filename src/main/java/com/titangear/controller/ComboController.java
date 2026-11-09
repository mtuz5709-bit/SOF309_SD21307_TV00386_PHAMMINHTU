package com.titangear.controller;

import com.titangear.exception.LoiNghiepVu;
import com.titangear.model.User;
import com.titangear.service.ComboService;
import com.titangear.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/combo")
public class ComboController {

    @Autowired private ComboService comboService;
    @Autowired private UserService  userService;

    // Tất cả combo đang bán.
    @GetMapping("")
    public String danhSach(Model model) {
        model.addAttribute("combos", comboService.layComboDangBan(0));
        return "customer/combos";
    }

    // Chi tiết một combo.
    // Combo không tồn tại / đã tắt -> quay về danh sách thay vì ném trang lỗi,
    // vì link combo có thể được chia sẻ rồi admin tắt combo sau đó.
    @GetMapping("/{id}")
    public String chiTiet(@PathVariable Integer id, Model model) {
        ComboService.ComboView combo = comboService.layTheoId(id);
        if (combo == null) return "redirect:/combo";
        model.addAttribute("cb", combo);
        return "customer/combo-detail";
    }

    // Mua trọn bộ — đẩy cả combo vào giỏ.
    //
    // CHỈ NHẬN comboId, KHÔNG nhận giá từ trình duyệt (luật 6): toàn bộ giá do
    // server tự tính lại, cùng nguyên tắc đang áp cho voucher. Nhận giá từ client
    // là mở đường cho khách sửa số tiền trên form rồi mua giá 0đ.
    @PostMapping("/{id}/mua")
    public String muaTronBo(@PathVariable Integer id,
                            @RequestParam Map<String, String> thamSo,
                            Authentication auth,
                            RedirectAttributes ra) {
        User user = userService.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        // Form gửi lên dạng sp_<viTriId> và mau_<viTriId>. Gom về map để service
        // kiểm; service mới là nơi quyết định id đó có hợp lệ với vị trí đó không.
        Map<Integer, Integer> chonSanPham = docLuaChon(thamSo, "sp_");
        Map<Integer, Integer> chonMau     = docLuaChon(thamSo, "mau_");

        try {
            comboService.themVaoGio(user.getId(), id, chonSanPham, chonMau);
        } catch (LoiNghiepVu e) {
            ra.addFlashAttribute("comboError", e.getMessage());
            return "redirect:/combo/" + id;
        }
        return "redirect:/cart";
    }

    // Lọc các tham số dạng "tienTo<số>" thành map {vị trí -> id đã chọn}.
    private Map<Integer, Integer> docLuaChon(Map<String, String> thamSo, String tienTo) {
        Map<Integer, Integer> ketQua = new HashMap<>();
        for (Map.Entry<String, String> e : thamSo.entrySet()) {
            if (!e.getKey().startsWith(tienTo)) continue;
            try {
                // Tham số do người dùng gửi lên nên có thể là rác -> bỏ qua lặng lẽ,
                // service sẽ dùng giá trị mặc định.
                ketQua.put(Integer.valueOf(e.getKey().substring(tienTo.length())),
                           Integer.valueOf(e.getValue()));
            } catch (NumberFormatException ignored) { }
        }
        return ketQua;
    }
}

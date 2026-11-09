package com.titangear.controller;

import com.titangear.model.Warranty;
import com.titangear.service.ProductService;
import com.titangear.service.UserService;
import com.titangear.service.WarrantyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Tra cứu bảo hành công khai — khách nhập mã trên phiếu là xem được còn hạn không.
// Không cần đăng nhập (đường dẫn /bao-hanh đã mở trong SecurityConfig).
@Controller
public class WarrantyLookupController {

    @Autowired private WarrantyService warrantyService;
    @Autowired private ProductService productService;
    @Autowired private UserService userService;

    @GetMapping("/bao-hanh")
    public String traCuu(@RequestParam(required = false) String serial,
                         Principal principal, Model model) {
        // Khách đã đăng nhập thì liệt kê sẵn phiếu của họ: nếu chỉ có mỗi ô nhập mã
        // thì khách chẳng biết serial của mình ở đâu ra mà gõ.
        if (principal != null) {
            userService.findByEmail(principal.getName()).ifPresent(u -> {
                List<Warranty> cuaToi = warrantyService.cuaKhach(u.getId());
                Map<Integer, String> ten = new HashMap<>();
                for (Warranty x : cuaToi) {
                    ten.computeIfAbsent(x.getProductId(),
                            pid -> productService.findById(pid).map(p -> p.getName()).orElse("—"));
                }
                model.addAttribute("cuaToi", cuaToi);
                model.addAttribute("tenSanPham", ten);
            });
        }

        if (serial != null && !serial.trim().isEmpty()) {
            model.addAttribute("daTim", true);
            model.addAttribute("serial", serial.trim());

            Warranty w = warrantyService.traCuu(serial).orElse(null);
            if (w != null) {
                model.addAttribute("w", w);
                model.addAttribute("tenSp", productService.findById(w.getProductId())
                        .map(p -> p.getName()).orElse("—"));
                model.addAttribute("lichSu", warrantyService.lichSuSua(w.getId()));
            }
        }
        return "customer/warranty-lookup";
    }
}

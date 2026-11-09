package com.titangear.controller;

import com.titangear.model.Product;
import com.titangear.service.ComboService;
import com.titangear.service.FlashSaleService;
import com.titangear.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    @Autowired private FlashSaleService flashSaleService;
    @Autowired private ProductService productService;
    @Autowired private ComboService comboService;

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        // Banner flash sale động (giữ nguyên tính năng cũ)
        model.addAttribute("activeFlashBanners", flashSaleService.getActiveBanners());

        // Lấy đúng 5 sản phẩm mới nhất, sắp xếp và giới hạn ngay ở tầng CSDL thay vì
        // nạp cả kho rồi cắt trong Java. Trang chủ là trang nhiều người xem nhất nên
        // không được nạp thừa. Lấy 5 vì khối "Hàng mới về" dùng lại 4 cái đầu.
        List<Product> moiNhat = productService.layMoiNhat(5);

        model.addAttribute("featured", moiNhat);                         // khối tab + bảng thông số
        model.addAttribute("newArrivals", moiNhat.stream().limit(4).toList());
        // Khối "Góc máy đồng bộ": 3 combo mới nhất còn bán được.
        // Giá do ComboService tính sẵn nên JSP không phải tra priceMap riêng.
        model.addAttribute("combos", comboService.layComboDangBan(3));
        // Giá hiệu lực chỉ tính cho các sản phẩm ĐANG HIỆN, không phải cho cả kho
        model.addAttribute("priceMap", flashSaleService.getEffectivePrices(moiNhat));

        return "customer/home";
    }
}

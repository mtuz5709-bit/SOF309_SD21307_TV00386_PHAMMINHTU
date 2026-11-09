package com.titangear.controller;

import com.titangear.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class SearchController {

    @Autowired
    private ProductService productService;

    @GetMapping("/api/search")
    public List<Map<String, Object>> suggest(@RequestParam String q) {
        if (q == null || q.trim().length() < 1) {
            return new ArrayList<>();
        }

        List<Map<String, Object>> result = new ArrayList<>();

        // timKiemChoKhach chứ KHÔNG phải search(): search() trả cả hàng đã ngừng
        // kinh doanh (bản đó dành cho trang quản trị), gợi ý ra thì khách bấm vào
        // là tới sản phẩm không mua được.
        productService.timKiemChoKhach(q.trim())
                .stream()
                .limit(6)
                .forEach(p -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id",       p.getId());
                    item.put("name",     p.getName());
                    item.put("price",    p.getPrice());
                    item.put("imageUrl", p.getImageUrl() != null ? p.getImageUrl() : "");
                    // stock_quantity do trigger quản, sản phẩm mới chưa nhập kho
                    // vẫn là NULL. Viết p.getStockQuantity() > 0 là Java tự mở hộp
                    // null -> NullPointerException -> API trả 500 và thanh tìm kiếm
                    // lặng lẽ không hiện gợi ý nào, không ai biết vì sao.
                    Integer ton = p.getStockQuantity();
                    item.put("inStock",  ton != null && ton > 0);
                    result.add(item);
                });

        return result;
    }
}
package com.titangear.controller;

import com.titangear.model.Product;
import com.titangear.model.ProductSpec;
import com.titangear.service.FlashSaleService;
import com.titangear.service.ProductService;
import com.titangear.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

// SO SÁNH SẢN PHẨM.
//
// Danh sách đang so sánh KHÔNG lưu trong CSDL — nó nằm ở localStorage của trình
// duyệt, khách bấm "So sánh ngay" thì gửi id lên đây.
@Controller
public class CompareController {

    // Quá 4 cột thì bảng tràn ngang, đọc không nổi.
    private static final int TOI_DA = 4;

    @Autowired private ProductService productService;
    @Autowired private FlashSaleService flashSaleService;
    @Autowired private ReviewService reviewService;

    @GetMapping("/so-sanh")
    public String soSanh(@RequestParam(required = false) String ids, Model model) {
        List<Product> ds = docDanhSach(ids);
        model.addAttribute("sanPham", ds);

        // Nhãn hiển thị của các thuộc tính (LINEAR -> "Linear", WIRELESS -> "Không dây")
        model.addAttribute("nhomSwitch",     com.titangear.constant.ProductAttr.SWITCH);
        model.addAttribute("nhomTrongLuong", com.titangear.constant.ProductAttr.WEIGHT);
        model.addAttribute("nhomKetNoi",     com.titangear.constant.ProductAttr.CONNECT);

        if (!ds.isEmpty()) {
            model.addAttribute("priceMap",   flashSaleService.getEffectivePrices(ds));
            model.addAttribute("danhGiaMap", reviewService.diemTheoDanhSach(
                    ds.stream().map(Product::getId).toList()));

            // Tên các dòng thông số: gộp tất cả specName của mọi sản phẩm, giữ thứ
            // tự xuất hiện. Sản phẩm nào thiếu thông số đó thì JSP hiện dấu "—".
            LinkedHashSet<String> dongThongSo = new LinkedHashSet<>();
            for (Product p : ds) {
                if (p.getSpecs() == null) continue;
                for (ProductSpec s : p.getSpecs()) dongThongSo.add(s.getSpecName());
            }
            model.addAttribute("dongThongSo", dongThongSo);
            model.addAttribute("giaThapNhat", giaThapNhat(ds, model));
        }
        return "customer/compare";
    }

    // "3,1,7" -> danh sách sản phẩm còn bán, bỏ id rác/trùng, tối đa 4 cái.
    private List<Product> docDanhSach(String ids) {
        List<Product> ds = new ArrayList<>();
        if (ids == null || ids.isBlank()) return ds;

        LinkedHashSet<Integer> daCo = new LinkedHashSet<>();
        for (String phan : ids.split(",")) {
            try {
                daCo.add(Integer.parseInt(phan.trim()));
            } catch (NumberFormatException ignored) {
                // id rác do người dùng sửa tay trên thanh địa chỉ — bỏ qua, không báo lỗi
            }
        }
        for (Integer id : daCo) {
            if (ds.size() >= TOI_DA) break;
            productService.findById(id)
                    .filter(p -> Boolean.TRUE.equals(p.getIsActive()))
                    .ifPresent(ds::add);
        }
        return ds;
    }

    // Giá thấp nhất trong nhóm — để JSP gắn nhãn "TỐT NHẤT".
    // CHỈ làm cho giá, điểm đánh giá và tồn kho: đó là 3 thứ mà "cao/thấp hơn là
    // tốt hơn" luôn đúng. Các dòng thông số khác ("55 g", "8000 Hz") thì tuỳ loại
    // mà nhỏ hơn hay lớn hơn mới tốt, đoán bừa là gắn nhãn sai.
    private BigDecimal giaThapNhat(List<Product> ds, Model model) {
        @SuppressWarnings("unchecked")
        Map<Integer, FlashSaleService.EffectivePrice> gia =
                (Map<Integer, FlashSaleService.EffectivePrice>) model.getAttribute("priceMap");

        BigDecimal min = null;
        for (Product p : ds) {
            BigDecimal g = (gia != null && gia.get(p.getId()) != null)
                    ? gia.get(p.getId()).getSalePrice()
                    : p.getPrice();
            if (g != null && (min == null || g.compareTo(min) < 0)) min = g;
        }
        return min;
    }
}

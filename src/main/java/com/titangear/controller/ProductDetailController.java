package com.titangear.controller;

import com.titangear.exception.LoiNghiepVu;
import com.titangear.model.Product;
import com.titangear.model.ProductVariant;
import com.titangear.model.User;
import com.titangear.repository.OrderItemRepository;
import com.titangear.service.FlashSaleService;
import com.titangear.service.ProductService;
import com.titangear.service.RestockNotificationService;
import com.titangear.service.ReviewService;
import com.titangear.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class ProductDetailController {

    @Autowired private ProductService productService;
    @Autowired private FlashSaleService flashSaleService;
    @Autowired private ReviewService reviewService;
    @Autowired private UserService userService;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private RestockNotificationService restockNotificationService;

    @GetMapping("/products/{id}")
    public String productDetail(@PathVariable("id") Integer id,
                                 @RequestParam(required = false, defaultValue = "recent") String sort,
                                 Authentication auth,
                                 Model model) {
        // Không có sản phẩm mang id này thì quay về danh sách, không dựng trang
        // chi tiết rỗng. Thiếu chốt này thì gõ sai id vẫn ra trang trắng mã 200:
        // tiêu đề trống, không ảnh, không giá — trông như web hỏng.
        Optional<Product> sanPham = productService.findById(id);
        if (sanPham.isEmpty()) {
            return "redirect:/products";
        }

        sanPham.ifPresent(p -> {
            model.addAttribute("product", p);
            model.addAttribute("effectivePrice", flashSaleService.getEffectivePrice(p));

            // Model 2 — số ĐANG GIỮ (reserved) mỗi biến thể để trang hiện "bán được"
            // (available = tồn − đang giữ), tránh cho khách thấy "còn X" ảo.
            if (p.getVariants() != null) {
                Map<Integer, Integer> reservedMap = new HashMap<>();
                for (ProductVariant v : p.getVariants()) {
                    Integer r = orderItemRepository.getReservedQtyByVariant(v.getId());
                    reservedMap.put(v.getId(), r != null ? r : 0);
                }
                model.addAttribute("reservedMap", reservedMap);
            }

            // prefill email cho form "báo hàng về" nếu khách đã đăng nhập
            model.addAttribute("currentEmail", auth != null ? auth.getName() : "");

            // Sản phẩm liên quan: cùng danh mục, bỏ chính nó và hàng đã ngừng bán.
            // Sản phẩm chưa gắn danh mục thì không có gì để "liên quan" -> để trống.
            if (p.getCategory() != null) {
                List<Product> lienQuan = productService.getByCategory(p.getCategory().getId()).stream()
                        .filter(x -> !x.getId().equals(id))
                        .filter(x -> Boolean.TRUE.equals(x.getIsActive()))
                        .limit(4)
                        .toList();
                model.addAttribute("lienQuan", lienQuan);
                model.addAttribute("giaLienQuan", flashSaleService.getEffectivePrices(lienQuan));
            }

            // Các phiên bản cùng dòng — hàng nút chuyển nhanh ở đầu trang.
            // Trả về danh sách rỗng nếu sản phẩm không thuộc dòng nào, JSP tự ẩn.
            model.addAttribute("cungDong", productService.cungDong(p));

            model.addAttribute("reviewSummary", reviewService.getSummary(id));
            model.addAttribute("reviews", reviewService.getReviews(id, sort));
            model.addAttribute("currentSort", sort);

            if (auth != null) {
                userService.findByEmail(auth.getName()).ifPresent(user -> {
                    model.addAttribute("canReview", reviewService.hasPurchased(user.getId(), id)
                            && !reviewService.hasReviewed(user.getId(), id));
                    model.addAttribute("myReview", reviewService.getMyReview(user.getId(), id));
                });
            }
        });
        return "customer/product-detail";
    }

    // Đăng ký "báo hàng về" khi sản phẩm hết hàng
    @PostMapping("/products/{id}/notify-restock")
    public String notifyRestock(@PathVariable("id") Integer id,
                                @RequestParam(required = false) Integer variantId,
                                @RequestParam String email) {
        // Validate server: email hợp lệ (client type=email chỉ là lớp ngoài, lách được)
        if (email == null || !email.trim().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            return "redirect:/products/" + id + "?notifyErr=true";
        }
        // variantId != null -> khách chờ ĐÚNG biến thể đó (vd chỉ chờ màu Đỏ)
        restockNotificationService.subscribe(id, variantId, email);
        return "redirect:/products/" + id + "?notifyOk=true";
    }

    // Gửi đánh giá mới (chỉ cho phép nếu đã mua hàng COMPLETED, kiểm tra lại ở service)
    @PostMapping("/products/{id}/reviews")
    public String submitReview(@PathVariable("id") Integer id,
                                @RequestParam Integer rating,
                                @RequestParam(required = false) String comment,
                                Authentication auth) {
        if (auth == null) {
            return "redirect:/login";
        }
        // Validate server: số sao phải 1..5 dù client gửi gì (vd rating=999)
        if (rating == null || rating < 1 || rating > 5) {
            return "redirect:/products/" + id + "?reviewError="
                    + java.net.URLEncoder.encode("Số sao đánh giá phải từ 1 đến 5.",
                        java.nio.charset.StandardCharsets.UTF_8) + "#reviews";
        }
        User user = userService.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        try {
            reviewService.submitReview(user.getId(), id, rating, comment);
            return "redirect:/products/" + id + "?reviewed=true#reviews";
        } catch (LoiNghiepVu e) {
            return "redirect:/products/" + id + "?reviewError="
                    + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8)
                    + "#reviews";
        }
    }
}

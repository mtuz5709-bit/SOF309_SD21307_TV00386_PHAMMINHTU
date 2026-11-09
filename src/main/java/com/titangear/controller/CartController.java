package com.titangear.controller;

import com.titangear.exception.LoiNghiepVu;
import com.titangear.model.CartItem;
import com.titangear.model.Product;
import com.titangear.model.User;
import com.titangear.service.CartService;
import com.titangear.service.ComboService;
import com.titangear.service.FlashSaleService;
import com.titangear.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private UserService userService;

    @Autowired
    private FlashSaleService flashSaleService;

    @Autowired
    private ComboService comboService;

    // Lấy user hiện tại đang đăng nhập
    private User getCurrentUser(Authentication auth) {
        return userService.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));
    }

    // Xem giỏ hàng
    @GetMapping("")
    public String viewCart(Authentication auth, Model model) {
        User user = getCurrentUser(auth);
        List<CartItem> items = cartService.getCart(user.getId());

        List<Product> products = items.stream().map(CartItem::getProduct).collect(Collectors.toList());
        Map<Integer, FlashSaleService.EffectivePrice> priceMap = flashSaleService.getEffectivePrices(products);

        // Tổng và giá từng dòng đều lấy từ ComboService — nơi DUY NHẤT biết trừ
        // tiền giảm của combo. Cộng tay "giá × số lượng" như trước sẽ ra tổng
        // KHÁC với trang đặt hàng, khách thấy 2 con số lệch nhau là mất tin ngay.
        Map<Integer, ComboService.GiaDong> giaDong = comboService.tinhGiaTungDong(items);
        BigDecimal total = items.stream()
                .map(i -> giaDong.get(i.getId()).getThanhTien())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("cartItems", items);
        model.addAttribute("total", total);
        model.addAttribute("priceMap", priceMap);
        model.addAttribute("giaDong", giaDong);
        return "customer/cart";
    }

    // Thêm vào giỏ — từ trang chi tiết sản phẩm
    @GetMapping("/add")
    public String addToCart(@RequestParam Integer productId,
                            @RequestParam(defaultValue = "1") Integer quantity,
                            @RequestParam(required = false) Integer variantId,
                            @RequestParam(defaultValue = "false") boolean buyNow,
                            Authentication auth) {
        User user = getCurrentUser(auth);
        try {
            cartService.addToCart(user.getId(), productId, quantity, variantId);
        } catch (LoiNghiepVu e) {
            return "redirect:/products/" + productId + "?qtyError="
                    + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8);
        }

        if (buyNow) {
            return "redirect:/cart";
        }
        return "redirect:/products/" + productId + "?added=true";
    }

    // Cập nhật số lượng
    @PostMapping("/update")
    public String updateCart(@RequestParam Integer cartItemId,
                             @RequestParam Integer quantity,
                             Authentication auth) {
        // CHỐNG IDOR: mọi thao tác lên giỏ đều gắn với user ĐANG ĐĂNG NHẬP,
        // service sẽ chỉ đụng tới item thuộc giỏ của đúng người này
        User user = getCurrentUser(auth);
        if (quantity <= 0) {
            cartService.removeItem(user.getId(), cartItemId);
        } else {
            try {
                cartService.updateQuantity(user.getId(), cartItemId, quantity);
            } catch (LoiNghiepVu e) {
                return "redirect:/cart?qtyError="
                        + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8);
            }
        }
        return "redirect:/cart";
    }

    // Xóa 1 sản phẩm khỏi giỏ — CHỐNG IDOR: chỉ xóa được item của chính mình
    @PostMapping("/remove")
    public String removeItem(@RequestParam Integer cartItemId, Authentication auth) {
        User user = getCurrentUser(auth);
        cartService.removeItem(user.getId(), cartItemId);
        return "redirect:/cart";
    }
}
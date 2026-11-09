package com.titangear.controller;

import com.titangear.constant.OrderStatus;
import com.titangear.constant.TinhThanh;
import com.titangear.exception.LoiNghiepVu;

import com.titangear.model.CartItem;
import com.titangear.model.Order;
import com.titangear.model.Product;
import com.titangear.model.User;
import com.titangear.service.CartService;
import com.titangear.service.ComboService;
import com.titangear.service.FlashSaleService;
import com.titangear.service.OrderService;
import com.titangear.service.SystemSettingService;
import com.titangear.service.TinhPhiVanChuyen;
import com.titangear.service.UserService;
import com.titangear.service.VoucherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    private static final Logger log = LoggerFactory.getLogger(CheckoutController.class);

    @Autowired
    private VoucherService voucherService;

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private FlashSaleService flashSaleService;

    @Autowired
    private ComboService comboService;

    @Autowired
    private TinhPhiVanChuyen tinhPhiVanChuyen;

    @Autowired
    private SystemSettingService systemSettingService;

    private User getCurrentUser(Authentication auth) {
        return userService.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));
    }

    // CHỐNG IDOR: chỉ trả về đơn hàng nếu nó thuộc về user đang đăng nhập.
    // Cùng pattern với MyOrdersController.cancelOrder().
    private Order getOwnedOrderOrNull(Integer orderId, Authentication auth) {
        User user = getCurrentUser(auth);
        return orderService.findById(orderId)
                .filter(o -> o.getUser().getId().equals(user.getId()))
                .orElse(null);
    }

    // Tổng tiền giỏ. Giao hẳn cho ComboService vì nó là NƠI DUY NHẤT biết cách
    // trừ tiền giảm của combo; tự cộng "giá × số lượng" ở đây như trước sẽ tính
    // thiếu phần giảm, khách thấy giá combo ở trang chủ nhưng bị thu tiền giá lẻ.
    private BigDecimal calcEffectiveTotal(List<CartItem> cartItems) {
        return comboService.tongTienGio(cartItems);
    }

    @GetMapping("")
    public String showCheckout(Authentication auth, Model model) {
        User user = getCurrentUser(auth);
        List<CartItem> cartItems = cartService.getCart(user.getId());
        if (cartItems.isEmpty()) return "redirect:/cart";

        List<Product> products = cartItems.stream().map(CartItem::getProduct).collect(Collectors.toList());
        Map<Integer, FlashSaleService.EffectivePrice> priceMap = flashSaleService.getEffectivePrices(products);
        BigDecimal total = calcEffectiveTotal(cartItems);

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("total", total);
        model.addAttribute("priceMap", priceMap);
        // Danh sách tỉnh + 3 con số phí để JSP tính thử ngay khi khách đổi tỉnh.
        // Số tiền THẬT vẫn do server tính lại lúc đặt (cùng nguyên tắc với voucher).
        model.addAttribute("dsTinh", TinhThanh.DANH_SACH);
        model.addAttribute("phiNoiTinh",  systemSettingService.getValue("SHIPPING_FEE_SAME_PROVINCE", "20000"));
        model.addAttribute("phiLienTinh", systemSettingService.getValue("SHIPPING_FEE_OTHER_PROVINCE", "35000"));
        model.addAttribute("nguongMienPhi", systemSettingService.getValue("FREE_SHIPPING_THRESHOLD", "500000"));
        model.addAttribute("tinhCuaKho",  systemSettingService.getValue("SHOP_PROVINCE", "TP. Hồ Chí Minh"));
        // Giá THẬT từng dòng (dòng combo đã trừ phần giảm) — JSP hiện số này,
        // priceMap chỉ còn dùng cho giá gạch ngang.
        model.addAttribute("giaDong", comboService.tinhGiaTungDong(cartItems));
        model.addAttribute("user", user);
        model.addAttribute("requireVnpay", orderService.requiresVnpay(cartItems, total));
        return "customer/checkout";
    }

    // Kiểm mã giảm giá bằng AJAX để khách thấy ngay số tiền được giảm.
    // CHỈ để hiển thị — số tiền thật vẫn được tính lại ở placeOrder.
    @PostMapping("/apply-voucher")
    @ResponseBody
    public Map<String, Object> applyVoucher(@RequestParam String code, Authentication auth) {
        User user = getCurrentUser(auth);
        List<CartItem> cartItems = cartService.getCart(user.getId());
        BigDecimal subtotal = calcEffectiveTotal(cartItems);

        // Truyền GIỎ vào để kiểm được mã ràng buộc combo — mã loại này không thể
        // xác minh bằng mỗi tổng tiền.
        VoucherService.KetQua kq = voucherService.check(code, user.getId(), subtotal, cartItems);

        Map<String, Object> res = new java.util.HashMap<>();
        res.put("ok", kq.isHopLe());
        res.put("discount", kq.getSoTienGiam());
        res.put("message", kq.getThongBao());
        return res;
    }

    @PostMapping("")
    public String placeOrder(
            @RequestParam String receiverName,
            @RequestParam String receiverPhone,
            @RequestParam String shippingAddress,
            @RequestParam String paymentMethod,
            @RequestParam(required = false) String note,
            @RequestParam(required = false) String voucherCode,
            @RequestParam(required = false) String province,
            Authentication auth) {

        User user = getCurrentUser(auth);
        List<CartItem> cartItems = cartService.getCart(user.getId());
        if (cartItems.isEmpty()) return "redirect:/cart";

        BigDecimal total = calcEffectiveTotal(cartItems);

        // Validate server: thông tin người nhận (client lách được nên phải chặn ở đây).
        // ĐỘ DÀI phải khớp giới hạn cột trong CSDL — thiếu chốt này thì địa chỉ 600
        // ký tự sẽ đi thẳng xuống SQL Server, bị lỗi "String or binary data would be
        // truncated", và câu lệnh INSERT kèm tên bảng/tên cột bị in ra cho khách xem.
        String ten = receiverName == null ? "" : receiverName.trim();
        String diaChi = shippingAddress == null ? "" : shippingAddress.trim();

        String vErr = null;
        if (ten.isEmpty())
            vErr = "Vui lòng nhập tên người nhận.";
        else if (ten.length() > 255)
            vErr = "Tên người nhận quá dài (tối đa 255 ký tự).";
        // Tên phải là TÊN, không phải một chữ cái hay một dãy số.
        // Shipper cầm đơn ghi "a" thì không biết gọi ai khi tới nơi.
        else if (ten.length() < 2 || !ten.matches(".*\\p{L}.*"))
            vErr = "Tên người nhận chưa hợp lệ, vui lòng nhập họ tên đầy đủ.";
        else if (receiverPhone == null || !receiverPhone.trim().matches("0\\d{9}"))
            vErr = "Số điện thoại phải gồm 10 số và bắt đầu bằng 0.";
        else if (diaChi.isEmpty())
            vErr = "Vui lòng nhập địa chỉ giao hàng.";
        else if (diaChi.length() > 500)
            vErr = "Địa chỉ giao hàng quá dài (tối đa 500 ký tự).";
        // Ba chốt dưới đây bắt địa chỉ phải đủ rõ để shipper tìm được nhà: đủ dài,
        // đủ số từ, và có chữ số. Gõ cho có kiểu "abc" là bị chặn, nhưng địa chỉ
        // thật dạng "Ấp 3, Xã Tân Bình" vẫn qua được.
        else if (diaChi.length() < 10)
            vErr = "Địa chỉ giao hàng chưa đủ rõ. Vui lòng ghi số nhà, đường, phường/xã, quận/huyện.";
        else if (diaChi.split("\\s+").length < 3)
            vErr = "Địa chỉ giao hàng chưa đủ rõ. Vui lòng ghi rõ số nhà, đường và phường/xã.";
        // Chặn kiểu gõ bừa cho đủ ký tự: "aaaaaaaaaaaa", "............"
        else if (diaChi.replaceAll("(.)\\1+", "$1").length() < 8)
            vErr = "Địa chỉ giao hàng chưa hợp lệ, vui lòng nhập lại.";
        else if (note != null && note.length() > 500)
            vErr = "Ghi chú quá dài (tối đa 500 ký tự).";
        // Ô chọn tỉnh ở giao diện chỉ là gợi ý — người gửi thẳng request điền được
        // chuỗi bất kỳ. Không kiểm thì lưu tỉnh rác vào đơn và báo cáo theo khu vực
        // sau này đầy dòng vô nghĩa.
        else if (!TinhThanh.hopLe(province))
            vErr = "Vui lòng chọn tỉnh/thành giao hàng.";
        if (vErr != null) {
            return "redirect:/checkout?error=" + java.net.URLEncoder.encode(
                    vErr, java.nio.charset.StandardCharsets.UTF_8);
        }

        // VOUCHER: luôn TÍNH LẠI ở server — không tin số tiền client gửi lên.
        // Mã hỏng/hết hạn thì bỏ qua, đơn vẫn đặt được với giá gốc.
        BigDecimal giamGia = BigDecimal.ZERO;
        Integer voucherId = null;
        if (voucherCode != null && !voucherCode.trim().isEmpty()) {
            VoucherService.KetQua kq = voucherService.check(voucherCode, user.getId(), total, cartItems);
            if (kq.isHopLe()) {
                giamGia = kq.getSoTienGiam();
                voucherId = kq.getVoucherId();
            }
        }

        Order order = new Order();
        order.setUser(user);
        order.setReceiverName(receiverName);
        order.setReceiverPhone(receiverPhone);
        order.setShippingAddress(shippingAddress);
        order.setPaymentMethod(paymentMethod);
        order.setNote(note);
        order.setDiscountAmount(giamGia);
        order.setVoucherId(voucherId);

        // PHÍ VẬN CHUYỂN — tính trên tiền hàng SAU giảm giá, vì ngưỡng miễn phí
        // phải áp cho số tiền khách thực trả cho hàng, không phải giá niêm yết.
        // Tính lại ở server, không nhận con số nào từ trình duyệt (luật 6).
        BigDecimal tienHang = total.subtract(giamGia);
        BigDecimal phiShip  = tinhPhiVanChuyen.tinhPhi(tienHang, province);
        order.setProvince(province.trim());
        order.setShippingFee(phiShip);
        order.setTotalAmount(tienHang.add(phiShip));

        Order saved;
        try {
            saved = orderService.createOrder(order, cartItems);
        } catch (LoiNghiepVu e) {
            // LỖI NGHIỆP VỤ — câu tiếng Việt viết cố ý cho khách đọc
            // ("hết hàng", "vượt giới hạn đơn COD"…). Hiện nguyên văn.
            return "redirect:/checkout?error=" + java.net.URLEncoder.encode(
                    e.getMessage(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (RuntimeException e) {
            // Mọi lỗi còn lại là hỏng hóc hệ thống: ghi chi tiết vào log, còn khách chỉ
            // thấy một câu chung. Không hiện thông điệp thô của Hibernate ra màn hình
            // vì nó lộ tên database, tên bảng và tên cột.
            log.error("Lỗi hệ thống khi tạo đơn hàng", e);
            return "redirect:/checkout?error=" + java.net.URLEncoder.encode(
                    "Không đặt được đơn hàng, vui lòng thử lại hoặc liên hệ 0900 000 011.",
                    java.nio.charset.StandardCharsets.UTF_8);
        }

        // Đơn đã tạo xong -> ghi nhận lượt dùng mã (chặn dùng lại + lưu vết)
        if (voucherId != null) {
            voucherService.ghiNhanDaDung(voucherId, user.getId(), saved.getId(), giamGia);
        }

        if ("VNPAY".equals(paymentMethod)) {
            // amount không truyền qua URL nữa — trang payment tự đọc từ DB
            return "redirect:/checkout/payment?orderId=" + saved.getId();
        }

        // COD: KHÔNG set isPaid ở đây — khách chưa trả tiền.
        // isPaid sẽ được set khi đơn chuyển COMPLETED (giao thành công, thu tiền)
        // — xem OrderService.updateStatus.
        return "redirect:/checkout/success?orderId=" + saved.getId();
    }

    @GetMapping("/payment")
    public String showPayment(@RequestParam Integer orderId,
                              Authentication auth,
                              Model model) {
        // CHỐNG IDOR: chỉ chủ đơn mới xem được trang thanh toán của đơn đó
        Order order = getOwnedOrderOrNull(orderId, auth);
        if (order == null) return "redirect:/my-orders";

        // SỐ TIỀN LẤY TỪ DB, không tin query param (chống sửa amount trên URL
        // để QR hiển thị số tiền nhỏ hơn giá trị đơn thật)
        long amount = order.getTotalAmount().longValue();

        model.addAttribute("orderId", orderId);
        model.addAttribute("amount", amount);
        String qrUrl = "https://img.vietqr.io/image/MB-205072009-compact2.png"
                + "?amount=" + amount
                + "&addInfo=TiTanGEAR%20Don%20" + orderId
                + "&accountName=Pham%20Minh%20Tu";
        model.addAttribute("qrUrl", qrUrl);
        return "customer/payment-mbbank";
    }

    // Khách bấm "Tôi đã chuyển khoản": chỉ điều hướng sang trang thành công với trạng
    // thái chờ xác nhận. Hàm này KHÔNG đánh dấu đã thanh toán — việc đó chỉ do webhook
    // của SePay làm khi tiền thật vào tài khoản.
    @GetMapping("/confirm-payment")
    public String confirmPayment(@RequestParam Integer orderId, Authentication auth) {
        Order order = getOwnedOrderOrNull(orderId, auth);
        if (order == null) return "redirect:/my-orders";
        return "redirect:/checkout/success?orderId=" + orderId;
    }

    @GetMapping("/success")
    public String orderSuccess(@RequestParam Integer orderId,
                               Authentication auth, Model model) {
        // Chỉ chủ đơn mới xem được chi tiết đơn của mình
        Order order = getOwnedOrderOrNull(orderId, auth);
        if (order == null) return "redirect:/my-orders";
        model.addAttribute("order", order);
        return "customer/order-success";
    }

    // Hết 15 phút → hủy đơn
    @GetMapping("/expired")
    public String orderExpired(@RequestParam Integer orderId,
                               Authentication auth, Model model) {
        // Chỉ chủ đơn mới hủy được đơn của mình
        Order order = getOwnedOrderOrNull(orderId, auth);
        if (order != null
                && OrderStatus.PENDING.equals(order.getStatus())
                && !Boolean.TRUE.equals(order.getIsPaid())) {
            orderService.updateStatus(orderId, OrderStatus.CANCELLED);
        }
        model.addAttribute("orderId", orderId);
        return "customer/order-expired";
    }
}
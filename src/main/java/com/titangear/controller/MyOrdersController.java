package com.titangear.controller;

import com.titangear.constant.OrderStatus;

import com.titangear.model.Order;
import com.titangear.model.OrderItem;
import com.titangear.model.User;
import com.titangear.model.Warranty;
import com.titangear.repository.OrderItemRepository;
import com.titangear.service.OrderService;
import com.titangear.service.UserService;
import com.titangear.service.WarrantyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/my-orders")
public class MyOrdersController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private WarrantyService warrantyService;

    private User getCurrentUser(Authentication auth) {
        return userService.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));
    }

    // Xem danh sách đơn hàng — có filter theo status
    @GetMapping("")
    public String myOrders(
            @RequestParam(required = false) String status,
            Authentication auth,
            Model model) {

        User user = getCurrentUser(auth);
        List<Order> orders;

        if (status != null && !status.isEmpty()) {
            orders = orderService.getOrdersByUserAndStatus(user.getId(), status);
        } else {
            orders = orderService.getOrdersByUser(user.getId());
        }

        // Nút "Đánh giá" (đơn COMPLETED) link thẳng tới trang sản phẩm: map
        // orderId -> product id món ĐẦU TIÊN (Order không kèm sẵn items).
        Map<Integer, Integer> firstProductByOrder = new HashMap<>();
        // Nút "Bảo hành": chỉ hiện khi đơn THẬT SỰ có phiếu (sản phẩm không khai
        // số tháng bảo hành thì không phát phiếu — hiện nút sẽ làm khách hụt hẫng).
        // Dùng chung vòng lặp trên để khỏi tra order_items thêm một lượt nữa.
        Map<Integer, Boolean> coBaoHanh = new HashMap<>();
        for (Order o : orders) {
            List<OrderItem> its = orderItemRepository.findByOrderId(o.getId());
            if (!its.isEmpty() && its.get(0).getProduct() != null) {
                firstProductByOrder.put(o.getId(), its.get(0).getProduct().getId());
            }
            coBaoHanh.put(o.getId(),
                    its.stream().anyMatch(it -> warrantyService.coPhieuChoDongDon(it.getId())));
        }
        model.addAttribute("firstProductByOrder", firstProductByOrder);
        model.addAttribute("coBaoHanh", coBaoHanh);

        model.addAttribute("user", user);
        model.addAttribute("orders", orders);
        return "customer/my-orders";
    }

    // Chi tiết 1 đơn của KHÁCH.
    //
    // Bắt buộc đối chiếu chủ đơn: thiếu bước này thì khách chỉ cần đổi số trên
    // thanh địa chỉ (/my-orders/1035 -> /my-orders/1036) là xem trọn đơn của
    // người khác, kèm tên, số điện thoại, địa chỉ nhà họ.
    @GetMapping("/{orderId}")
    public String chiTiet(@PathVariable Integer orderId, Authentication auth, Model model) {
        User user = getCurrentUser(auth);
        Order order = orderService.findById(orderId).orElse(null);
        if (order == null || order.getUser() == null
                || !user.getId().equals(order.getUser().getId())) {
            return "redirect:/my-orders";
        }

        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);

        // Serial bảo hành theo từng dòng đơn — khách xem đơn là thấy luôn mã máy mình
        Map<Integer, List<Warranty>> baoHanh = new HashMap<>();
        for (OrderItem it : items) {
            List<Warranty> ds = warrantyService.theoDongDon(it.getId());
            if (!ds.isEmpty()) baoHanh.put(it.getId(), ds);
        }

        model.addAttribute("user", user);
        model.addAttribute("order", order);
        model.addAttribute("items", items);
        model.addAttribute("baoHanh", baoHanh);
        return "customer/order-detail";
    }

    // Khách tự hủy đơn, chỉ hủy được khi đơn còn ở trạng thái Chờ xác nhận.
    // Mọi nhánh đều phải báo kết quả: hủy được thì báo thành công, không hủy được
    // thì nói rõ đơn đang ở trạng thái nào. Im lặng redirect về là khách bấm xong
    // thấy đơn vẫn nằm đó mà không hiểu vì sao.
    @PostMapping("/{orderId}/cancel")
    public String cancelOrder(@PathVariable Integer orderId, Authentication auth,
                              org.springframework.web.servlet.mvc.support.RedirectAttributes ra) {
        User user = getCurrentUser(auth);

        Order order = orderService.findById(orderId).orElse(null);
        if (order == null || !order.getUser().getId().equals(user.getId())) {
            // Không phải đơn của mình: trả về đúng một câu, KHÔNG nói rõ đơn có
            // tồn tại hay không để người ngoài không dò được id đơn của người khác.
            ra.addFlashAttribute("loi", "Không tìm thấy đơn hàng này.");
            return "redirect:/my-orders";
        }
        if (!OrderStatus.PENDING.equals(order.getStatus())) {
            ra.addFlashAttribute("loi", "Đơn #" + orderId + " đang ở trạng thái \""
                    + nhanTrangThai(order.getStatus()) + "\" nên không tự hủy được nữa. "
                    + "Vui lòng liên hệ shop để được hỗ trợ.");
            return "redirect:/my-orders";
        }
        // Đơn đã thanh toán online mà khách tự hủy thì shop phải hoàn tiền tay —
        // báo rõ để khách biết đường chờ, đừng để khách tưởng mất tiền.
        boolean daTra = Boolean.TRUE.equals(order.getIsPaid());

        orderService.updateStatus(orderId, OrderStatus.CANCELLED);
        ra.addFlashAttribute("thanhCong", daTra
                ? "Đã hủy đơn #" + orderId + ". Đơn này đã thanh toán online, shop sẽ liên hệ hoàn tiền."
                : "Đã hủy đơn #" + orderId + ".");
        return "redirect:/my-orders";
    }

    // Nhãn tiếng Việt của trạng thái đơn — dùng trong câu báo cho khách.
    private String nhanTrangThai(String s) {
        if (OrderStatus.PENDING.equals(s))        return "Chờ xác nhận";
        if (OrderStatus.AWAITING_STOCK.equals(s)) return "Chờ hàng";
        if (OrderStatus.SHIPPING.equals(s))       return "Đang giao";
        if (OrderStatus.COMPLETED.equals(s))      return "Hoàn thành";
        if (OrderStatus.CANCELLED.equals(s))      return "Đã hủy";
        return String.valueOf(s);
    }
}
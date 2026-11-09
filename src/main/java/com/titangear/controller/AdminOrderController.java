package com.titangear.controller;

import com.titangear.config.RequirePermission;

import com.titangear.constant.OrderStatus;

import com.titangear.model.Order;
import com.titangear.model.User;
import com.titangear.repository.OrderItemRepository;
import com.titangear.service.OrderService;
import com.titangear.service.UserService;
import com.titangear.service.WarehousePermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminOrderController {

    @Autowired private OrderService orderService;
    @Autowired private UserService userService;
    @Autowired private OrderItemRepository orderItemRepository;
    // Dùng cho phiếu xuất kho: xem trước FIFO sẽ lấy lô nào, hoặc đã lấy lô nào.
    @Autowired private com.titangear.service.StockService stockService;

    private static final int PAGE_SIZE = 10;

    @RequirePermission(WarehousePermissionService.CAN_MANAGE_ORDERS)
    @GetMapping("/orders")
    public String listOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(required = false) String paid,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            Authentication auth,
            Model model) {

        Pageable pageable = PageRequest.of(page, PAGE_SIZE,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Order> orderPage;
        if (q != null && !q.trim().isEmpty()) {
            orderPage = orderService.searchOrders(q.trim(), pageable);   // tìm theo tên/SĐT/mã đơn
        } else if (status != null && !status.isEmpty()) {
            orderPage = orderService.getOrdersByStatusPaged(status, pageable);
        } else {
            orderPage = orderService.getOrdersPaged(pageable);
        }

        List<Order> filtered = orderPage.getContent().stream()
                .filter(o -> paymentMethod == null || paymentMethod.isEmpty()
                        || paymentMethod.equals(o.getPaymentMethod()))
                .filter(o -> {
                    if ("paid".equals(paid))   return Boolean.TRUE.equals(o.getIsPaid());
                    if ("unpaid".equals(paid)) return !Boolean.TRUE.equals(o.getIsPaid());
                    return true;
                })
                .toList();

        model.addAttribute("orders",          filtered);
        model.addAttribute("currentPage",     page);
        model.addAttribute("totalPages",      orderPage.getTotalPages());
        model.addAttribute("totalOrders",     orderService.countAll());
        model.addAttribute("selectedPayment", paymentMethod);
        model.addAttribute("selectedPaid",    paid);

        // ---- Số đếm cho tab (bám mẫu Admin) ----
        model.addAttribute("cntPending",   orderService.countByStatus("PENDING"));
        model.addAttribute("cntShipping",  orderService.countByStatus("SHIPPING"));
        model.addAttribute("cntCompleted", orderService.countByStatus("COMPLETED"));
        model.addAttribute("cntCancelled", orderService.countByStatus("CANCELLED"));
        model.addAttribute("cntAwaiting",  orderService.countByStatus("AWAITING_STOCK"));

        // ---- Thẻ thống kê ----
        java.time.LocalDate now = java.time.LocalDate.now();
        int m = now.getMonthValue(), y = now.getYear();
        List<Order> completed = orderService.getOrdersByStatus("COMPLETED");
        long completedMonth = completed.stream()
                .filter(o -> o.getCreatedAt() != null
                        && o.getCreatedAt().getMonthValue() == m
                        && o.getCreatedAt().getYear() == y)
                .count();
        java.math.BigDecimal revenueMonth = completed.stream()
                .filter(o -> o.getCreatedAt() != null
                        && o.getCreatedAt().getMonthValue() == m
                        && o.getCreatedAt().getYear() == y)
                .map(Order::getTotalAmount)
                .filter(java.util.Objects::nonNull)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        model.addAttribute("statPending",        orderService.countByStatus("PENDING"));
        model.addAttribute("statShipping",       orderService.countByStatus("SHIPPING"));
        model.addAttribute("statCompletedMonth", completedMonth);
        model.addAttribute("statRevenueMonth",    revenueMonth);
        model.addAttribute("statRevenueMonthM",   revenueMonth.doubleValue() / 1_000_000.0);
        model.addAttribute("statMonthLabel",      String.format("%02d/%d", m, y));

        return "admin/orders";
    }

    @RequirePermission(WarehousePermissionService.CAN_MANAGE_ORDERS)
    @GetMapping("/orders/{id}")
    public String orderDetail(@PathVariable Integer id, Model model) {
        Order order = orderService.findById(id).orElse(null);
        if (order == null) {
            return "redirect:/admin/orders";
        }
        java.util.List<com.titangear.model.OrderItem> items = orderItemRepository.findByOrderId(id);
        model.addAttribute("order", order);
        model.addAttribute("items", items);

        // PHIẾU XUẤT KHO — chỉ cho thủ kho biết phải ra khu nào, kệ nào, lấy
        // thùng có số lô nào. Không có phần này thì hệ thống tính FIFO xong
        // nhưng không nói ra, thủ kho ra kho lấy đại thùng nào gần tay: trên sổ
        // sách là FIFO còn ngoài kho là ngẫu nhiên.
        //
        // Hai trạng thái khác nhau:
        //   - đơn CHƯA duyệt  -> phiếu DỰ KIẾN, tính trước xem FIFO sẽ lấy lô nào
        //   - đơn ĐÃ duyệt    -> phiếu BẰNG CHỨNG, đọc từ stock_out đã ghi;
        //                        dùng khi cần truy ngược lô lỗi bị thu hồi.
        boolean chuaXuat = OrderStatus.PENDING.equals(order.getStatus())
                        || OrderStatus.AWAITING_STOCK.equals(order.getStatus());
        if (chuaXuat) {
            java.util.Map<Integer, java.util.List<com.titangear.service.StockService.PhanLo>> phieu =
                    new java.util.LinkedHashMap<>();
            for (com.titangear.model.OrderItem it : items) {
                phieu.put(it.getId(), stockService.xemTruocPhieuXuat(it));
            }
            model.addAttribute("phieuXuat", phieu);
        } else {
            model.addAttribute("daXuat", stockService.daXuatTheoDon(id));
        }
        model.addAttribute("chuaXuat", chuaXuat);

        model.addAttribute("adminActive", "orders");
        return "admin/order-detail";
    }

    @RequirePermission(WarehousePermissionService.CAN_MANAGE_ORDERS)
    @PostMapping("/orders/{id}/confirm-payment")
    public String confirmPayment(@PathVariable Integer id, Authentication auth) {
        orderService.confirmPayment(id);
        return "redirect:/admin/orders";
    }

    @RequirePermission(WarehousePermissionService.CAN_MANAGE_ORDERS)
    @PostMapping("/orders/{id}/status")
    public String updateStatus(
            @PathVariable("id") Integer id,
            @RequestParam("status") String status,
            Authentication auth,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {

        try {
            if (OrderStatus.SHIPPING.equals(status)) {
                Integer adminId = userService.findByEmail(auth.getName())
                        .map(u -> u.getId()).orElse(1);
                Order order = orderService.findById(id).orElse(null);
                if (order != null) {
                    if (OrderStatus.PENDING.equals(order.getStatus())
                            || OrderStatus.AWAITING_STOCK.equals(order.getStatus())) {
                        // approveExport trả "success" hoặc chuỗi "Thiếu hàng…" (đã đặt
                        // đơn về AWAITING_STOCK) — surface ra để admin biết, không nuốt.
                        String result = orderService.approveExport(id, adminId);
                        if (!"success".equals(result)) {
                            redirectAttributes.addFlashAttribute("errorMsg", result);
                        }
                    } else {
                        orderService.updateStatus(id, status);
                    }
                }
            } else {
                orderService.updateStatus(id, status);
            }
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }

        return "redirect:/admin/orders";
    }
}
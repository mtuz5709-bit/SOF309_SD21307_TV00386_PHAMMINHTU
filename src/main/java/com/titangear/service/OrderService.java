package com.titangear.service;

import com.titangear.exception.LoiNghiepVu;
import com.titangear.constant.OrderStatus;

import com.titangear.model.*;
import com.titangear.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    @Autowired private OrderRepository      orderRepository;
    @Autowired private OrderItemRepository  orderItemRepository;
    @Autowired private CartItemRepository   cartItemRepository;
    @Autowired private StockService         stockService;
    @Autowired private WarrantyService      warrantyService;
    @Autowired private FlashSaleService     flashSaleService;
    @Autowired private SystemSettingService systemSettingService;
    @Autowired private ComboService         comboService;
    @Autowired private AuditLogService      auditLogService;

    private static final String COD_MAX_QTY_KEY    = "COD_MAX_QTY_PER_PRODUCT";
    private static final String COD_MAX_ACTIVE_ORDERS_KEY = "COD_MAX_ACTIVE_ORDERS";
    private static final String COD_MAX_AMOUNT_KEY = "COD_MAX_ORDER_AMOUNT";
    private static final String MAX_QTY_KEY        = "MAX_QTY_PER_PRODUCT_PER_ORDER";

    // Kiểm số lượng từng dòng giỏ trước khi chạm tới DB: chặn số âm, số 0, và số
    // vượt giới hạn mỗi đơn đặt trong Cài đặt hệ thống. Trigger TRG_OrderItems_MaxQtyLimit
    // trong DB kiểm lại lần nữa làm lớp cuối.
    private void validateMaxQtyPerProduct(List<CartItem> cartItems) {
        int maxQty;
        try {
            maxQty = Integer.parseInt(systemSettingService.getValue(MAX_QTY_KEY, "10"));
        } catch (NumberFormatException e) {
            maxQty = 10;
        }
        for (CartItem item : cartItems) {
            // Chặn số lượng âm và bằng 0. Các chốt khác đều so theo chiều "lớn hơn"
            // nên số âm lọt hết, mà số âm thì thành tiền âm và tổng đơn bị trừ đi.
            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new LoiNghiepVu(
                        "Số lượng của \"" + item.getProduct().getName() + "\" không hợp lệ. "
                                + "Vui lòng xóa sản phẩm này khỏi giỏ và thêm lại.");
            }
            if (item.getQuantity() > maxQty) {
                throw new LoiNghiepVu(
                        "Sản phẩm \"" + item.getProduct().getName() + "\" vượt quá số lượng tối đa cho phép ("
                                + maxQty + " sản phẩm/đơn hàng). Vui lòng giảm số lượng trong giỏ hàng.");
            }
        }
    }

    // Xét số lượng của đơn có vượt ngưỡng cho phép thanh toán COD không. Vượt thì
    // khách buộc phải chuyển khoản trước qua VNPAY.
    public boolean requiresVnpayForQuantity(List<CartItem> cartItems) {
        int threshold;
        try {
            threshold = Integer.parseInt(systemSettingService.getValue(COD_MAX_QTY_KEY, "3"));
        } catch (NumberFormatException e) {
            threshold = 3;
        }
        final int maxQty = threshold;
        return cartItems.stream().anyMatch(item -> item.getQuantity() > maxQty);
    }

    // Xét tổng tiền đơn có vượt ngưỡng cho phép COD không. Tổng tiền đã tính theo
    // giá sau giảm nếu sản phẩm đang có flash sale.
    public boolean requiresVnpayForAmount(java.math.BigDecimal totalAmount) {
        java.math.BigDecimal threshold;
        try {
            threshold = new java.math.BigDecimal(systemSettingService.getValue(COD_MAX_AMOUNT_KEY, "5000000"));
        } catch (NumberFormatException e) {
            threshold = new java.math.BigDecimal("5000000");
        }
        return totalAmount != null && totalAmount.compareTo(threshold) > 0;
    }

    // Gộp hai điều kiện: chỉ cần số lượng hoặc tổng tiền vượt ngưỡng là bắt buộc VNPAY.
    public boolean requiresVnpay(List<CartItem> cartItems, java.math.BigDecimal totalAmount) {
        return requiresVnpayForQuantity(cartItems) || requiresVnpayForAmount(totalAmount);
    }

    // Đếm số đơn COD đang chờ giao của khách. Vượt ngưỡng đặt trong Cài đặt hệ thống
    // thì chặn đặt thêm và gợi ý chuyển sang VNPAY. Đơn VNPAY không bị giới hạn vì
    // tiền đã thu trước.
    private void validateCodActiveOrderLimit(Order order) {
        if (!"COD".equals(order.getPaymentMethod())) return;

        int maxActive;
        try {
            maxActive = Integer.parseInt(systemSettingService.getValue(COD_MAX_ACTIVE_ORDERS_KEY, "3"));
        } catch (NumberFormatException e) {
            maxActive = 3;
        }

        long dangCho = orderRepository.countActiveCodOrders(order.getUser().getId());
        if (dangCho >= maxActive) {
            throw new LoiNghiepVu(
                    "Bạn đang có " + dangCho + " đơn COD chưa giao xong (tối đa " + maxActive + "). "
                            + "Vui lòng đợi giao xong, hoặc chọn thanh toán chuyển khoản (VNPAY) để đặt thêm ngay.");
        }
    }

    // Kiểm mọi sản phẩm và biến thể trong giỏ còn đang bán không. Giỏ hàng là dữ
    // liệu sống lâu, khách để vài ngày là admin có thể đã ngừng bán món đó.
    private void validateConDangBan(List<CartItem> cartItems) {
        for (CartItem cartItem : cartItems) {
            Product p = cartItem.getProduct();
            ProductVariant variant = cartItem.getVariant();
            if (!Boolean.TRUE.equals(p.getIsActive())) {
                throw new LoiNghiepVu("Sản phẩm \"" + p.getName()
                        + "\" đã ngừng kinh doanh. Vui lòng xóa khỏi giỏ hàng.");
            }
            // Màu đã ngừng bán thì cũng không đặt được
            if (variant != null && !Boolean.TRUE.equals(variant.getIsActive())) {
                throw new LoiNghiepVu("Phiên bản \"" + p.getName()
                        + (variant.getColor() != null ? " - " + variant.getColor() : "")
                        + "\" đã ngừng kinh doanh. Vui lòng chọn màu khác.");
            }
        }
    }

    // Tạo đơn hàng: chạy lần lượt các chốt kiểm, ghi bảng orders và order_items,
    // rồi xóa giỏ hàng của khách.
    @Transactional
    public Order createOrder(Order order, List<CartItem> cartItems) {
        validateMaxQtyPerProduct(cartItems);

        // Kiểm giỏ còn bán được TRƯỚC các chốt thanh toán, để không báo nhầm lý do
        // "vui lòng chuyển khoản" cho đơn thật ra chứa hàng đã ngừng kinh doanh.
        validateConDangBan(cartItems);

        validateCodActiveOrderLimit(order);

        if ("COD".equals(order.getPaymentMethod()) && requiresVnpay(cartItems, order.getTotalAmount())) {
            throw new LoiNghiepVu(
                    "Đơn hàng có số lượng hoặc giá trị lớn, vui lòng thanh toán trước qua chuyển khoản (VNPAY) để đặt hàng.");
        }

        for (CartItem cartItem : cartItems) {
            Product p = cartItem.getProduct();
            ProductVariant variant = cartItem.getVariant();

            // Kiểm tồn theo biến thể nếu khách chọn màu, không thì kiểm ở mức sản phẩm.
            // Số đặt được = tồn kho trừ đi phần đã nằm trong đơn khác chưa giao.
            int onHand;
            int reserved;
            String label;
            if (variant != null) {
                onHand   = variant.getStock() != null ? variant.getStock() : 0;
                Integer r = orderItemRepository.getReservedQtyByVariant(variant.getId());
                reserved = r != null ? r : 0;
                label = p.getName() + " (" + variant.getName()
                        + (variant.getColor() != null ? " - " + variant.getColor() : "") + ")";
            } else {
                onHand   = p.getStockQuantity() != null ? p.getStockQuantity() : 0;
                Integer r = orderItemRepository.getTotalPendingQuantity(p.getId());
                reserved = r != null ? r : 0;
                label = p.getName();
            }
            int availableQty = onHand - reserved;
            if (availableQty < cartItem.getQuantity()) {
                throw new LoiNghiepVu(
                        "\"" + label + "\" không đủ hàng! " +
                                "Còn có thể đặt: " + Math.max(0, availableQty) + " sản phẩm.");
            }
        }
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        Order savedOrder = orderRepository.save(order);

        // Chốt giá tại thời điểm đặt hàng. Gọi ComboService một lần cho cả giỏ để
        // lấy giá đã tính flash sale và đã chia tiền giảm của combo vào từng dòng.
        Map<Integer, ComboService.GiaDong> bangGia = comboService.tinhGiaTungDong(cartItems);

        for (CartItem cartItem : cartItems) {
            ComboService.GiaDong gia = bangGia.get(cartItem.getId());

            OrderItem item = new OrderItem();
            item.setOrder(savedOrder);
            item.setProduct(cartItem.getProduct());
            item.setVariant(cartItem.getVariant());
            // Giữ combo_id để chi tiết đơn gom được các món cùng bộ, và để báo cáo
            // tách được doanh thu combo với doanh thu bán lẻ.
            item.setCombo(cartItem.getCombo());
            item.setProductName(cartItem.getProduct().getName());
            item.setUnitPrice(gia.getDonGia());
            item.setQuantity(cartItem.getQuantity());
            // Lấy thẳng thành tiền đã chia. Không nhân lại đơn giá, vì phần tiền lẻ
            // do làm tròn đã dồn vào dòng cuối để tổng khớp đúng giá khách đã thấy.
            item.setLineTotal(gia.getThanhTien());
            orderItemRepository.save(item);
        }

        cartItemRepository.deleteByUserId(order.getUser().getId());
        return savedOrder;
    }


    // Đổi mã trạng thái sang nhãn tiếng Việt để đưa vào câu báo cho admin.
    private String nhanTrangThai(String s) {
        if (OrderStatus.PENDING.equals(s))        return "Chờ xác nhận";
        if (OrderStatus.AWAITING_STOCK.equals(s)) return "Chờ hàng";
        if (OrderStatus.SHIPPING.equals(s))       return "Đang giao";
        if (OrderStatus.COMPLETED.equals(s))      return "Hoàn thành";
        if (OrderStatus.CANCELLED.equals(s))      return "Đã hủy";
        return String.valueOf(s);
    }

    @Transactional
    public String approveExport(Integer orderId, Integer exportedByUserId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn"));

        // Chặn duyệt xuất kho lần thứ hai cho cùng một đơn, vì như vậy là trừ tồn
        // gấp đôi. Chốt đặt ở service để mọi đường gọi tới đều đi qua.
        if (!OrderStatus.PENDING.equals(order.getStatus())
                && !OrderStatus.AWAITING_STOCK.equals(order.getStatus())) {
            throw new LoiNghiepVu("Đơn này đã được duyệt xuất kho rồi (đang ở trạng thái \""
                    + nhanTrangThai(order.getStatus()) + "\").");
        }

        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);

        // Kiểm tồn kho trước khi xuất, theo biến thể nếu order_item có biến thể.
        // Dùng tồn thực có, không trừ phần giữ chỗ vì chính đơn này đang được xuất.
        for (OrderItem item : items) {
            Product p = item.getProduct();
            ProductVariant variant = item.getVariant();
            int onHand = (variant != null)
                    ? (variant.getStock() != null ? variant.getStock() : 0)
                    : (p.getStockQuantity() != null ? p.getStockQuantity() : 0);
            if (onHand < item.getQuantity()) {
                order.setStatus(OrderStatus.AWAITING_STOCK);
                order.setUpdatedAt(LocalDateTime.now());
                orderRepository.save(order);
                String what = (variant != null)
                        ? p.getName() + " (" + variant.getName()
                          + (variant.getColor() != null ? " - " + variant.getColor() : "") + ")"
                        : p.getName();
                return "Thiếu hàng: " + what
                        + " (cần " + item.getQuantity()
                        + ", còn " + onHand + ")";
            }
        }

        User exporter = new User();
        exporter.setId(exportedByUserId);

        for (OrderItem item : items) {
            // FIFO chọn lô theo biến thể nếu có, StockService tự phân biệt.
            // Trigger DB tự trừ tồn qua remaining_quantity của lô. Không trừ
            // variant.stock bằng tay ở đây, sẽ thành trừ hai lần.
            stockService.stockOutFIFO(item, item.getQuantity(), exporter);
        }

        order.setStatus(OrderStatus.SHIPPING);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        // Ghi nhật ký: đây là thao tác trừ tồn kho, cần biết ai duyệt
        auditLogService.ghi(AuditLogService.DUYET_DON, "orders", orderId,
                "Duyệt xuất kho đơn #" + orderId + " (" + items.size() + " dòng hàng)");
        return "success";
    }


    public Optional<Order> findById(Integer id) {
        return orderRepository.findById(id);
    }

    public List<Order> getOrdersByUser(Integer userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public List<Order> getOrdersByStatus(String status) {
        return orderRepository.findByStatus(status);
    }

    public List<Order> getOrdersByUserAndStatus(Integer userId, String status) {
        return orderRepository.findByUserIdAndStatus(userId, status);
    }

    public Page<Order> getOrdersPaged(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    public Page<Order> getOrdersByStatusPaged(String status, Pageable pageable) {
        return orderRepository.findByStatus(status, pageable);
    }

    // Tìm đơn theo tên hoặc số điện thoại người nhận, hoặc theo mã đơn
    public Page<Order> searchOrders(String q, Pageable pageable) {
        return orderRepository.searchOrders(q, pageable);
    }

    public long countAll() {
        return orderRepository.count();
    }

    public long countByStatus(String status) {
        return orderRepository.countByStatus(status);
    }

    // Đánh dấu đơn đã nhận được tiền. Chỉ webhook của cổng thanh toán gọi hàm này,
    // sau khi đã đối chiếu số tiền thật chuyển vào khớp với tổng đơn.
    //
    // Đơn ĐÃ HỦY thì KHÔNG đánh dấu đã trả tiền, chỉ ghi log cảnh báo để người
    // quản trị đối soát và hoàn tiền cho khách.
    //
    // Vì sao cần chốt này: đơn chuyển khoản chưa trả tiền sẽ bị DonQuaHanService
    // tự hủy sau 15 phút và trả hàng về kho. Nếu tiền vào SAU mốc đó — khách chuyển
    // chậm, ngân hàng trễ, hoặc webhook lỗi rồi mới gửi lại — thì không có chốt này
    // đơn sẽ mang trạng thái tự mâu thuẫn: "đã hủy" nhưng "đã thanh toán". Tiền thật
    // nằm trong tài khoản, hàng đã trả về kho, khách không nhận được gì, mà hệ thống
    // không báo cho ai biết.
    @Transactional
    public void confirmPayment(Integer orderId) {
        orderRepository.findById(orderId).ifPresent(order -> {
            if (OrderStatus.CANCELLED.equals(order.getStatus())) {
                log.warn("Nhận được tiền cho đơn #{} nhưng đơn ĐÃ HỦY (quá hạn thanh toán) "
                       + "-> KHÔNG đánh dấu đã trả tiền. Cần đối soát và hoàn {}đ cho khách.",
                        orderId, order.getTotalAmount());
                return;
            }
            order.setIsPaid(true);
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);
        });
    }

    // Bảng khai các bước chuyển trạng thái hợp lệ: từ trạng thái này đi được tới
    // những trạng thái nào. Mọi bước nhảy không có trong bảng đều bị chặn.
    // Đơn phải đi qua SHIPPING — nơi duy nhất gọi xuất kho FIFO — mới lên COMPLETED được.
    private static final Map<String, Set<String>> LUONG_HOP_LE = Map.of(
            OrderStatus.PENDING,        Set.of(OrderStatus.SHIPPING, OrderStatus.AWAITING_STOCK, OrderStatus.CANCELLED),
            OrderStatus.AWAITING_STOCK, Set.of(OrderStatus.SHIPPING, OrderStatus.CANCELLED),
            OrderStatus.SHIPPING,       Set.of(OrderStatus.COMPLETED, OrderStatus.CANCELLED),
            OrderStatus.COMPLETED,      Set.of(),
            OrderStatus.CANCELLED,      Set.of()
    );

    // Lấy các trạng thái đích hợp lệ từ trạng thái hiện tại, dùng để dựng ô chọn
    // ở trang quản trị.
    public Set<String> trangThaiCoTheChuyen(String hienTai) {
        return LUONG_HOP_LE.getOrDefault(hienTai, Set.of());
    }

    @Transactional
    public void updateStatus(Integer orderId, String status) {
        orderRepository.findById(orderId).ifPresent(order -> {
            String hienTai = order.getStatus();
            if (OrderStatus.COMPLETED.equals(hienTai) || OrderStatus.CANCELLED.equals(hienTai)) {
                throw new LoiNghiepVu(
                        "Đơn hàng đã kết thúc (Hoàn thành/Đã hủy), không thể thay đổi trạng thái nữa!");
            }
            // Bấm lại đúng trạng thái đang có thì bỏ qua
            if (hienTai.equals(status)) return;

            if (!trangThaiCoTheChuyen(hienTai).contains(status)) {
                throw new LoiNghiepVu(
                        "Không thể chuyển đơn từ \"" + nhanTrangThai(hienTai) + "\" sang \""
                                + nhanTrangThai(status) + "\". "
                                + "Đơn phải được duyệt xuất kho (Đang giao) trước khi Hoàn thành.");
            }
            // Ghi nhật ký: đổi trạng thái đơn đụng tới tiền và tồn kho nên phải biết
            // ai làm. Hủy đơn dùng mã hành động riêng cho dễ lọc.
            auditLogService.ghi(
                    OrderStatus.CANCELLED.equals(status)
                            ? AuditLogService.HUY_DON : AuditLogService.DOI_TRANG_THAI,
                    "orders", orderId,
                    nhanTrangThai(hienTai), nhanTrangThai(status),
                    "Đơn #" + orderId + ": " + nhanTrangThai(hienTai) + " → " + nhanTrangThai(status));

            order.setStatus(status);
            order.setUpdatedAt(LocalDateTime.now());

            // Đơn COD chỉ đánh dấu đã thanh toán khi chuyển sang COMPLETED, vì tiền
            // thu lúc giao hàng thành công chứ không phải lúc đặt.
            if (OrderStatus.COMPLETED.equals(status) && "COD".equals(order.getPaymentMethod())) {
                order.setIsPaid(true);
            }

            // Giao xong thì tự sinh phiếu bảo hành cho từng chiếc trong đơn. Bọc
            // try/catch để lỗi bảo hành không chặn việc hoàn thành đơn.
            if (OrderStatus.COMPLETED.equals(status)) {
                try {
                    warrantyService.taoChoDonHang(orderId);
                } catch (Exception e) {
                    log.error("Lỗi tạo phiếu bảo hành cho đơn {}", orderId, e);
                }
            }

            // Hủy đơn thì hoàn hàng về kho, cộng trả đúng lô đã lấy. Đơn chưa xuất
            // kho không có dòng stock_out nên hàm này không làm gì.
            if (OrderStatus.CANCELLED.equals(status)) {
                stockService.returnStockForOrder(orderId);
            }

            orderRepository.save(order);
        });
    }
}

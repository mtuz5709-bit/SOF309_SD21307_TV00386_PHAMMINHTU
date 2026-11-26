package com.titangear.service;

import com.titangear.constant.OrderStatus;
import com.titangear.model.CartItem;
import com.titangear.model.Order;
import com.titangear.model.Product;
import com.titangear.model.ProductVariant;
import com.titangear.model.User;
import com.titangear.repository.OrderItemRepository;
import com.titangear.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Test các quy tắc CHỐNG BOM HÀNG:
 *  - đơn lớn/nhiều -> bắt buộc chuyển khoản (VNPAY)
 *  - giới hạn số đơn COD đang chờ giao của mỗi khách (VNPAY không giới hạn)
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private SystemSettingService systemSettingService;
    @Mock private OrderRepository orderRepository;
    @Mock private OrderItemRepository orderItemRepository;
    /** Hủy đơn gọi hoàn kho -> phải có bản giả, không thì NullPointerException. */
    @Mock private StockService stockService;
    /** Đổi trạng thái đơn có ghi nhật ký thao tác -> cũng cần bản giả. */
    @Mock private AuditLogService auditLogService;
    @InjectMocks private OrderService orderService;

    private CartItem cartItem(int qty) {
        // Phải có sản phẩm thật: cart_items.product_id là NOT NULL, và createOrder
        // đọc product.isActive ngay từ chốt đầu tiên.
        Product sp = new Product();
        sp.setId(1);
        sp.setName("Sản phẩm test");
        sp.setIsActive(true);
        CartItem it = new CartItem();
        it.setQuantity(qty);
        it.setProduct(sp);
        return it;
    }

    private Order donCua(int userId, String phuongThuc) {
        User u = new User();
        u.setId(userId);
        Order o = new Order();
        o.setUser(u);
        o.setPaymentMethod(phuongThuc);
        o.setTotalAmount(new BigDecimal("100000"));
        return o;
    }

    @Test
    void soLuongVuotNguong_batBuocVnpay() {
        when(systemSettingService.getValue(eq("COD_MAX_QTY_PER_PRODUCT"), anyString())).thenReturn("3");
        assertTrue(orderService.requiresVnpayForQuantity(List.of(cartItem(5))));
    }

    @Test
    void soLuongTrongNguong_choCod() {
        when(systemSettingService.getValue(eq("COD_MAX_QTY_PER_PRODUCT"), anyString())).thenReturn("3");
        assertFalse(orderService.requiresVnpayForQuantity(List.of(cartItem(2))));
    }

    @Test
    void tongTienVuotNguong_batBuocVnpay() {
        when(systemSettingService.getValue(eq("COD_MAX_ORDER_AMOUNT"), anyString())).thenReturn("5000000");
        assertTrue(orderService.requiresVnpayForAmount(new BigDecimal("6000000")));
    }

    @Test
    void tongTienDuoiNguong_choCod() {
        when(systemSettingService.getValue(eq("COD_MAX_ORDER_AMOUNT"), anyString())).thenReturn("5000000");
        assertFalse(orderService.requiresVnpayForAmount(new BigDecimal("1000000")));
    }

    // ---- Giới hạn số đơn COD đang chờ (thay cho luật cấm mua lại cùng sản phẩm) ----

    @Test
    void codDatNguong_thiChanDatThem() {
        when(systemSettingService.getValue(eq("MAX_QTY_PER_PRODUCT_PER_ORDER"), anyString())).thenReturn("10");
        when(systemSettingService.getValue(eq("COD_MAX_ACTIVE_ORDERS"), anyString())).thenReturn("3");
        when(orderRepository.countActiveCodOrders(7)).thenReturn(3L);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.createOrder(donCua(7, "COD"), List.of(cartItem(1))));

        assertTrue(ex.getMessage().contains("đơn COD"), "Phải báo rõ lý do là giới hạn đơn COD");
        assertTrue(ex.getMessage().contains("VNPAY"), "Phải gợi ý lối thoát: chuyển khoản để đặt tiếp");
    }

    @Test
    void vnpayKhongBiGioiHan_khongThemDemDonCod() {
        when(systemSettingService.getValue(eq("MAX_QTY_PER_PRODUCT_PER_ORDER"), anyString())).thenReturn("10");

        // Đơn trả trước: tiền đã thu -> không có rủi ro bom hàng.
        // (Luồng sau đó cần nhiều phụ thuộc khác nên bỏ qua lỗi phát sinh —
        //  điều cần kiểm ở đây là KHÔNG hề đếm đơn COD.)
        try {
            orderService.createOrder(donCua(7, "VNPAY"), List.of(cartItem(1)));
        } catch (Exception ignored) {
        }

        verify(orderRepository, never()).countActiveCodOrders(any());
    }

    // ---- Kiểm tồn kho lúc đặt hàng (hàng rào cuối, chặn bán quá số hàng có) ----

    private CartItem gioHang(int soLuong, Integer tonBienThe, String tenSp) {
        Product sp = new Product();
        sp.setId(1);
        sp.setName(tenSp);
        sp.setIsActive(true);          // hàng trong giỏ thì mặc nhiên đang bán
        ProductVariant bt = new ProductVariant();
        bt.setId(11);
        bt.setName("Bản Pro");
        bt.setColor("Đen");
        bt.setIsActive(true);
        bt.setStock(tonBienThe);
        CartItem it = new CartItem();
        it.setQuantity(soLuong);
        it.setProduct(sp);
        it.setVariant(bt);
        return it;
    }

    @Test
    void datNhieuHonTonKho_thiBiChan() {
        when(systemSettingService.getValue(eq("MAX_QTY_PER_PRODUCT_PER_ORDER"), anyString())).thenReturn("10");
        when(orderItemRepository.getReservedQtyByVariant(11)).thenReturn(0);

        // Kho còn 2, khách đặt 3
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.createOrder(donCua(7, "VNPAY"), List.of(gioHang(3, 2, "Chuột X"))));

        assertTrue(ex.getMessage().contains("không đủ hàng"), "Phải nói rõ là thiếu hàng");
        assertTrue(ex.getMessage().contains("Còn có thể đặt: 2"),
                "Phải nói còn đặt được mấy cái, đừng bắt khách đoán");
        verify(orderRepository, never()).save(any());
    }

    @Test
    void hangDaBiDonKhacGiuCho_thiKhongTinhLaConHang() {
        when(systemSettingService.getValue(eq("MAX_QTY_PER_PRODUCT_PER_ORDER"), anyString())).thenReturn("10");
        // Kho còn 5 nhưng 5 cái đó đã nằm trong đơn của người khác chưa giao
        when(orderItemRepository.getReservedQtyByVariant(11)).thenReturn(5);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.createOrder(donCua(7, "VNPAY"), List.of(gioHang(1, 5, "Chuột X"))));

        assertTrue(ex.getMessage().contains("Còn có thể đặt: 0"),
                "Tồn kho trừ đi phần đã giữ chỗ mới là số đặt được");
    }

    /**
     * Số lượng ÂM phải bị chặn. Tìm ra khi thao tác thật: gõ tay
     * /cart/add?quantity=-1 là giỏ nhận số âm, thành tiền âm, và tổng đơn bị TRỪ
     * đi — thêm 1 chuột 1.450.000đ kèm 1 bàn phím số lượng -1 ra tổng −650.000đ.
     * Mọi chốt cũ đều so sánh theo chiều "lớn hơn" nên số âm lọt hết.
     */
    /**
     * Giỏ hàng là dữ liệu sống lâu: khách bỏ hàng vào giỏ rồi để vài ngày, trong
     * lúc đó admin ngừng bán sản phẩm. Không chặn thì khách vẫn đặt được món shop
     * đã thôi kinh doanh — shop buộc phải giao hoặc gọi điện xin lỗi hủy đơn.
     */
    @Test
    void sanPhamDaNgungBan_thiKhongDatDuoc() {
        CartItem dong = gioHang(1, 100, "Chuột đã ngừng bán");
        dong.getProduct().setIsActive(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.createOrder(donCua(7, "COD"), List.of(dong)));

        assertTrue(ex.getMessage().contains("ngừng kinh doanh"),
                "Phải chặn trước cả chốt thanh toán, và báo đúng lý do");
    }

    @Test
    void mauDaNgungBan_thiKhongDatDuoc() {
        CartItem dong = gioHang(1, 100, "Chuột X");
        dong.getVariant().setIsActive(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.createOrder(donCua(7, "COD"), List.of(dong)));

        assertTrue(ex.getMessage().contains("ngừng kinh doanh"),
                "Phải chặn trước cả chốt thanh toán, và báo đúng lý do");
    }

    @Test
    void soLuongAmTrongGio_thiBiChan() {
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.createOrder(donCua(7, "COD"), List.of(gioHang(-1, 100, "Bàn phím X"))));

        assertTrue(ex.getMessage().contains("không hợp lệ"),
                "Phải chặn số lượng âm trước khi tạo đơn");
    }

    /* ================= LUỒNG TRẠNG THÁI ĐƠN ================= */

    private Order donTrangThai(String trangThai) {
        Order o = donCua(7, "COD");
        o.setId(500);
        o.setStatus(trangThai);
        return o;
    }

    /**
     * ĐÂY LÀ LỖI NẶNG NHẤT ĐÃ VÁ: nhảy thẳng Chờ xác nhận -> Hoàn thành nghĩa là
     * đơn xong xuôi, COD đánh dấu đã thu tiền, phiếu bảo hành sinh ra, NHƯNG hàng
     * chưa bao giờ xuất kho. Tồn kho không giảm và báo cáo lấy giá vốn = 0.
     */
    @Test
    void nhayThangTuChoXacNhanSangHoanThanh_thiBiChan() {
        when(orderRepository.findById(500)).thenReturn(Optional.of(donTrangThai(OrderStatus.PENDING)));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.updateStatus(500, OrderStatus.COMPLETED));

        assertTrue(ex.getMessage().contains("duyệt xuất kho"),
                "Phải nói rõ lý do: đơn chưa qua bước xuất kho");
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void dangGiaoSangHoanThanh_thiChoPhep() {
        when(orderRepository.findById(500)).thenReturn(Optional.of(donTrangThai(OrderStatus.SHIPPING)));

        assertDoesNotThrow(() -> orderService.updateStatus(500, OrderStatus.COMPLETED));
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void huyDonDangChoXacNhan_thiChoPhep() {
        when(orderRepository.findById(500)).thenReturn(Optional.of(donTrangThai(OrderStatus.PENDING)));

        assertDoesNotThrow(() -> orderService.updateStatus(500, OrderStatus.CANCELLED));
        verify(orderRepository).save(any(Order.class));
    }

    /** Duyệt xuất kho lần 2 cho cùng một đơn = trừ tồn gấp đôi. */
    @Test
    void duyetXuatKhoLanHai_thiBiChan() {
        when(orderRepository.findById(500)).thenReturn(Optional.of(donTrangThai(OrderStatus.SHIPPING)));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.approveExport(500, 1));

        assertTrue(ex.getMessage().contains("đã được duyệt xuất kho"));
    }
}

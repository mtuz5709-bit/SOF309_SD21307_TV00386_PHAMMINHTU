package com.titangear.service;

import com.titangear.exception.LoiNghiepVu;
import com.titangear.dao.StockReportDao;
import com.titangear.model.OrderItem;
import com.titangear.model.Product;
import com.titangear.model.StockIn;
import com.titangear.model.StockOut;
import com.titangear.model.User;
import com.titangear.repository.StockInRepository;
import com.titangear.repository.StockOutRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test logic XUẤT KHO FIFO — trái tim của Model 2. Không đụng DB (mock repo).
 */
@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock private StockInRepository  stockInRepository;
    @Mock private StockOutRepository stockOutRepository;
    @Mock private StockReportDao     stockReportDao;

    @InjectMocks private StockService stockService;

    private OrderItem orderItem(int productId, int qty) {
        Product p = new Product();
        p.setId(productId);
        OrderItem oi = new OrderItem();
        oi.setProduct(p);      // không có biến thể -> đi nhánh lô mức sản phẩm
        oi.setQuantity(qty);
        return oi;
    }

    private StockIn lot(int id, int remaining, String importPrice) {
        StockIn s = new StockIn();
        s.setId(id);
        s.setRemainingQuantity(remaining);
        s.setImportPrice(new BigDecimal(importPrice));
        return s;
    }

    @Test
    void xuatKho_phaiTruLoCuNhatTruoc() {
        StockIn loA = lot(1, 10, "100000");   // lô cũ
        StockIn loB = lot(2, 10, "120000");   // lô mới
        when(stockInRepository.findProductLotsFIFO(5))
                .thenReturn(new ArrayList<>(List.of(loA, loB)));
        User exporter = new User();
        exporter.setId(1);

        // cần 15 -> lấy hết 10 của lô A rồi 5 của lô B
        stockService.stockOutFIFO(orderItem(5, 15), 15, exporter);

        assertEquals(0, loA.getRemainingQuantity().intValue(), "Lô cũ phải bị trừ hết trước");
        assertEquals(5, loB.getRemainingQuantity().intValue(), "Lô mới chỉ bị trừ phần còn thiếu");
        verify(stockOutRepository, times(2)).save(any(StockOut.class)); // 2 dòng xuất (2 lô)
    }

    @Test
    void hoanKho_congTraDungLoVaDanhDauDaHoan() {
        StockIn lo = lot(7, 6, "100000");
        StockOut out = new StockOut();
        out.setStockIn(lo);
        out.setQuantity(4);          // đơn này đã lấy 4 từ lô #7
        out.setReturned(false);
        when(stockOutRepository.findByOrderItem_Order_IdAndReturnedFalse(99))
                .thenReturn(new ArrayList<>(List.of(out)));

        int soDong = stockService.returnStockForOrder(99);

        assertEquals(1, soDong);
        verify(stockInRepository).restoreLot(7, 4);   // cộng trả ĐÚNG lô, ĐÚNG số
        assertTrue(out.getReturned(), "Phải đánh dấu đã hoàn kho");
        assertNotNull(out.getReturnedAt());
        verify(stockOutRepository).save(out);         // giữ bản ghi, không xoá
    }

    @Test
    void hoanKho_donChuaXuat_thiKhongLamGi() {
        when(stockOutRepository.findByOrderItem_Order_IdAndReturnedFalse(100))
                .thenReturn(new ArrayList<>());

        assertEquals(0, stockService.returnStockForOrder(100));
        verify(stockInRepository, never()).restoreLot(any(), any());
    }

    @Test
    void xuatKho_khiThieuHang_thiNemLoi_khongGhiGi() {
        StockIn loA = lot(1, 10, "100000");
        when(stockInRepository.findProductLotsFIFO(5))
                .thenReturn(new ArrayList<>(List.of(loA)));
        User exporter = new User();
        exporter.setId(1);
        OrderItem oi = orderItem(5, 25);       // cần 25 > tồn 10

        assertThrows(LoiNghiepVu.class,
                () -> stockService.stockOutFIFO(oi, 25, exporter));
        verify(stockOutRepository, never()).save(any()); // không để lại xuất kho "nửa vời"
    }
}

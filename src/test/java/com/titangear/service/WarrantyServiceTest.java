package com.titangear.service;

import com.titangear.exception.LoiNghiepVu;
import com.titangear.model.Order;
import com.titangear.model.OrderItem;
import com.titangear.model.Product;
import com.titangear.model.User;
import com.titangear.model.Warranty;
import com.titangear.repository.OrderItemRepository;
import com.titangear.repository.OrderRepository;
import com.titangear.repository.WarrantyRepairRepository;
import com.titangear.repository.WarrantyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test phát phiếu bảo hành tự động khi đơn hoàn thành.
 */
@ExtendWith(MockitoExtension.class)
class WarrantyServiceTest {

    @Mock private WarrantyRepository warrantyRepository;
    @Mock private WarrantyRepairRepository repairRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private OrderRepository orderRepository;

    @InjectMocks private WarrantyService warrantyService;

    private void coDon(int orderId, int userId) {
        User u = new User();
        u.setId(userId);
        Order o = new Order();
        o.setId(orderId);
        o.setUser(u);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(o));
    }

    private OrderItem dongDon(int itemId, Integer soThangBaoHanh, int soLuong) {
        Product p = new Product();
        p.setId(99);
        p.setName("Chuột X");
        p.setWarrantyMonths(soThangBaoHanh);
        OrderItem it = new OrderItem();
        it.setId(itemId);
        it.setProduct(p);
        it.setQuantity(soLuong);
        return it;
    }

    /** Trả lại chính đối tượng được lưu, gán id giả để service đặt serial. */
    private void luuTraLaiCoId() {
        when(warrantyRepository.save(any(Warranty.class))).thenAnswer(inv -> {
            Warranty w = inv.getArgument(0);
            if (w.getId() == null) w.setId(7);
            return w;
        });
    }

    @Test
    void muaBaCai_thiSinhBaPhieu() {
        coDon(1, 5);
        when(orderItemRepository.findByOrderId(1)).thenReturn(List.of(dongDon(10, 12, 3)));
        when(warrantyRepository.existsByOrderItemId(10)).thenReturn(false);
        luuTraLaiCoId();

        int daTao = warrantyService.taoChoDonHang(1);

        assertEquals(3, daTao, "Mua 3 chiếc phải ra 3 phiếu bảo hành");
    }

    @Test
    void tinhDungNgayHetHanTheoSoThang() {
        coDon(1, 5);
        when(orderItemRepository.findByOrderId(1)).thenReturn(List.of(dongDon(10, 12, 1)));
        when(warrantyRepository.existsByOrderItemId(10)).thenReturn(false);
        luuTraLaiCoId();

        warrantyService.taoChoDonHang(1);

        ArgumentCaptor<Warranty> bat = ArgumentCaptor.forClass(Warranty.class);
        verify(warrantyRepository, atLeastOnce()).save(bat.capture());
        Warranty w = bat.getValue();
        assertEquals(LocalDate.now(), w.getStartDate());
        assertEquals(LocalDate.now().plusMonths(12), w.getEndDate());
    }

    @Test
    void serialTheoIdDangSRSauKhiLuu() {
        coDon(1, 5);
        when(orderItemRepository.findByOrderId(1)).thenReturn(List.of(dongDon(10, 24, 1)));
        when(warrantyRepository.existsByOrderItemId(10)).thenReturn(false);
        luuTraLaiCoId();

        warrantyService.taoChoDonHang(1);

        ArgumentCaptor<Warranty> bat = ArgumentCaptor.forClass(Warranty.class);
        verify(warrantyRepository, atLeastOnce()).save(bat.capture());
        String serial = bat.getValue().getSerialNo();

        // Phần ID vẫn phải bám theo id -> đảm bảo DUY NHẤT, không cần kiểm trùng.
        assertTrue(serial.startsWith("SR000007-"),
                "Serial phải bắt đầu bằng SR + 6 chữ số của id, nhận được: " + serial);

        // Phần hậu tố NGẪU NHIÊN -> chống dò tuần tự.
        // Trước đây serial chỉ là "SR000007", ai cũng đoán được mã kế tiếp và dò
        // sạch trang tra cứu bảo hành công khai để biết shop bán được bao nhiêu.
        assertTrue(serial.matches("SR\\d{6}-[A-Z2-9]{4}"),
                "Serial phải có hậu tố 4 ký tự ngẫu nhiên, nhận được: " + serial);
        // CHỈ soi phần hậu tố: phần "SR000007" vốn có số 0, soi cả chuỗi là sai.
        String hauTo = serial.substring(serial.indexOf('-') + 1);
        assertFalse(hauTo.matches(".*[ILO01].*"),
                "Hậu tố không được chứa I, L, O, 0, 1 — khách nhìn phiếu in dễ gõ nhầm, nhận được: " + hauTo);
    }

    /** Hai phiếu khác nhau phải ra hậu tố khác nhau, nếu không thì vô nghĩa. */
    @Test
    void haiSerialKhacNhau_thiHauToKhacNhau() {
        coDon(1, 5);
        when(orderItemRepository.findByOrderId(1)).thenReturn(List.of(dongDon(10, 24, 3)));
        when(warrantyRepository.existsByOrderItemId(10)).thenReturn(false);
        luuTraLaiCoId();

        warrantyService.taoChoDonHang(1);

        ArgumentCaptor<Warranty> bat = ArgumentCaptor.forClass(Warranty.class);
        verify(warrantyRepository, atLeastOnce()).save(bat.capture());
        long soHauToKhacNhau = bat.getAllValues().stream()
                .map(Warranty::getSerialNo)
                .filter(s -> s != null && s.matches("SR\\d{6}-[A-Z2-9]{4}"))
                .map(s -> s.substring(s.indexOf('-') + 1))
                .distinct().count();

        assertTrue(soHauToKhacNhau > 1,
                "3 phiếu mà chỉ ra 1 hậu tố thì phần ngẫu nhiên không hoạt động");
    }

    @Test
    void sanPhamKhongKhaiBaoHanh_thiKhongSinhPhieu() {
        coDon(1, 5);
        when(orderItemRepository.findByOrderId(1)).thenReturn(List.of(dongDon(10, null, 2)));
        when(warrantyRepository.existsByOrderItemId(10)).thenReturn(false);

        int daTao = warrantyService.taoChoDonHang(1);

        assertEquals(0, daTao);
        verify(warrantyRepository, never()).save(any());
    }

    @Test
    void goiLanHai_khongSinhTrung() {
        coDon(1, 5);
        when(orderItemRepository.findByOrderId(1)).thenReturn(List.of(dongDon(10, 12, 2)));
        // Lần này coi như dòng đơn đã có phiếu rồi
        when(warrantyRepository.existsByOrderItemId(10)).thenReturn(true);

        int daTao = warrantyService.taoChoDonHang(1);

        assertEquals(0, daTao, "Đơn đã phát phiếu thì gọi lại không được nhân đôi");
        verify(warrantyRepository, never()).save(any());
    }

    /* ---- Tự tắt khi hết hạn ---- */

    private Warranty phieuHetHanNgay(int id, LocalDate ngayHetHan) {
        Warranty w = new Warranty();
        w.setId(id);
        w.setSerialNo(String.format("SR%06d", id));
        w.setEndDate(ngayHetHan);
        return w;
    }

    @Test
    void phieuHetHan_thiKhongGhiDuocSuaChua() {
        // Hết hạn từ hôm qua
        when(warrantyRepository.findById(3)).thenReturn(
                Optional.of(phieuHetHanNgay(3, LocalDate.now().minusDays(1))));

        LoiNghiepVu loi = assertThrows(LoiNghiepVu.class,
                () -> warrantyService.themLanSua(3, "Khách mang máy tới", "TIEP_NHAN"));

        assertTrue(loi.getMessage().contains("hết hạn"));
        verify(repairRepository, never()).save(any());
    }

    @Test
    void phieuConHan_thiVanGhiDuocSuaChua() {
        when(warrantyRepository.findById(4)).thenReturn(
                Optional.of(phieuHetHanNgay(4, LocalDate.now().plusMonths(6))));

        warrantyService.themLanSua(4, "Khách báo lỗi nút chuột", "TIEP_NHAN");

        verify(repairRepository).save(any());
    }

    @Test
    void ngayCuoiCungVanConDuocBaoHanh() {
        // Đúng ngày end_date vẫn phải được nhận — không cắt sớm 1 ngày của khách
        when(warrantyRepository.findById(5)).thenReturn(
                Optional.of(phieuHetHanNgay(5, LocalDate.now())));

        warrantyService.themLanSua(5, "Khách tới ngày cuối", "TIEP_NHAN");

        verify(repairRepository).save(any());
    }

    @Test
    void locDungTungNhomTrangThai() {
        Warranty conHan  = phieuHetHanNgay(1, LocalDate.now().plusMonths(6)); // còn 6 tháng
        Warranty sapHet  = phieuHetHanNgay(2, LocalDate.now().plusDays(10));  // còn 10 ngày
        Warranty hetHan  = phieuHetHanNgay(3, LocalDate.now().minusDays(1));
        List<Warranty> ds = List.of(conHan, sapHet, hetHan);

        assertEquals(List.of(conHan, sapHet), warrantyService.loc(ds, WarrantyService.CON_HAN),
                "Sắp hết vẫn là còn hạn");
        assertEquals(List.of(sapHet), warrantyService.loc(ds, WarrantyService.SAP_HET));
        assertEquals(List.of(hetHan), warrantyService.loc(ds, WarrantyService.HET_HAN));
        assertEquals(ds, warrantyService.loc(ds, null), "Không chọn tab thì trả nguyên danh sách");
    }
}

package com.titangear.service;

import com.titangear.model.CartItem;
import com.titangear.model.Combo;
import com.titangear.model.Voucher;
import com.titangear.repository.VoucherRepository;
import com.titangear.repository.VoucherUsageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Test luật áp dụng voucher — phần dễ bị lạm dụng nhất nếu tính sai.
 */
@ExtendWith(MockitoExtension.class)
class VoucherServiceTest {

    @Mock private VoucherRepository voucherRepository;
    @Mock private VoucherUsageRepository usageRepository;
    @InjectMocks private VoucherService voucherService;

    private Voucher voucher(String type, String value) {
        Voucher v = new Voucher();
        v.setId(1);
        v.setCode("GIAM10");
        v.setDiscountType(type);
        v.setDiscountValue(new BigDecimal(value));
        v.setActive(true);
        v.setUsedCount(0);
        v.setOncePerUser(false);
        return v;
    }

    private void coMa(Voucher v) {
        when(voucherRepository.findByCodeIgnoreCase("GIAM10")).thenReturn(Optional.of(v));
    }

    @Test
    void maKhongTonTai_thiTuChoi() {
        when(voucherRepository.findByCodeIgnoreCase("SAI")).thenReturn(Optional.empty());
        VoucherService.KetQua kq = voucherService.check("SAI", 1, new BigDecimal("1000000"));
        assertFalse(kq.isHopLe());
        assertTrue(kq.getThongBao().contains("không tồn tại"));
    }

    @Test
    void maHetHan_thiTuChoi() {
        Voucher v = voucher(Voucher.TYPE_PERCENT, "10");
        v.setEndAt(LocalDateTime.now().minusDays(1));
        coMa(v);
        VoucherService.KetQua kq = voucherService.check("GIAM10", 1, new BigDecimal("1000000"));
        assertFalse(kq.isHopLe());
        assertTrue(kq.getThongBao().contains("hết hạn"));
    }

    @Test
    void chuaDatDonToiThieu_thiTuChoi() {
        Voucher v = voucher(Voucher.TYPE_PERCENT, "10");
        v.setMinOrderAmount(new BigDecimal("500000"));
        coMa(v);
        VoucherService.KetQua kq = voucherService.check("GIAM10", 1, new BigDecimal("200000"));
        assertFalse(kq.isHopLe());
        assertTrue(kq.getThongBao().contains("tối thiểu"));
    }

    @Test
    void hetLuotSuDung_thiTuChoi() {
        Voucher v = voucher(Voucher.TYPE_PERCENT, "10");
        v.setUsageLimit(5);
        v.setUsedCount(5);
        coMa(v);
        VoucherService.KetQua kq = voucherService.check("GIAM10", 1, new BigDecimal("1000000"));
        assertFalse(kq.isHopLe());
        assertTrue(kq.getThongBao().contains("hết lượt"));
    }

    @Test
    void khachDaDungRoi_thiTuChoi() {
        Voucher v = voucher(Voucher.TYPE_PERCENT, "10");
        v.setOncePerUser(true);
        coMa(v);
        when(usageRepository.existsByVoucherIdAndUserId(1, 7)).thenReturn(true);
        VoucherService.KetQua kq = voucherService.check("GIAM10", 7, new BigDecimal("1000000"));
        assertFalse(kq.isHopLe());
        assertTrue(kq.getThongBao().contains("đã dùng mã này"));
    }

    @Test
    void giamPhanTram_bichanBoiTranToiDa() {
        Voucher v = voucher(Voucher.TYPE_PERCENT, "10");   // 10% của 2.000.000 = 200.000
        v.setMaxDiscountAmount(new BigDecimal("100000"));   // nhưng trần chỉ 100.000
        coMa(v);
        VoucherService.KetQua kq = voucherService.check("GIAM10", 1, new BigDecimal("2000000"));
        assertTrue(kq.isHopLe());
        assertEquals(0, kq.getSoTienGiam().compareTo(new BigDecimal("100000")));
    }

    @Test
    void giamTienLonHonDon_thiChiGiamBangDon_khongAm() {
        Voucher v = voucher(Voucher.TYPE_AMOUNT, "500000"); // giảm 500k
        coMa(v);
        VoucherService.KetQua kq = voucherService.check("GIAM10", 1, new BigDecimal("300000")); // đơn 300k
        assertTrue(kq.isHopLe());
        assertEquals(0, kq.getSoTienGiam().compareTo(new BigDecimal("300000")),
                "Giảm không được vượt giá trị đơn (tổng phải >= 0)");
    }

    /* ---------- MÃ RÀNG BUỘC COMBO ---------- */

    private Combo comboId(int id, String ten) {
        Combo c = new Combo();
        c.setId(id);
        c.setName(ten);
        return c;
    }

    private CartItem dong(Combo thuocCombo) {
        CartItem d = new CartItem();
        d.setId(1);
        d.setQuantity(1);
        d.setCombo(thuocCombo);
        return d;
    }

    @Test
    void maRangBuocCombo_gioCoDungComboDo_thiDungDuoc() {
        Combo cb = comboId(7, "Combo Aim Lab");
        Voucher v = voucher(Voucher.TYPE_AMOUNT, "100000");
        v.setCombo(cb);
        coMa(v);

        VoucherService.KetQua kq = voucherService.check("GIAM10", 1,
                new BigDecimal("3000000"), List.of(dong(cb)));

        assertTrue(kq.isHopLe());
    }

    @Test
    void maRangBuocCombo_gioLaComboKHAC_thiTuChoi() {
        Voucher v = voucher(Voucher.TYPE_AMOUNT, "100000");
        v.setCombo(comboId(7, "Combo Aim Lab"));
        coMa(v);

        VoucherService.KetQua kq = voucherService.check("GIAM10", 1,
                new BigDecimal("3000000"), List.of(dong(comboId(9, "Combo khac"))));

        assertFalse(kq.isHopLe());
        assertTrue(kq.getThongBao().contains("Combo Aim Lab"),
                "Phải nói rõ mã dành cho combo nào");
    }

    @Test
    void maChiDonCoCombo_gioToanHangLe_thiTuChoi() {
        Voucher v = voucher(Voucher.TYPE_AMOUNT, "100000");
        v.setChiDonCoCombo(true);
        coMa(v);

        VoucherService.KetQua kq = voucherService.check("GIAM10", 1,
                new BigDecimal("3000000"), List.of(dong(null)));   // combo = null: mua lẻ

        assertFalse(kq.isHopLe());
    }

    /**
     * Gọi bản cũ (không truyền giỏ) với mã ràng buộc combo thì phải TỪ CHỐI.
     * Không chứng minh được điều kiện thì thà không giảm, còn hơn giảm nhầm
     * cho đơn không có combo.
     */
    @Test
    void maRangBuocCombo_goiBanKhongCoGio_thiTuChoi() {
        Voucher v = voucher(Voucher.TYPE_AMOUNT, "100000");
        v.setCombo(comboId(7, "Combo Aim Lab"));
        coMa(v);

        VoucherService.KetQua kq = voucherService.check("GIAM10", 1, new BigDecimal("3000000"));

        assertFalse(kq.isHopLe());
    }
}

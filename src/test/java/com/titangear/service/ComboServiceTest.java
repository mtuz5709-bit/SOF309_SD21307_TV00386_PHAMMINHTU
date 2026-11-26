package com.titangear.service;

import com.titangear.model.CartItem;
import com.titangear.model.Combo;
import com.titangear.model.ComboItem;
import com.titangear.model.Product;
import com.titangear.repository.CartItemRepository;
import com.titangear.repository.ComboRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

/**
 * Test luật tính giá combo.
 *
 * Ba luật dưới đây không kiểm được bằng dữ liệu thật trên web (phải có đợt
 * flash sale đang chạy, phải có hàng hết kho, phải có combo nhập sai tiền giảm)
 * nên chốt bằng test tự động.
 */
@ExtendWith(MockitoExtension.class)
class ComboServiceTest {

    @Mock private ComboRepository comboRepository;
    @Mock private FlashSaleService flashSaleService;
    @InjectMocks private ComboService comboService;

    /* ---------- dựng dữ liệu ---------- */

    private Product sanPham(int id, String gia, boolean dangBan, int ton) {
        Product p = new Product();
        p.setId(id);
        p.setPrice(new BigDecimal(gia));
        p.setIsActive(dangBan);
        p.setStockQuantity(ton);
        return p;
    }

    private Combo combo(String tienGiam, Product... sanPhams) {
        Combo c = new Combo();
        c.setId(1);
        c.setName("Combo test");
        c.setDiscountAmount(new BigDecimal(tienGiam));
        Set<ComboItem> items = new LinkedHashSet<>();
        int i = 1;
        for (Product p : sanPhams) {
            ComboItem it = new ComboItem();
            it.setId(i++);
            it.setCombo(c);
            it.setProduct(p);
            it.setQuantity(1);
            items.add(it);
        }
        c.setItems(items);
        return c;
    }

    /** Giá hiệu lực = giá gốc (không có đợt giảm nào đang chạy) */
    private void khongCoFlashSale(Product... sanPhams) {
        Map<Integer, FlashSaleService.EffectivePrice> map = new HashMap<>();
        for (Product p : sanPhams) {
            map.put(p.getId(), new FlashSaleService.EffectivePrice(p.getPrice(), p.getPrice(), null));
        }
        when(flashSaleService.getEffectivePrices(anyList())).thenReturn(map);
    }

    /* ---------- LUẬT 1: tiền giảm ---------- */

    @Test
    void khongCoFlashSale_giaBangTongTruTienGiam() {
        Product a = sanPham(1, "1000000", true, 5);
        Product b = sanPham(2, "2000000", true, 5);
        Combo c = combo("300000", a, b);
        khongCoFlashSale(a, b);
        when(comboRepository.findDangBan()).thenReturn(List.of(c));

        List<ComboService.ComboView> ds = comboService.layComboDangBan(0);

        assertEquals(1, ds.size());
        assertEquals(0, new BigDecimal("3000000").compareTo(ds.get(0).getGiaGoc()));
        assertEquals(0, new BigDecimal("2700000").compareTo(ds.get(0).getGiaBan()));
        assertEquals(0, new BigDecimal("300000").compareTo(ds.get(0).getTietKiem()));
    }

    @Test
    void tienGiamBangHoacVuotTongGia_thiAnCombo() {
        // Gõ nhầm tiền giảm (hoặc sau này hạ giá sản phẩm) -> thà ẩn còn hơn bán giá 0đ
        Product a = sanPham(1, "1000000", true, 5);
        Combo c = combo("1000000", a);
        khongCoFlashSale(a);
        when(comboRepository.findDangBan()).thenReturn(List.of(c));

        assertTrue(comboService.layComboDangBan(0).isEmpty());
    }

    @Test
    void comboKhongCoMonNao_thiAn() {
        Combo c = combo("100000");   // không sản phẩm nào
        when(comboRepository.findDangBan()).thenReturn(List.of(c));

        assertTrue(comboService.layComboDangBan(0).isEmpty());
    }

    /* ---------- LUẬT 2: không cộng dồn, lấy giá có lợi cho khách ---------- */

    @Test
    void flashSaleReHonGiaCombo_thiLayGiaFlashSale() {
        // Giá gốc 3.000.000, combo giảm 300.000 -> 2.700.000
        // Nhưng flash sale đang bán 2 món chỉ còn 2.400.000 -> phải lấy 2.400.000,
        // nếu không thì mua combo ĐẮT HƠN mua lẻ.
        Product a = sanPham(1, "1000000", true, 5);
        Product b = sanPham(2, "2000000", true, 5);
        Combo c = combo("300000", a, b);

        Map<Integer, FlashSaleService.EffectivePrice> map = new HashMap<>();
        map.put(1, new FlashSaleService.EffectivePrice(new BigDecimal("1000000"), new BigDecimal("800000"), 20));
        map.put(2, new FlashSaleService.EffectivePrice(new BigDecimal("2000000"), new BigDecimal("1600000"), 20));
        when(flashSaleService.getEffectivePrices(anyList())).thenReturn(map);
        when(comboRepository.findDangBan()).thenReturn(List.of(c));

        ComboService.ComboView v = comboService.layComboDangBan(0).get(0);

        assertEquals(0, new BigDecimal("2400000").compareTo(v.getGiaBan()));
        // Tiết kiệm tính theo giá niêm yết nên khách thấy đúng mức lợi thật
        assertEquals(0, new BigDecimal("600000").compareTo(v.getTietKiem()));
    }

    @Test
    void flashSaleNongHonGiaCombo_thiVanLayGiaCombo() {
        // Flash sale chỉ giảm nhẹ (2.900.000) trong khi combo còn 2.700.000
        // -> giữ giá combo, KHÔNG cộng dồn hai mức giảm (cộng dồn là bán âm vốn).
        Product a = sanPham(1, "1000000", true, 5);
        Product b = sanPham(2, "2000000", true, 5);
        Combo c = combo("300000", a, b);

        Map<Integer, FlashSaleService.EffectivePrice> map = new HashMap<>();
        map.put(1, new FlashSaleService.EffectivePrice(new BigDecimal("1000000"), new BigDecimal("950000"), 5));
        map.put(2, new FlashSaleService.EffectivePrice(new BigDecimal("2000000"), new BigDecimal("1950000"), 3));
        when(flashSaleService.getEffectivePrices(anyList())).thenReturn(map);
        when(comboRepository.findDangBan()).thenReturn(List.of(c));

        ComboService.ComboView v = comboService.layComboDangBan(0).get(0);
        assertEquals(0, new BigDecimal("2700000").compareTo(v.getGiaBan()));
    }

    /* ---------- LUẬT 4: combo tự ẩn khi không bán được ---------- */

    @Test
    void motMonHetHang_thiKhongHienTrongDanhSach() {
        Product a = sanPham(1, "1000000", true, 5);
        Product b = sanPham(2, "2000000", true, 0);   // hết hàng
        Combo c = combo("300000", a, b);
        khongCoFlashSale(a, b);
        when(comboRepository.findDangBan()).thenReturn(List.of(c));

        assertTrue(comboService.layComboDangBan(0).isEmpty());
    }

    @Test
    void motMonNgungBan_thiKhongHienTrongDanhSach() {
        Product a = sanPham(1, "1000000", true, 5);
        Product b = sanPham(2, "2000000", false, 5);  // is_active = 0
        Combo c = combo("300000", a, b);
        khongCoFlashSale(a, b);
        when(comboRepository.findDangBan()).thenReturn(List.of(c));

        assertTrue(comboService.layComboDangBan(0).isEmpty());
    }

    /**
     * Trang CHI TIẾT vẫn mở được khi combo hết hàng (link đã chia sẻ), chỉ khác
     * là cờ conHang = false để giao diện báo "tạm hết" thay vì ném trang lỗi.
     */
    @Test
    void chiTiet_comboHetHang_vanTraVeNhungConHangLaFalse() {
        Product a = sanPham(1, "1000000", true, 0);
        Combo c = combo("100000", a);
        khongCoFlashSale(a);
        when(comboRepository.findDangBanTheoId(1)).thenReturn(Optional.of(c));

        ComboService.ComboView v = comboService.layTheoId(1);

        assertNotNull(v);
        assertFalse(v.isConHang());
        assertEquals(0, new BigDecimal("900000").compareTo(v.getGiaBan()));
    }

    @Test
    void chiTiet_comboKhongTonTai_traVeNull() {
        when(comboRepository.findDangBanTheoId(99)).thenReturn(Optional.empty());
        assertNull(comboService.layTheoId(99));
    }

    /* ---------- giới hạn số lượng hiển thị ---------- */

    /* ---------- PHÂN BỔ TIỀN GIẢM VÀO TỪNG DÒNG GIỎ ---------- */

    /** Dòng giỏ thuộc combo — phải neo vào ĐÚNG vị trí (combo_item) của bộ. */
    private CartItem dongGio(int id, Product p, Combo c, int soLuong) {
        ComboItem viTri = c == null ? null : c.getItems().stream()
                .filter(it -> it.getProduct().getId().equals(p.getId()))
                .findFirst().orElse(null);
        return dongGio(id, p, c, soLuong, viTri);
    }

    private CartItem dongGio(int id, Product p, Combo c, int soLuong, ComboItem viTri) {
        CartItem d = new CartItem();
        d.setId(id);
        d.setProduct(p);
        d.setCombo(c);
        d.setComboItem(viTri);
        d.setQuantity(soLuong);
        return d;
    }

    /**
     * Dòng combo KHÔNG neo vào vị trí nào (dữ liệu cũ, hoặc bị sửa tay) thì
     * không được hưởng giảm giá — vì không xác minh được nó có thật sự là
     * một bộ hợp lệ hay không.
     */
    @Test
    void dongComboKhongNeoViTri_thiKhongDuocGiam() {
        Product a = sanPham(1, "1000000", true, 5);
        Product b = sanPham(2, "2000000", true, 5);
        Combo c = combo("300000", a, b);
        khongCoFlashSale(a, b);
        when(comboRepository.findDangBanTheoId(1)).thenReturn(Optional.of(c));

        List<CartItem> gio = List.of(dongGio(11, a, c, 1, null), dongGio(12, b, c, 1, null));

        assertEquals(0, new BigDecimal("3000000").compareTo(comboService.tongTienGio(gio)));
    }

    @Test
    void phanBoGiamGia_tongCacDongPhaiBangDungGiaCombo() {
        // 3 món lẻ tổng 3.000.000, combo giảm 1.000.000 -> phải thu đúng 2.000.000.
        // Chia đều ra là 666.666,67 mỗi dòng -> làm tròn từng dòng sẽ LỆCH.
        // Dòng cuối phải nhận phần lẻ để tổng khớp tuyệt đối.
        Product a = sanPham(1, "1000000", true, 5);
        Product b = sanPham(2, "1000000", true, 5);
        Product c3 = sanPham(3, "1000000", true, 5);
        Combo c = combo("1000000", a, b, c3);
        khongCoFlashSale(a, b, c3);
        when(comboRepository.findDangBanTheoId(1)).thenReturn(Optional.of(c));

        List<CartItem> gio = List.of(dongGio(11, a, c, 1), dongGio(12, b, c, 1), dongGio(13, c3, c, 1));
        BigDecimal tong = comboService.tongTienGio(gio);

        assertEquals(0, new BigDecimal("2000000").compareTo(tong),
                "Tổng thu phải bằng đúng giá combo, không lệch đồng nào");
    }

    @Test
    void donGiaTungDongTrongCombo_phaiThapHonGiaNiemYet() {
        Product a = sanPham(1, "1000000", true, 5);
        Product b = sanPham(2, "2000000", true, 5);
        Combo c = combo("300000", a, b);
        khongCoFlashSale(a, b);
        when(comboRepository.findDangBanTheoId(1)).thenReturn(Optional.of(c));

        List<CartItem> gio = List.of(dongGio(11, a, c, 1), dongGio(12, b, c, 1));
        Map<Integer, ComboService.GiaDong> gia = comboService.tinhGiaTungDong(gio);

        assertTrue(gia.get(11).getDonGia().compareTo(new BigDecimal("1000000")) < 0);
        assertTrue(gia.get(12).getDonGia().compareTo(new BigDecimal("2000000")) < 0);
        assertEquals(0, new BigDecimal("2700000").compareTo(
                gia.get(11).getThanhTien().add(gia.get(12).getThanhTien())));
    }

    /**
     * Admin sửa combo (bớt/đổi món) SAU KHI khách đã bỏ vào giỏ -> các dòng trong
     * giỏ không còn khớp combo. Khi đó phải bán theo giá lẻ, tuyệt đối không giảm
     * theo một combo đã khác đi.
     */
    @Test
    void gioKhongConKhopCombo_thiKhongGiamNua() {
        Product a = sanPham(1, "1000000", true, 5);
        Product b = sanPham(2, "2000000", true, 5);
        Combo c = combo("300000", a, b);           // combo hiện có 2 món
        khongCoFlashSale(a, b);
        when(comboRepository.findDangBanTheoId(1)).thenReturn(Optional.of(c));

        // giỏ chỉ còn 1 món của bộ
        List<CartItem> gio = List.of(dongGio(11, a, c, 1));
        BigDecimal tong = comboService.tongTienGio(gio);

        assertEquals(0, new BigDecimal("1000000").compareTo(tong));
    }

    @Test
    void dongMuaLe_khongBiAnhHuongBoiCombo() {
        Product a = sanPham(1, "1000000", true, 5);
        khongCoFlashSale(a);

        List<CartItem> gio = List.of(dongGio(11, a, null, 2));   // combo = null
        Map<Integer, ComboService.GiaDong> gia = comboService.tinhGiaTungDong(gio);

        assertEquals(0, new BigDecimal("1000000").compareTo(gia.get(11).getDonGia()));
        assertEquals(0, new BigDecimal("2000000").compareTo(gia.get(11).getThanhTien()));
    }

    @Test
    void gioiHan_chiLayDungSoLuongYeuCau() {
        Product a = sanPham(1, "1000000", true, 5);
        List<Combo> nhieuCombo = new ArrayList<>();
        for (int i = 0; i < 5; i++) nhieuCombo.add(combo("100000", a));
        khongCoFlashSale(a);
        when(comboRepository.findDangBan()).thenReturn(nhieuCombo);

        assertEquals(3, comboService.layComboDangBan(3).size());
    }
}

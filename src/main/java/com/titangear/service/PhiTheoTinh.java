package com.titangear.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

// Tính phí vận chuyển bằng cách so tỉnh khách chọn với tỉnh đặt kho.
//
//   trùng tỉnh  -> phí nội tỉnh
//   khác tỉnh   -> phí liên tỉnh
//   tiền hàng ≥ ngưỡng miễn phí -> 0đ
//
// Cả 4 con số đều đọc từ Cài đặt hệ thống, admin đổi trên web không cần build lại.
//
// GIỚI HẠN ĐÃ BIẾT: chỉ phân biệt được tới cấp TỈNH. Shop ở TP.HCM thì khách ở
// Quận 1 và khách ở Củ Chi trả phí như nhau, dù thực tế giao Củ Chi tốn hơn.
// Muốn phân nội thành/ngoại thành phải thu thêm cấp quận/huyện — khi đó nên gọi
// thẳng API đơn vị vận chuyển vì họ có bảng giá tới từng quận.
@Service
public class PhiTheoTinh implements TinhPhiVanChuyen {

    private static final Logger log = LoggerFactory.getLogger(PhiTheoTinh.class);

    private static final String K_TINH_KHO   = "SHOP_PROVINCE";
    private static final String K_PHI_NOI    = "SHIPPING_FEE_SAME_PROVINCE";
    private static final String K_PHI_NGOAI  = "SHIPPING_FEE_OTHER_PROVINCE";
    private static final String K_NGUONG_FREE= "FREE_SHIPPING_THRESHOLD";

    @Autowired private SystemSettingService systemSettingService;

    @Override
    public BigDecimal tinhPhi(BigDecimal tienHang, String tinhKhach) {
        BigDecimal hang = tienHang == null ? BigDecimal.ZERO : tienHang;

        // Miễn phí theo ngưỡng — kiểm TRƯỚC, vì miễn rồi thì không cần biết ở tỉnh nào
        BigDecimal nguong = doSo(K_NGUONG_FREE, "500000");
        if (nguong.compareTo(BigDecimal.ZERO) > 0 && hang.compareTo(nguong) >= 0) {
            return BigDecimal.ZERO;
        }

        // Chưa chọn tỉnh -> lấy mức CAO HƠN (liên tỉnh).
        // Thà tính dư rồi khách thắc mắc, còn hơn tính thiếu và shop chịu lỗ.
        if (!cungTinhVoiKho(tinhKhach)) {
            return doSo(K_PHI_NGOAI, "35000");
        }
        return doSo(K_PHI_NOI, "20000");
    }

    @Override
    public String moTa(String tinhKhach) {
        BigDecimal nguong = doSo(K_NGUONG_FREE, "500000");
        String mien = nguong.compareTo(BigDecimal.ZERO) > 0
                ? " · Miễn phí cho đơn từ " + dinhDang(nguong) + "đ"
                : "";
        if (tinhKhach == null || tinhKhach.isBlank()) {
            return "Chọn tỉnh/thành để biết phí vận chuyển" + mien;
        }
        return (cungTinhVoiKho(tinhKhach) ? "Giao nội tỉnh" : "Giao liên tỉnh") + mien;
    }

    // So tỉnh khách với tỉnh đặt kho, bỏ qua hoa/thường và khoảng trắng thừa.
    private boolean cungTinhVoiKho(String tinhKhach) {
        if (tinhKhach == null || tinhKhach.isBlank()) return false;
        String kho = systemSettingService.getValue(K_TINH_KHO, "TP. Hồ Chí Minh");
        return tinhKhach.trim().equalsIgnoreCase(kho.trim());
    }

    // Đọc một con số từ Cài đặt. Admin gõ nhầm chữ vào ô số thì KHÔNG được để
    // trang đặt hàng chết — lấy giá trị mặc định và ghi log để còn biết mà sửa.
    private BigDecimal doSo(String khoa, String macDinh) {
        String v = systemSettingService.getValue(khoa, macDinh);
        try {
            return new BigDecimal(v.trim());
        } catch (NumberFormatException e) {
            log.warn("Cài đặt '{}' không phải số (đang là \"{}\") -> dùng mặc định {}", khoa, v, macDinh);
            return new BigDecimal(macDinh);
        }
    }

    private String dinhDang(BigDecimal n) {
        return String.format("%,d", n.longValue()).replace(',', '.');
    }
}

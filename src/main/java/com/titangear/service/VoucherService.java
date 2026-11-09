package com.titangear.service;

import com.titangear.exception.LoiNghiepVu;
import com.titangear.model.CartItem;
import com.titangear.model.Voucher;
import com.titangear.model.VoucherUsage;
import com.titangear.repository.VoucherRepository;
import com.titangear.repository.VoucherUsageRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class VoucherService {

    private static final Logger log = LoggerFactory.getLogger(VoucherService.class);

    @Autowired private VoucherRepository voucherRepository;
    @Autowired private VoucherUsageRepository usageRepository;

    // Kết quả kiểm mã: hợp lệ hay không, giảm bao nhiêu, và lý do để hiện cho khách.
    @Data
    @AllArgsConstructor
    public static class KetQua {
        private boolean hopLe;
        private BigDecimal soTienGiam;
        private String thongBao;
        private Integer voucherId;

        static KetQua loi(String thongBao) {
            return new KetQua(false, BigDecimal.ZERO, thongBao, null);
        }
    }

    // Kiểm mã + tính số tiền được giảm cho 1 khách với 1 giá trị đơn cụ thể.
    // LUÔN gọi lại ở server lúc đặt hàng — không tin số tiền client gửi lên.
    public KetQua check(String code, Integer userId, BigDecimal subtotal) {
        return check(code, userId, subtotal, null);
    }

    // Bản đầy đủ — nhận thêm GIỎ HÀNG để kiểm được điều kiện theo combo.
    //
    // Vì sao phải truyền giỏ vào: mã "chỉ dùng cho combo" không thể xác minh
    // bằng mỗi tổng tiền. Bản 3 tham số ở trên giữ lại cho các chỗ gọi cũ và
    // luôn TỪ CHỐI mã ràng buộc combo (không có giỏ thì không chứng minh được
    // điều kiện — thà từ chối còn hơn giảm nhầm).
    //
    // @Transactional(readOnly) BẮT BUỘC: Voucher.combo khai LAZY nên đọc
    // getCombo().getName() (để ghép vào câu báo lỗi) cần phiên Hibernate còn mở.
    // Thiếu nó thì ném LazyInitializationException -> lỗi 500 ngay khi mã bị từ
    // chối. Test đơn vị KHÔNG bắt được lỗi này vì trong test Combo là object
    // thường, không phải proxy — phải chạy thật trên web mới lòi ra.
    @Transactional(readOnly = true)
    public KetQua check(String code, Integer userId, BigDecimal subtotal, List<CartItem> gio) {
        if (code == null || code.trim().isEmpty()) {
            return KetQua.loi("Vui lòng nhập mã giảm giá.");
        }
        Voucher v = voucherRepository.findByCodeIgnoreCase(code.trim()).orElse(null);
        if (v == null) {
            return KetQua.loi("Mã giảm giá không tồn tại.");
        }
        if (!Boolean.TRUE.equals(v.getActive())) {
            return KetQua.loi("Mã này đã ngừng áp dụng.");
        }

        LocalDateTime now = LocalDateTime.now();
        if (v.getStartAt() != null && now.isBefore(v.getStartAt())) {
            return KetQua.loi("Mã chưa tới thời gian sử dụng.");
        }
        if (v.getEndAt() != null && now.isAfter(v.getEndAt())) {
            return KetQua.loi("Mã đã hết hạn.");
        }

        BigDecimal tong = subtotal != null ? subtotal : BigDecimal.ZERO;
        if (v.getMinOrderAmount() != null && tong.compareTo(v.getMinOrderAmount()) < 0) {
            return KetQua.loi("Đơn tối thiểu "
                    + v.getMinOrderAmount().setScale(0, RoundingMode.HALF_UP) + "đ mới dùng được mã này.");
        }

        if (v.getUsageLimit() != null && v.getUsedCount() != null
                && v.getUsedCount() >= v.getUsageLimit()) {
            return KetQua.loi("Mã đã hết lượt sử dụng.");
        }

        if (Boolean.TRUE.equals(v.getOncePerUser()) && userId != null
                && usageRepository.existsByVoucherIdAndUserId(v.getId(), userId)) {
            return KetQua.loi("Bạn đã dùng mã này rồi.");
        }

        // ---- Điều kiện theo COMBO ----
        // Đặt SAU các kiểm tra chung để khách nhận đúng lý do bị từ chối
        // (mã hết hạn thì báo hết hạn, chứ không báo "thiếu combo").
        if (v.getCombo() != null) {
            if (!gioCoCombo(gio, v.getCombo().getId())) {
                return KetQua.loi("Mã này chỉ áp dụng cho combo \"" + v.getCombo().getName() + "\".");
            }
        } else if (Boolean.TRUE.equals(v.getChiDonCoCombo())) {
            if (!gioCoCombo(gio, null)) {
                return KetQua.loi("Mã này chỉ áp dụng cho đơn hàng có combo.");
            }
        }

        BigDecimal giam = tinhSoTienGiam(v, tong);
        if (giam.compareTo(BigDecimal.ZERO) <= 0) {
            return KetQua.loi("Mã không giảm được cho đơn hàng này.");
        }
        return new KetQua(true, giam, "Áp dụng mã thành công!", v.getId());
    }

    // Giỏ có chứa combo không. comboId = null nghĩa là "combo bất kỳ".
    // Giỏ rỗng/null -> false: không chứng minh được điều kiện thì không giảm.
    private boolean gioCoCombo(List<CartItem> gio, Integer comboId) {
        if (gio == null || gio.isEmpty()) return false;
        return gio.stream().anyMatch(d -> d.getCombo() != null
                && (comboId == null || comboId.equals(d.getCombo().getId())));
    }

    // PERCENT: subtotal × value / 100, chặn trần maxDiscountAmount nếu có.
    // AMOUNT : đúng số tiền, nhưng không vượt quá giá trị đơn (tổng không âm).
    private BigDecimal tinhSoTienGiam(Voucher v, BigDecimal subtotal) {
        BigDecimal giam;
        if (Voucher.TYPE_PERCENT.equals(v.getDiscountType())) {
            giam = subtotal.multiply(v.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
            if (v.getMaxDiscountAmount() != null && giam.compareTo(v.getMaxDiscountAmount()) > 0) {
                giam = v.getMaxDiscountAmount();
            }
        } else {
            giam = v.getDiscountValue();
        }
        // Không bao giờ giảm quá giá trị đơn -> tổng thanh toán không âm
        if (giam.compareTo(subtotal) > 0) giam = subtotal;
        return giam.setScale(0, RoundingMode.HALF_UP);
    }

    // Ghi nhận đã dùng mã (gọi sau khi tạo đơn thành công).
    @Transactional
    public void ghiNhanDaDung(Integer voucherId, Integer userId, Integer orderId, BigDecimal soTienGiam) {
        VoucherUsage u = new VoucherUsage();
        u.setVoucherId(voucherId);
        u.setUserId(userId);
        u.setOrderId(orderId);
        u.setDiscountAmount(soTienGiam);
        u.setUsedAt(LocalDateTime.now());
        usageRepository.save(u);

        // Cộng lượt bằng một câu UPDATE nguyên tử (xem VoucherRepository.tangLuotDung).
        // Trả 0 nghĩa là mã vừa hết lượt do người khác dùng xong trước trong tích tắc.
        // Đơn đã tạo rồi nên vẫn giữ mức giảm cho khách này, nhưng phải ghi log để
        // admin biết có một lượt vượt giới hạn.
        int doi = voucherRepository.tangLuotDung(voucherId);
        if (doi == 0) {
            log.warn("Voucher #{} đã hết lượt nhưng đơn #{} vẫn được giảm (2 khách dùng cùng lúc)",
                    voucherId, orderId);
        }
    }

    // ---- Dùng cho trang quản trị ----
    public List<Voucher> getAll() { return voucherRepository.findAllByOrderByIdDesc(); }

    public java.util.Optional<Voucher> findById(Integer id) { return voucherRepository.findById(id); }

    public boolean codeDaTonTai(String code) { return voucherRepository.existsByCodeIgnoreCase(code); }

    // Bảng ký tự sinh mã — CỐ TÌNH bỏ O/0 và I/1 vì khách đọc mã trên giấy hay
    // gõ nhầm hai cặp này, sinh ra khiếu nại "mã không dùng được" oan.
    private static final String KY_TU = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int SO_KY_TU = 6;
    private static final int SO_LAN_THU = 10;

    // Sinh mã ngẫu nhiên dạng TTG + 6 ký tự, đã kiểm không trùng mã cũ.
    // Thử tối đa 10 lần rồi mới chịu thua — với 32^6 ≈ 1 tỉ tổ hợp thì đụng trùng
    // 10 lần liên tiếp là chuyện gần như không xảy ra.
    public String sinhMa() {
        java.util.Random rd = new java.util.Random();
        for (int lan = 0; lan < SO_LAN_THU; lan++) {
            StringBuilder sb = new StringBuilder("TTG");
            for (int i = 0; i < SO_KY_TU; i++) {
                sb.append(KY_TU.charAt(rd.nextInt(KY_TU.length())));
            }
            String ma = sb.toString();
            if (!codeDaTonTai(ma)) return ma;
        }
        throw new LoiNghiepVu("Không sinh được mã mới sau " + SO_LAN_THU + " lần thử");
    }

    public Voucher save(Voucher v) { return voucherRepository.save(v); }
}

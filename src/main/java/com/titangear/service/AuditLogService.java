package com.titangear.service;

import com.titangear.model.AuditLog;
import com.titangear.model.User;
import com.titangear.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.List;

// Ghi nhật ký thao tác quản trị.
//
// NGUYÊN TẮC QUAN TRỌNG NHẤT: ghi nhật ký KHÔNG BAO GIỜ được làm hỏng việc chính.
// Toàn bộ thân hàm bọc try/catch — nếu bảng nhật ký lỗi thì chỉ ghi log kỹ thuật,
// tuyệt đối không ném ngoại lệ ra ngoài. Duyệt đơn thất bại chỉ vì không ghi được
// nhật ký là đổi một vấn đề nhỏ lấy một vấn đề lớn.
@Service
public class AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);

    /* Mã hành động — khai hằng số để không gõ sai chuỗi ở mỗi nơi gọi */
    public static final String SUA_GIA         = "SUA_GIA";
    public static final String SUA_SAN_PHAM    = "SUA_SAN_PHAM";
    public static final String THEM_SAN_PHAM   = "THEM_SAN_PHAM";
    public static final String AN_SAN_PHAM     = "AN_SAN_PHAM";
    public static final String DUYET_DON       = "DUYET_DON";
    public static final String DOI_TRANG_THAI  = "DOI_TRANG_THAI";
    public static final String HUY_DON         = "HUY_DON";
    public static final String NHAP_KHO        = "NHAP_KHO";
    public static final String DOI_CAI_DAT     = "DOI_CAI_DAT";
    public static final String SUA_NGUOI_DUNG  = "SUA_NGUOI_DUNG";
    public static final String KHOA_TAI_KHOAN  = "KHOA_TAI_KHOAN";
    public static final String SUA_VOUCHER     = "SUA_VOUCHER";
    public static final String SUA_COMBO       = "SUA_COMBO";
    public static final String DOI_VI_TRI      = "DOI_VI_TRI";

    // Bảng tra mã hành động sang tên tiếng Việt để hiện ra màn hình. Mã vẫn lưu
    // dạng chữ in hoa trong DB cho dễ lọc và không phụ thuộc ngôn ngữ hiển thị.
    private static final java.util.Map<String, String> TEN_TIENG_VIET =
            java.util.Map.ofEntries(
                    java.util.Map.entry(SUA_GIA,        "Đổi giá"),
                    java.util.Map.entry(SUA_SAN_PHAM,   "Sửa sản phẩm"),
                    java.util.Map.entry(THEM_SAN_PHAM,  "Thêm sản phẩm"),
                    java.util.Map.entry(AN_SAN_PHAM,    "Ẩn sản phẩm"),
                    java.util.Map.entry(DUYET_DON,      "Duyệt xuất kho"),
                    java.util.Map.entry(DOI_TRANG_THAI, "Đổi trạng thái đơn"),
                    java.util.Map.entry(HUY_DON,        "Hủy đơn"),
                    java.util.Map.entry(NHAP_KHO,       "Nhập kho"),
                    java.util.Map.entry(DOI_VI_TRI,     "Đổi vị trí kho"),
                    java.util.Map.entry(DOI_CAI_DAT,    "Đổi cài đặt"),
                    java.util.Map.entry(SUA_NGUOI_DUNG, "Sửa tài khoản"),
                    java.util.Map.entry(KHOA_TAI_KHOAN, "Khóa tài khoản"),
                    java.util.Map.entry(SUA_VOUCHER,    "Sửa mã giảm giá"),
                    java.util.Map.entry(SUA_COMBO,      "Sửa combo"));

    // Tra tên tiếng Việt của một mã hành động. Mã lạ thì trả về chính nó để nhật ký
    // cũ vẫn đọc được sau khi thêm mã mới.
    public static String tenTiengViet(String ma) {
        if (ma == null) return "";
        return TEN_TIENG_VIET.getOrDefault(ma, ma);
    }

    @Autowired private AuditLogRepository auditLogRepository;
    @Autowired private UserService        userService;

    // Bản gọn: chỉ mô tả việc đã làm.
    public void ghi(String hanhDong, String doiTuong, Integer doiTuongId, String moTa) {
        ghi(hanhDong, doiTuong, doiTuongId, null, null, moTa);
    }

    // Bản đầy đủ: kèm giá trị trước và sau khi đổi.
    public void ghi(String hanhDong, String doiTuong, Integer doiTuongId,
                    String giaTriCu, String giaTriMoi, String moTa) {
        try {
            AuditLog nk = new AuditLog();
            nk.setHanhDong(hanhDong);
            nk.setDoiTuong(doiTuong);
            nk.setDoiTuongId(doiTuongId);
            nk.setGiaTriCu(catNgan(giaTriCu));
            nk.setGiaTriMoi(catNgan(giaTriMoi));
            nk.setMoTa(catNgan(moTa));
            nk.setThoiDiem(LocalDateTime.now());
            nk.setDiaChiIp(layDiaChiIp());

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                nk.setUserEmail(auth.getName());
                userService.findByEmail(auth.getName()).map(User::getId).ifPresent(nk::setUserId);
            }
            auditLogRepository.save(nk);

        } catch (Exception e) {
            // KHÔNG ném ra ngoài — xem ghi chú ở đầu lớp
            log.error("Không ghi được nhật ký thao tác [{} {} #{}]", hanhDong, doiTuong, doiTuongId, e);
        }
    }

    // Cột chỉ 500 ký tự — cắt bớt thay vì để lỗi truncate làm hỏng việc chính.
    private String catNgan(String s) {
        if (s == null) return null;
        return s.length() <= 500 ? s : s.substring(0, 497) + "...";
    }

    // Địa chỉ IP người thao tác. Ưu tiên X-Forwarded-For vì khi chạy sau proxy
    // (nginx, cloudflare) thì getRemoteAddr() chỉ trả IP của proxy.
    private String layDiaChiIp() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return null;   // gọi từ tác vụ nền, không có request
            HttpServletRequest req = attrs.getRequest();
            String ip = req.getHeader("X-Forwarded-For");
            if (ip != null && !ip.isBlank()) return ip.split(",")[0].trim();
            return req.getRemoteAddr();
        } catch (Exception e) {
            return null;
        }
    }

    /* ---- Đọc cho trang quản trị ---- */

    public Page<AuditLog> layTrang(String hanhDong, Pageable pageable) {
        return (hanhDong == null || hanhDong.isBlank())
                ? auditLogRepository.findAllByOrderByThoiDiemDesc(pageable)
                : auditLogRepository.findByHanhDongOrderByThoiDiemDesc(hanhDong, pageable);
    }

    public List<String> cacLoaiHanhDong() {
        return auditLogRepository.cacLoaiHanhDong();
    }

    // Lịch sử thao tác của một đối tượng — vd mọi thay đổi trên đơn #1039.
    public List<AuditLog> lichSuCua(String doiTuong, Integer doiTuongId) {
        return auditLogRepository.findByDoiTuongAndDoiTuongIdOrderByThoiDiemDesc(doiTuong, doiTuongId);
    }
}

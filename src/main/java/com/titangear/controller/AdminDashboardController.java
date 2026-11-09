package com.titangear.controller;

import com.titangear.config.RequirePermission;
import com.titangear.constant.OrderStatus;
import com.titangear.dao.DashboardDao;
import com.titangear.service.OrderService;
import com.titangear.service.WarehousePermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

// Trang Tổng quan (Dashboard) cho admin — gom số liệu quan trọng nhất về 1 chỗ.
// Mọi con số đều lấy từ DB thật (xem DashboardDao).
@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    @Autowired private DashboardDao dashboardDao;
    @Autowired private OrderService orderService;

    @RequirePermission(WarehousePermissionService.CAN_VIEW_REVENUE)
    @GetMapping("/dashboard")
    public String dashboard(Model model) {

        // ---- 4 thẻ chỉ số ----
        BigDecimal homNay = dashboardDao.doanhThuHomNay();
        BigDecimal homQua = dashboardDao.doanhThuHomQua();
        model.addAttribute("doanhThuHomNay", homNay);
        model.addAttribute("doanhThuHomNayM", homNay.doubleValue() / 1_000_000.0);
        model.addAttribute("soSanhHomQua", tinhPhanTramThayDoi(homQua, homNay));

        model.addAttribute("donMoiHomNay", dashboardDao.donMoiHomNay());
        model.addAttribute("donChoXuLy",   dashboardDao.donChoXuLy());
        model.addAttribute("khachMoiHomNay", dashboardDao.khachMoiHomNay());
        model.addAttribute("gioHangDangMo",  dashboardDao.gioHangDangMo());

        List<Map<String, Object>> canChuY = dashboardDao.canChuY();
        model.addAttribute("canChuY", canChuY);
        model.addAttribute("soCanhBao", canChuY.size());

        // ---- Biểu đồ doanh thu 14 ngày ----
        List<Map<String, Object>> chuoi = dashboardDao.doanhThu14Ngay();
        StringBuilder nhan = new StringBuilder("[");
        StringBuilder giaTri = new StringBuilder("[");
        BigDecimal tong14 = BigDecimal.ZERO;
        for (int i = 0; i < chuoi.size(); i++) {
            Map<String, Object> r = chuoi.get(i);
            String ngay = String.valueOf(r.get("ngay"));           // yyyy-MM-dd
            String ddMM = ngay.length() >= 10 ? ngay.substring(8, 10) + "/" + ngay.substring(5, 7) : ngay;
            BigDecimal dt = (BigDecimal) r.get("doanh_thu");
            if (dt == null) dt = BigDecimal.ZERO;
            tong14 = tong14.add(dt);
            if (i > 0) { nhan.append(","); giaTri.append(","); }
            nhan.append("\"").append(ddMM).append("\"");
            giaTri.append(dt.toPlainString());
        }
        model.addAttribute("nhan14Ngay", nhan.append("]").toString());
        model.addAttribute("giaTri14Ngay", giaTri.append("]").toString());
        model.addAttribute("tong14Ngay", tong14);
        model.addAttribute("tong14NgayM", tong14.doubleValue() / 1_000_000.0);

        // ---- Donut trạng thái đơn ----
        long cho     = orderService.countByStatus(OrderStatus.PENDING);
        long choHang = orderService.countByStatus(OrderStatus.AWAITING_STOCK);
        long dangGiao= orderService.countByStatus(OrderStatus.SHIPPING);
        long hoanTat = orderService.countByStatus(OrderStatus.COMPLETED);
        long daHuy   = orderService.countByStatus(OrderStatus.CANCELLED);
        model.addAttribute("ttCho", cho);
        model.addAttribute("ttChoHang", choHang);
        model.addAttribute("ttDangGiao", dangGiao);
        model.addAttribute("ttHoanTat", hoanTat);
        model.addAttribute("ttDaHuy", daHuy);
        model.addAttribute("tongDon", cho + choHang + dangGiao + hoanTat + daHuy);

        // ---- Danh sách ----
        // Bán chạy: cần số bán CAO NHẤT để vẽ thanh tiến trình theo tỉ lệ
        List<Map<String, Object>> banChay = dashboardDao.banChayNhat(5);
        long banNhieuNhat = 0;
        for (Map<String, Object> p : banChay) {
            Object v = p.get("da_ban");
            long n = (v instanceof Number) ? ((Number) v).longValue() : 0;
            if (n > banNhieuNhat) banNhieuNhat = n;
        }
        model.addAttribute("banChay", banChay);
        model.addAttribute("banNhieuNhat", banNhieuNhat == 0 ? 1 : banNhieuNhat);
        model.addAttribute("hoatDong", dashboardDao.hoatDongGanDay(6));

        model.addAttribute("capNhatLuc",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        model.addAttribute("adminActive", "dashboard");
        return "admin/dashboard";
    }

    // % thay đổi so với kỳ trước; hôm qua = 0 thì không so sánh được.
    private String tinhPhanTramThayDoi(BigDecimal truoc, BigDecimal sau) {
        if (truoc == null || truoc.compareTo(BigDecimal.ZERO) == 0) {
            return sau.compareTo(BigDecimal.ZERO) > 0 ? "mới có doanh thu" : "chưa có doanh thu";
        }
        BigDecimal pct = sau.subtract(truoc)
                .multiply(BigDecimal.valueOf(100))
                .divide(truoc, 1, RoundingMode.HALF_UP);
        return (pct.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "") + pct.toPlainString() + "% so với hôm qua";
    }
}

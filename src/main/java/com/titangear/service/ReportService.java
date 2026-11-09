package com.titangear.service;

import com.titangear.dao.RevenueReportDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    @Autowired
    private RevenueReportDao revenueReportDao;

    public Map<String, Object> getSummary() {
        return revenueReportDao.getSummary();
    }

    public List<Map<String, Object>> getRevenueByMonth() {
        return revenueReportDao.getRevenueByMonth();
    }

    public List<Map<String, Object>> getTopProducts() {
        return revenueReportDao.getTopProducts();
    }

    public List<Map<String, Object>> getAllProductsSales() {
        return withRevenuePercent(revenueReportDao.getAllProductsSales());
    }

    public List<Map<String, Object>> getProductsSalesByMonthYear(Integer month, Integer year) {
        return withRevenuePercent(revenueReportDao.getProductsSalesByMonthYear(month, year));
    }

    // Thêm cột % đóng góp vào tổng doanh thu cho từng dòng — tính trên đúng
    // tập dữ liệu đang hiển thị (nếu đang lọc theo tháng/năm thì % tính trên
    // tổng doanh thu của riêng tháng/năm đó, không phải tổng cả năm).
    private List<Map<String, Object>> withRevenuePercent(List<Map<String, Object>> rows) {
        BigDecimal grandTotal = rows.stream()
                .map(r -> toBigDecimal(r.get("doanh_thu")))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Map<String, Object> r = new LinkedHashMap<>(row);
            BigDecimal revenue = toBigDecimal(row.get("doanh_thu"));
            r.put("doanh_thu", revenue);
            BigDecimal percent = grandTotal.signum() > 0
                    ? revenue.multiply(new BigDecimal("100")).divide(grandTotal, 1, java.math.RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            r.put("phan_tram_doanh_thu", percent);
            result.add(r);
        }
        return result;
    }

    public List<Map<String, Object>> getRevenueByDateRange(String fromDate, String toDate) {
        return revenueReportDao.getRevenueByDateRange(fromDate, toDate);
    }

    public List<Map<String, Object>> getRevenueByCategory() {
        return revenueReportDao.getRevenueByCategory();
    }

    // Doanh thu ĐỦ 12 THÁNG của 1 năm cụ thể — tháng nào không có đơn hàng
    // hoàn thành thì vẫn hiện ra với doanh thu = 0, để biểu đồ so sánh
    // đủ Tháng 1 -> Tháng 12, không bị thiếu cột nào.
    public List<Map<String, Object>> getRevenueByMonthForYear(int year) {
        List<Map<String, Object>> raw = revenueReportDao.getRevenueByMonthForYear(year);

        Map<Integer, Map<String, Object>> byMonth = new java.util.HashMap<>();
        for (Map<String, Object> row : raw) {
            byMonth.put((Integer) row.get("thang"), row);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            Map<String, Object> found = byMonth.get(m);
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("thang", m);
            r.put("doanh_thu", found != null ? toBigDecimal(found.get("doanh_thu")) : BigDecimal.ZERO);
            r.put("so_don", found != null ? found.get("so_don") : 0);
            result.add(r);
        }
        return result;
    }

    // Doanh thu - giá vốn = lợi nhuận thật theo từng tháng, kèm so sánh lỗ/lời
    // với tháng liền trước. Giá vốn lấy từ import_cost thật của đúng lô FIFO
    // đã xuất (đã tính sẵn ở RevenueReportDao), không phải ước tính.
    public List<Map<String, Object>> getProfitByMonth() {
        List<Map<String, Object>> raw = revenueReportDao.getProfitByMonth();
        List<Map<String, Object>> result = new ArrayList<>();

        BigDecimal previousProfit = null;

        for (Map<String, Object> row : raw) {
            Map<String, Object> r = new LinkedHashMap<>(row);

            BigDecimal revenue = toBigDecimal(row.get("doanh_thu"));
            BigDecimal cost    = toBigDecimal(row.get("gia_von"));
            BigDecimal profit  = revenue.subtract(cost);

            r.put("doanh_thu", revenue);
            r.put("gia_von", cost);
            r.put("loi_nhuan", profit);
            r.put("la_loi", profit.signum() >= 0); // true = lời/hòa vốn, false = lỗ

            if (previousProfit != null) {
                r.put("thay_doi", profit.subtract(previousProfit));
                r.put("tang_so_thang_truoc", profit.compareTo(previousProfit) >= 0);
            } else {
                r.put("thay_doi", null);
            }

            previousProfit = profit;
            result.add(r);
        }

        return result;
    }

    // Lãi theo từng phiên bản: tính sẵn lợi nhuận + tỷ suất để JSP khỏi làm toán.
    // Tỷ suất tính trên DOANH THU (lãi/doanh thu), đây là cách quen thuộc khi
    // đánh giá "bán mặt hàng này có ăn không".
    public List<Map<String, Object>> getProfitByVariant() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> row : revenueReportDao.getProfitByVariant()) {
            Map<String, Object> r = new LinkedHashMap<>(row);

            BigDecimal doanhThu = toBigDecimal(row.get("doanh_thu"));
            BigDecimal giaVon   = toBigDecimal(row.get("gia_von"));
            BigDecimal loi      = doanhThu.subtract(giaVon);

            r.put("loi_nhuan", loi);
            r.put("la_loi", loi.signum() >= 0);
            r.put("ty_suat", doanhThu.signum() > 0
                    ? loi.multiply(BigDecimal.valueOf(100))
                         .divide(doanhThu, 1, java.math.RoundingMode.HALF_UP)
                    : BigDecimal.ZERO);
            // Chưa ghi nhận giá vốn (hàng bán trước khi có sổ kho) -> đừng khoe
            // "lãi 100%", nói thẳng là chưa tính được
            r.put("thieu_gia_von", giaVon.signum() == 0);

            result.add(r);
        }
        return result;
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal bd) return bd;
        if (value instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        return BigDecimal.ZERO;
    }

    // Tạo danh sách năm cho dropdown (năm hiện tại về trước 4 năm)
    public List<Integer> getYearOptions() {
        int currentYear = LocalDate.now().getYear();
        List<Integer> years = new ArrayList<>();
        for (int y = currentYear; y >= currentYear - 4; y--) years.add(y);
        return years;
    }
}

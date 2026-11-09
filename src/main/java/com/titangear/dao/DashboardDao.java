package com.titangear.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

// Số liệu cho trang Dashboard admin. TẤT CẢ đều lấy từ dữ liệu thật trong DB —
// không có chỉ số bịa. Những thứ hệ thống không đo được (lượt truy cập, số người
// đang online) đã được thay bằng chỉ số có thật: khách đăng ký mới trong ngày và
// số giỏ hàng đang mở.
@Repository
public class DashboardDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Doanh thu hôm nay — chỉ tính đơn ĐÃ THU TIỀN (is_paid = 1).
    public BigDecimal doanhThuHomNay() {
        String sql = """
            SELECT ISNULL(SUM(total_amount - ISNULL(shipping_fee,0)), 0)
            FROM orders
            WHERE is_paid = 1
              AND CAST(created_at AS DATE) = CAST(GETDATE() AS DATE)
            """;
        BigDecimal v = jdbcTemplate.queryForObject(sql, BigDecimal.class);
        return v != null ? v : BigDecimal.ZERO;
    }

    // Doanh thu hôm qua — để so sánh tăng/giảm.
    public BigDecimal doanhThuHomQua() {
        String sql = """
            SELECT ISNULL(SUM(total_amount - ISNULL(shipping_fee,0)), 0)
            FROM orders
            WHERE is_paid = 1
              AND CAST(created_at AS DATE) = CAST(DATEADD(day, -1, GETDATE()) AS DATE)
            """;
        BigDecimal v = jdbcTemplate.queryForObject(sql, BigDecimal.class);
        return v != null ? v : BigDecimal.ZERO;
    }

    public int donMoiHomNay() {
        String sql = "SELECT COUNT(*) FROM orders WHERE CAST(created_at AS DATE) = CAST(GETDATE() AS DATE)";
        Integer v = jdbcTemplate.queryForObject(sql, Integer.class);
        return v != null ? v : 0;
    }

    // Đơn đang chờ xử lý (chưa giao, chưa hủy) — con số cần hành động.
    public int donChoXuLy() {
        String sql = "SELECT COUNT(*) FROM orders WHERE status IN ('PENDING','AWAITING_STOCK')";
        Integer v = jdbcTemplate.queryForObject(sql, Integer.class);
        return v != null ? v : 0;
    }

    // Thay cho "khách truy cập" (không đo được): khách đăng ký mới hôm nay.
    public int khachMoiHomNay() {
        String sql = "SELECT COUNT(*) FROM users WHERE role = 'CUSTOMER' AND CAST(created_at AS DATE) = CAST(GETDATE() AS DATE)";
        Integer v = jdbcTemplate.queryForObject(sql, Integer.class);
        return v != null ? v : 0;
    }

    // Thay cho "đang online" (không đo được): số khách đang có hàng trong giỏ.
    public int gioHangDangMo() {
        String sql = "SELECT COUNT(DISTINCT user_id) FROM cart_items";
        Integer v = jdbcTemplate.queryForObject(sql, Integer.class);
        return v != null ? v : 0;
    }

    // Doanh thu 14 ngày gần nhất (đủ 14 mốc, ngày không có đơn = 0).
    public List<Map<String, Object>> doanhThu14Ngay() {
        String sql = """
            WITH ngay AS (
                SELECT CAST(DATEADD(day, -13, GETDATE()) AS DATE) AS d
                UNION ALL
                SELECT CAST(DATEADD(day, 1, d) AS DATE) FROM ngay
                WHERE d < CAST(GETDATE() AS DATE)
            )
            SELECT n.d AS ngay,
                   ISNULL(SUM(o.total_amount - ISNULL(o.shipping_fee,0)), 0) AS doanh_thu
            FROM ngay n
            LEFT JOIN orders o
                   ON CAST(o.created_at AS DATE) = n.d AND o.is_paid = 1
            GROUP BY n.d
            ORDER BY n.d
            """;
        return jdbcTemplate.queryForList(sql);
    }

    // Top sản phẩm bán chạy (tính theo số lượng đã bán, bỏ đơn đã hủy).
    public List<Map<String, Object>> banChayNhat(int soLuong) {
        String sql = """
            SELECT TOP (?)
                   p.name       AS ten_sp,
                   p.image_url  AS anh,
                   SUM(oi.quantity) AS da_ban
            FROM order_items oi
            JOIN orders   o ON o.id = oi.order_id
            JOIN products p ON p.id = oi.product_id
            WHERE o.status <> 'CANCELLED'
            GROUP BY p.id, p.name, p.image_url
            ORDER BY SUM(oi.quantity) DESC
            """;
        return jdbcTemplate.queryForList(sql, soLuong);
    }

    // Hoạt động gần đây: trộn đơn mới + đánh giá mới + nhập kho gần nhất.
    public List<Map<String, Object>> hoatDongGanDay(int soLuong) {
        String sql = """
            SELECT TOP (?) * FROM (
                SELECT o.created_at AS thoi_diem,
                       u.full_name  AS ai,
                       N'vừa đặt đơn hàng'  AS hanh_dong,
                       N'#' + CAST(o.id AS NVARCHAR) AS chi_tiet,
                       'order' AS loai
                FROM orders o JOIN users u ON u.id = o.user_id
                UNION ALL
                SELECT r.created_at, u.full_name,
                       N'vừa đánh giá sản phẩm',
                       p.name, 'review'
                FROM reviews r
                JOIN users u    ON u.id = r.user_id
                JOIN products p ON p.id = r.product_id
                UNION ALL
                SELECT si.imported_at, u.full_name,
                       N'vừa nhập kho',
                       p.name + N' (+' + CAST(si.quantity AS NVARCHAR) + N')', 'stock'
                FROM stock_in si
                JOIN users u    ON u.id = si.imported_by
                JOIN products p ON p.id = si.product_id
            ) t
            ORDER BY thoi_diem DESC
            """;
        return jdbcTemplate.queryForList(sql, soLuong);
    }

    // Sản phẩm cần chú ý: hết hàng hoặc sắp hết (<= 5).
    public List<Map<String, Object>> canChuY() {
        String sql = """
            SELECT TOP 6 name AS ten_sp, stock_quantity AS ton
            FROM products
            WHERE is_active = 1 AND stock_quantity <= 5
            ORDER BY stock_quantity ASC
            """;
        return jdbcTemplate.queryForList(sql);
    }
}

package com.titangear.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Map;

@Repository
public class RevenueReportDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Tổng doanh thu + số đơn thành công
    public Map<String, Object> getSummary() {
        String sql = """
            SELECT
                COUNT(*)            AS tong_don,
                SUM(total_amount - ISNULL(shipping_fee,0))   AS tong_doanh_thu
            FROM orders
            WHERE status = 'COMPLETED'
            """;
        return jdbcTemplate.queryForMap(sql);
    }

    // Doanh thu theo tháng — dùng cho biểu đồ Chart.js
    public List<Map<String, Object>> getRevenueByMonth() {
        String sql = """
            SELECT
                MONTH(created_at)   AS thang,
                YEAR(created_at)    AS nam,
                COUNT(*)            AS so_don,
                SUM(total_amount - ISNULL(shipping_fee,0))   AS doanh_thu
            FROM orders
            WHERE status = 'COMPLETED'
            GROUP BY YEAR(created_at), MONTH(created_at)
            ORDER BY nam, thang
            """;
        return jdbcTemplate.queryForList(sql);
    }

    // Top 5 sản phẩm bán chạy nhất
    public List<Map<String, Object>> getTopProducts() {
        String sql = """
            SELECT TOP 5
                p.name              AS ten_sp,
                SUM(oi.quantity)    AS so_luong_ban,
                SUM(oi.line_total)  AS doanh_thu
            FROM order_items oi
            JOIN orders   o ON o.id  = oi.order_id
            JOIN products p ON p.id  = oi.product_id
            WHERE o.status = 'COMPLETED'
            GROUP BY p.id, p.name
            ORDER BY so_luong_ban DESC
            """;
        return jdbcTemplate.queryForList(sql);
    }

    // Tất cả sản phẩm đã bán — không lọc
    public List<Map<String, Object>> getAllProductsSales() {
        String sql = """
            SELECT
                p.name              AS ten_sp,
                c.name              AS danh_muc,
                SUM(oi.quantity)    AS so_luong_ban,
                SUM(oi.line_total)  AS doanh_thu
            FROM order_items oi
            JOIN orders     o ON o.id  = oi.order_id
            JOIN products   p ON p.id  = oi.product_id
            JOIN categories c ON c.id  = p.category_id
            WHERE o.status = 'COMPLETED'
            GROUP BY p.id, p.name, c.name
            ORDER BY doanh_thu DESC
            """;
        return jdbcTemplate.queryForList(sql);
    }

    // Sản phẩm đã bán — lọc theo tháng/năm
    public List<Map<String, Object>> getProductsSalesByMonthYear(Integer month, Integer year) {
        StringBuilder sql = new StringBuilder("""
            SELECT
                p.name              AS ten_sp,
                c.name              AS danh_muc,
                SUM(oi.quantity)    AS so_luong_ban,
                SUM(oi.line_total)  AS doanh_thu
            FROM order_items oi
            JOIN orders     o ON o.id  = oi.order_id
            JOIN products   p ON p.id  = oi.product_id
            JOIN categories c ON c.id  = p.category_id
            WHERE o.status = 'COMPLETED'
            """);

        if (month != null) sql.append("  AND MONTH(o.created_at) = ").append(month).append("\n");
        if (year  != null) sql.append("  AND YEAR(o.created_at)  = ").append(year).append("\n");

        sql.append("GROUP BY p.id, p.name, c.name ORDER BY doanh_thu DESC");
        return jdbcTemplate.queryForList(sql.toString());
    }

    // Doanh thu theo khoảng ngày
    public List<Map<String, Object>> getRevenueByDateRange(String fromDate, String toDate) {
        String sql = """
            SELECT
                CAST(created_at AS DATE)    AS ngay,
                COUNT(*)                    AS so_don,
                SUM(total_amount - ISNULL(shipping_fee,0))           AS doanh_thu
            FROM orders
            WHERE status = 'COMPLETED'
              AND created_at BETWEEN ? AND ?
            GROUP BY CAST(created_at AS DATE)
            ORDER BY ngay
            """;
        return jdbcTemplate.queryForList(sql, fromDate, toDate);
    }

    // Doanh thu theo từng tháng CỦA 1 NĂM CỤ THỂ — dùng cho biểu đồ so sánh 12 tháng
    public List<Map<String, Object>> getRevenueByMonthForYear(int year) {
        String sql = """
            SELECT
                MONTH(created_at)  AS thang,
                COUNT(*)           AS so_don,
                SUM(total_amount - ISNULL(shipping_fee,0))  AS doanh_thu
            FROM orders
            WHERE status = 'COMPLETED' AND YEAR(created_at) = ?
            GROUP BY MONTH(created_at)
            ORDER BY thang
            """;
        return jdbcTemplate.queryForList(sql, year);
    }

    // Doanh thu + GIÁ VỐN THẬT theo tháng — giá vốn lấy từ stock_out.import_cost
    // (giá nhập thực tế của đúng lô FIFO đã xuất, không phải giá nhập trung bình
    // hay ước tính). Nhờ đó tính được lợi nhuận thật: doanh thu - giá vốn,
    // chứ không chỉ nhìn doanh thu đơn thuần (doanh thu cao chưa chắc đã lời
    // nếu giá nhập tháng đó tăng; doanh thu thấp vẫn có thể lời nếu bán được
    // giá tốt hơn giá vốn).
    // Dùng LEFT JOIN vì lý thuyết có thể có order_item chưa từng được xuất kho
    // (dữ liệu lỗi/cũ) — khi đó giá vốn tính là 0 thay vì làm mất luôn dòng doanh thu.
    public List<Map<String, Object>> getProfitByMonth() {
        // Gom giá vốn theo từng dòng đơn trước rồi mới nối vào bảng đơn hàng.
        // Không JOIN thẳng stock_out: một dòng đơn lấy hàng từ nhiều lô sẽ có nhiều
        // dòng stock_out, làm dòng order_items bị nhân bản và doanh thu đội lên.
        String sql = """
            SELECT
                YEAR(o.created_at)  AS nam,
                MONTH(o.created_at) AS thang,
                SUM(oi.line_total)  AS doanh_thu,
                SUM(ISNULL(gv.gia_von, 0)) AS gia_von
            FROM orders o
            JOIN order_items oi ON oi.order_id = o.id
            LEFT JOIN (
                SELECT order_item_id, SUM(import_cost * quantity) AS gia_von
                FROM stock_out
                GROUP BY order_item_id
            ) gv ON gv.order_item_id = oi.id
            WHERE o.status = 'COMPLETED'
            GROUP BY YEAR(o.created_at), MONTH(o.created_at)
            ORDER BY nam, thang
            """;
        return jdbcTemplate.queryForList(sql);
    }

    // Lãi theo TỪNG PHIÊN BẢN sản phẩm (gộp chung các màu — người dùng chỉ cần
    // biết bản Pro hay bản Ultimate lãi hơn, không quan tâm màu đen hay trắng).
    //
    // Giá vốn lấy từ stock_out.import_cost = giá nhập THẬT của đúng lô FIFO đã
    // xuất, không phải ước tính hay bình quân.
    public List<Map<String, Object>> getProfitByVariant() {
        String sql = """
            SELECT
                p.name AS san_pham,
                ISNULL(v.name, N'(không chia phiên bản)') AS phien_ban,
                SUM(oi.quantity)   AS so_luong_ban,
                SUM(oi.line_total) AS doanh_thu,
                SUM(ISNULL(gv.gia_von, 0)) AS gia_von
            FROM order_items oi
            JOIN orders   o ON o.id = oi.order_id
            JOIN products p ON p.id = oi.product_id
            LEFT JOIN product_variants v ON v.id = oi.variant_id
            LEFT JOIN (
                SELECT order_item_id, SUM(import_cost * quantity) AS gia_von
                FROM stock_out
                GROUP BY order_item_id
            ) gv ON gv.order_item_id = oi.id
            WHERE o.status = 'COMPLETED'
            GROUP BY p.name, v.name
            ORDER BY doanh_thu DESC
            """;
        return jdbcTemplate.queryForList(sql);
    }

    // Tổng doanh thu theo TỪNG DANH MỤC (Chuột, Bàn phím, Tai nghe...)
    public List<Map<String, Object>> getRevenueByCategory() {
        String sql = """
            SELECT
                c.name              AS danh_muc,
                SUM(oi.quantity)    AS so_luong_ban,
                SUM(oi.line_total)  AS doanh_thu
            FROM order_items oi
            JOIN orders     o ON o.id  = oi.order_id
            JOIN products   p ON p.id  = oi.product_id
            JOIN categories c ON c.id  = p.category_id
            WHERE o.status = 'COMPLETED'
            GROUP BY c.id, c.name
            ORDER BY doanh_thu DESC
            """;
        return jdbcTemplate.queryForList(sql);
    }
}
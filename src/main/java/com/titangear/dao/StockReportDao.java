package com.titangear.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Map;

@Repository
public class StockReportDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Báo cáo tồn kho — tổng nhập/tổng xuất chỉ tính trong 1 THÁNG cụ thể
    // (mặc định tháng hiện tại), KHÔNG cộng dồn từ trước tới giờ nữa.
    public List<Map<String, Object>> getStockReport(int year, int month) {
        String sql = """
            SELECT
                p.id                          AS ma_sp,
                p.name                        AS ten_sp,
                c.name                        AS danh_muc,
                ISNULL(SUM(CASE
                    WHEN YEAR(si.imported_at) = ? AND MONTH(si.imported_at) = ?
                    THEN si.quantity ELSE 0
                END), 0)                      AS tong_nhap,
                ISNULL((
                    SELECT SUM(oi2.quantity)
                    FROM stock_out se2
                    JOIN order_items oi2 ON oi2.id = se2.order_item_id
                    WHERE oi2.product_id = p.id
                      AND se2.is_returned = 0
                      AND YEAR(se2.exported_at) = ? AND MONTH(se2.exported_at) = ?
                ), 0)                         AS tong_xuat,
                p.stock_quantity              AS ton_hien_tai
            FROM products p
            JOIN  categories       c  ON c.id          = p.category_id
            LEFT JOIN stock_in  si ON si.product_id = p.id
            -- KHÔNG lọc cứng p.is_active = 1.
            -- Báo cáo kho là chứng từ của MỘT THÁNG đã qua. Lọc theo trạng thái
            -- HIỆN TẠI thì tháng 3 nhập 100 con chuột X, tháng 6 ngừng bán X, mở
            -- lại báo cáo tháng 3 sẽ không còn dòng nào của X — số liệu quá khứ tự
            -- đổi theo hiện tại. Vẫn giữ sản phẩm đang bán để báo cáo đủ danh mục,
            -- và lấy thêm sản phẩm đã ngừng bán NẾU tháng đó có phát sinh hoặc còn tồn.
            WHERE p.is_active = 1
               OR p.stock_quantity > 0
               OR EXISTS (SELECT 1 FROM stock_in si3
                          WHERE si3.product_id = p.id
                            AND YEAR(si3.imported_at) = ? AND MONTH(si3.imported_at) = ?)
               OR EXISTS (SELECT 1 FROM stock_out se3
                          JOIN order_items oi3 ON oi3.id = se3.order_item_id
                          WHERE oi3.product_id = p.id AND se3.is_returned = 0
                            AND YEAR(se3.exported_at) = ? AND MONTH(se3.exported_at) = ?)
            GROUP BY p.id, p.name, c.name, p.stock_quantity
            ORDER BY p.id
            """;
        return jdbcTemplate.queryForList(sql,
                year, month,   // tong_nhap
                year, month,   // tong_xuat
                year, month,   // EXISTS nhap
                year, month);  // EXISTS xuat
    }

    // Lịch sử nhập kho — mỗi dòng stock_in tự nó là một lô (đợt nhập).
    // Thêm cột ma_lo (= id của chính dòng stock_in này) để đối chiếu được
    // với lịch sử xuất kho (biết được lô này đã xuất đi đâu).
    public List<Map<String, Object>> getAllStockInHistory(String tim) {
        String sql = """
            SELECT TOP 100
                si.id           AS ma_lo,
                si.imported_at,
                p.name          AS ten_sp,
                si.quantity,
                si.import_price,
                si.supplier,
                u.full_name     AS nguoi_nhap,
                si.note,
                si.remaining_quantity AS quantity_left,
                -- Vị trí cất hàng: để thủ kho biết ra khu nào, kệ nào mà tìm.
                -- Ghép sẵn chuỗi ở SQL cho JSP khỏi phải xử lý trường hợp NULL.
                si.khu,
                si.ke,
                CASE WHEN si.khu IS NULL OR si.ke IS NULL THEN N'— chưa gán —'
                     ELSE N'KHU ' + si.khu + N' · KỆ ' + CAST(si.ke AS NVARCHAR(10))
                END             AS vi_tri
            FROM stock_in si
            JOIN products p ON p.id = si.product_id
            JOIN users    u ON u.id = si.imported_by
            %s
            ORDER BY si.imported_at DESC
            """;

        // Lọc ngay trong truy vấn chứ không nạp 100 dòng rồi lọc trong Java: bảng
        // này chỉ lấy TOP 100 nên lọc sau sẽ bỏ sót lô cũ hơn nằm ngoài 100 dòng đó.
        //
        // Ghép mệnh đề WHERE khi có từ khóa, thay vì viết "? IS NULL OR ..." — SQL
        // Server không suy ra được kiểu của tham số null trần nên câu đó chạy sai.
        // Từ khóa vẫn truyền bằng tham số ?, KHÔNG nối thẳng vào chuỗi SQL, để không
        // mở đường cho tấn công chèn SQL.
        String tuKhoa = (tim == null || tim.isBlank()) ? null : tim.trim();
        if (tuKhoa == null) {
            return jdbcTemplate.queryForList(String.format(sql, ""));
        }
        String dieuKien = "WHERE p.name LIKE N'%' + ? + N'%' "
                        + "OR CAST(si.id AS NVARCHAR(20)) LIKE '%' + ? + '%'";
        return jdbcTemplate.queryForList(String.format(sql, dieuKien), tuKhoa, tuKhoa);
    }

    // Lịch sử xuất kho — thêm cột ma_lo (= stock_in_id) để biết chính xác
    // xuất từ lô nào (truy vết FIFO rõ ràng, đối chiếu được với bảng nhập).
    public List<Map<String, Object>> getAllStockOutHistory() {
        String sql = """
            SELECT TOP 100
                se.stock_in_id  AS ma_lo,
                se.exported_at,
                p.name          AS ten_sp,
                oi.quantity,
                se.import_cost,
                o.id            AS ma_don,
                u.full_name     AS nguoi_xuat,
                se.note,
                se.is_returned  AS da_hoan
            FROM stock_out se
            JOIN order_items oi ON oi.id = se.order_item_id
            JOIN products    p  ON p.id  = oi.product_id
            JOIN orders      o  ON o.id  = oi.order_id
            JOIN users       u  ON u.id  = se.exported_by
            ORDER BY se.exported_at DESC
            """;
        return jdbcTemplate.queryForList(sql);
    }

    public List<Map<String, Object>> getStockInHistory(Integer productId) {
        String sql = """
            SELECT
                si.id           AS ma_lo,
                si.imported_at,
                p.name          AS ten_sp,
                si.quantity,
                si.import_price,
                si.supplier,
                u.full_name     AS nguoi_nhap,
                si.note,
                si.remaining_quantity AS quantity_left
            FROM stock_in si
            JOIN products p ON p.id = si.product_id
            JOIN users    u ON u.id = si.imported_by
            WHERE si.product_id = ?
            ORDER BY si.imported_at DESC
            """;
        return jdbcTemplate.queryForList(sql, productId);
    }

    public List<Map<String, Object>> getStockOutHistory(Integer productId) {
        String sql = """
            SELECT
                se.stock_in_id  AS ma_lo,
                se.exported_at,
                p.name          AS ten_sp,
                oi.quantity,
                se.import_cost,
                o.id            AS ma_don,
                u.full_name     AS nguoi_xuat,
                se.note
            FROM stock_out se
            JOIN order_items oi ON oi.id = se.order_item_id
            JOIN products    p  ON p.id  = oi.product_id
            JOIN orders      o  ON o.id  = oi.order_id
            JOIN users       u  ON u.id  = se.exported_by
            WHERE oi.product_id = ?
            ORDER BY se.exported_at DESC
            """;
        return jdbcTemplate.queryForList(sql, productId);
    }

    public List<Map<String, Object>> getLowStockProducts() {
        String sql = """
            SELECT
                p.id             AS ma_sp,
                p.name           AS ten_sp,
                c.name           AS danh_muc,
                p.stock_quantity AS ton_hien_tai
            FROM products p
            JOIN categories c ON c.id = p.category_id
            WHERE p.is_active = 1 AND p.stock_quantity <= 10
            ORDER BY p.stock_quantity ASC
            """;
        return jdbcTemplate.queryForList(sql);
    }
}

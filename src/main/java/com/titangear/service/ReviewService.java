package com.titangear.service;

import com.titangear.exception.LoiNghiepVu;
import com.titangear.model.Product;
import com.titangear.model.Review;
import com.titangear.model.User;
import com.titangear.repository.ProductRepository;
import com.titangear.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReviewService {

    @Autowired private ReviewRepository reviewRepository;
    @Autowired private ProductRepository productRepository;

    // Điểm đánh giá của cả một trang sản phẩm, gom trong 1 truy vấn.
    // Khoá là productId; sản phẩm chưa ai đánh giá thì KHÔNG có mặt trong map
    // (JSP tự hiểu là "chưa có đánh giá") — nhẹ hơn là nhồi bản ghi rỗng.
    public Map<Integer, DiemDanhGia> diemTheoDanhSach(List<Integer> ids) {
        Map<Integer, DiemDanhGia> ket = new HashMap<>();
        if (ids == null || ids.isEmpty()) return ket;

        for (Object[] r : reviewRepository.tomTatTheoDanhSach(ids)) {
            int    id    = ((Number) r[0]).intValue();
            double avg   = r[1] != null ? ((Number) r[1]).doubleValue() : 0d;
            long   total = r[2] != null ? ((Number) r[2]).longValue()   : 0L;
            ket.put(id, new DiemDanhGia(Math.round(avg * 10.0) / 10.0, total));
        }
        return ket;
    }

    // Điểm gọn cho thẻ sản phẩm: 4.8 · 126 lượt.
    public static class DiemDanhGia {
        private final double diem;
        private final long soLuot;

        public DiemDanhGia(double diem, long soLuot) { this.diem = diem; this.soLuot = soLuot; }

        public double getDiem()   { return diem; }
        public long   getSoLuot() { return soLuot; }
        // EL không gọi được Math.round -> dựng sẵn để JSP vẽ đủ số sao đặc.
        public long   getSoSaoDac() { return Math.round(diem); }
    }

    // ------------------------------------------------------------
    // TỔNG QUAN ĐÁNH GIÁ (điểm trung bình + phân bố theo sao)
    // ------------------------------------------------------------
    public ReviewSummary getSummary(Integer productId) {
        List<Object[]> rows = reviewRepository.getSummary(productId);
        Object[] row = (rows != null && !rows.isEmpty()) ? rows.get(0) : null;

        // Nhận kết quả qua Number rồi mới đổi kiểu, vì AVG(rating) trên cột INT của
        // SQL Server có thể trả về Integer, Double hay BigDecimal tùy phiên bản.
        // Ép thẳng sang Double là văng ClassCastException.
        Double avg   = (row != null && row[0] != null) ? ((Number) row[0]).doubleValue() : 0d;
        Long   total = (row != null && row[1] != null) ? ((Number) row[1]).longValue()   : 0L;

        Map<Integer, Long> breakdown = new HashMap<>();
        for (int star = 1; star <= 5; star++) breakdown.put(star, 0L);
        for (Object[] r : reviewRepository.countByRatingGrouped(productId)) {
            breakdown.put(((Number) r[0]).intValue(), ((Number) r[1]).longValue());
        }

        return new ReviewSummary(
                Math.round(avg * 100.0) / 100.0, // làm tròn 2 chữ số thập phân
                total,
                breakdown
        );
    }

    // ------------------------------------------------------------
    // DANH SÁCH REVIEW THEO KIỂU SẮP XẾP
    // ------------------------------------------------------------
    public List<Review> getReviews(Integer productId, String sort) {
        if (sort == null) sort = "recent";
        switch (sort) {
            case "highest":
                return reviewRepository.findByProductIdOrderByRatingDescCreatedAtDesc(productId);
            case "lowest":
                return reviewRepository.findByProductIdOrderByRatingAscCreatedAtDesc(productId);
            case "oldest":
                return reviewRepository.findByProductIdOrderByCreatedAtAsc(productId);
            case "recent":
            default:
                return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId);
        }
    }

    // ------------------------------------------------------------
    // CÓ ĐỦ ĐIỀU KIỆN ĐÁNH GIÁ KHÔNG (đã mua hàng COMPLETED + chưa từng đánh giá)
    // ------------------------------------------------------------
    public boolean hasPurchased(Integer userId, Integer productId) {
        return reviewRepository.hasPurchased(userId, productId);
    }

    public boolean hasReviewed(Integer userId, Integer productId) {
        return reviewRepository.existsByUserIdAndProductId(userId, productId);
    }

    public Review getMyReview(Integer userId, Integer productId) {
        return reviewRepository.findByUserIdAndProductId(userId, productId).orElse(null);
    }

    // ------------------------------------------------------------
    // GỬI ĐÁNH GIÁ MỚI (hoặc sửa lại nếu đã từng đánh giá sản phẩm này)
    // ------------------------------------------------------------
    @Transactional
    public void submitReview(Integer userId, Integer productId, Integer rating, String comment) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new LoiNghiepVu("Số sao đánh giá phải từ 1 đến 5");
        }
        if (!reviewRepository.hasPurchased(userId, productId)) {
            throw new LoiNghiepVu("Bạn cần mua và nhận hàng thành công mới có thể đánh giá sản phẩm này");
        }
        // Cột reviews.comment giới hạn NVARCHAR(2000). Không chặn ở đây thì bình
        // luận dài hơn sẽ đi thẳng xuống SQL Server và ném lỗi 500 vào mặt khách.
        if (comment != null && comment.length() > 2000) {
            throw new LoiNghiepVu(
                    "Nội dung đánh giá quá dài (tối đa 2000 ký tự, bạn đang nhập " + comment.length() + ").");
        }

        Review review = reviewRepository.findByUserIdAndProductId(userId, productId).orElse(null);
        if (review == null) {
            review = new Review();
            User user = new User();
            user.setId(userId);
            review.setUser(user);
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
            review.setProduct(product);
            review.setCreatedAt(LocalDateTime.now());
        }
        review.setRating(rating);
        review.setComment(comment);
        review.setUpdatedAt(LocalDateTime.now());
        reviewRepository.save(review);
    }

    // ------------------------------------------------------------
    public static class ReviewSummary {
        public final double average;
        public final long total;
        public final Map<Integer, Long> breakdown; // sao (1-5) -> số lượng

        public ReviewSummary(double average, long total, Map<Integer, Long> breakdown) {
            this.average = average;
            this.total = total;
            this.breakdown = breakdown;
        }

        public double getAverage() { return average; }
        public long getTotal() { return total; }
        public Map<Integer, Long> getBreakdown() { return breakdown; }

        // EL (JSP) không gọi được Math.round() trực tiếp -> cần getter riêng để vẽ đủ số sao
        public long getRoundedAverage() { return Math.round(average); }

        // % chiều dài thanh phân bố cho 1 mức sao, dùng để set width thanh progress
        public double getPercent(int star) {
            if (total == 0) return 0;
            return (breakdown.getOrDefault(star, 0L) * 100.0) / total;
        }
    }
}

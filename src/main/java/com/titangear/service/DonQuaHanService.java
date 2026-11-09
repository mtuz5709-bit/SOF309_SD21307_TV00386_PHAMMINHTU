package com.titangear.service;

import com.titangear.constant.OrderStatus;
import com.titangear.model.Order;
import com.titangear.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

// Tự hủy đơn chuyển khoản quá hạn thanh toán.
//
// VẤN ĐỀ TRƯỚC ĐÂY: trang QR có đồng hồ đếm ngược 15 phút, hết giờ thì TRÌNH DUYỆT
// gọi /checkout/expired để hủy đơn. Nhưng khách ĐÓNG TRÌNH DUYỆT là không còn ai
// gọi cả — đơn nằm mãi ở "Chờ xác nhận". Hai hậu quả:
//
//   1. Đơn rác dồn trong trang quản trị, admin không biết cái nào thật cái nào bỏ
//   2. NẶNG HƠN: đơn PENDING vẫn ĐANG GIỮ CHỖ tồn kho (cột "đang giữ" trừ vào số
//      bán được), nên hàng đó không bán được cho ai khác. Shop còn hàng trong kho
//      mà web báo hết.
//
// Cách làm: một tác vụ chạy nền mỗi 5 phút, quét đơn VNPAY chưa trả tiền quá 15
// phút rồi hủy. Mốc 15 phút lấy đúng bằng đồng hồ trên trang QR để khách thấy
// cùng một con số ở cả hai nơi.
@Service
public class DonQuaHanService {

    private static final Logger log = LoggerFactory.getLogger(DonQuaHanService.class);

    // Khớp với đồng hồ đếm ngược ở payment-mbbank.jsp — sửa thì phải sửa cả hai.
    private static final int HAN_PHUT = 15;

    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderService    orderService;

    // Chạy mỗi 5 phút. Không chạy dày hơn vì đây là việc dọn dẹp, chậm vài phút
    // không sao; quét liên tục chỉ tốn truy vấn vô ích.
    //
    // initialDelay: chờ 1 phút sau khi khởi động rồi mới chạy lần đầu, để ứng dụng
    // nạp xong hẳn — tránh đụng lúc Hibernate còn đang dựng.
    @Scheduled(fixedDelay = 5 * 60 * 1000, initialDelay = 60 * 1000)
    public void huyDonQuaHan() {
        LocalDateTime moc = LocalDateTime.now().minusMinutes(HAN_PHUT);
        List<Order> quaHan = orderRepository.findDonChuyenKhoanQuaHan(moc);
        if (quaHan.isEmpty()) return;

        int daHuy = 0;
        for (Order o : quaHan) {
            try {
                // Đi qua updateStatus để dùng chung mọi luật: kiểm luồng trạng thái
                // hợp lệ và hoàn kho (đơn PENDING chưa xuất kho nên hoàn kho tự
                // không làm gì — an toàn).
                orderService.updateStatus(o.getId(), OrderStatus.CANCELLED);
                daHuy++;
            } catch (RuntimeException e) {
                // Một đơn lỗi KHÔNG được làm chết cả vòng quét — đơn còn lại vẫn phải dọn
                log.error("Không hủy được đơn quá hạn #{}", o.getId(), e);
            }
        }
        log.info("Đã tự hủy {} đơn chuyển khoản quá hạn {} phút", daHuy, HAN_PHUT);
    }
}

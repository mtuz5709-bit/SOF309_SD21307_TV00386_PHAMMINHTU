package com.titangear.service;

import java.math.BigDecimal;

// Cách tính phí vận chuyển.
//
// VÌ SAO TÁCH THÀNH GIAO DIỆN (interface) THAY VÌ VIẾT THẲNG MỘT HÀM:
// hiện tại phí tính theo tỉnh (bản {@link PhiTheoTinh}), nhưng dự định sau này
// gọi API của đơn vị vận chuyển thật để lấy giá chính xác. Tách sẵn thì lúc đó
// chỉ cần viết thêm một bản cài đặt mới, KHÔNG phải sửa trang đặt hàng hay
// chỗ ghi đơn — những nơi này chỉ biết tới giao diện này, không biết bên dưới
// tính bằng cách nào.
//
// Đây cũng là chỗ đặt "đường lui": bản gọi API sẽ tự rơi về bản tính theo tỉnh
// khi mạng hỏng hoặc API quá hạn trả lời, để khách vẫn đặt hàng được.
public interface TinhPhiVanChuyen {

    // @param tienHang  tổng tiền hàng SAU khi đã trừ giảm giá (combo, flash sale, voucher)
    // @param tinhKhach tỉnh/thành khách chọn khi đặt hàng
    // @return phí vận chuyển, 0 nếu được miễn
    BigDecimal tinhPhi(BigDecimal tienHang, String tinhKhach);

    // Mô tả ngắn cách tính — hiện cho khách xem ở trang đặt hàng.
    String moTa(String tinhKhach);
}

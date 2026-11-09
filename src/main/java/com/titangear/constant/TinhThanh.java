package com.titangear.constant;

import java.util.List;

// Danh sách 34 tỉnh/thành phố Việt Nam.
//
// VÌ SAO LÀ 34 CHỨ KHÔNG PHẢI 63: Nghị quyết 202/2025/QH15 (12/6/2025) sắp xếp
// lại đơn vị hành chính cấp tỉnh, từ 63 xuống còn 34 (28 tỉnh + 6 thành phố
// trực thuộc Trung ương), hiệu lực từ 01/07/2025.
//
// VÌ SAO ĐỂ TRONG CODE MÀ KHÔNG LÀM BẢNG RIÊNG: danh sách này gần như không đổi
// (mấy chục năm mới sắp xếp một lần), không có quan hệ với bảng nào khác, và
// không ai cần thêm/sửa/xoá lúc chạy. Làm thành bảng thì phải thêm màn hình quản
// trị cho một thứ chẳng bao giờ động tới.
//
// Khi nào NÊN chuyển thành bảng: lúc nối API đơn vị vận chuyển thật (GHN/GHTK),
// vì khi đó phải lưu kèm MÃ TỈNH của họ và cả cấp quận/phường.
public class TinhThanh {

    public static final List<String> DANH_SACH = List.of(
            "An Giang", "Bắc Ninh", "Cà Mau", "Cao Bằng", "Đắk Lắk", "Điện Biên",
            "Đồng Nai", "Đồng Tháp", "Gia Lai", "Hà Tĩnh", "Hưng Yên", "Khánh Hòa",
            "Lai Châu", "Lâm Đồng", "Lạng Sơn", "Lào Cai", "Nghệ An", "Ninh Bình",
            "Phú Thọ", "Quảng Ngãi", "Quảng Ninh", "Quảng Trị", "Sơn La", "Tây Ninh",
            "Thái Nguyên", "Thanh Hóa", "TP. Cần Thơ", "TP. Đà Nẵng", "TP. Hà Nội",
            "TP. Hải Phòng", "TP. Hồ Chí Minh", "TP. Huế", "Tuyên Quang", "Vĩnh Long"
    );

    // Tỉnh khách gửi lên có nằm trong danh sách không.
    //
    // PHẢI KIỂM: ô chọn ở trang đặt hàng chỉ là giao diện, người gửi thẳng request
    // điền được chuỗi bất kỳ. Không kiểm thì lưu được tỉnh rác vào đơn, và báo cáo
    // doanh thu theo khu vực sau này sẽ có những dòng vô nghĩa.
    public static boolean hopLe(String tinh) {
        return tinh != null && DANH_SACH.contains(tinh.trim());
    }

    private TinhThanh() { }   // lớp hằng số, không tạo đối tượng
}

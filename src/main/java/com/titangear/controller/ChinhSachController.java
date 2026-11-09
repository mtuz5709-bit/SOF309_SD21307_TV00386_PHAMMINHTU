package com.titangear.controller;

import com.titangear.service.SystemSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

// Ba trang pháp lý: Điều khoản sử dụng · Chính sách bảo mật · Chính sách bảo hành.
//
// VÌ SAO CẦN: web đang thu thập họ tên, số điện thoại, địa chỉ của khách mà không
// có bất kỳ cam kết nào về việc dùng dữ liệu đó — Nghị định 13/2023/NĐ-CP về bảo
// vệ dữ liệu cá nhân yêu cầu phải công bố. Ngoài ra khách mua hàng online cũng cần
// biết luật đổi trả, bảo hành, hủy đơn trước khi trả tiền.
//
// Ba trang dùng CHUNG một JSP, phân biệt bằng biến "trang" — nội dung khác nhau
// nhưng bố cục, thanh chuyển trang và phần liên hệ giống hệt. Tách 3 file JSP là
// lặp lại cùng một khung ba lần, sửa bố cục phải sửa cả ba.
//
// Thông tin cửa hàng lấy từ Cài đặt hệ thống, không viết cứng: đổi địa chỉ hay số
// điện thoại thì sửa một chỗ, cả ba trang cùng đổi.
@Controller
public class ChinhSachController {

    @Autowired private SystemSettingService systemSettingService;

    @GetMapping("/dieu-khoan")
    public String dieuKhoan(Model model) {
        return dungTrang(model, "dieu-khoan", "Điều khoản sử dụng");
    }

    @GetMapping("/chinh-sach-bao-mat")
    public String baoMat(Model model) {
        return dungTrang(model, "bao-mat", "Chính sách bảo mật");
    }

    @GetMapping("/chinh-sach-bao-hanh")
    public String baoHanh(Model model) {
        return dungTrang(model, "bao-hanh", "Chính sách bảo hành");
    }

    private String dungTrang(Model model, String trang, String tieuDe) {
        model.addAttribute("trang", trang);
        model.addAttribute("tieuDe", tieuDe);
        model.addAttribute("ngayApDung", "01/08/2026");
        model.addAttribute("diaChiShop",
                systemSettingService.getValue("SHOP_ADDRESS", "42 Nguyễn Văn Tiết, Thủ Dầu Một, TP. Hồ Chí Minh"));
        model.addAttribute("sdt",   "0900 000 011");
        model.addAttribute("email", "support@titangear.vn");
        return "customer/chinh-sach";
    }
}

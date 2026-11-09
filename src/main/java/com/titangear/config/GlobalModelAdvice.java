package com.titangear.config;

import com.titangear.model.FlashSale;
import com.titangear.model.User;
import com.titangear.repository.OrderRepository;
import com.titangear.repository.ProductRepository;
import com.titangear.service.CartService;
import com.titangear.service.SystemSettingService;
import com.titangear.service.FlashSaleService;
import com.titangear.service.WarrantyService;
import com.titangear.service.UserService;
import com.titangear.service.WarehousePermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Nạp sẵn các dữ liệu mà header và footer cần vào model của MỌI request.
//  chạy trước mọi controller, nên đặt ở đây thì trang nào cũng có,
// khỏi phải lặp lại ở từng controller.
@ControllerAdvice
public class GlobalModelAdvice {

    @Autowired private FlashSaleService flashSaleService;
    @Autowired private SystemSettingService systemSettingService;
    @Autowired private UserService userService;
    @Autowired private WarehousePermissionService warehousePermissionService;
    @Autowired private CartService cartService;
    @Autowired private WarrantyService warrantyService;
    @Autowired private OrderRepository orderRepository;
    @Autowired private ProductRepository productRepository;

    @ModelAttribute("activeFlashTextBars")
    public List<FlashSale> activeFlashTextBars() {
        return flashSaleService.getActiveTextBars();
    }

    // Địa chỉ cửa hàng hiện ở chân trang. Lấy từ Cài đặt hệ thống để sửa một chỗ
    // là đổi khắp mọi trang.
    @ModelAttribute("diaChiShop")
    public String diaChiShop() {
        return systemSettingService.getValue("SHOP_ADDRESS",
                "42 Nguyễn Văn Tiết, Thủ Dầu Một, TP. Hồ Chí Minh");
    }

    // Màu nền ô avatar, dùng chung một màu xanh thương hiệu cho mọi người.
    private static final String MAU_AVATAR = "#1B49FF";

    // Gom thông tin người đang đăng nhập cho ô avatar ở header:
    //   ten    - họ tên đầy đủ
    //   chuCai - chữ cái đầu của tên, "Trần Minh Tú" cho ra "T"
    //   mau    - màu nền ô avatar
    // Trả null nếu chưa đăng nhập, JSP tự hiện icon mặc định.
    @ModelAttribute("me")
    public Map<String, String> me(Principal principal) {
        if (principal == null) return null;
        User u = userService.findByEmail(principal.getName()).orElse(null);
        if (u == null) return null;

        String ten = (u.getFullName() != null && !u.getFullName().isBlank())
                ? u.getFullName().trim()
                : u.getEmail();

        // Lấy chữ cái đầu của TỪ CUỐI (người Việt gọi nhau bằng tên)
        String[] tu = ten.split("\\s+");
        String chuCai = tu[tu.length - 1].substring(0, 1).toUpperCase();

        Map<String, String> me = new HashMap<>();
        me.put("ten", ten);
        me.put("chuCai", chuCai);
        me.put("mau", MAU_AVATAR);
        return me;
    }

    // Đếm số món trong giỏ để hiện trên icon giỏ hàng ở header. Chưa đăng nhập thì
    // trả 0 để JSP ẩn con số đi.
    @ModelAttribute("soGioHang")
    public int soGioHang(Principal principal) {
        if (principal == null) return 0;
        return userService.findByEmail(principal.getName())
                .map(u -> cartService.countItems(u.getId()))
                .orElse(0);
    }

    // Đếm các con số cảnh báo trên menu quản trị: đơn chờ xử lý, phiếu bảo hành
    // sắp hết hạn, hàng sắp hết. Chỉ chạy cho ADMIN và WAREHOUSE; khách vào trang
    // bán hàng thì trả map rỗng ngay, không tốn truy vấn nào.
    @ModelAttribute("canhBao")
    public Map<String, Long> canhBao(Principal principal) {
        Map<String, Long> so = new HashMap<>();
        if (principal == null) return so;

        String role = userService.findByEmail(principal.getName()).map(User::getRole).orElse(null);
        if (!"ADMIN".equals(role) && !"WAREHOUSE".equals(role)) return so;

        so.put("donHang", orderRepository.demDonChoXuLy());       // chờ xác nhận + chờ hàng
        so.put("baoHanh", warrantyService.demSapHetHan());        // còn ≤30 ngày là hết hạn
        so.put("khoHang", productRepository.demSapHetHang());     // tồn ≤5, cần nhập thêm
        return so;
    }

    // Lấy các cờ phân quyền của thủ kho để ẩn/hiện link trong menu. Gom một lượt
    // tra tài khoản thay vì tra riêng cho từng quyền. Admin luôn có đủ quyền.
    @ModelAttribute("warehousePerms")
    public Map<String, Boolean> warehousePerms(Principal principal) {
        Map<String, Boolean> perms = new HashMap<>();
        String role = (principal != null)
                ? userService.findByEmail(principal.getName()).map(User::getRole).orElse(null)
                : null;

        perms.put("viewProducts",  warehousePermissionService.hasPermission(role, WarehousePermissionService.CAN_VIEW_PRODUCTS));
        perms.put("viewRevenue",   warehousePermissionService.hasPermission(role, WarehousePermissionService.CAN_VIEW_REVENUE));
        perms.put("manageOrders",  warehousePermissionService.hasPermission(role, WarehousePermissionService.CAN_MANAGE_ORDERS));
        perms.put("isAdmin",       "ADMIN".equals(role));
        return perms;
    }
}

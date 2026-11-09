package com.titangear.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ErrorController {

    // Dùng @RequestMapping (nhận MỌI method) chứ KHÔNG phải @GetMapping.
    //
    // Spring Security khai accessDeniedPage("/403"), và nó FORWARD tới đường dẫn
    // này chứ không redirect — forward giữ nguyên method của request gốc. Nên khi
    // một request POST bị từ chối (token CSRF hết hạn, hoặc không đủ quyền), cái
    // forward đó cũng là POST. Nếu ở đây chỉ nhận GET thì Spring trả 405 Method
    // Not Allowed, và người dùng thấy trang "400 – Yêu cầu không hợp lệ" thay vì
    // trang 403 tử tế.
    //
    // Ca gặp thật: admin mở trang quản trị rồi để đó tới khi hết phiên đăng nhập,
    // quay lại bấm nút Xóa — token trong trang đã cũ, request bị chặn, và lẽ ra
    // phải được báo "hết phiên, đăng nhập lại".
    @RequestMapping("/403")
    public String accessDenied() {
        return "common/403";
    }

    @RequestMapping("/404")
    public String notFound() {
        return "common/404";
    }

    @RequestMapping("/500")
    public String serverError() {
        return "common/500";
    }
}
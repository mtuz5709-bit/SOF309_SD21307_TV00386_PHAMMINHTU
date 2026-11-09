package com.titangear.controller;

import com.titangear.model.AuditLog;
import com.titangear.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

// Trang xem nhật ký thao tác — CHỈ ADMIN (SecurityConfig đã chặn /admin/** ở mức role).
//
// Cố ý KHÔNG có chức năng sửa hay xóa: nhật ký mà sửa được thì không còn giá trị
// làm bằng chứng khi cần quy trách nhiệm.
@Controller
@RequestMapping("/admin/nhat-ky")
public class AdminAuditLogController {

    private static final int MOI_TRANG = 30;

    @Autowired private AuditLogService auditLogService;

    @GetMapping
    public String xem(@RequestParam(required = false) String hanhDong,
                      @RequestParam(defaultValue = "0") int page,
                      Model model) {

        Page<AuditLog> trang = auditLogService.layTrang(hanhDong, PageRequest.of(Math.max(0, page), MOI_TRANG));

        model.addAttribute("nhatKy",      trang.getContent());
        model.addAttribute("trangHienTai", trang.getNumber());
        model.addAttribute("tongTrang",   trang.getTotalPages());
        model.addAttribute("tongDong",    trang.getTotalElements());
        model.addAttribute("hanhDongLoc", hanhDong);

        // Dãy nút lọc: khóa là mã hành động (dùng cho tham số URL), giá trị là tên
        // tiếng Việt hiện lên nút. Dùng LinkedHashMap để giữ nguyên thứ tự.
        java.util.Map<String, String> cacLoai = new java.util.LinkedHashMap<>();
        for (String ma : auditLogService.cacLoaiHanhDong()) {
            cacLoai.put(ma, AuditLogService.tenTiengViet(ma));
        }
        model.addAttribute("cacLoai", cacLoai);
        return "admin/nhat-ky";
    }
}

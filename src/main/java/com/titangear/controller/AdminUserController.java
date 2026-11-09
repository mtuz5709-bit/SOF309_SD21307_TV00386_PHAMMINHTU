package com.titangear.controller;

import com.titangear.exception.LoiNghiepVu;
import com.titangear.model.User;
import com.titangear.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    @Autowired
    private UserService userService;

    // Đúng 3 vai trò hệ thống hiểu được — xem SecurityConfig.hasRole(...).
    private static final java.util.Set<String> VAI_TRO_HOP_LE =
            java.util.Set.of("ADMIN", "WAREHOUSE", "CUSTOMER");

    // Danh sách user (+ tìm kiếm theo tên/email)
    @GetMapping("")
    public String listUsers(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("users",
                (q != null && !q.trim().isEmpty())
                        ? userService.search(q.trim())
                        : userService.getAllUsers());
        return "admin/users";
    }

    // Form tạo user mới
    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("user", new User());
        return "admin/user-form";
    }

    // Lưu user mới
    @PostMapping("/add")
    public String addUser(
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam String password,
            @RequestParam String role,
            Model model) {

        if (userService.emailExists(email)) {
            model.addAttribute("error", "Email đã tồn tại!");
            model.addAttribute("user", new User());
            return "admin/user-form";
        }

        // Chỉ nhận đúng 3 vai trò hệ thống hiểu được. Lưu thẳng chuỗi form gửi lên
        // thì sửa HTML là tạo được tài khoản role "admin" viết thường hay "ADMINN",
        // mà Spring Security so khớp hasRole() không trúng cái nào nên tài khoản đó
        // không vào được đâu và cũng không báo lỗi gì.
        if (!VAI_TRO_HOP_LE.contains(role)) {
            model.addAttribute("error", "Vai trò không hợp lệ.");
            model.addAttribute("user", new User());
            return "admin/user-form";
        }

        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPasswordHash(password);
        user.setRole(role);
        userService.createUser(user);

        return "redirect:/admin/users?created=true";
    }

    // Khóa / mở tài khoản
    @PostMapping("/{id}/toggle")
    public String toggleUser(@PathVariable Integer id, Principal principal, RedirectAttributes ra) {
        if (laChinhMinh(id, principal)) {
            ra.addFlashAttribute("loi", "Không thể tự khóa tài khoản của chính mình.");
            return "redirect:/admin/users";
        }
        userService.toggleUserStatus(id);
        return "redirect:/admin/users";
    }

    // Xóa user
    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable Integer id, Principal principal, RedirectAttributes ra) {
        if (laChinhMinh(id, principal)) {
            ra.addFlashAttribute("loi", "Không thể tự xóa tài khoản của chính mình.");
            return "redirect:/admin/users";
        }
        try {
            userService.deleteUser(id);
        } catch (LoiNghiepVu e) {
            // Tài khoản đã có đơn/đánh giá/lịch sử xuất kho thì không xóa được —
            // hiện lý do cho admin thay vì để lỗi khóa ngoại văng ra trang 500.
            ra.addFlashAttribute("loi", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    // Tự khóa/tự xóa chính mình = tự cắt quyền quản trị của mình. Bấm nhầm một
    // cái là phải vào tận SQL Server sửa tay mới đăng nhập lại được, nên chặn
    // ngay tại đây thay vì chỉ ẩn nút ngoài giao diện.
    private boolean laChinhMinh(Integer id, Principal principal) {
        if (principal == null || id == null) return false;
        return userService.findByEmail(principal.getName())
                .map(u -> id.equals(u.getId()))
                .orElse(false);
    }
}
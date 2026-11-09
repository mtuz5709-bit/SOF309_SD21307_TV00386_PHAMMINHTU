package com.titangear.controller;

import com.titangear.model.User;
import com.titangear.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private UserService userService;

    private User getCurrentUser(Authentication auth) {
        return userService.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));
    }

    // Xem profile
    // Không còn lấy danh sách orders ở đây nữa - trang /profile đã bỏ bảng đơn hàng
    // nhúng trùng lặp, chuyển hẳn qua trang /my-orders riêng (đầy đủ filter theo
    // trạng thái, không cần load 2 lần cùng 1 dữ liệu mỗi khi vào trang tài khoản).
    @GetMapping("")
    public String showProfile(Authentication auth, Model model) {
        User user = getCurrentUser(auth);
        model.addAttribute("user", user);
        return "customer/profile";
    }

    // Cập nhật thông tin
    @PostMapping("/update")
    public String updateProfile(
            @RequestParam String fullName,
            @RequestParam String phone,
            @RequestParam String address,
            Authentication auth) {

        User user = getCurrentUser(auth);
        user.setFullName(fullName);
        user.setPhone(phone);
        user.setAddress(address);
        userService.updateProfile(user);
        return "redirect:/profile?updated=true";
    }

    // Đổi mật khẩu
    @PostMapping("/change-password")
    public String changePassword(
            @RequestParam String oldPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            Authentication auth,
            Model model) {

        User user = getCurrentUser(auth);

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("user", user);
            model.addAttribute("passwordError", "Mật khẩu mới không khớp!");
            return "customer/profile";
        }

        if (newPassword.length() < 6) {
            model.addAttribute("user", user);
            model.addAttribute("passwordError", "Mật khẩu mới phải có ít nhất 6 ký tự!");
            return "customer/profile";
        }

        boolean success = userService.changePassword(user.getId(), oldPassword, newPassword);
        if (!success) {
            model.addAttribute("user", user);
            model.addAttribute("passwordError", "Mật khẩu cũ không đúng!");
            return "customer/profile";
        }

        return "redirect:/profile?passwordChanged=true";
    }
}

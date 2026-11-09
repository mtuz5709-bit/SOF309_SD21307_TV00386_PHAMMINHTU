package com.titangear.service;

import com.titangear.exception.LoiNghiepVu;
import com.titangear.model.User;
import com.titangear.repository.OrderRepository;
import com.titangear.repository.ReviewRepository;
import com.titangear.repository.StockOutRepository;
import com.titangear.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Dùng để kiểm tài khoản còn dữ liệu ràng buộc không trước khi cho xóa
    @Autowired private OrderRepository   orderRepository;
    @Autowired private ReviewRepository  reviewRepository;
    @Autowired private StockOutRepository stockOutRepository;

    // Đăng ký tài khoản mới — mặc định CUSTOMER
    public void register(User user) {
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        user.setRole("CUSTOMER");
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    // Admin tạo user với role tùy chọn
    public void createUser(User user) {
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    // Kiểm tra email đã tồn tại chưa
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    // Lấy thông tin user theo email
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Lấy thông tin user theo id
    public Optional<User> findById(Integer id) {
        return userRepository.findById(id);
    }

    // Cập nhật thông tin profile
    public void updateProfile(User user) {
        userRepository.save(user);
    }

    // Đặt lại mật khẩu KHÔNG cần mật khẩu cũ — chỉ dùng cho luồng quên mật khẩu,
    // sau khi đã xác thực bằng mã OTP gửi về đúng email của chủ tài khoản.
    public boolean datLaiMatKhau(String email, String matKhauMoi) {
        return userRepository.findByEmail(email).map(u -> {
            u.setPasswordHash(passwordEncoder.encode(matKhauMoi));
            userRepository.save(u);
            return true;
        }).orElse(false);
    }

    // Đổi mật khẩu
    public boolean changePassword(Integer userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return false;
        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            return false;
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return true;
    }

    // Khóa / mở tài khoản
    public void toggleUserStatus(Integer userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setIsActive(!user.getIsActive());
            userRepository.save(user);
        });
    }

    // Xóa tài khoản, chỉ cho xóa khi tài khoản chưa phát sinh dữ liệu gì: chưa có đơn,
    // chưa viết đánh giá, chưa từng xuất kho, và không phải admin cuối cùng.
    // Năm bảng đang trỏ tới users nên xóa tài khoản đã mua hàng là mất luôn lịch sử
    // mua bán. Muốn chặn một người thì khóa tài khoản bằng toggleUserStatus.
    public void deleteUser(Integer userId) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new LoiNghiepVu("Không tìm thấy tài khoản này."));

        int soDon = orderRepository.findByUserId(userId).size();
        if (soDon > 0) {
            throw new LoiNghiepVu("Tài khoản \"" + u.getEmail() + "\" đã có " + soDon
                    + " đơn hàng nên không xóa được — xóa là mất luôn lịch sử mua bán. "
                    + "Hãy dùng nút Khóa để chặn đăng nhập.");
        }
        if (reviewRepository.countByUserId(userId) > 0) {
            throw new LoiNghiepVu("Tài khoản \"" + u.getEmail() + "\" đã viết đánh giá sản phẩm "
                    + "nên không xóa được. Hãy dùng nút Khóa để chặn đăng nhập.");
        }
        // Nhân viên kho đã từng xuất hàng: stock_out.exported_by trỏ tới đây,
        // xóa là mất dấu ai xuất lô hàng nào.
        if (!"CUSTOMER".equals(u.getRole()) && stockOutRepository.countByExportedById(userId) > 0) {
            throw new LoiNghiepVu("Tài khoản \"" + u.getEmail() + "\" đã từng xuất kho "
                    + "nên không xóa được — sẽ mất dấu người xuất hàng. Hãy dùng nút Khóa.");
        }
        // Xóa mất admin cuối cùng là không còn ai vào được trang quản trị nữa,
        // phải sửa tay trong SQL Server mới cứu được.
        if ("ADMIN".equals(u.getRole()) && userRepository.findByRole("ADMIN").size() <= 1) {
            throw new LoiNghiepVu("Đây là tài khoản ADMIN duy nhất — xóa xong sẽ không ai "
                    + "vào được trang quản trị. Hãy tạo admin khác trước.");
        }

        userRepository.deleteById(userId);
    }

    // Lấy danh sách tất cả user
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Lấy danh sách user theo role
    public List<User> getUsersByRole(String role) {
        return userRepository.findByRole(role);
    }

    // Tìm kiếm admin: theo tên hoặc email
    public List<User> search(String keyword) {
        return userRepository.findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(keyword, keyword);
    }
}
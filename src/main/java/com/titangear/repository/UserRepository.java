package com.titangear.repository;

import com.titangear.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    // Tìm user theo email — dùng cho đăng nhập
    Optional<User> findByEmail(String email);

    // Kiểm tra email đã tồn tại chưa — dùng khi đăng ký
    boolean existsByEmail(String email);

    // Tìm user theo role — dùng cho admin quản lý user
    java.util.List<User> findByRole(String role);

    // Tìm kiếm admin: theo tên hoặc email (không phân biệt hoa thường)
    java.util.List<User> findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String fullName, String email);
}
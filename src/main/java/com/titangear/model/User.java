package com.titangear.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "full_name", nullable = false)
    @jakarta.validation.constraints.Size(max = 255, message = "Họ tên tối đa 255 ký tự")
    private String fullName;

    @Column(name = "email", nullable = false, unique = true)
    @jakarta.validation.constraints.Size(max = 255, message = "Email tối đa 255 ký tự")
    private String email;

    @Column(name = "phone")
    @jakarta.validation.constraints.Size(max = 20, message = "Số điện thoại tối đa 20 ký tự")
    private String phone;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "role")
    private String role; // ADMIN, CUSTOMER, WAREHOUSE

    @Column(name = "address")
    @jakarta.validation.constraints.Size(max = 500, message = "Địa chỉ tối đa 500 ký tự")
    private String address;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
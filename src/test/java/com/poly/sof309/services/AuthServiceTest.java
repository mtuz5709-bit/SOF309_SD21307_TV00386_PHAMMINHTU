package com.poly.sof309.services;

import com.titangear.exception.LoiNghiepVu;
import com.titangear.model.User;
import com.titangear.repository.UserRepository;
import com.titangear.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;

/**
 * Unit Test kiểm thử chức năng Đăng ký & Đăng nhập (UserService)
 * Chia thành 2 bộ Suite theo chuẩn JUnit 5:
 * - @Tag("smoke"): Kiểm thử khói (các luồng chính phải chạy thông).
 * - @Tag("regression"): Kiểm thử hồi quy (biên, trùng lặp, sai mật khẩu, khóa tài khoản).
 */
@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Spy
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @InjectMocks
    private UserService userService;

    private List<User> dsUser;

    @BeforeEach
    public void setUp() {
        dsUser = new ArrayList<>();

        User admin = new User();
        admin.setId(1);
        admin.setFullName("Admin TiTanGEAR");
        admin.setEmail("admin@titangear.vn");
        admin.setPhone("0900000011");
        admin.setPasswordHash(passwordEncoder.encode("Admin@123"));
        admin.setRole("ADMIN");
        admin.setIsActive(true);
        dsUser.add(admin);

        User lockedUser = new User();
        lockedUser.setId(2);
        lockedUser.setFullName("Tài Khoản Khóa");
        lockedUser.setEmail("locked@titangear.vn");
        lockedUser.setPhone("0900000099");
        lockedUser.setPasswordHash(passwordEncoder.encode("Locked@123"));
        lockedUser.setRole("CUSTOMER");
        lockedUser.setIsActive(false);
        dsUser.add(lockedUser);

        // Giả lập lưu và tìm kiếm trực tiếp trên danh sách dsUser
        lenient().when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            if (u.getId() == null) {
                u.setId(dsUser.size() + 1);
                dsUser.add(u);
            }
            return u;
        });

        lenient().when(userRepository.findByEmail(anyString())).thenAnswer(inv -> {
            String email = inv.getArgument(0);
            return dsUser.stream()
                    .filter(u -> u.getEmail().equalsIgnoreCase(email))
                    .findFirst();
        });

        lenient().when(userRepository.existsByEmail(anyString())).thenAnswer(inv -> {
            String email = inv.getArgument(0);
            return dsUser.stream()
                    .anyMatch(u -> u.getEmail().equalsIgnoreCase(email));
        });
    }

    // =========================================================================
    // 1. SMOKE TEST SUITE (@Tag("smoke"))
    // =========================================================================

    @Test
    @Tag("smoke")
    @DisplayName("TC_SMOKE_REG_01: Đăng ký thành công với giá trị hợp lệ -> tạo User, lưu danh sách & đối chiếu khớp")
    public void testDangKyThanhCong() {
        User newUser = new User();
        newUser.setFullName("Phạm Minh Tu");
        newUser.setEmail("minhtu.new@titangear.vn");
        newUser.setPhone("0912345678");
        newUser.setPasswordHash("Titan@123");

        User created = userService.register(newUser);

        // 1. Khẳng định đối tượng trả về không null và đã được cấp ID
        assertNotNull(created, "User trả về sau khi đăng ký không được null");
        assertNotNull(created.getId(), "User mới tạo phải có ID");

        // 2. Lấy ngược User từ danh sách và đối chiếu từng thuộc tính
        Optional<User> found = userService.findByEmail("minhtu.new@titangear.vn");
        assertTrue(found.isPresent(), "Phải tìm thấy User vừa đăng ký trong danh sách");
        assertEquals("minhtu.new@titangear.vn", found.get().getEmail());
        assertEquals("Phạm Minh Tu", found.get().getFullName());
        assertEquals("0912345678", found.get().getPhone());
        assertEquals("CUSTOMER", found.get().getRole());
        assertTrue(found.get().getIsActive());
        assertTrue(passwordEncoder.matches("Titan@123", found.get().getPasswordHash()));
    }

    @Test
    @Tag("smoke")
    @DisplayName("TC_SMOKE_LOGIN_01: Đăng nhập thành công với admin@titangear.vn / Admin@123")
    public void testDangNhapThanhCong() {
        User loggedIn = userService.login("admin@titangear.vn", "Admin@123");

        assertNotNull(loggedIn, "User đăng nhập thành công không được null");
        assertEquals("admin@titangear.vn", loggedIn.getEmail());
        assertEquals("Admin TiTanGEAR", loggedIn.getFullName());
        assertEquals("ADMIN", loggedIn.getRole());
        assertTrue(loggedIn.getIsActive());
    }

    @Test
    @Tag("smoke")
    @DisplayName("TC_SMOKE_FLOW_01: Đăng ký tài khoản mới xong -> get kiểm tra -> đăng nhập bằng tài khoản đó")
    public void testDangKyRoiDangNhapNgay() {
        User newUser = new User();
        newUser.setFullName("Nguyễn Văn An");
        newUser.setEmail("vanan@titangear.vn");
        newUser.setPhone("0987654321");
        newUser.setPasswordHash("Gear@2026");

        User created = userService.register(newUser);
        assertNotNull(created);

        Optional<User> found = userService.findByEmail("vanan@titangear.vn");
        assertTrue(found.isPresent());

        User loggedIn = userService.login("vanan@titangear.vn", "Gear@2026");
        assertNotNull(loggedIn);
        assertEquals(created.getId(), loggedIn.getId());
        assertEquals("vanan@titangear.vn", loggedIn.getEmail());
    }

    // =========================================================================
    // 2. REGRESSION TEST SUITE (@Tag("regression"))
    // =========================================================================

    @Test
    @Tag("regression")
    @DisplayName("TC_REG_DUP_01: Đăng ký thất bại khi trùng email admin@titangear.vn")
    public void testDangKyTrungEmail() {
        User dupUser = new User();
        dupUser.setFullName("Trùng Email");
        dupUser.setEmail("admin@titangear.vn");
        dupUser.setPhone("0912345678");
        dupUser.setPasswordHash("Titan@123");

        LoiNghiepVu ex = assertThrows(LoiNghiepVu.class, () -> userService.register(dupUser));
        assertEquals("Email đã được sử dụng!", ex.getMessage());
        assertEquals(2, dsUser.size(), "Danh sách User không được thêm mới khi bị lỗi");
    }

    @ParameterizedTest
    @Tag("regression")
    @DisplayName("TC_REG_VAL_01: Kiểm tra các giá trị biên đăng ký -> bắt đúng từng chữ thông báo lỗi")
    @CsvSource({
            "'', 'hople@titangear.vn', '0912345678', 'Titan@123', 'Vui lòng nhập họ và tên!'",
            "'A', 'hople@titangear.vn', '0912345678', 'Titan@123', 'Họ tên phải có ít nhất 2 ký tự!'",
            "'Phạm Minh Tu', 'khong_co_acong.vn', '0912345678', 'Titan@123', 'Email không hợp lệ!'",
            "'Phạm Minh Tu', 'hople@titangear.vn', '091234567', 'Titan@123', 'Số điện thoại phải đúng 10 chữ số!'",
            "'Phạm Minh Tu', 'hople@titangear.vn', '0912345678', '12345', 'Mật khẩu phải có ít nhất 6 ký tự!'"
    })
    public void testDangKyDuLieuBienKhongHopLe(
            String fullName, String email, String phone, String password, String expectedMsg) {
        User invalidUser = new User();
        invalidUser.setFullName(fullName);
        invalidUser.setEmail(email);
        invalidUser.setPhone(phone);
        invalidUser.setPasswordHash(password);

        LoiNghiepVu ex = assertThrows(LoiNghiepVu.class, () -> userService.register(invalidUser));
        assertEquals(expectedMsg, ex.getMessage());
    }

    @Test
    @Tag("regression")
    @DisplayName("TC_LOGIN_FAIL_01: Đăng nhập thất bại khi sai mật khẩu (SaiMatKhau@999)")
    public void testDangNhapSaiMatKhau() {
        LoiNghiepVu ex = assertThrows(LoiNghiepVu.class,
                () -> userService.login("admin@titangear.vn", "SaiMatKhau@999"));
        assertEquals("Email hoặc mật khẩu không đúng!", ex.getMessage());
    }

    @Test
    @Tag("regression")
    @DisplayName("TC_LOGIN_FAIL_02: Đăng nhập thất bại khi tài khoản locked@titangear.vn bị khóa")
    public void testDangNhapTaiKhoanBiKhoa() {
        LoiNghiepVu ex = assertThrows(LoiNghiepVu.class,
                () -> userService.login("locked@titangear.vn", "Locked@123"));
        assertEquals("Tài khoản của bạn đã bị khóa!", ex.getMessage());
    }

    @ParameterizedTest
    @Tag("regression")
    @DisplayName("TC_LOGIN_FAIL_03: Đăng nhập thất bại khi bỏ trống email hoặc mật khẩu")
    @CsvSource({
            "'', 'Admin@123', 'Vui lòng nhập email!'",
            "'admin@titangear.vn', '', 'Vui lòng nhập mật khẩu!'"
    })
    public void testDangNhapBoTrongTruong(String email, String password, String expectedMsg) {
        LoiNghiepVu ex = assertThrows(LoiNghiepVu.class, () -> userService.login(email, password));
        assertEquals(expectedMsg, ex.getMessage());
    }
}

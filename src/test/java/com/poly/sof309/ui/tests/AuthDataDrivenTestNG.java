package com.poly.sof309.ui.tests;

import com.poly.sof309.ui.listeners.TestListener;
import com.titangear.exception.LoiNghiepVu;
import com.titangear.model.User;
import com.titangear.repository.UserRepository;
import com.titangear.service.UserService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;

/**
 * [Y4 - Tuần 4] TestNG Framework chuyên nghiệp cho luồng Đăng ký & Đăng nhập Back-End:
 * - @DataProvider test lặp nhiều tài khoản với giá trị cụ thể.
 * - Phân nhóm groups = "smoke" và groups = "regression".
 * - Thiết lập ràng buộc phụ thuộc dependsOnMethods.
 */
@Listeners(TestListener.class)
public class AuthDataDrivenTestNG {

    @Mock
    private UserRepository userRepository;

    @Spy
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @InjectMocks
    private UserService userService;

    private AutoCloseable mocks;
    private List<User> dsUser;

    @BeforeClass(alwaysRun = true)
    public void setUpClass() {
        mocks = MockitoAnnotations.openMocks(this);
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

    @AfterClass(alwaysRun = true)
    public void tearDownClass() throws Exception {
        if (mocks != null) {
            mocks.close();
        }
    }

    @DataProvider(name = "loginAccounts")
    public Object[][] getLoginAccounts() {
        return new Object[][]{
                {"admin@titangear.vn", "Admin@123", true, "ADMIN"},
                {"admin@titangear.vn", "SaiMatKhau@999", false, "Email hoặc mật khẩu không đúng!"},
                {"locked@titangear.vn", "Locked@123", false, "Tài khoản của bạn đã bị khóa!"},
                {"khongtontai@titangear.vn", "123456", false, "Email hoặc mật khẩu không đúng!"}
        };
    }

    @Test(groups = {"smoke"}, dataProvider = "loginAccounts",
            description = "Data-Driven TestNG: kiểm thử lặp nhiều bộ tài khoản đăng nhập")
    public void testDangNhapNhieuTaiKhoan(
            String email, String rawPassword, boolean expectSuccess, String expectedDetail) {
        if (expectSuccess) {
            User loggedIn = userService.login(email, rawPassword);
            Assert.assertNotNull(loggedIn, "User đăng nhập thành công không được null");
            Assert.assertEquals(loggedIn.getEmail(), email);
            Assert.assertEquals(loggedIn.getRole(), expectedDetail);
        } else {
            LoiNghiepVu ex = Assert.expectThrows(LoiNghiepVu.class,
                    () -> userService.login(email, rawPassword));
            Assert.assertEquals(ex.getMessage(), expectedDetail);
        }
    }

    @Test(groups = {"smoke"},
            description = "Đăng ký tài khoản mới hợp lệ -> lưu vào danh sách và lấy ngược đối chiếu")
    public void testDangKyTaiKhoanMoi() {
        User newUser = new User();
        newUser.setFullName("Nguyễn Văn An");
        newUser.setEmail("vanan.testng@titangear.vn");
        newUser.setPhone("0987654321");
        newUser.setPasswordHash("Gear@2026");

        User created = userService.register(newUser);
        Assert.assertNotNull(created, "User trả về sau đăng ký không được null");
        Assert.assertNotNull(created.getId(), "User mới phải có ID");

        Optional<User> found = userService.findByEmail("vanan.testng@titangear.vn");
        Assert.assertTrue(found.isPresent(), "Phải lấy lại được User vừa đăng ký từ danh sách");
        Assert.assertEquals(found.get().getFullName(), "Nguyễn Văn An");
        Assert.assertEquals(found.get().getRole(), "CUSTOMER");
    }

    @Test(groups = {"regression"}, dependsOnMethods = {"testDangKyTaiKhoanMoi"},
            description = "dependsOnMethods: Chỉ chạy đăng nhập sau khi testDangKyTaiKhoanMoi thành công")
    public void testDangNhapTaiKhoanVuaDangKy() {
        User loggedIn = userService.login("vanan.testng@titangear.vn", "Gear@2026");

        Assert.assertNotNull(loggedIn);
        Assert.assertEquals(loggedIn.getEmail(), "vanan.testng@titangear.vn");
        Assert.assertEquals(loggedIn.getFullName(), "Nguyễn Văn An");
    }

    @Test(groups = {"regression"}, dependsOnMethods = {"testDangKyTaiKhoanMoi"},
            description = "dependsOnMethods: Đăng ký lại đúng email vừa tạo phải bị chặn trùng lặp")
    public void testDangKyTrungEmailBiChan() {
        User dupUser = new User();
        dupUser.setFullName("Nguyễn Văn An Trùng");
        dupUser.setEmail("vanan.testng@titangear.vn");
        dupUser.setPhone("0987654321");
        dupUser.setPasswordHash("Gear@2026");

        LoiNghiepVu ex = Assert.expectThrows(LoiNghiepVu.class,
                () -> userService.register(dupUser));
        Assert.assertEquals(ex.getMessage(), "Email đã được sử dụng!");
    }
}

# HỒ SƠ ĐẶC TẢ KỊCH BẢN KIỂM THỬ (TEST CASES)
### Dự án: TiTanGEAR E-Commerce System
> **Sinh viên thực hiện:** Phạm Minh Tu – **MSSV:** TV00386 – **Lớp:** SD21307

---

## 0. CHIẾN LƯỢC TEST TỪNG THÀNH PHẦN & CÁCH ĐẶC TẢ VALUE CỤ THỂ

1. **Hai mục tiêu của bộ Test Suite:**
   * **1. Smoke Test Suite (`@Tag("smoke")`) – Kiểm thử khói:** Chạy nhanh các chức năng sống còn (Đăng ký hợp lệ, Đăng nhập hợp lệ, luồng Đăng ký xong $\rightarrow$ Đăng nhập, CRUD sản phẩm, Thêm vào giỏ) để xác nhận hệ thống chạy thông suốt.
   * **2. Regression Test Suite (`@Tag("regression")`) – Kiểm thử hồi quy:** Quét toàn bộ dữ liệu biên (`@CsvSource`), trùng email, bỏ trống trường (`"Vui lòng nhập họ và tên!"`), tài khoản bị khóa, giá bán âm, vượt tồn kho để đảm bảo khi sửa code không làm hỏng logic cũ.
2. **Thứ tự bắt đầu kiểm thử từng thành phần (Bottom-Up):**
   * **Bước 1 – Tầng Service (`AuthServiceTest.java`):** Test logic cốt lõi của `UserService.register(user)` và `UserService.login(email, password)` trên danh sách dữ liệu giả lập (`dsUser`).
   * **Bước 2 – Tầng Controller (`AuthControllerTest.java`, `ProductControllerTest.java`):** Test điều hướng, validate form và xử lý ngoại lệ ở tầng điều khiển.
   * **Bước 3 – Tầng UI (`LoginTest.java`):** Test giao diện trình duyệt thật bằng Selenium POM.
3. **Nguyên tắc kiểm chứng Đăng ký & Đăng nhập bằng Value cụ thể:**
   * Khi gọi `User created = userService.register(newUser)` với đầy đủ value cụ thể: nếu có lỗi thì ném `LoiNghiepVu`, nếu hợp lệ thì lưu vào danh sách `dsUser` và **trả về `User` vừa tạo**.
   * **3 bước khẳng định thành công:** `assertNotNull(created)` (không null) $\rightarrow$ gọi `userService.findByEmail(...)` lấy ngược từ danh sách ra $\rightarrow$ `assertEquals` đối chiếu khớp từng thuộc tính (`fullName`, `email`, `phone`, `role`, `isActive`) và đăng nhập thử bằng chính tài khoản đó.

---

## 1. SMOKE TEST SUITE (KIỂM THỬ KHÓI – `@Tag("smoke")`)

| Test Case ID | Thành phần / Class | Mô tả ca kiểm thử | Dữ liệu đầu vào cụ thể (Test Data - Value) | Các bước & Kiểm chứng (Assertions) | Kết quả mong đợi | Trạng thái |
| :---: | :--- | :--- | :--- | :--- | :--- | :---: |
| **TC_SMOKE_REG_01** | `AuthServiceTest` | Đăng ký thành công với thông tin hợp lệ | `fullName = "Phạm Minh Tu"`<br>`email = "minhtu.new@titangear.vn"`<br>`phone = "0912345678"`<br>`passwordHash = "Titan@123"` | 1. Khởi tạo `User` với value trên<br>2. Gọi `created = userService.register(newUser)`<br>3. `assertNotNull(created)` & `assertNotNull(created.getId())`<br>4. Gọi `findByEmail("minhtu.new@titangear.vn")` đối chiếu khớp từng trường | Trả về `User` khác `null`, `role = "CUSTOMER"`, `isActive = true`, lấy ngược lại từ danh sách trùng khớp 100% | **PASS** |
| **TC_SMOKE_LOGIN_01** | `AuthServiceTest` | Đăng nhập thành công với tài khoản Admin | `email = "admin@titangear.vn"`<br>`rawPassword = "Admin@123"` | 1. Gọi `loggedIn = userService.login("admin@titangear.vn", "Admin@123")`<br>2. `assertNotNull(loggedIn)`<br>3. `assertEquals` kiểm tra `email`, `fullName`, `role`, `isActive` | Trả về `User` khác `null`, `fullName = "Admin TiTanGEAR"`, `role = "ADMIN"`, `isActive = true` | **PASS** |
| **TC_SMOKE_FLOW_01** | `AuthServiceTest` | Luồng liên hoàn: Đăng ký xong $\rightarrow$ lấy lại kiểm tra $\rightarrow$ đăng nhập ngay | `fullName = "Nguyễn Văn An"`<br>`email = "vanan@titangear.vn"`<br>`phone = "0987654321"`<br>`passwordHash = "Gear@2026"` | 1. Gọi `register(newUser)` $\rightarrow$ `assertNotNull(created)`<br>2. Gọi `findByEmail("vanan@titangear.vn")` $\rightarrow$ `assertTrue(isPresent)`<br>3. Gọi `login("vanan@titangear.vn", "Gear@2026")` $\rightarrow$ `assertEquals(created.getId(), loggedIn.getId())` | Tài khoản mới tạo lấy được từ danh sách và đăng nhập thành công ngay lập tức | **PASS** |
| **TC_AUTH_01** | `AuthControllerTest` | Đăng nhập quyền Admin điều hướng đúng | `role = "ROLE_ADMIN"` | 1. Gọi `authController.dashboard(auth)`<br>2. `assertEquals("redirect:/admin/dashboard", redirectUrl)` | Điều hướng về `"redirect:/admin/dashboard"` | **PASS** |
| **TC_AUTH_02** | `AuthControllerTest` | Hiển thị trang đăng nhập chuẩn | Không có | 1. Gọi `authController.login()` | Trả về view `"common/login"` | **PASS** |
| **TC_AUTH_04** | `AuthControllerTest` | Submit form đăng ký gửi mã OTP | `fullName = "Phạm Minh Tu"`, `email = "minhtu@gmail.com"`, `phone = "0912345678"`, `password = "123456"` | 1. Gọi `authController.register(...)`<br>2. `assertEquals("redirect:/verify-otp", view)` | Lưu session `pending_otp = "123456"` và chuyển sang `"/verify-otp"` | **PASS** |
| **TC_CART_03** | `AuthControllerTest` | Xác thực mã OTP 6 số hợp lệ | `otp = "123456"`, `pending_email = "minhtu@gmail.com"` | 1. Gọi `authController.verifyOtp("123456", session, model)`<br>2. `verify(userService).register(any())` | Lưu user và chuyển hướng `"/login?registered=true"` | **PASS** |
| **TC_PROD_01** | `ProductControllerTest` | Thêm mới sản phẩm hợp lệ | `id = 1`, `name = "Chuột ATK F1 Ultimate"`, `price = 1850000`, `stockQuantity = 20` | 1. Gọi `adminProductController.addProduct(...)`<br>2. `assertEquals("redirect:/admin/products", view)` | Lưu sản phẩm và chuyển hướng `"/admin/products"` | **PASS** |
| **TC_PROD_03** | `ProductControllerTest` | Lấy thông tin sản phẩm theo ID | `id = 1` | 1. Gọi `adminProductController.editForm(1, model)` | Trả về `"admin/product-form"` kèm `sampleProduct1` | **PASS** |
| **TC_PROD_04** | `ProductControllerTest` | Lấy toàn bộ danh sách sản phẩm | `page = 1` (danh sách có 2 SP mẫu) | 1. Gọi `adminProductController.listProducts(...)` | Danh sách `>= 2` bản ghi, trả về `"admin/products"` | **PASS** |
| **TC_PROD_05** | `ProductControllerTest` | Xóa sản phẩm theo ID | `id = 2` (`"Bàn phím cơ ATK RS6 Air"`) | 1. Gọi `adminProductController.deleteProduct(2)` | Gọi `deleteProduct(2)` và redirect `"/admin/products"` | **PASS** |
| **TC_CART_01** | `ProductControllerTest` | Thêm sản phẩm vào giỏ hàng hợp lệ | `userId = 1`, `productId = 1`, `quantity = 2` | 1. Gọi `cartController.addToCart(1, 2, null, false, auth)` | Thêm vào giỏ và redirect `"/products/1?added=true"` | **PASS** |

---

## 2. REGRESSION TEST SUITE (KIỂM THỬ HỒI QUY – `@Tag("regression")`)

| Test Case ID | Thành phần / Class | Mô tả ca kiểm thử | Dữ liệu đầu vào cụ thể (Test Data - Value) | Ngoại lệ & Thông điệp kỳ vọng chính xác | Trạng thái |
| :---: | :--- | :--- | :--- | :--- | :---: |
| **TC_REG_DUP_01** | `AuthServiceTest` | Đăng ký thất bại do trùng email đã tồn tại | `fullName = "Trùng Email"`, `email = "admin@titangear.vn"`, `phone = "0912345678"`, `passwordHash = "Titan@123"` | Ném `LoiNghiepVu`: `"Email đã được sử dụng!"` và danh sách `dsUser` vẫn giữ nguyên `2` bản ghi | **PASS** |
| **TC_REG_VAL_01a** | `AuthServiceTest` | Đăng ký thất bại khi bỏ trống họ và tên (`@CsvSource`) | `fullName = ""`, `email = "hople@titangear.vn"`, `phone = "0912345678"`, `password = "Titan@123"` | Ném `LoiNghiepVu`: `"Vui lòng nhập họ và tên!"` *(so khớp chính xác từng ký tự để bắt lỗi chính tả)* | **PASS** |
| **TC_REG_VAL_01b** | `AuthServiceTest` | Đăng ký thất bại khi họ tên < 2 ký tự (`@CsvSource`) | `fullName = "A"`, `email = "hople@titangear.vn"`, `phone = "0912345678"`, `password = "Titan@123"` | Ném `LoiNghiepVu`: `"Họ tên phải có ít nhất 2 ký tự!"` | **PASS** |
| **TC_REG_VAL_01c** | `AuthServiceTest` | Đăng ký thất bại khi email sai định dạng (`@CsvSource`) | `fullName = "Phạm Minh Tu"`, `email = "khong_co_acong.vn"`, `phone = "0912345678"`, `password = "Titan@123"` | Ném `LoiNghiepVu`: `"Email không hợp lệ!"` | **PASS** |
| **TC_REG_VAL_01d** | `AuthServiceTest` | Đăng ký thất bại khi SĐT không đủ 10 số (`@CsvSource`) | `fullName = "Phạm Minh Tu"`, `email = "hople@titangear.vn"`, `phone = "091234567"`, `password = "Titan@123"` | Ném `LoiNghiepVu`: `"Số điện thoại phải đúng 10 chữ số!"` | **PASS** |
| **TC_REG_VAL_01e** | `AuthServiceTest` | Đăng ký thất bại khi mật khẩu < 6 ký tự (`@CsvSource`) | `fullName = "Phạm Minh Tu"`, `email = "hople@titangear.vn"`, `phone = "0912345678"`, `password = "12345"` | Ném `LoiNghiepVu`: `"Mật khẩu phải có ít nhất 6 ký tự!"` | **PASS** |
| **TC_LOGIN_FAIL_01** | `AuthServiceTest` | Đăng nhập thất bại khi sai mật khẩu | `email = "admin@titangear.vn"`, `rawPassword = "SaiMatKhau@999"` | Ném `LoiNghiepVu`: `"Email hoặc mật khẩu không đúng!"` | **PASS** |
| **TC_LOGIN_FAIL_02** | `AuthServiceTest` | Đăng nhập thất bại với tài khoản bị khóa | `email = "locked@titangear.vn"`, `rawPassword = "Locked@123"` | Ném `LoiNghiepVu`: `"Tài khoản của bạn đã bị khóa!"` | **PASS** |
| **TC_LOGIN_FAIL_03** | `AuthServiceTest` | Đăng nhập thất bại khi bỏ trống email hoặc mật khẩu (`@CsvSource`) | 1) `email = ""`, `pass = "Admin@123"`<br>2) `email = "admin@titangear.vn"`, `pass = ""` | 1) `"Vui lòng nhập email!"`<br>2) `"Vui lòng nhập mật khẩu!"` | **PASS** |
| **TC_AUTH_03** | `AuthControllerTest` | Kiểm tra biên form đăng ký trên Controller (`@CsvSource`) | 4 bộ: email `"email_sai_dinh_dang"`, SĐT `"09123"`, pass `"123"`, tên `"A"` | Giữ lại `"customer/register"` và gán đúng từng thông báo lỗi vào `model` | **PASS** |
| **TC_AUTH_05** | `AuthControllerTest` | Controller báo lỗi trùng email | `email = "admin@titangear.vn"` | Giữ lại `"customer/register"`, `error = "Email đã được sử dụng!"` | **PASS** |
| **TC_AUTH_EX** | `AuthControllerTest` | Bắt ngoại lệ khi dịch vụ gửi OTP gặp sự cố | `email = "error@gmail.com"`, `otp = "123456"` | Ném `LoiNghiepVu`: `"Không thể gửi mã OTP tới email này"` | **PASS** |
| **TC_PROD_02** | `ProductControllerTest` | Báo lỗi khi giá sản phẩm `<= 0` | `id = 4`, `name = "Lót chuột Gaming lỗi giá"`, `price = -50000` | Ném `LoiNghiepVu`: `"Giá sản phẩm phải lớn hơn 0"` | **PASS** |
| **TC_CART_02** | `ProductControllerTest` | Thêm vào giỏ vượt quá tồn kho | `userId = 1`, `productId = 1`, `quantity = 999` | Ném `LoiNghiepVu`: `"Số lượng trong kho không đủ đáp ứng!"` | **PASS** |
| **TC_CART_04** | `ProductControllerTest` | Cập nhật số lượng giỏ hàng `<= 0` (`@CsvSource`) | `cartItemId = 10`, `quantity = {0, -1, -5, 3}` | `<= 0` gọi `removeItem(1, 10)`; `quantity = 3` gọi `updateQuantity(1, 10, 3)` | **PASS** |

---

## 3. CÁCH CHẠY TỪNG SUITE & KIỂM CHỨNG ĐỘ TIN CẬY (CÀI BUG THỬ FAIL $\rightarrow$ SỬA LẠI PASS)

### Lệnh chạy từng Suite với Maven:
* **Chạy riêng Smoke Test Suite:** `mvn test -Dgroups=smoke`
* **Chạy riêng Regression Test Suite:** `mvn test -Dgroups=regression`
* **Chạy riêng AuthServiceTest:** `mvn test -Dtest=AuthServiceTest`
* **Chạy toàn bộ Test Suite:** `mvn test`

### Minh chứng kiểm thử bắt Bug thực tế (Mutation Check):
1. **Pha 1 (Pass 100%):** Toàn bộ `AuthServiceTest`, `AuthControllerTest`, `ProductControllerTest` đều đạt **PASS (0 Failure, 0 Error)**.
2. **Pha 2 (Cố tình cài bug của Dev vào `UserService.java`):** Sửa thông báo `"Vui lòng nhập họ và tên!"` thành `"Vui lohng nhập họ và tên."` $\rightarrow$ chạy lại `AuthServiceTest` $\rightarrow$ **TC_REG_VAL_01a lập tức báo FAIL**:
   * `expected: <Vui lòng nhập họ và tên!> but was: <Vui lohng nhập họ và tên.>`
   * Giúp QA phát hiện chính xác dòng code dev viết sai chính tả/sai logic.
3. **Pha 3 (Fix bug & chạy lại):** Sửa lại thành `"Vui lòng nhập họ và tên!"` $\rightarrow$ chạy lại `mvn test` $\rightarrow$ toàn bộ đạt **100% PASS (`BUILD SUCCESS`)**.

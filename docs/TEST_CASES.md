# BỘ KỊCH BẢN KIỂM THỬ CHI TIẾT (TEST CASES)
### Dự án: TiTanGEAR E-Commerce System
> **Sinh viên thực hiện:** Phạm Minh Tu – **MSSV:** TV00386 – **Lớp:** SD21307

---

## 1. MODULE GIỎ HÀNG & OTP (CART & OTP - BÁM SÁT BÀI 1 LAB 1)

| Test Case ID | Tên ca kiểm thử | Loại test | Tiền điều kiện | Các bước thực hiện | Dữ liệu đầu vào (Test Data) | Kết quả mong đợi | Trạng thái |
| :---: | :--- | :---: | :--- | :--- | :--- | :--- | :---: |
| **TC_CART_01** | Thêm sản phẩm vào giỏ với số lượng hợp lệ | Positive | Sản phẩm còn tồn kho (>= 5) | 1. Chọn sản phẩm hợp lệ<br>2. Gọi hàm thêm vào giỏ hàng | `productId = 1`<br>`quantity = 2` | Thêm vào giỏ thành công, chuyển hướng kèm `added=true` | **PASS** |
| **TC_CART_02** | Thêm sản phẩm vượt quá số lượng tồn kho (Lab 1) | Negative | Tồn kho sản phẩm chỉ còn 5 | 1. Chọn sản phẩm<br>2. Nhập số lượng vượt tồn kho | `productId = 1`<br>`quantity = 999` | Ném `LoiNghiepVu`, thông báo số lượng trong kho không đủ | **PASS** |
| **TC_CART_03** | Xác thực mã OTP 6 số hợp lệ (Sau khi Debug) | Positive | Đang ở bước xác nhận đăng ký | 1. Nhập mã OTP hợp lệ `123456`<br>2. Bấm xác nhận | `otp = "123456"` | Xác thực thành công, lưu user vào DB và chuyển sang `/login` | **PASS** |
| **TC_CART_04** | Cập nhật số lượng sản phẩm trong giỏ <= 0 (`@CsvSource`) | Boundary | Giỏ hàng đã có sản phẩm | 1. Nhập bộ dữ liệu biên số lượng (`0`, `-1`, `-5`, `3`)<br>2. Gọi hàm cập nhật giỏ hàng | `cartItemId = 10`<br>`quantity = {0, -1, -5, 3}` | Số lượng `<= 0` tự động xóa khỏi giỏ; `> 0` cập nhật thành công | **PASS** |

---

## 2. MODULE XÁC THỰC (AUTHENTICATION - SLIDE 1)

| Test Case ID | Tên ca kiểm thử | Loại test | Tiền điều kiện | Các bước thực hiện | Dữ liệu đầu vào (Test Data) | Kết quả mong đợi | Trạng thái |
| :---: | :--- | :---: | :--- | :--- | :--- | :--- | :---: |
| **TC_AUTH_01** | Đăng nhập thành công với Admin | Positive | Tài khoản đã có trong DB | 1. Đăng nhập quyền `ROLE_ADMIN`<br>2. Điều hướng qua `/dashboard` | `user = "admin@titangear.vn"`<br>`role = "ROLE_ADMIN"` | Đăng nhập thành công, chuyển hướng `/admin/dashboard` | **PASS** |
| **TC_AUTH_02** | Hiển thị trang đăng nhập chuẩn | Positive | Truy cập `/login` | 1. Gọi `authController.login()` | Không có | Trả về đúng view `"common/login"` | **PASS** |
| **TC_AUTH_03** | Kiểm tra biên validation form đăng ký (`@CsvSource`) | Boundary | Mở form `/register` | 1. Truyền 4 bộ dữ liệu sai biên (email sai, SĐT 5 số, mật khẩu 3 ký tự, tên 1 ký tự)<br>2. Gọi `register()` | 4 bộ dữ liệu `@CsvSource` | Giữ lại trang `"customer/register"` và trả về đúng thông báo lỗi từng trường | **PASS** |
| **TC_AUTH_04** | Đăng ký người dùng mới hợp lệ | Positive | Chưa có email này trong DB | 1. Điền form đăng ký hợp lệ<br>2. Bấm đăng ký | `email = "minhtu@gmail.com"`<br>`pass = "123456"` | Sinh mã OTP 6 số lưu session, chuyển hướng `/verify-otp` | **PASS** |
| **TC_AUTH_05** | Đăng ký thất bại khi trùng email | Negative | Email đã tồn tại | 1. Nhập email đã có<br>2. Bấm đăng ký | `email = "admin@titangear.vn"` | Báo lỗi `"Email đã được sử dụng!"` | **PASS** |
| **TC_AUTH_EX** | Bắt ngoại lệ khi dịch vụ OTP gặp sự cố (`assertThrows`) | Negative | Giả lập lỗi gửi OTP | 1. Gọi `otpService.sendOtp()` với email lỗi | `email = "error@gmail.com"` | Ném ngoại lệ `LoiNghiepVu` và kiểm tra đúng message lỗi | **PASS** |

---

## 3. MODULE QUẢN LÝ SẢN PHẨM (PRODUCT CRUD)

| Test Case ID | Tên ca kiểm thử | Loại test | Tiền điều kiện | Các bước thực hiện | Dữ liệu đầu vào (Test Data) | Kết quả mong đợi | Trạng thái |
| :---: | :--- | :---: | :--- | :--- | :--- | :--- | :---: |
| **TC_PROD_01** | Thêm mới sản phẩm hợp lệ | Positive | Đã đăng nhập quyền Admin | 1. Nhập đầy đủ thông tin<br>2. Gọi hàm `addProduct()` | `name = "Chuột ATK F1 Ultimate"`, `price = 1,850,000` | Sản phẩm được tạo thành công, redirect `/admin/products` | **PASS** |
| **TC_PROD_02** | Ném ngoại lệ khi giá sản phẩm <= 0 (`assertThrows`) | Negative | Form thêm sản phẩm | 1. Nhập giá bán âm<br>2. Gọi hàm thêm sản phẩm | `price = -50,000` | Ném ngoại lệ `LoiNghiepVu("Giá sản phẩm phải lớn hơn 0")` | **PASS** |
| **TC_PROD_03** | Lấy thông tin sản phẩm theo ID | Positive | Sản phẩm `ID = 1` đã có | 1. Gọi hàm `editForm(1)` | `id = 1` | Trả về view `"admin/product-form"` kèm đúng đối tượng sản phẩm | **PASS** |
| **TC_PROD_04** | Lấy toàn bộ danh sách sản phẩm | Positive | Hệ thống có dữ liệu | 1. Gọi hàm `listProducts()` | `page = 1` | Danh sách trả về `>= 2` bản ghi, view `"admin/products"` | **PASS** |
| **TC_PROD_05** | Xóa sản phẩm theo ID thành công | Positive | Sản phẩm `ID = 2` tồn tại | 1. Gọi hàm `deleteProduct(2)` | `id = 2` | Xóa thành công, redirect `/admin/products` | **PASS** |

---

## 4. TỔNG HỢP KẾT QUẢ CHẠY AUTOMATION UNIT TEST (JUNIT 5 & MOCKITO)

* **Cơ chế tự động hóa (Automation Mechanism):**
  * Sử dụng **JUnit 5 (Jupiter 5.10.2)** kết hợp **Mockito (`@ExtendWith(MockitoExtension.class)`, `@Mock`, `@InjectMocks`)** để cô lập hoàn toàn các phụ thuộc (`UserService`, `OtpService`, `ProductService`, `CartService`, `HttpSession`, `Model`).
  * Áp dụng **`@ParameterizedTest` + `@CsvSource`** để tự động hóa kiểm thử lặp qua nhiều bộ dữ liệu biên (Boundary Value Analysis).
  * Áp dụng **`assertThrows(LoiNghiepVu.class, ...)`** để bắt và kiểm chứng ngoại lệ nghiệp vụ.
  * Thực thi tự động qua **Maven Surefire Plugin (`mvn test`)**, **VS Code Test Runner** và **GitHub Actions CI/CD**.

* **Thống kê kết quả thực thi thực tế:**

| Class Kiểm thử (`src/test/java/com/poly/sof309/controllers/`) | Số hàm `@Test` / `@ParameterizedTest` | Tổng số ca chạy tự động (Test Runs) | Kết quả (Pass / Fail / Error) | Tỷ lệ thành công |
| :--- | :---: | :---: | :---: | :---: |
| **`AuthControllerTest.java`** | 7 phương thức (1 `@CsvSource` x 4 dòng) | **10 tests** | `10 Pass / 0 Fail / 0 Error` | **100% PASS** |
| **`ProductControllerTest.java`** | 8 phương thức (1 `@CsvSource` x 4 dòng) | **11 tests** | `11 Pass / 0 Fail / 0 Error` | **100% PASS** |
| **`SampleControllerTest.java`** | 2 phương thức | **2 tests** | `2 Pass / 0 Fail / 0 Error` | **100% PASS** |
| **TỔNG CỘNG (CONTROLLER UNIT TESTS)** | **17 phương thức** | **23 tests** | **23 Pass / 0 Fail / 0 Error** | **100% PASS (BUILD SUCCESS)** |

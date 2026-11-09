# BỘ KỊCH BẢN KIỂM THỬ CHI TIẾT (TEST CASES)
### Dự án: TiTanGEAR E-Commerce System

---

## 1. MODULE GIỎ HÀNG & OTP (CART & OTP - BÁM SÁT BÀI 1 LAB 1)

| Test Case ID | Tên ca kiểm thử | Loại test | Tiền điều kiện | Các bước thực hiện | Dữ liệu đầu vào (Test Data) | Kết quả mong đợi | Trạng thái |
| :---: | :--- | :---: | :--- | :--- | :--- | :--- | :---: |
| **TC_CART_01** | Thêm sản phẩm vào giỏ với số lượng hợp lệ | Positive | Sản phẩm còn tồn kho (>= 5) | 1. Chọn sản phẩm hợp lệ<br>2. Gọi hàm thêm vào giỏ hàng | `productId = 1`<br>`quantity = 2` | Thêm vào giỏ thành công, chuyển hướng kèm `added=true` | **PASS** |
| **TC_CART_02** | Thêm sản phẩm vượt quá số lượng tồn kho (Lab 1) | Negative | Tồn kho sản phẩm chỉ còn 5 | 1. Chọn sản phẩm<br>2. Nhập số lượng vượt tồn kho | `productId = 1`<br>`quantity = 999` | Ném `LoiNghiepVu`, thông báo số lượng trong kho không đủ | **PASS** |
| **TC_CART_03** | Xác thực mã OTP 6 số hợp lệ (Sau khi Debug) | Positive | Đang ở bước xác nhận đăng ký | 1. Nhập mã OTP hợp lệ `123456`<br>2. Bấm xác nhận | `otp = "123456"` | Xác thực thành công, lưu user vào DB và chuyển sang `/login` | **PASS** |
| **TC_CART_04** | Cập nhật số lượng sản phẩm trong giỏ <= 0 | Negative | Giỏ hàng đã có sản phẩm | 1. Nhập số lượng <= 0<br>2. Bấm cập nhật giỏ hàng | `cartItemId = 1`<br>`quantity = 0` | Tự động xóa sản phẩm khỏi giỏ hàng | **PASS** |

---

## 2. MODULE XÁC THỰC (AUTHENTICATION - SLIDE 1)

| Test Case ID | Tên ca kiểm thử | Loại test | Tiền điều kiện | Các bước thực hiện | Dữ liệu đầu vào (Test Data) | Kết quả mong đợi | Trạng thái |
| :---: | :--- | :---: | :--- | :--- | :--- | :--- | :---: |
| **TC_AUTH_01** | Đăng nhập thành công với Admin | Positive | Tài khoản đã có trong DB | 1. Nhập email, password đúng<br>2. Bấm đăng nhập | `user = "admin@titangear.vn"`<br>`pass = "123456"` | Đăng nhập thành công, chuyển hướng `/admin/dashboard` | **PASS** |
| **TC_AUTH_02** | Đăng nhập thất bại khi sai mật khẩu | Negative | Tài khoản tồn tại | 1. Nhập email đúng<br>2. Nhập mật khẩu sai | `user = "admin@titangear.vn"`<br>`pass = "WrongPass999"` | Báo lỗi `"Email hoặc mật khẩu không đúng!"` | **PASS** |
| **TC_AUTH_03** | Báo lỗi khi để trống mật khẩu | Negative | Form đăng nhập | 1. Nhập email<br>2. Để trống mật khẩu và bấm đăng nhập | `user = "admin@titangear.vn"`<br>`pass = ""` | Báo lỗi yêu cầu nhập mật khẩu | **PASS** |
| **TC_AUTH_04** | Đăng ký người dùng mới hợp lệ | Positive | Chưa có email này trong DB | 1. Điền form đăng ký hợp lệ<br>2. Bấm đăng ký | `email = "minhtu@gmail.com"`<br>`pass = "123456"` | Gửi mã OTP thành công, chuyển hướng `/verify-otp` | **PASS** |
| **TC_AUTH_05** | Đăng ký thất bại khi trùng email | Negative | Email đã tồn tại | 1. Nhập email đã có<br>2. Bấm đăng ký | `email = "admin@titangear.vn"` | Báo lỗi `"Email đã được sử dụng!"` | **PASS** |

---

## 3. MODULE QUẢN LÝ SẢN PHẨM (PRODUCT CRUD)

| Test Case ID | Tên ca kiểm thử | Loại test | Tiền điều kiện | Các bước thực hiện | Dữ liệu đầu vào (Test Data) | Kết quả mong đợi | Trạng thái |
| :---: | :--- | :---: | :--- | :--- | :--- | :--- | :---: |
| **TC_PROD_01** | Thêm mới sản phẩm hợp lệ | Positive | Đã đăng nhập quyền Admin | 1. Nhập đầy đủ thông tin<br>2. Gọi hàm thêm mới | `name = "Chuột ATK F1 Ultimate"`, `price = 1,850,000` | Sản phẩm được tạo thành công | **PASS** |
| **TC_PROD_02** | Báo lỗi khi giá sản phẩm <= 0 | Negative | Form thêm sản phẩm | 1. Nhập giá bán âm<br>2. Bấm lưu | `price = -50,000` | Báo lỗi validation giá không hợp lệ | **PASS** |
| **TC_PROD_03** | Tìm kiếm sản phẩm theo ID | Positive | Sản phẩm `ID = 1` đã có | 1. Gọi hàm tìm kiếm theo ID | `id = 1` | Trả về đúng thông tin sản phẩm `ID = 1` | **PASS** |
| **TC_PROD_04** | Lấy toàn bộ danh sách sản phẩm | Positive | Hệ thống có dữ liệu | 1. Gọi hàm lấy danh sách | Không có | Danh sách có >= 2 bản ghi | **PASS** |
| **TC_PROD_05** | Xóa sản phẩm theo ID thành công | Positive | Sản phẩm `ID = 2` tồn tại | 1. Gọi hàm xóa sản phẩm `ID = 2` | `id = 2` | Xóa thành công, tìm lại không còn hiển thị | **PASS** |

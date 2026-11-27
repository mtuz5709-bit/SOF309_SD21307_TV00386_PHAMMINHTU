# HỒ SƠ ĐẶC TẢ KỊCH BẢN KIỂM THỬ BACK-END (BE UNIT TEST CASES)
### Dự án: TiTanGEAR E-Commerce System
> **Sinh viên thực hiện:** Phạm Minh Tu – **MSSV:** TV00386 – **Lớp:** SD21307

---

## 0. CHIẾN LƯỢC TEST BACK-END (BE – TẦNG SERVICE) & ĐẶC TẢ VALUE CỤ THỂ

1. **Phạm vi kiểm thử Back-End (BE):**
   * Tập trung hoàn toàn vào **tầng Service (Business Logic & Data Validation)** trong `src/test/java/com/poly/sof309/services/` (`AuthServiceTest`, `ProductServiceTest`, `CartServiceTest`), **không** test giao diện View/HTML/redirect.
   * Mỗi ca test cô lập CSDL bằng Mockito + danh sách giả lập (`List<User>`, `List<Product>`, `List<CartItem>`), truyền **Value cụ thể** và khẳng định trực tiếp dữ liệu trả về / ngoại lệ nghiệp vụ (`LoiNghiepVu`).
2. **Hai mục tiêu của bộ Test Suite Back-End (tổng 30 ca BE):**
   * **1. Smoke Test Suite (`@Tag("smoke")` – 11 ca):** Kiểm thử khói các nghiệp vụ sống còn (3 ca Auth + 4 ca Product + 4 ca Cart).
   * **2. Regression Test Suite (`@Tag("regression")` – 19 ca):** Kiểm thử hồi quy các ca biên (`@ParameterizedTest` + `@CsvSource`), trùng email, bỏ trống trường (`"Vui lòng nhập họ và tên!"`), tài khoản bị khóa, giá bán `<= 0`, tồn kho âm, giỏ hàng vượt giới hạn (10 ca Auth + 5 ca Product + 4 ca Cart).
3. **Nguyên tắc kiểm chứng Đăng ký & CRUD bằng Value cụ thể:**
   * Gọi `User created = userService.register(newUser)` (hoặc `Product created = productService.addProduct(spMoi)`): sai dữ liệu thì ném `LoiNghiepVu`, đúng dữ liệu thì lưu vào danh sách và **trả về đối tượng vừa tạo**.
   * **3 bước khẳng định:** `assertNotNull(created)` $\rightarrow$ gọi `findByEmail(...)` / `findById(...)` lấy ngược từ danh sách $\rightarrow$ `assertEquals` đối chiếu khớp từng thuộc tính.

---

## 1. SMOKE TEST SUITE BACK-END (`@Tag("smoke")` – 11 ca)

| Test Case ID | Class Service Test | Mô tả ca kiểm thử BE | Dữ liệu đầu vào cụ thể (Test Data - Value) | Các bước & Kiểm chứng (Assertions) | Kết quả mong đợi | Trạng thái |
| :---: | :--- | :--- | :--- | :--- | :--- | :---: |
| **TC_SMOKE_REG_01** | `AuthServiceTest` | Đăng ký thành công với thông tin hợp lệ | `fullName = "Phạm Minh Tu"`<br>`email = "minhtu.new@titangear.vn"`<br>`phone = "0912345678"`<br>`passwordHash = "Titan@123"` | 1. Gọi `created = userService.register(newUser)`<br>2. `assertNotNull(created)` & `assertNotNull(created.getId())`<br>3. Gọi `findByEmail("minhtu.new@titangear.vn")` đối chiếu từng trường | Trả về `User` khác `null`, `role = "CUSTOMER"`, `isActive = true`, lấy ngược từ `dsUser` trùng khớp 100% | **PASS** |
| **TC_SMOKE_LOGIN_01** | `AuthServiceTest` | Đăng nhập thành công với tài khoản Admin | `email = "admin@titangear.vn"`<br>`rawPassword = "Admin@123"` | 1. Gọi `loggedIn = userService.login("admin@titangear.vn", "Admin@123")`<br>2. `assertNotNull(loggedIn)`<br>3. `assertEquals` kiểm tra `email`, `fullName`, `role`, `isActive` | Trả về `User` khác `null`, `fullName = "Admin TiTanGEAR"`, `role = "ADMIN"`, `isActive = true` | **PASS** |
| **TC_SMOKE_FLOW_01** | `AuthServiceTest` | Luồng liên hoàn BE: Đăng ký xong $\rightarrow$ get kiểm tra $\rightarrow$ đăng nhập ngay | `fullName = "Nguyễn Văn An"`<br>`email = "vanan@titangear.vn"`<br>`phone = "0987654321"`<br>`passwordHash = "Gear@2026"` | 1. Gọi `register(newUser)` $\rightarrow$ `assertNotNull(created)`<br>2. Gọi `findByEmail("vanan@titangear.vn")` $\rightarrow$ `assertTrue(isPresent)`<br>3. Gọi `login("vanan@titangear.vn", "Gear@2026")` $\rightarrow$ `assertEquals(created.getId(), loggedIn.getId())` | Tài khoản mới tạo lấy được từ danh sách và đăng nhập thành công ngay lập tức | **PASS** |
| **TC_SMOKE_PROD_01** | `ProductServiceTest` | Thêm mới sản phẩm hợp lệ xuống Service | `name = "Tai nghe HyperX Cloud III"`<br>`price = 2490000`<br>`stockQuantity = 10` | 1. Gọi `created = productService.addProduct(spMoi)`<br>2. `assertNotNull(created)` & `assertNotNull(created.getId())`<br>3. Gọi `findById(created.getId())` đối chiếu `name`, `price`, `stockQuantity` | Trả về `Product` khác `null`, `isActive = true`, lưu vào `dsSanPham` khớp 100% | **PASS** |
| **TC_SMOKE_PROD_02** | `ProductServiceTest` | Lấy danh sách sản phẩm đang bán (`isActive = true`) | Danh sách `dsSanPham` có 2 sản phẩm mẫu (`id = 1, 2`) | 1. Gọi `list = productService.getAllProducts()`<br>2. `assertEquals(2, list.size())`<br>3. `assertEquals("Chuột ATK F1 Ultimate", list.get(0).getName())` | Trả về đúng 2 sản phẩm đang kinh doanh | **PASS** |
| **TC_SMOKE_PROD_03** | `ProductServiceTest` | Cập nhật giá sản phẩm #1 & ghi nhận AuditLog đổi giá | `id = 1`, `name = "Chuột ATK F1 Ultimate"`, `price = 1750000` (giá cũ `1850000`) | 1. Gọi `productService.updateProduct(spSua)`<br>2. Gọi `findById(1)` kiểm tra giá mới `1750000`<br>3. `verify(auditLogService).ghi("SUA_GIA", "products", 1, "1850000", "1750000", ...)` | Giá được cập nhật thành `1750000` và ghi log đổi giá chính xác | **PASS** |
| **TC_SMOKE_PROD_04** | `ProductServiceTest` | Xóa mềm sản phẩm theo ID (`deleteProduct`) | `id = 2` (`"Bàn phím cơ ATK RS6 Air"`) | 1. Gọi `productService.deleteProduct(2)`<br>2. Gọi `findById(2)` kiểm tra `isActive = false`<br>3. `assertEquals(1, productService.getAllProducts().size())` | Sản phẩm #2 bị ẩn (`isActive = false`), danh sách đang bán còn `1` | **PASS** |
| **TC_SMOKE_CART_01** | `CartServiceTest` | Thêm sản phẩm vào giỏ hàng | `userId = 1`, `productId = 1`, `quantity = 2`, `variantId = null` | 1. Gọi `cartService.addToCart(1, 1, 2, null)`<br>2. Gọi `gio = cartService.getCart(1)`<br>3. `assertEquals(1, gio.size())` & `assertEquals(2, gio.get(0).getQuantity())` | Giỏ hàng user #1 có `1` dòng sản phẩm #1 với `quantity = 2` | **PASS** |
| **TC_SMOKE_CART_02** | `CartServiceTest` | Thêm trùng sản phẩm vào giỏ -> tự động cộng dồn số lượng | Lần 1: `userId = 1, productId = 1, qty = 2`<br>Lần 2: `userId = 1, productId = 1, qty = 3` | 1. Gọi `addToCart` 2 lần liên tiếp<br>2. Gọi `gio = cartService.getCart(1)`<br>3. `assertEquals(1, gio.size())` & `assertEquals(5, gio.get(0).getQuantity())` | Không tạo dòng trùng, gộp thành 1 dòng có `quantity = 5` | **PASS** |
| **TC_SMOKE_CART_03** | `CartServiceTest` | Cập nhật số lượng sản phẩm trong giỏ hàng | `userId = 1`, `productId = 1`, số lượng cũ `2` $\rightarrow$ mới `4` | 1. Thêm `(1, 1, 2, null)`<br>2. Gọi `cartService.updateQuantity(1, itemId, 4)`<br>3. `assertEquals(4, cartService.getCart(1).get(0).getQuantity())` | Số lượng dòng giỏ hàng đổi thành `4` | **PASS** |
| **TC_SMOKE_CART_04** | `CartServiceTest` | Xóa sản phẩm khỏi giỏ hàng | `userId = 1`, `productId = 1`, `quantity = 2` | 1. Thêm `(1, 1, 2, null)`<br>2. Gọi `cartService.removeItem(1, itemId)`<br>3. `assertTrue(cartService.getCart(1).isEmpty())` | Dòng hàng bị xóa khỏi `dsGioHang`, giỏ hàng trống | **PASS** |

---

## 2. REGRESSION TEST SUITE BACK-END (`@Tag("regression")` – 19 ca)

| Test Case ID | Class Service Test | Mô tả ca kiểm thử biên & ngoại lệ BE | Dữ liệu đầu vào cụ thể (Test Data - Value) | Ngoại lệ & Thông điệp kỳ vọng chính xác | Trạng thái |
| :---: | :--- | :--- | :--- | :--- | :---: |
| **TC_REG_DUP_01** | `AuthServiceTest` | Đăng ký thất bại do trùng email đã tồn tại | `fullName = "Trùng Email"`, `email = "admin@titangear.vn"`, `phone = "0912345678"`, `passwordHash = "Titan@123"` | Ném `LoiNghiepVu`: `"Email đã được sử dụng!"` và `dsUser` vẫn giữ nguyên `2` bản ghi | **PASS** |
| **TC_REG_VAL_01a** | `AuthServiceTest` | Đăng ký thất bại khi bỏ trống họ và tên (`@CsvSource`) | `fullName = ""`, `email = "hople@titangear.vn"`, `phone = "0912345678"`, `password = "Titan@123"` | Ném `LoiNghiepVu`: `"Vui lòng nhập họ và tên!"` *(so khớp chính xác từng ký tự)* | **PASS** |
| **TC_REG_VAL_01b** | `AuthServiceTest` | Đăng ký thất bại khi họ tên < 2 ký tự (`@CsvSource`) | `fullName = "A"`, `email = "hople@titangear.vn"`, `phone = "0912345678"`, `password = "Titan@123"` | Ném `LoiNghiepVu`: `"Họ tên phải có ít nhất 2 ký tự!"` | **PASS** |
| **TC_REG_VAL_01c** | `AuthServiceTest` | Đăng ký thất bại khi email sai định dạng (`@CsvSource`) | `fullName = "Phạm Minh Tu"`, `email = "khong_co_acong.vn"`, `phone = "0912345678"`, `password = "Titan@123"` | Ném `LoiNghiepVu`: `"Email không hợp lệ!"` | **PASS** |
| **TC_REG_VAL_01d** | `AuthServiceTest` | Đăng ký thất bại khi SĐT không đủ 10 số (`@CsvSource`) | `fullName = "Phạm Minh Tu"`, `email = "hople@titangear.vn"`, `phone = "091234567"`, `password = "Titan@123"` | Ném `LoiNghiepVu`: `"Số điện thoại phải đúng 10 chữ số!"` | **PASS** |
| **TC_REG_VAL_01e** | `AuthServiceTest` | Đăng ký thất bại khi mật khẩu < 6 ký tự (`@CsvSource`) | `fullName = "Phạm Minh Tu"`, `email = "hople@titangear.vn"`, `phone = "0912345678"`, `password = "12345"` | Ném `LoiNghiepVu`: `"Mật khẩu phải có ít nhất 6 ký tự!"` | **PASS** |
| **TC_LOGIN_FAIL_01** | `AuthServiceTest` | Đăng nhập thất bại khi sai mật khẩu | `email = "admin@titangear.vn"`, `rawPassword = "SaiMatKhau@999"` | Ném `LoiNghiepVu`: `"Email hoặc mật khẩu không đúng!"` | **PASS** |
| **TC_LOGIN_FAIL_02** | `AuthServiceTest` | Đăng nhập thất bại với tài khoản bị khóa | `email = "locked@titangear.vn"`, `rawPassword = "Locked@123"` | Ném `LoiNghiepVu`: `"Tài khoản của bạn đã bị khóa!"` | **PASS** |
| **TC_LOGIN_FAIL_03** | `AuthServiceTest` | Đăng nhập thất bại khi bỏ trống email hoặc mật khẩu (`@CsvSource`) | 1) `email = ""`, `pass = "Admin@123"`<br>2) `email = "admin@titangear.vn"`, `pass = ""` | 1) `"Vui lòng nhập email!"`<br>2) `"Vui lòng nhập mật khẩu!"` | **PASS** |
| **TC_REG_PROD_01a** | `ProductServiceTest` | Thêm sản phẩm khi tên rỗng (`@CsvSource`) | `name = ""`, `price = 1500000`, `stockQuantity = 10` | Ném `LoiNghiepVu`: `"Tên sản phẩm không được để trống!"`, `dsSanPham` giữ nguyên `2` SP | **PASS** |
| **TC_REG_PROD_01b** | `ProductServiceTest` | Thêm sản phẩm khi giá bằng `0` (`@CsvSource`) | `name = "Lót chuột Gaming"`, `price = 0`, `stockQuantity = 10` | Ném `LoiNghiepVu`: `"Giá sản phẩm phải lớn hơn 0!"` | **PASS** |
| **TC_REG_PROD_01c** | `ProductServiceTest` | Thêm sản phẩm khi giá âm (`@CsvSource`) | `name = "Lót chuột Gaming"`, `price = -50000`, `stockQuantity = 10` | Ném `LoiNghiepVu`: `"Giá sản phẩm phải lớn hơn 0!"` | **PASS** |
| **TC_REG_PROD_01d** | `ProductServiceTest` | Thêm sản phẩm khi tồn kho âm (`@CsvSource`) | `name = "Lót chuột Gaming"`, `price = 500000`, `stockQuantity = -5` | Ném `LoiNghiepVu`: `"Số lượng tồn kho không được âm!"` | **PASS** |
| **TC_REG_PROD_02** | `ProductServiceTest` | Cập nhật sản phẩm ID không tồn tại | `id = 999`, `name = "Sản phẩm không tồn tại"`, `price = 1000000` | Ném `RuntimeException`: `"Không tìm thấy sản phẩm #999"` | **PASS** |
| **TC_REG_CART_01a** | `CartServiceTest` | Thêm vào giỏ với số lượng `0` (`@CsvSource`) | `userId = 1`, `productId = 1`, `quantity = 0` | Ném `LoiNghiepVu`: `"Số lượng phải lớn hơn 0."`, `dsGioHang` vẫn trống | **PASS** |
| **TC_REG_CART_01b** | `CartServiceTest` | Thêm vào giỏ với số lượng âm (`@CsvSource`) | `userId = 1`, `productId = 1`, `quantity = -3` | Ném `LoiNghiepVu`: `"Số lượng phải lớn hơn 0."`, `dsGioHang` vẫn trống | **PASS** |
| **TC_REG_CART_02** | `CartServiceTest` | Thêm vào giỏ vượt quá giới hạn tối đa (`10`/đơn) | `userId = 1`, `productId = 1`, `quantity = 11` | Ném `LoiNghiepVu`: `"Bạn chỉ được mua tối đa 10 sản phẩm này trong 1 đơn hàng."` | **PASS** |
| **TC_REG_CART_03** | `CartServiceTest` | Thêm sản phẩm đã ngừng bán vào giỏ | `userId = 1`, `productId = 99` (`isActive = false`), `quantity = 1` | Ném `IllegalArgumentException`: `"Sản phẩm không tồn tại hoặc đã ngừng bán."` | **PASS** |

---

## 3. CÁCH CHẠY TỪNG SUITE BACK-END & KIỂM CHỨNG BẮT BUG

### Lệnh chạy từng Suite với Maven:
* **Chạy riêng Smoke Test Suite BE (11 ca):** `mvn test "-Dgroups=smoke"`
* **Chạy riêng Regression Test Suite BE (19 ca):** `mvn test "-Dgroups=regression"`
* **Chạy toàn bộ 3 Service Test BE (`Auth`, `Product`, `Cart` – 30 ca):** `mvn test "-Dtest=com.poly.sof309.services.*Test"`
* **Chạy toàn bộ JUnit 5 (Y3):** `mvn test`
* **Chạy TestNG Tuần 4 (Y4):** `mvn test -Ptestng`

### Minh chứng kiểm thử bắt Bug thực tế (Mutation Check):
1. **Pha 1 (Pass 100%):** Cả 3 lớp Service BE (`AuthServiceTest`, `ProductServiceTest`, `CartServiceTest`) đều đạt **30/30 PASS (0 Failure, 0 Error)**.
2. **Pha 2 (Cố tình cài bug gõ nhầm vào dòng 37 `UserService.java`):** Sửa `"Vui lòng nhập họ và tên!"` thành `"Vui lohng nhập họ và tên."` $\rightarrow$ chạy `mvn test "-Dtest=AuthServiceTest"` $\rightarrow$ **TC_REG_VAL_01a báo FAIL**:
   * `expected: <Vui lòng nhập họ và tên!> but was: <Vui lohng nhập họ và tên.>`
3. **Pha 3 (Fix bug & chạy lại):** Sửa lại `"Vui lòng nhập họ và tên!"` $\rightarrow$ chạy lại `mvn test` $\rightarrow$ toàn bộ đạt **100% PASS (`BUILD SUCCESS`)**.

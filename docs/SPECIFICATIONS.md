# TÀI LIỆU ĐẶC TẢ NGHIỆP VỤ HỆ THỐNG (SPECIFICATIONS)
> **Dự án:** Hệ thống Thương mại Điện tử Phụ kiện Gaming TiTanGEAR  
> **Sinh viên thực hiện:** Phạm Minh Tu - TV00386 (Lớp SD21307)

---

## 1. THÔNG TIN CHUNG
* **Tên ứng dụng:** TiTanGEAR E-Commerce System
* **Công nghệ:** Java 21, Spring MVC 6, Hibernate 6, SQL Server, Maven, JUnit 5, Selenium WebDriver 4
* **Cổng chạy local:** `http://localhost:8080`

---

## 2. QUY TẮC NGHIỆP VỤ CHI TIẾT (BUSINESS RULES)

### A. Chức năng Xác thực & Quản lý Tài khoản (Auth)
* **Đăng nhập (`/login`):**
  * `username` (Email): Bắt buộc, không được để trống.
  * `password`: Bắt buộc, không được để trống.
  * Kiểm tra đúng tài khoản/mật khẩu $\rightarrow$ chuyển hướng theo quyền (`ROLE_ADMIN` về `/admin/dashboard`, `ROLE_WAREHOUSE` về `/warehouse/stock`, Khách hàng về `/home`); sai $\rightarrow$ báo lỗi `Email hoặc mật khẩu không đúng!`.
* **Đăng ký tài khoản mới (`/register`):**
  * `fullName`: Tối thiểu 2 ký tự, bắt buộc.
  * `email`: Bắt buộc có định dạng hợp lệ (chứa `@`), không được trùng lặp trong DB.
  * `phone`: Nếu nhập phải đúng 10 chữ số (`0-9`).
  * `password`: Tối thiểu 6 ký tự.

### B. Chức năng Giỏ hàng & Thanh toán (Cart & Checkout - Bám sát Lab 1)
* **Nguyên tắc xử lý giỏ hàng & giao dịch:**
  * Số lượng sản phẩm thêm vào giỏ (`quantity`) bắt buộc phải lớn hơn 0.
  * **Kiểm tra tồn kho:** Số lượng đặt mua không được vượt quá số lượng tồn kho (`stock`). Nếu vượt quá tồn kho $\rightarrow$ Hệ thống dừng thao tác và ném ra ngoại lệ `LoiNghiepVu`.
  * **Cập nhật giỏ hàng:** Nếu cập nhật số lượng `<= 0` $\rightarrow$ Tự động xóa sản phẩm khỏi giỏ hàng.
  * **Xác thực OTP:** Khi đăng ký tài khoản hoặc khôi phục mật khẩu, bắt buộc phải cung cấp mã OTP 6 chữ số hợp lệ trong thời hạn **5 phút** (tối đa nhập sai 5 lần). Nếu không có OTP hoặc OTP sai $\rightarrow$ Từ chối xác thực và báo lỗi.

### C. Module CRUD Quản lý Sản phẩm (Product)
* **Thêm mới (Create):**
  * `id`: Tự động tăng, duy nhất (không trùng lặp).
  * `name`: Tối thiểu 3 ký tự, bắt buộc.
  * `price`: Bắt buộc lớn hơn 0 VNĐ.
  * `stock`: Số lượng tồn kho không được âm (>= 0).
* **Xem danh sách (Read):** Hiển thị đầy đủ danh sách sản phẩm phụ kiện gaming (Chuột, Bàn phím, Tai nghe) có trong kho.
* **Cập nhật (Update):** Cho phép sửa thông tin của sản phẩm đã tồn tại.
* **Xóa (Delete):** Xóa hoặc ngừng kinh doanh sản phẩm khỏi danh sách theo mã `id`.

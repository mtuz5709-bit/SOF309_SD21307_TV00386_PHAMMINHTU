# MINH CHỨNG SỬ DỤNG AI: SINH MÃ UNIT TEST VỚI JUNIT 5 (Y3 & Y6)
> **Minh họa nội dung:** Slide 1 & Tuần 2 - Kiểm thử đơn vị, Parameterized Test & Exception Testing

* **Sinh viên thực hiện:** Phạm Minh Tu – **MSSV:** TV00386 – **Lớp:** SD21307
* **Công cụ AI:** Gemini Pro / Cursor AI
* **Ngày thực hiện:** 11/11/2026

---

## 1. Ngữ cảnh & Mục tiêu
Cần sinh bộ Unit Test bằng **JUnit 5** và **Mockito** cho các Controller chính của dự án **TiTanGEAR**:
1. `AuthControllerTest.java`: Kiểm thử luồng đăng nhập, đăng ký, xác thực OTP 6 số, kiểm tra biên Form với `@ParameterizedTest` + `@CsvSource`.
2. `ProductControllerTest.java`: Kiểm thử CRUD sản phẩm (`AdminProductController`) và giới hạn tồn kho giỏ hàng (`CartController`) với `assertThrows(LoiNghiepVu.class)`.

## 2. Câu lệnh gửi cho AI (Prompt)
```text
Dựa vào các class AuthController, AdminProductController và CartController của dự án TiTanGEAR:
Hãy viết các Unit Test class sử dụng JUnit 5 và Mockito theo các tiêu chuẩn của Slide 1 và Tuần 2:
- Sử dụng @BeforeEach để thiết lập dữ liệu độc lập cho từng ca test.
- Áp dụng @ParameterizedTest kết hợp @CsvSource để kiểm tra các giá trị biên Form đăng ký và số lượng giỏ hàng.
- Sử dụng đầy đủ các assertions: assertEquals, assertTrue, assertFalse, assertNotNull, assertThrows.
- Có ca test kiểm tra ngoại lệ LoiNghiepVu khi số lượng đặt mua vượt quá tồn kho hoặc giá sản phẩm <= 0.
```

## 3. Kết quả do AI sinh ra (Raw Output)
AI sinh ra khung code JUnit 5 cơ bản cho `AuthControllerTest` và `ProductControllerTest`. Tuy nhiên:
* Ban đầu AI dùng `Exception.class` chung chung trong `assertThrows()` thay vì bắt đúng ngoại lệ nghiệp vụ `LoiNghiepVu.class` của dự án TiTanGEAR.
* Một số phụ thuộc Spring (`UserService`, `OtpService`, `ProductService`, `CartService`) cần được cô lập bằng `@Mock` và `@InjectMocks` để chạy Unit Test độc lập không phụ thuộc kết nối SQL Server.

## 4. Tinh chỉnh của Sinh viên (Human Refinement)
* **Sửa đổi:**
  * Sử dụng `@ExtendWith(MockitoExtension.class)`, `@Mock` và `@InjectMocks` để cô lập hoàn toàn các tầng Service và `HttpSession`.
  * Bắt đúng ngoại lệ cụ thể `LoiNghiepVu.class` trong `assertThrows()` và kiểm tra nội dung thông báo lỗi (`ex.getMessage()`).
  * Bổ sung `@Timeout(value = 2, unit = TimeUnit.SECONDS)` để đảm bảo hiệu năng xử lý của các hàm Controller.
* **Kết quả:** Đã lưu tại `src/test/java/com/poly/sof309/controllers/AuthControllerTest.java` và `src/test/java/com/poly/sof309/controllers/ProductControllerTest.java`, thực thi pass 100%.

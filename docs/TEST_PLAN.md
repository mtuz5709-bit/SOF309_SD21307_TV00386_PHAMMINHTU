# KẾ HOẠCH KIỂM THỬ (TEST PLAN)

* **Dự án:** Hệ thống Thương mại Điện tử Phụ kiện Gaming – **TiTanGEAR**
* **Sinh viên thực hiện:** Phạm Minh Tu – **MSSV:** TV00386 – **Lớp:** SD21307
* **Giảng viên hướng dẫn:** ThS. Trần Quang Bình (`binhtq7`)
* **Thời gian thực hiện:** 02/11/2026 – 09/12/2026

---

## 1. MỤC TIÊU KIỂM THỬ (TEST OBJECTIVES)

| STT | Mục tiêu kiểm thử | Tầm quan trọng trong thực tế | Hoạt động kiểm thử cụ thể | Phân loại |
| :---: | :--- | :--- | :--- | :---: |
| **1** | **Xác thực tính toàn vẹn của luồng Đăng nhập / Đăng ký** | Ngăn chặn truy cập trái phép và bảo vệ thông tin tài khoản khách hàng. | Viết Unit Test kiểm tra tính hợp lệ của mật khẩu, định dạng email và bắt lỗi trùng email. | **Verification** |
| **2** | **Đảm bảo tính chính xác của Giỏ hàng & Xác thực OTP** | Tránh đặt hàng vượt quá số lượng tồn kho và bảo mật tài khoản qua mã OTP. | Viết Test Script kiểm thử giới hạn tồn kho (`LoiNghiepVu`) và xác thực mã OTP 6 số. | **Verification** |
| **3** | **Đảm bảo tính đúng đắn của dữ liệu CRUD Sản phẩm** | Dữ liệu kho phụ kiện gaming phải nhất quán, giá bán và số lượng không bị âm. | Viết Unit Test kiểm tra các phương thức Thêm, Sửa, Xóa và bắt lỗi validation. | **Verification** |
| **4** | **Kiểm thử giao diện người dùng tự động (UI Automation)** | Đảm bảo thao tác đăng nhập, đăng ký và mua hàng mượt mà trên trình duyệt thực tế. | Sử dụng Selenium WebDriver (mô hình POM) kiểm thử tự động quy trình người dùng. | **Validation** |
| **5** | **Tự động hóa kiểm thử liên tục với CI/CD** | Đảm bảo mã nguồn mới đẩy lên không làm lỗi các chức năng cũ (Regression Testing). | Thiết lập GitHub Actions tự động thực thi toàn bộ Test Suite khi push code. | **Verification** |

---

## 2. PHẠM VI KIỂM THỬ (TEST SCOPE)

* **Trong phạm vi (In-Scope):**
  * **Module Xác thực (`AuthController`):** Đăng nhập (`/login`), Đăng ký (`/register`), Xác thực mã OTP (`/verify-otp`).
  * **Module Giỏ hàng (`CartController`):** Thêm sản phẩm vào giỏ, cập nhật số lượng, xóa sản phẩm, kiểm tra giới hạn tồn kho.
  * **Module Quản lý Sản phẩm (`AdminProductController`):** Thêm mới, xem danh sách, cập nhật, xóa/ẩn sản phẩm.
* **Ngoài phạm vi (Out-of-Scope):**
  * Kiểm thử hiệu năng chịu tải cao (Performance / Stress Testing).
  * Kiểm thử bảo mật thâm nhập chuyên sâu (Penetration Testing) và cổng thanh toán bên thứ ba thật.

---

## 3. LỊCH TRÌNH & PHÂN CÔNG THỰC HIỆN (TEST SCHEDULE & MILESTONES)

| Giai đoạn | Thời gian | Hạng mục công việc thực hiện | Người thực hiện | Sản phẩm bàn giao (Deliverables) | Trạng thái |
| :---: | :---: | :--- | :---: | :--- | :---: |
| **Tuần 1** | `02/11/2026` – `08/11/2026` | • Khởi tạo Repo từ Template chuẩn.<br>• Tích hợp mã nguồn Web **TiTanGEAR** vào `src/main/` (**Y1**).<br>• Viết Đặc tả nghiệp vụ, lập Kế hoạch kiểm thử & Thiết kế bộ Test Case (**Y2**). | **Phạm Minh Tu**<br>*(QA / Dev)* | • Repo GitHub cá nhân<br>• `SPECIFICATIONS.md`<br>• `TEST_PLAN.md`<br>• `TEST_CASES.md` | Hoàn thành |
| **Tuần 2** | `09/11/2026` – `15/11/2026` | • Viết Unit Test cho Controller bằng JUnit 5 (**Y3**).<br>• Áp dụng `@ParameterizedTest` + `@CsvSource` kiểm tra biên Form.<br>• Áp dụng `assertThrows()` bắt ngoại lệ nghiệp vụ. | **Phạm Minh Tu**<br>*(QA / Tester)* | • Code test trong `controllers/`<br>• `mvn test` đạt 100% Pass<br>• `01_test_design_ai.md` | Đang thực hiện |
| **Tuần 3** | `16/11/2026` – `22/11/2026` | • Thiết kế kiến trúc **Selenium Page Object Model (POM)** (**Y4 - P1**).<br>• Xây dựng các Page Classes (`LoginPage`, `RegisterPage`, `ProductPage`).<br>• Xử lý bất đồng bộ với `WebDriverWait`. | **Phạm Minh Tu**<br>*(Automation QA)* | • Thư mục `ui/pages/` & `ui/tests/`<br>• Kịch bản Selenium mở Chrome chạy mượt ở local | Kế hoạch |
| **Tuần 4** | `23/11/2026` – `29/11/2026` | • Nâng cấp **TestNG Framework** (**Y4 - P2**).<br>• Phân nhóm `groups = "smoke"`, `dependsOnMethods` và `@DataProvider`.<br>• Cấu hình `ITestListener` tự động chụp màn hình khi test lỗi. | **Phạm Minh Tu**<br>*(Automation QA)* | • File `testng.xml`<br>• Thư mục `screenshots/`<br>• `03_selenium_pom_ai.md` | Kế hoạch |
| **Tuần 5** | `30/11/2026` – `06/12/2026` | • Cấu hình CI/CD với **GitHub Actions** (`.github/workflows/test.yml`) (**Y5**).<br>• Chạy Chrome Headless trên Ubuntu & xuất báo cáo Artifacts.<br>• Dùng AI phân tích log debug (**Y6**). | **Phạm Minh Tu**<br>*(DevOps / QA)* | • Tab GitHub Actions báo **Xanh (Pass)**<br>• File zip báo cáo Artifacts<br>• `04_debug_logs_ai.md` | Kế hoạch |
| **Tuần 6** | `07/12/2026` – `09/12/2026` | • Nhờ AI review và tối ưu hóa mã nguồn kiểm thử (**Y6**).<br>• Rà soát trọn bộ 6 file minh chứng `docs/ai-logs/`.<br>• Hoàn thiện Slide bảo vệ Assignment. | **Phạm Minh Tu**<br>*(QA Lead)* | • Đầy đủ 6 file `docs/ai-logs/`<br>• Slide thuyết trình & gói `.zip` nộp bài | Kế hoạch |

---

## 4. MÔI TRƯỜNG & CÔNG CỤ KIỂM THỬ (TEST ENVIRONMENT & TOOLS)

| Thành phần | Cấu hình / Phiên bản sử dụng | Vai trò trong dự án |
| :--- | :--- | :--- |
| **Ngôn ngữ & Nền tảng** | Java JDK 21 LTS, Spring MVC 6.1.6, Hibernate 6.4 | Môi trường chạy ứng dụng Web TiTanGEAR |
| **Cơ sở dữ liệu** | Microsoft SQL Server 2022 | Lưu trữ dữ liệu người dùng, sản phẩm, đơn hàng |
| **Unit Test Framework** | JUnit 5 (Jupiter 5.10.2) + Mockito 5.11.0 | Kiểm thử đơn vị tầng Controller & Service |
| **UI Automation Tool** | Selenium WebDriver 4.20.0 + Google Chrome | Kiểm thử tự động giao diện Web theo mô hình POM |
| **CI/CD Pipeline** | GitHub Actions (`ubuntu-latest`, Temurin JDK 21) | Tự động chạy kiểm thử mỗi khi `git push` |

---

## 5. CHIẾN LƯỢC & TIÊU CHÍ NGHIỆM THU (STRATEGY & EXIT CRITERIA)

* **Phân biệt Kiểm thử (Testing) và Gỡ lỗi (Debugging):**
  * **Kiểm thử (Testing):** Thiết kế và chạy các kịch bản JUnit 5 / Selenium để phát hiện khiếm khuyết (Defects) và sự cố (Failures) khi hệ thống phản hồi khác với kết quả mong đợi.
  * **Gỡ lỗi (Debugging):** Phân tích stack trace lỗi $\rightarrow$ tìm nguyên nhân gốc rễ (Root Cause) trong code $\rightarrow$ chỉnh sửa mã nguồn $\rightarrow$ chạy lại test để xác nhận lỗi đã được xử lý triệt để.
* **Tiêu chí bắt đầu (Entry Criteria):**
  * Ứng dụng Web TiTanGEAR biên dịch thành công và khởi chạy ổn định tại `http://localhost:8080`.
  * Tài liệu đặc tả `SPECIFICATIONS.md` và danh sách `TEST_CASES.md` đã được phê duyệt.
* **Tiêu chí kết thúc & Nghiệm thu (Exit Criteria):**
  * 100% các kịch bản Unit Test và UI Test thực thi đạt trạng thái **PASS (Màu xanh)**.
  * Pipeline tự động trên tab **GitHub Actions** hiển thị dấu tích **Xanh (Success)**.
  * Hoàn thiện đầy đủ nhật ký tương tác AI trong thư mục `docs/ai-logs/`.

# MINH CHỨNG SỬ DỤNG AI: SELENIUM POM & TESTNG FRAMEWORK (TUẦN 4 – Y4 & Y6)
> **Minh họa nội dung:** TestNG Framework (`groups`, `dependsOnMethods`, `@DataProvider`, `ITestListener`, `testng.xml`) + Selenium Page Object Model (POM)

* **Sinh viên thực hiện:** Phạm Minh Tu – **MSSV:** TV00386 – **Lớp:** SD21307
* **Ngày thực hiện:** 28/11/2026

---

## 1. Ngữ cảnh & Mục tiêu
Nâng cấp khung kiểm thử tự động dự án **TiTanGEAR** lên **TestNG 7.10.2** kết hợp **Selenium POM 4.20.0**:
1. Tách riêng **Page Object Classes** (`LoginPage.java`, `RegisterPage.java`, `ProductPage.java`, `DriverManager.java`) dùng `WebDriverWait` theo locator thật của các trang JSP (`/login`, `/register`, `/products`).
2. Xây dựng **Data-Driven Testing** với `@DataProvider` để chạy lặp nhiều tài khoản hợp lệ, sai mật khẩu, tài khoản bị khóa và chưa tồn tại.
3. Phân nhóm kiểm thử `groups = "smoke"`, `groups = "regression"` và ràng buộc thứ tự `dependsOnMethods` trong `AuthDataDrivenTestNG.java` & `SeleniumLoginTestNG.java`.
4. Cài đặt **`TestListener implements ITestListener`** tự động chụp ảnh màn hình lưu vào thư mục `screenshots/` khi ca test bị fail, điều phối qua `testng.xml`.

## 2. Câu lệnh gửi cho AI (Prompt)
```text
Dựa vào các file JSP (common/login.jsp, customer/register.jsp, header.jsp, customer/products.jsp)
và nghiệp vụ UserService của dự án TiTanGEAR:
Hãy nâng cấp bộ test sang TestNG theo chuẩn Tuần 4 (Y4 - P2):
1. Cấu hình Maven profile "testng" trong pom.xml để chạy song song không xung đột với JUnit 5 (Y3).
2. Cập nhật LoginPage, bổ sung RegisterPage, ProductPage và DriverManager (hỗ trợ Chrome/Edge & Headless).
3. Viết TestListener implements ITestListener tự chụp ảnh màn hình vào thư mục screenshots/ khi onTestFailure.
4. Viết AuthDataDrivenTestNG và SeleniumLoginTestNG áp dụng @DataProvider, groups="smoke"/"regression", dependsOnMethods.
5. Tạo testng.xml và testng-ui.xml để chạy Suite và demo chụp ảnh lỗi.
```

## 3. Kết quả AI đề xuất & Tinh chỉnh của Sinh viên (Human Refinement)
* **Vấn đề gặp phải nếu cấu hình mặc định:** Khi thêm thư viện `testng` vào `pom.xml`, Maven Surefire 3.2.5 có thể chọn nhầm provider TestNG thay cho JUnit 5 khiến bộ Unit Test Y3 bị bỏ qua.
* **Giải pháp sinh viên tinh chỉnh:**
  * Khai báo tường minh `surefire-junit-platform` cho cấu hình mặc định (`mvn test` chạy toàn bộ JUnit 5 Y3).
  * Tạo `<profile><id>testng</id>...</profile>` dùng `surefire-testng` + `suiteXmlFile` (`mvn test -Ptestng` chạy TestNG Y4).
  * Trong `DriverManager.java`, dùng `ChromeDriver` để Selenium Manager tự tải Chrome for Testing & ChromeDriver tương thích và bật `--headless=new` khi chạy với `-Dheadless=true` hoặc biến `CI=true`.
  * Dùng locator thật (`By.name("username")`, `By.cssSelector("button.btn-auth[type='submit']")`, `By.cssSelector(".auth-error")`) khớp đúng HTML trong `common/login.jsp` thay vì `By.id("username")` của template cũ.

## 4. Kết quả chạy kiểm chứng thực tế
* **Lệnh 1 (`mvn test -Ptestng`):** Chạy `testng.xml` (`AuthDataDrivenTestNG` — `@DataProvider` + `dependsOnMethods`):
  * **Kết quả:** **7/7 PASS (0 Failure, 0 Error, 0 Skipped)** — `BUILD SUCCESS`.
* **Lệnh 2 (`mvn test -Ptestng -Dgroups=smoke`):** Chạy riêng nhóm Smoke trên TestNG:
  * **Kết quả:** **5/5 PASS (0 Failure, 0 Error, 0 Skipped)** — `BUILD SUCCESS`.
* **Lệnh 3 (`mvn test -Ptestng -DsuiteXmlFile=testng-ui.xml -Dgroups=screenshot-demo -Dheadless=true`):** Demo test fail có chủ đích để kiểm tra `ITestListener`:
  * **Kết quả:** `TestListener.onTestFailure` tự động chụp và lưu thành công file ảnh `.png` vào thư mục `screenshots/`.
* **Lệnh 4 (`mvn test`):** Chạy lại toàn bộ JUnit 5 (Y3):
  * **Kết quả:** Không xung đột, giữ nguyên **114 tests run, 0 Failure, 0 Error** (`1 Skipped` là `LoginTest` `@Disabled`).

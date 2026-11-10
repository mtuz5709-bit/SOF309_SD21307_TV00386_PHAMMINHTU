# MINH CHỨNG SỬ DỤNG AI: THIẾT KẾ TEST PLAN & TEST CASE (Y2 & Y6)
> **Minh họa nội dung:** Slide 1 & Lab 1 môn học

* **Sinh viên thực hiện:** Phạm Minh Tu – **MSSV:** TV00386 – **Lớp:** SD21307
* **Công cụ AI:** Gemini Pro / ChatGPT-4o
* **Ngày thực hiện:** 08/11/2026

---

## 1. Ngữ cảnh & Yêu cầu cung cấp cho AI (RAG Context)
Cung cấp nội dung từ file `docs/SPECIFICATIONS.md` mô tả về hệ thống thương mại điện tử **TiTanGEAR**:
- **Module Auth:** Đăng ký, Đăng nhập, phân quyền (`ROLE_ADMIN`, `ROLE_WAREHOUSE`, Khách hàng).
- **Module Giỏ hàng & OTP:** Kiểm tra số lượng đặt mua không vượt quá tồn kho (`LoiNghiepVu`) và xác thực mã OTP 6 chữ số (hiệu lực 5 phút, tối đa sai 5 lần).
- **Module Product:** Quản lý CRUD sản phẩm phụ kiện gaming.

## 2. Câu lệnh gửi cho AI (Prompt)
```text
Bạn là chuyên gia QA Lead. Dựa vào tài liệu đặc tả nghiệp vụ sau của dự án TiTanGEAR:
[Dán nội dung docs/SPECIFICATIONS.md]

Yêu cầu:
1. Hãy phân tích mối liên hệ Error -> Defect -> Failure trong trường hợp người dùng thêm sản phẩm vượt quá tồn kho hoặc xác thực OTP (theo bài học Lab 1).
2. Thiết kế Test Plan (có lịch trình, phân công, phạm vi) và Bảng Test Case chi tiết cho 3 module: Authentication, Giỏ hàng & OTP, và Product CRUD.
3. Phân biệt rõ mục tiêu Verification (Xác minh) và Validation (Xác nhận) cho từng hoạt động kiểm thử.
```

## 3. Kết quả do AI sinh ra (Raw AI Output)
AI đã phân tích chính xác:
* **Error (Sai sót con người):** Lập trình viên thiết kế luồng giỏ hàng quên không kiểm tra giới hạn tồn kho khả dụng hoặc quên giới hạn số lần nhập sai mã OTP.
* **Defect (Khuyết tật mã nguồn):** Phương thức `addToCart()` không đối chiếu `quantity > stock` hoặc `verifyOtp()` không đếm số lần nhập sai liên tiếp.
* **Failure (Sự cố):** Hệ thống cho phép đặt hàng vượt quá số lượng hàng thực tế trong kho hoặc bị dò mã OTP trái phép.
* AI sinh ra bảng Test Case cho 3 module nhưng ban đầu thiếu ca kiểm tra cập nhật số lượng giỏ hàng `<= 0` và kiểm tra giá bán sản phẩm âm (Boundary value).

## 4. Tinh chỉnh của Sinh viên (Human Refinement)
* **Bổ sung Test Case:** Đã thêm Test Case `TC_CART_04` (Kiểm tra cập nhật số lượng giỏ hàng `<= 0` tự động xóa sản phẩm) và `TC_PROD_02` (Kiểm tra giá sản phẩm `<= 0`).
* **Bổ sung Test Plan:** Thêm bảng lịch trình thực hiện chi tiết 6 tuần, người phụ trách (`Phạm Minh Tu - TV00386`) và bảng môi trường kiểm thử thực tế (JDK 21, Spring MVC 6, SQL Server).
* **Chuẩn hóa:** Phân tách rõ ràng giữa Verification (kiểm tra code đáp ứng đặc tả) và Validation (đáp ứng đúng nhu cầu người dùng).
* **Kết quả cuối cùng:** Đã lưu hoàn thiện tại `docs/SPECIFICATIONS.md`, `docs/TEST_PLAN.md` và `docs/TEST_CASES.md`.

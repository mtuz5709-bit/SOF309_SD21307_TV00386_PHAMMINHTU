package com.titangear.exception;

// Lỗi nghiệp vụ: câu thông báo bên trong được viết sẵn bằng tiếng Việt để hiện
// thẳng cho người dùng đọc, ví dụ "Bạn đang có 3 đơn COD chưa giao xong".
//
// Có lớp riêng để controller phân biệt được hai loại lỗi:
//   lỗi do người dùng làm sai -> bắt LoiNghiepVu, hiện nguyên văn câu thông báo
//   lỗi do hệ thống hỏng      -> hiện câu chung chung, chi tiết chỉ ghi vào log
// Không lẫn hai loại thì lỗi SQL không bị in ra màn hình khách, tránh lộ tên bảng
// và tên cột của cơ sở dữ liệu.
//
// Kế thừa RuntimeException để không phải khai throws dọc theo mọi tầng, và để
//  tự động rollback khi nó được ném ra.
public class LoiNghiepVu extends RuntimeException {

    public LoiNghiepVu(String thongDiep) {
        super(thongDiep);
    }

    public LoiNghiepVu(String thongDiep, Throwable nguyenNhan) {
        super(thongDiep, nguyenNhan);
    }
}

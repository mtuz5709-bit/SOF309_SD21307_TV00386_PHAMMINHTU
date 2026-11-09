package com.titangear.config;

import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

// Bắt lỗi tập trung cho các exception ném ra từ controller: ghi CHI TIẾT vào log
// (cho dev), nhưng chỉ trả về KHÁCH trang 500 lịch sự — KHÔNG phơi stack trace
// (tránh rò rỉ tên bảng/class/framework ra ngoài).
//
// Dùng setStatus (không sendError) để render thẳng view, không kích hoạt lại
// error-page của container. Các lỗi NGOÀI controller do <error-page> ở web.xml lo.
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Không tìm thấy đường dẫn -> trả đúng 404 (không nhầm thành 500).
    @ExceptionHandler(NoHandlerFoundException.class)
    public String handleNotFound(HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        return "common/404";
    }

    // Thiếu file tĩnh (ảnh sản phẩm đã bị xoá, favicon…) — chuyện thường, chỉ là
    // 404 cho 1 tài nguyên, KHÔNG phải sự cố hệ thống. Trả 404 rỗng và chỉ ghi
    // log mức DEBUG để không làm nhiễu log bằng vệt đỏ ERROR.
    @ExceptionHandler(NoResourceFoundException.class)
    public void handleMissingStatic(NoResourceFoundException e, HttpServletResponse response) {
        log.debug("Không tìm thấy tài nguyên tĩnh: {}", e.getResourcePath());
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    // Lỗi do YÊU CẦU của phía client sai (thiếu tham số, sai kiểu dữ liệu) —
    // đây KHÔNG phải sự cố hệ thống nên phải trả 400, không được nhầm thành 500.
    // Chỉ log mức WARN vì không cần dev đi sửa gì.
    @ExceptionHandler({ServletRequestBindingException.class, MethodArgumentTypeMismatchException.class})
    public String handleBadRequest(Exception e, HttpServletResponse response) {
        log.warn("Yêu cầu không hợp lệ: {}", e.getMessage());
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return "common/400";
    }

    // Sai phương thức (vd GET vào endpoint chỉ nhận POST) -> 405.
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public String handleMethodNotAllowed(HttpRequestMethodNotSupportedException e, HttpServletResponse response) {
        log.warn("Sai phương thức HTTP: {}", e.getMessage());
        response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        return "common/400";
    }

    // Mọi lỗi còn lại -> log chi tiết cho dev + trang 500 lịch sự cho khách.
    @ExceptionHandler(Exception.class)
    public String handleAll(Exception e, HttpServletResponse response) {
        log.error("Lỗi hệ thống chưa được xử lý", e);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return "common/500";
    }
}

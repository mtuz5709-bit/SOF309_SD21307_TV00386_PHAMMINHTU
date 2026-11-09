package com.titangear.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Gắn lên method hoặc class của controller để yêu cầu một quyền cụ thể, ví dụ:
//     (WarehousePermissionService.CAN_VIEW_PRODUCTS)
// Việc kiểm quyền chạy tập trung ở PermissionInterceptor nên đổi quy tắc chỉ sửa
// một chỗ. Giá trị truyền vào là khóa quyền trong bảng system_settings, xem các
// hằng khai trong WarehousePermissionService.
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {
    String value();
}

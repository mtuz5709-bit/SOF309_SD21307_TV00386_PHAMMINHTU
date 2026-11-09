package com.titangear.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// ADMIN luôn có toàn quyền, không cần kiểm tra gì thêm.
// WAREHOUSE chỉ được vào các khu vực cụ thể NẾU admin đã bật quyền tương ứng
// trong trang Cài đặt — mặc định TẮT hết (an toàn), admin phải chủ động cấp.
//
// Đây là lớp kiểm tra thứ 2, nằm SAU cổng chặn thô của Spring Security
// (SecurityConfig chỉ cho WAREHOUSE chạm tới được URL, còn có thực sự dùng
// được hay không do lớp này quyết định — tách 2 lớp để admin đổi quyền
// ngay trong Cài đặt mà không cần build lại code/đổi SecurityConfig).
@Service
public class WarehousePermissionService {

    @Autowired
    private SystemSettingService systemSettingService;

    public static final String CAN_VIEW_PRODUCTS = "WAREHOUSE_CAN_VIEW_PRODUCTS";
    public static final String CAN_VIEW_REVENUE  = "WAREHOUSE_CAN_VIEW_REVENUE";
    public static final String CAN_MANAGE_ORDERS = "WAREHOUSE_CAN_MANAGE_ORDERS";

    public boolean hasPermission(String role, String permissionKey) {
        if ("ADMIN".equals(role)) return true;
        if (!"WAREHOUSE".equals(role)) return false;
        return systemSettingService.getBoolean(permissionKey, false);
    }
}

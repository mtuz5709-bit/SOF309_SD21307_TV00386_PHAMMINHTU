package com.titangear.constant;

import java.util.LinkedHashMap;
import java.util.Map;

// Thuộc tính chuẩn hoá của sản phẩm — dùng cho BỘ LỌC phía khách và dropdown ở
// form admin. Để mã (LINEAR, ULTRA...) trong DB và nhãn tiếng Việt ở đây, nên
// đổi chữ hiển thị không phải sửa dữ liệu.
//
// Dùng LinkedHashMap để giữ đúng thứ tự hiển thị như thiết kế.
public class ProductAttr {

    // Loại switch (bàn phím)
    public static final Map<String, String> SWITCH = new LinkedHashMap<>();
    // Nhóm trọng lượng (chuột)
    public static final Map<String, String> WEIGHT = new LinkedHashMap<>();
    // Kiểu kết nối
    public static final Map<String, String> CONNECT = new LinkedHashMap<>();

    static {
        SWITCH.put("LINEAR",   "Linear");
        SWITCH.put("TACTILE",  "Tactile");
        SWITCH.put("CLICKY",   "Clicky");
        SWITCH.put("MAGNETIC", "Magnetic (Hall)");

        WEIGHT.put("ULTRA", "Siêu nhẹ < 60g");
        WEIGHT.put("MID",   "60 – 70g");
        WEIGHT.put("HEAVY", "Trên 70g");

        CONNECT.put("WIRED",    "Có dây");
        CONNECT.put("WIRELESS", "Không dây");
        CONNECT.put("DUAL",     "Dual");
    }

    private ProductAttr() { }
}

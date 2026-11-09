package com.titangear.constant;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

// Khai các khu trong kho và số kệ tối đa của mỗi khu.
// Gom về một chỗ để form nhập kho, ô sửa vị trí và các chốt kiểm phía server đều
// đọc chung một nguồn — thêm khu mới hay đổi số kệ chỉ sửa ở đây.
public final class KhoViTri {

    private KhoViTri() {}

    // Số kệ của mỗi khu. Kệ đánh số từ 1 tới hết.
    public static final int SO_KE_MOI_KHU = 50;

    // Mã khu và tên đầy đủ hiện trên ô chọn.
    public static final Map<String, String> CAC_KHU = new LinkedHashMap<>() {{
        put("A", "Khu A — Chuột");
        put("B", "Khu B — Tai nghe");
        put("C", "Khu C — Bàn phím");
    }};

    // Danh sách số kệ 1..50, đổ vào datalist để gõ số là lọc ra ngay.
    public static final List<Integer> DANH_SACH_KE =
            IntStream.rangeClosed(1, SO_KE_MOI_KHU).boxed().toList();

    // Kiểm một cặp (khu, kệ) có hợp lệ không. Dùng ở cả lúc nhập kho lẫn lúc đổi
    // vị trí, để không có đường nào ghi được vị trí nằm ngoài sơ đồ kho.
    public static boolean hopLe(String khu, Integer ke) {
        if (khu == null || ke == null) return false;
        return CAC_KHU.containsKey(khu.trim().toUpperCase())
                && ke >= 1 && ke <= SO_KE_MOI_KHU;
    }

    // Câu báo lỗi dùng chung khi vị trí không hợp lệ.
    public static String loiViTri() {
        return "Vị trí không hợp lệ. Khu phải là "
                + String.join(", ", CAC_KHU.keySet())
                + " và số kệ từ 1 đến " + SO_KE_MOI_KHU + ".";
    }
}

package com.titangear.controller;

import com.titangear.exception.LoiNghiepVu;
import com.titangear.service.SystemSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/settings")
public class AdminSettingsController {

    @Autowired
    private SystemSettingService systemSettingService;

    @Autowired
    private com.titangear.service.AuditLogService auditLogService;

    @GetMapping
    public String settings(Model model) {
        model.addAttribute("settings", systemSettingService.getAll());
        return "admin/settings";
    }

    private static final java.util.Set<String> NUMERIC_SETTINGS = java.util.Set.of(
            "MAX_QTY_PER_PRODUCT_PER_ORDER",
            "COD_MAX_QTY_PER_PRODUCT",
            "COD_MAX_ORDER_AMOUNT",
            "COD_MAX_ACTIVE_ORDERS"
    );

    @PostMapping("/update")
    public String update(@RequestParam String settingKey,
                          @RequestParam String settingValue,
                          org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            // Các setting dạng số phải là số nguyên dương
            if (NUMERIC_SETTINGS.contains(settingKey)) {
                int value = Integer.parseInt(settingValue.trim());
                if (value <= 0) {
                    throw new LoiNghiepVu("Giá trị phải lớn hơn 0");
                }
            }
            // Nhật ký: cài đặt điều khiển luật bán hàng (ngưỡng COD, phí ship,
            // giới hạn số lượng) — đổi một con số là đổi hành vi cả hệ thống.
            String giaTriCu = systemSettingService.getValue(settingKey, "");
            systemSettingService.updateValue(settingKey, settingValue.trim());
            auditLogService.ghi(com.titangear.service.AuditLogService.DOI_CAI_DAT, "system_settings", null,
                    giaTriCu, settingValue.trim(), "Đổi cài đặt " + settingKey);
            redirectAttributes.addFlashAttribute("successMsg", "Đã cập nhật cài đặt thành công!");
        } catch (NumberFormatException e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Giá trị phải là số nguyên hợp lệ!");
        } catch (LoiNghiepVu e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/settings";
    }
}

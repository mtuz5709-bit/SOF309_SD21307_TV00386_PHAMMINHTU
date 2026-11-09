package com.titangear.service;

import com.titangear.model.SystemSetting;
import com.titangear.repository.SystemSettingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SystemSettingService {

    @Autowired
    private SystemSettingRepository systemSettingRepository;

    public List<SystemSetting> getAll() {
        return systemSettingRepository.findAll();
    }

    public String getValue(String key, String defaultValue) {
        return systemSettingRepository.findBySettingKey(key)
                .map(SystemSetting::getSettingValue)
                .orElse(defaultValue);
    }

    // Setting dạng bật/tắt (giá trị lưu "true"/"false") — dùng cho các cờ phân quyền
    public boolean getBoolean(String key, boolean defaultValue) {
        String raw = getValue(key, String.valueOf(defaultValue));
        return "true".equalsIgnoreCase(raw);
    }

    public void updateValue(String key, String value) {
        SystemSetting setting = systemSettingRepository.findBySettingKey(key)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cài đặt: " + key));
        setting.setSettingValue(value);
        systemSettingRepository.save(setting);
    }
}

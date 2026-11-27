package com.poly.sof309.ui.listeners;

import com.poly.sof309.ui.pages.DriverManager;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * [Y4 - Tuần 4] ITestListener tự động chụp ảnh màn hình lưu vào thư mục screenshots/
 * mỗi khi một ca TestNG thất bại.
 */
public class TestListener implements ITestListener {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    @Override
    public void onTestSuccess(ITestResult result) {
        System.out.println("[TestNG PASS] " + result.getName());
    }

    @Override
    public void onTestFailure(ITestResult result) {
        System.out.println("[TestNG FAIL] " + result.getName()
                + (result.getThrowable() != null ? " -> " + result.getThrowable().getMessage() : ""));

        WebDriver driver = DriverManager.getDriver();
        if (driver == null) {
            return;
        }

        try {
            File dir = new File("screenshots");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            String fileName = result.getName() + "_" + LocalDateTime.now().format(FMT) + ".png";
            File dest = new File(dir, fileName);
            FileUtils.copyFile(src, dest);
            System.out.println("[TestListener] Da luu anh chup loi tai: " + dest.getPath());
        } catch (IOException e) {
            System.err.println("[TestListener] Khong the luu anh chup man hinh: " + e.getMessage());
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        System.out.println("[TestNG SKIP] " + result.getName());
    }
}

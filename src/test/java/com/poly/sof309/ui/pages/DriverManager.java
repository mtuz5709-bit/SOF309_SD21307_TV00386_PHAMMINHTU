package com.poly.sof309.ui.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.time.Duration;

/**
 * [Y4 - Tuần 4] Quản lý WebDriver bằng ThreadLocal để an toàn cho TestListener chụp ảnh lỗi.
 * Sử dụng ChromeDriver (Selenium Manager tự tải Chrome for Testing & ChromeDriver tương thích)
 * và hỗ trợ --headless=new khi chạy với -Dheadless=true hoặc CI=true.
 */
public class DriverManager {

    private static final ThreadLocal<WebDriver> DRIVER_HOLDER = new ThreadLocal<>();

    public static WebDriver getDriver() {
        return DRIVER_HOLDER.get();
    }

    public static WebDriver initDriver() {
        boolean isHeadless = Boolean.parseBoolean(System.getenv("CI"))
                || Boolean.parseBoolean(System.getProperty("headless", "false"));

        ChromeOptions options = new ChromeOptions();
        if (isHeadless) {
            options.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage");
        }
        options.addArguments("--window-size=1920,1080");

        WebDriver driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
        driver.manage().window().maximize();
        DRIVER_HOLDER.set(driver);
        return driver;
    }

    public static void quitDriver() {
        WebDriver driver = DRIVER_HOLDER.get();
        if (driver != null) {
            try {
                driver.quit();
            } finally {
                DRIVER_HOLDER.remove();
            }
        }
    }
}

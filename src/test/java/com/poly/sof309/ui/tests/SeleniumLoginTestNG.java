package com.poly.sof309.ui.tests;

import com.poly.sof309.ui.listeners.TestListener;
import com.poly.sof309.ui.pages.DriverManager;
import com.poly.sof309.ui.pages.LoginPage;
import com.poly.sof309.ui.pages.ProductPage;
import com.poly.sof309.ui.pages.RegisterPage;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * [Y4 - Tuần 4] Kịch bản Selenium POM kết hợp TestNG (@DataProvider, groups, dependsOnMethods, ITestListener).
 * - Nhóm "ui": Chạy trực tiếp trên Web thật (khi Tomcat localhost:8080 đang bật).
 * - Nhóm "screenshot-demo": Demo ITestListener tự chụp ảnh màn hình lỗi vào thư mục screenshots/.
 */
@Listeners(TestListener.class)
public class SeleniumLoginTestNG {

    private WebDriver driver;
    private LoginPage loginPage;
    private RegisterPage registerPage;
    private ProductPage productPage;

    private String getBaseUrl() {
        return System.getProperty("baseUrl", "http://localhost:8080");
    }

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        driver = DriverManager.initDriver();
        loginPage = new LoginPage(driver);
        registerPage = new RegisterPage(driver);
        productPage = new ProductPage(driver);
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        DriverManager.quitDriver();
    }

    @DataProvider(name = "uiLoginAccounts")
    public Object[][] getUiLoginAccounts() {
        return new Object[][]{
                {"admin@titangear.vn", "Admin@123", true, "/admin/dashboard"},
                {"admin@titangear.vn", "SaiMatKhau@999", false, "Email hoặc mật khẩu không đúng!"},
                {"locked@titangear.vn", "Locked@123", false, "Tài khoản của bạn đã bị khóa"}
        };
    }

    @Test(groups = {"ui", "smoke"}, dataProvider = "uiLoginAccounts",
            description = "Kiểm thử đăng nhập giao diện thật với @DataProvider")
    public void testLoginUiDataDriven(
            String email, String password, boolean expectSuccess, String expectedTextOrUrl) {
        driver.get(getBaseUrl() + "/login");
        loginPage.login(email, password);

        if (expectSuccess) {
            Assert.assertTrue(driver.getCurrentUrl().contains(expectedTextOrUrl),
                    "Đăng nhập thành công phải chuyển hướng tới: " + expectedTextOrUrl);
        } else {
            Assert.assertTrue(loginPage.getErrorMessage().contains(expectedTextOrUrl),
                    "Thông báo lỗi UI phải chứa: " + expectedTextOrUrl);
        }
    }

    @Test(groups = {"ui", "regression"}, dependsOnMethods = {"testLoginUiDataDriven"},
            description = "dependsOnMethods: Sau khi kiểm tra đăng nhập UI -> test tìm kiếm sản phẩm")
    public void testOpenProductSearchUi() {
        driver.get(getBaseUrl() + "/products");
        productPage.searchProduct("ATK");
        Assert.assertTrue(driver.getCurrentUrl().contains("keyword=ATK"),
                "URL tìm kiếm phải chứa tham số keyword=ATK");
    }

    @Test(groups = {"screenshot-demo"},
            description = "Demo ITestListener tự động chụp ảnh màn hình vào screenshots/ khi test fail")
    public void testChupAnhManHinhKhiFailDemo() {
        String html = """
                <!DOCTYPE html>
                <html lang="vi">
                <head><meta charset="UTF-8"/><title>Đăng Nhập – TiTanGEAR (Demo Fail)</title></head>
                <body style="font-family:Arial,sans-serif;background:#111827;color:#fff;padding:40px;">
                  <div style="max-width:420px;margin:40px auto;background:#1f2937;padding:28px;border-radius:10px;">
                    <h2 style="margin-top:0;color:#f87171;">TiTanGEAR – Đăng nhập</h2>
                    <div id="errBox" class="auth-error" style="display:none;background:#7f1d1d;padding:10px;border-radius:6px;margin-bottom:14px;">
                      Email hoặc mật khẩu không đúng!
                    </div>
                    <form onsubmit="document.getElementById('errBox').style.display='block'; return false;">
                      <div style="margin-bottom:12px;">
                        <label>Email</label><br/>
                        <input type="text" name="username" style="width:100%;padding:8px;margin-top:4px;"/>
                      </div>
                      <div style="margin-bottom:16px;">
                        <label>Mật khẩu</label><br/>
                        <input type="password" name="password" style="width:100%;padding:8px;margin-top:4px;"/>
                      </div>
                      <button type="submit" class="btn-auth" style="width:100%;padding:10px;background:#ef4444;color:#fff;border:0;border-radius:6px;">
                        Đăng nhập
                      </button>
                    </form>
                  </div>
                </body>
                </html>
                """;
        driver.get("data:text/html;charset=utf-8," + URLEncoder.encode(html, StandardCharsets.UTF_8).replace("+", "%20"));

        loginPage.login("admin@titangear.vn", "SaiMatKhau@999");

        // Cố tình kiểm tra chuyển trang thất bại để ITestListener chụp ảnh màn hình lưu vào screenshots/
        Assert.assertTrue(driver.getCurrentUrl().contains("/admin/dashboard"),
                "Demo FAIL có chủ đích: đăng nhập sai mật khẩu không thể vào /admin/dashboard");
    }
}

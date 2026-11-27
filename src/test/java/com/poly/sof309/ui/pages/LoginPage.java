package com.poly.sof309.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * [Y4] Page Object Model (POM) cho trang Đăng nhập TiTanGEAR (/login - common/login.jsp)
 * Sử dụng WebDriverWait thay vì Thread.sleep().
 */
public class LoginPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By txtUsername = By.name("username");
    private final By txtPassword = By.name("password");
    private final By btnLogin = By.cssSelector("button.btn-auth[type='submit']");
    private final By lblErrorMessage = By.cssSelector(".auth-error");
    private final By lblSuccessMessage = By.cssSelector(".auth-success");

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void enterUsername(String email) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(txtUsername));
        el.clear();
        el.sendKeys(email);
    }

    public void enterPassword(String password) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(txtPassword));
        el.clear();
        el.sendKeys(password);
    }

    public void clickLogin() {
        wait.until(ExpectedConditions.elementToBeClickable(btnLogin)).click();
    }

    public void login(String email, String password) {
        enterUsername(email);
        enterPassword(password);
        clickLogin();
    }

    public String getErrorMessage() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(lblErrorMessage)).getText();
    }

    public String getSuccessMessage() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(lblSuccessMessage)).getText();
    }
}

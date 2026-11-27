package com.poly.sof309.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * [Y4] Page Object Model (POM) cho trang Đăng ký TiTanGEAR (/register - customer/register.jsp)
 */
public class RegisterPage {
    private final WebDriverWait wait;

    private final By txtFullName = By.name("fullName");
    private final By txtEmail = By.name("email");
    private final By txtPhone = By.name("phone");
    private final By txtPassword = By.name("password");
    private final By btnRegister = By.cssSelector("button.btn-auth[type='submit']");
    private final By lblErrorMessage = By.cssSelector(".auth-error");

    public RegisterPage(WebDriver driver) {
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void register(String fullName, String email, String phone, String password) {
        WebElement elName = wait.until(ExpectedConditions.visibilityOfElementLocated(txtFullName));
        elName.clear();
        elName.sendKeys(fullName);

        WebElement elEmail = wait.until(ExpectedConditions.visibilityOfElementLocated(txtEmail));
        elEmail.clear();
        elEmail.sendKeys(email);

        WebElement elPhone = wait.until(ExpectedConditions.visibilityOfElementLocated(txtPhone));
        elPhone.clear();
        elPhone.sendKeys(phone);

        WebElement elPass = wait.until(ExpectedConditions.visibilityOfElementLocated(txtPassword));
        elPass.clear();
        elPass.sendKeys(password);

        wait.until(ExpectedConditions.elementToBeClickable(btnRegister)).click();
    }

    public String getErrorMessage() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(lblErrorMessage)).getText();
    }
}

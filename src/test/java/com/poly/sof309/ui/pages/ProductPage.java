package com.poly.sof309.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * [Y4] Page Object Model (POM) cho trang Sản phẩm & Tìm kiếm TiTanGEAR (/products)
 */
public class ProductPage {
    private final WebDriverWait wait;

    private final By btnOpenSearch = By.cssSelector("button[aria-label='Tìm kiếm']");
    private final By txtSearchInput = By.id("searchInput");
    private final By btnSubmitSearch = By.cssSelector("button.search-btn");
    private final By filterForm = By.id("filterForm");
    private final By txtMinPrice = By.name("minPrice");
    private final By txtMaxPrice = By.name("maxPrice");

    public ProductPage(WebDriver driver) {
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void searchProduct(String keyword) {
        wait.until(ExpectedConditions.elementToBeClickable(btnOpenSearch)).click();
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(txtSearchInput));
        input.clear();
        input.sendKeys(keyword);
        wait.until(ExpectedConditions.elementToBeClickable(btnSubmitSearch)).click();
    }

    public void filterByPrice(String minPrice, String maxPrice) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(filterForm));
        WebElement minEl = wait.until(ExpectedConditions.visibilityOfElementLocated(txtMinPrice));
        minEl.clear();
        minEl.sendKeys(minPrice);

        WebElement maxEl = wait.until(ExpectedConditions.visibilityOfElementLocated(txtMaxPrice));
        maxEl.clear();
        maxEl.sendKeys(maxPrice);
        maxEl.submit();
    }
}

package ru.msu.web.system;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentSystemTest extends AbstractSystemTest {

    private WebDriverWait webWait() {
        return new WebDriverWait(driver, Duration.ofSeconds(5));
    }

    @Test
    void listPayments_salaryReport() {
        driver.get(url("/payments?type=salary&dateFrom=2024-01-01&dateTo=2024-06-30"));
        String src = driver.getPageSource();
        assertTrue(src.contains("Ведомость по зарплате"));
        assertTrue(src.contains("Иванов Александр"));
    }

    @Test
    void employeeExpenseReport_summary() {
        driver.get(url("/payments?employeeId=1&dateFrom=2024-01-01&dateTo=2024-06-30"));
        String src = driver.getPageSource();
        assertTrue(src.contains("Отчёт по расходам на сотрудника"));
        assertTrue(src.contains("Среднемесячно"));
    }

    @Test
    void editPayment_success() {
        driver.get(url("/payments/1/edit"));
        driver.findElement(By.name("amount")).clear();
        driver.findElement(By.name("amount")).sendKeys("510000");
        driver.findElement(By.cssSelector("button[type=submit]")).click();

        webWait().until(ExpectedConditions.urlContains("/payments"));
        assertTrue(driver.getPageSource().contains("510000"));
    }
}
package ru.msu.web.system;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentSystemTest extends AbstractSystemTest {

    private WebDriverWait webWait() {
        return new WebDriverWait(driver, Duration.ofSeconds(5));
    }

    private String get(String path) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(url(path)).openConnection();
        connection.setRequestMethod("GET");
        try (var input = connection.getInputStream()) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
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

    @Test
    void createPayment_success() {
        driver.get(url("/payments/new"));
        driver.findElement(By.name("amount")).clear();
        driver.findElement(By.name("amount")).sendKeys("123456");
        driver.findElement(By.name("paymentDate")).sendKeys("2025-12-31");
        driver.findElement(By.cssSelector("button[type=submit]")).click();

        webWait().until(ExpectedConditions.urlContains("/payments"));
        assertTrue(driver.getPageSource().contains("123456"));
    }

    @Test
    void paymentReportEndpoints_printAndExport() throws Exception {
        String printHtml = get("/payments/print?type=salary&projectId=1&sort=amountAsc");
        assertTrue(printHtml.contains("Ведомость по зарплате"));
        assertTrue(printHtml.contains("window.print()"));

        String csv = get("/payments/export?type=one_time_bonus&projectId=11&sort=employeeDesc");
        assertTrue(csv.contains("id,employee,project,position,type,amount,paymentDate,status,reason"));
        assertTrue(csv.contains("\"Завершение проекта\""));
        assertTrue(csv.contains("\"Шестаков Андрей Михайлович\"") || csv.contains("\"Смирнов Алексей Дмитриевич\""));
    }
}
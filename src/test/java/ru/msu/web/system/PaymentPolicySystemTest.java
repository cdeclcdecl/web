package ru.msu.web.system;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentPolicySystemTest extends AbstractSystemTest {

    private WebDriverWait webWait() {
        return new WebDriverWait(driver, Duration.ofSeconds(5));
    }

    @Test
    void listPolicies_filterByProject() {
        driver.get(url("/policies?type=salary&projectId=1"));
        String src = driver.getPageSource();
        assertTrue(src.contains("Руководство компании"));
        assertTrue(src.contains("salary"));
    }

    @Test
    void createPolicy_success() {
        driver.get(url("/policies/new"));
        new Select(driver.findElement(By.name("policyType"))).selectByValue("one_time_bonus");
        driver.findElement(By.name("amount")).sendKeys("12345");
        driver.findElement(By.name("reason")).sendKeys("Тестовая причина политики");
        driver.findElement(By.cssSelector("button[type=submit]")).click();

        webWait().until(ExpectedConditions.urlContains("/policies"));
        assertTrue(driver.getPageSource().contains("Тестовая причина политики"));
    }

    @Test
    void editPolicy_success() {
        driver.get(url("/policies/1/edit"));
        driver.findElement(By.name("description")).sendKeys("Обновленное описание политики");
        driver.findElement(By.cssSelector("button[type=submit]")).click();

        webWait().until(ExpectedConditions.urlContains("/policies"));
        assertTrue(driver.getPageSource().contains("Обновленное описание политики"));
    }
}
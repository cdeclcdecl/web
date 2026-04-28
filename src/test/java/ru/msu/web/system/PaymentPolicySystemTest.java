package ru.msu.web.system;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertFalse;
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

    @Test
    void listPolicies_filtersByEventAndMinYearsSorted() {
        driver.get(url("/policies?type=seniority&minYears=5&event=5&sort=minYearsDesc"));

        String desc = driver.getPageSource();
        assertTrue(desc.contains("20% за 15 лет стажа"));
        assertTrue(desc.contains("10% за 5 лет стажа"));
        assertFalse(desc.contains("5% за 3 года стажа"));
        assertTrue(desc.indexOf("20% за 15 лет стажа") < desc.indexOf("10% за 5 лет стажа"));

        driver.get(url("/policies?type=seniority&minYears=5&event=5&sort=minYearsAsc"));

        String asc = driver.getPageSource();
        assertTrue(asc.indexOf("10% за 5 лет стажа") < asc.indexOf("20% за 15 лет стажа"));
    }

    @Test
    void deletePolicy_success() {
        String reason = "Delete Policy System Test";

        driver.get(url("/policies/new"));
        new Select(driver.findElement(By.name("policyType"))).selectByValue("one_time_bonus");
        driver.findElement(By.name("amount")).sendKeys("54321");
        driver.findElement(By.name("reason")).sendKeys(reason);
        driver.findElement(By.cssSelector("button[type=submit]")).click();

        webWait().until(ExpectedConditions.urlContains("/policies"));
        WebElement deleteButton = driver.findElement(By.xpath("//tr[td[contains(., '" + reason + "')]]//button[contains(@class, 'btn-danger')]"));
        deleteButton.click();

        webWait().until(ExpectedConditions.urlContains("/policies"));
        assertFalse(driver.getPageSource().contains(reason));
    }
}